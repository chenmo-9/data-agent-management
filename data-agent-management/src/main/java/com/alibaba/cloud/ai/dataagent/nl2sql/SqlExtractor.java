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

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SqlExtractor {

	private static final Pattern FENCED_BLOCK = Pattern.compile("(?is)```(?:sql)?\\s*(.*?)```");

	private static final Pattern SELECT_PATTERN = Pattern.compile("(?is)\\bselect\\b");

	public String extract(String content) {
		if (!StringUtils.hasText(content)) {
			return "";
		}
		String fromFence = extractFromFence(content);
		if (StringUtils.hasText(fromFence)) {
			return fromFence;
		}
		return extractFromText(content);
	}

	private String extractFromFence(String content) {
		Matcher matcher = FENCED_BLOCK.matcher(content);
		while (matcher.find()) {
			String candidate = extractFromText(matcher.group(1));
			if (StringUtils.hasText(candidate)) {
				return candidate;
			}
		}
		return "";
	}

	private String extractFromText(String content) {
		String normalized = cleanLabels(content);
		Matcher matcher = SELECT_PATTERN.matcher(normalized);
		if (!matcher.find()) {
			return "";
		}
		String candidate = normalized.substring(matcher.start()).trim();
		int semicolon = candidate.indexOf(';');
		if (semicolon >= 0) {
			candidate = candidate.substring(0, semicolon).trim();
		}
		else {
			candidate = cutTrailingExplanation(candidate);
		}
		return stripMarkdown(candidate).trim();
	}

	private String cleanLabels(String content) {
		return stripMarkdown(content)
			.replaceFirst("(?is)^\\s*(sql|SQL|查询语句|下面是 SQL|下面是SQL)\\s*[:：]\\s*", "")
			.trim();
	}

	private String stripMarkdown(String content) {
		return content.replace("```sql", "").replace("```SQL", "").replace("```", "").trim();
	}

	private String cutTrailingExplanation(String sql) {
		String[] lines = sql.split("\\R");
		StringBuilder builder = new StringBuilder();
		for (String line : lines) {
			String trimmed = line.trim();
			if (builder.length() > 0 && looksLikeExplanation(trimmed)) {
				break;
			}
			if (builder.length() > 0) {
				builder.append(System.lineSeparator());
			}
			builder.append(line);
		}
		return builder.toString().trim();
	}

	private boolean looksLikeExplanation(String line) {
		if (!StringUtils.hasText(line)) {
			return false;
		}
		return line.startsWith("这个") || line.startsWith("该") || line.startsWith("用于") || line.startsWith("说明")
				|| line.startsWith("解释") || line.startsWith("This query") || line.startsWith("The query");
	}

}
