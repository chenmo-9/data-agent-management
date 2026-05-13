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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecretServiceImpl implements SecretService {

	private static final String DEV_SECRET_KEY = "dataagent-dev-secret-key-change-me-32";

	private final SecurityProperties securityProperties;

	private String effectiveSecretKey;

	@PostConstruct
	public void init() {
		if (securityProperties.getSecretKey() == null || securityProperties.getSecretKey().isBlank()) {
			effectiveSecretKey = DEV_SECRET_KEY;
			log.warn("DATAAGENT_SECRET_KEY is not set, using dev key. Do not use in production.");
		}
		else {
			effectiveSecretKey = securityProperties.getSecretKey();
		}
	}

	@Override
	public String encryptIfNeeded(String raw) {
		if (raw == null || raw.isBlank() || isPlaceholder(raw) || isEncrypted(raw)) {
			return raw;
		}
		return securityProperties.getEncryptPrefix() + CryptoUtils.encrypt(raw, effectiveSecretKey);
	}

	@Override
	public String decryptIfNeeded(String value) {
		if (value == null || value.isBlank() || !isEncrypted(value)) {
			return value;
		}
		String payload = value.substring(securityProperties.getEncryptPrefix().length());
		return CryptoUtils.decrypt(payload, effectiveSecretKey);
	}

	@Override
	public String maskSecret(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		String plain = isEncrypted(value) ? decryptIfNeeded(value) : value;
		if (plain.length() <= securityProperties.getMaskPrefixLength() + securityProperties.getMaskSuffixLength()) {
			return "****";
		}
		int prefixLength = securityProperties.getMaskPrefixLength();
		int suffixLength = securityProperties.getMaskSuffixLength();
		return plain.substring(0, prefixLength) + "****" + plain.substring(plain.length() - suffixLength);
	}

	@Override
	public boolean isEncrypted(String value) {
		return value != null && value.startsWith(securityProperties.getEncryptPrefix());
	}

	@Override
	public boolean isPlaceholder(String value) {
		if (value == null) {
			return true;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() || "****".equals(trimmed) || trimmed.matches(".{1,8}\\*{4}.{1,8}");
	}

}
