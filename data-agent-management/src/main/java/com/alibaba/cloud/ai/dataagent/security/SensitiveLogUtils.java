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

public final class SensitiveLogUtils {

	private SensitiveLogUtils() {
	}

	public static String maskJdbcUrl(String url) {
		if (url == null) {
			return null;
		}
		return maskSecretFields(url);
	}

	public static String maskSecretFields(String text) {
		if (text == null) {
			return null;
		}
		String masked = text.replaceAll("(?i)(password=)[^&\\s]+", "$1****")
			.replaceAll("(?i)(api[-_]?key=)[^&\\s]+", "$1****")
			.replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]+(\")", "$1****$2")
			.replaceAll("(?i)(\"api[-_]?key\"\\s*:\\s*\")[^\"]+(\")", "$1****$2")
			.replaceAll("(?i)(DATAAGENT_SECRET_KEY\\s*[:=]\\s*)[^&\\s\"]+", "$1****")
			.replaceAll("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[A-Za-z0-9._\\-]+", "$1****")
			.replaceAll("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+", "$1****");
		return masked.replaceAll("sk-[A-Za-z0-9_\\-]{8,}", "sk-****");
	}

}
