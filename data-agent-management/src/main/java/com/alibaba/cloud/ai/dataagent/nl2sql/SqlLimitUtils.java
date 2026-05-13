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
package com.alibaba.cloud.ai.dataagent.nl2sql;

import java.util.Locale;

public final class SqlLimitUtils {

	private SqlLimitUtils() {
	}

	public static String removeTrailingSemicolon(String sql) {
		String normalized = sql == null ? "" : sql.trim();
		while (normalized.endsWith(";")) {
			normalized = normalized.substring(0, normalized.length() - 1).trim();
		}
		return normalized;
	}

	public static boolean hasLimit(String sql) {
		if (sql == null) {
			return false;
		}
		return sql.toLowerCase(Locale.ROOT).matches("(?s).*\\blimit\\s+\\d+.*");
	}

	public static String appendLimitIfNeeded(String sql, int limit) {
		String normalized = removeTrailingSemicolon(sql);
		if (hasLimit(normalized)) {
			return normalized;
		}
		return normalized + " LIMIT " + limit;
	}

}
