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

import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SqlValidator {

	private static final List<String> FORBIDDEN_KEYWORDS = List.of("insert", "update", "delete", "drop", "alter",
			"truncate", "create", "merge", "replace", "grant", "revoke", "call", "exec", "commit", "rollback");

	private static final List<String> FORBIDDEN_PATTERNS = List.of("into\\s+outfile", "into\\s+dumpfile", "load_file\\s*\\(",
			"sleep\\s*\\(", "benchmark\\s*\\(", "information_schema", "mysql\\.user");

	private final SecurityProperties securityProperties;

	public String validate(String sql) {
		SqlValidationResult result = validateDetailed(sql);
		if (!Boolean.TRUE.equals(result.getValid())) {
			throw new BusinessException(result.getMessage());
		}
		return result.getSanitizedSql();
	}

	public SqlValidationResult validateDetailed(String sql) {
		if (!StringUtils.hasText(sql)) {
			return invalid("SQL cannot be empty", "empty");
		}
		String normalizedSql = stripMarkdown(sql).trim();
		if (hasMiddleSemicolon(normalizedSql)) {
			return invalid("Multiple SQL statements are not allowed", "multi_statement");
		}
		normalizedSql = SqlLimitUtils.removeTrailingSemicolon(normalizedSql);
		String lowerSql = normalizedSql.toLowerCase(Locale.ROOT);
		if (!lowerSql.startsWith("select") && !(lowerSql.startsWith("with") && lowerSql.contains("select"))) {
			return invalid("Only SELECT or WITH SELECT SQL is allowed", "not_select");
		}
		if (lowerSql.contains("--") || lowerSql.contains("/*") || lowerSql.contains("*/")) {
			return invalid("SQL comments are not allowed", "comment");
		}
		for (String keyword : FORBIDDEN_KEYWORDS) {
			if (lowerSql.matches("(?s).*\\b" + keyword + "\\b.*")) {
				return invalid("Forbidden SQL keyword: " + keyword, "keyword:" + keyword);
			}
		}
		for (String pattern : FORBIDDEN_PATTERNS) {
			if (lowerSql.matches("(?s).*\\b" + pattern + ".*")) {
				return invalid("Forbidden SQL pattern: " + pattern, "pattern:" + pattern);
			}
		}
		boolean limitApplied = false;
		int limit = securityProperties.getSql().getDefaultLimit();
		if (Boolean.TRUE.equals(securityProperties.getSql().getForceLimit()) && !SqlLimitUtils.hasLimit(normalizedSql)) {
			normalizedSql = SqlLimitUtils.appendLimitIfNeeded(normalizedSql, limit);
			limitApplied = true;
		}
		return SqlValidationResult.builder()
			.valid(true)
			.sanitizedSql(normalizedSql)
			.message("SQL validated")
			.limitApplied(limitApplied)
			.limit(limit)
			.build();
	}

	private String stripMarkdown(String sql) {
		return sql.replace("```sql", "").replace("```SQL", "").replace("```", "").trim();
	}

	private boolean hasMiddleSemicolon(String sql) {
		String trimmed = sql == null ? "" : sql.trim();
		int first = trimmed.indexOf(';');
		return first >= 0 && first < trimmed.length() - 1;
	}

	private SqlValidationResult invalid(String message, String blockedReason) {
		return SqlValidationResult.builder()
			.valid(false)
			.message(message)
			.blockedReason(blockedReason)
			.limitApplied(false)
			.build();
	}

}
