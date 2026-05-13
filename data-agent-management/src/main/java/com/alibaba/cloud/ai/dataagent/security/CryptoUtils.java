/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoUtils {

	private static final String CIPHER = "AES/GCM/NoPadding";

	private static final int IV_LENGTH = 12;

	private static final int TAG_LENGTH_BITS = 128;

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private CryptoUtils() {
	}

	public static String encrypt(String plaintext, String secretKey) {
		try {
			byte[] iv = new byte[IV_LENGTH];
			SECURE_RANDOM.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(CIPHER);
			cipher.init(Cipher.ENCRYPT_MODE, buildKey(secretKey), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
		}
		catch (Exception ex) {
			throw new IllegalStateException("secret encryption failed", ex);
		}
	}

	public static String decrypt(String payload, String secretKey) {
		try {
			String[] parts = payload.split(":", 2);
			if (parts.length != 2) {
				throw new IllegalArgumentException("invalid encrypted payload");
			}
			byte[] iv = Base64.getDecoder().decode(parts[0]);
			byte[] encrypted = Base64.getDecoder().decode(parts[1]);
			Cipher cipher = Cipher.getInstance(CIPHER);
			cipher.init(Cipher.DECRYPT_MODE, buildKey(secretKey), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
		}
		catch (Exception ex) {
			throw new IllegalStateException("secret decryption failed", ex);
		}
	}

	private static SecretKeySpec buildKey(String secretKey) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] key = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(key, "AES");
	}

}
