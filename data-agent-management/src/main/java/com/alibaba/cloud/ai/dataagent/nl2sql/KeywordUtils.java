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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class KeywordUtils {

	private static final List<String> DICTIONARY = List.of("销售额", "订单", "下单", "创建时间", "时间", "最近", "数量", "金额", "用户",
			"商品", "地区", "利润", "收入", "revenue", "sales", "amount", "order", "count", "sum", "user",
			"customer", "profit", "date", "time", "created", "price");

	private static final Set<String> STOP_WORDS = Set.of("的", "了", "是", "多少", "查询", "统计", "一下", "请", "how", "what",
			"show", "the", "is", "are", "of", "a", "an", "to", "for");

	public List<String> extractKeywords(String question) {
		if (!StringUtils.hasText(question)) {
			return List.of();
		}
		String normalized = normalize(question);
		Set<String> keywords = new LinkedHashSet<>();
		for (String token : normalized.split("\\s+")) {
			if (token.length() > 1 && !STOP_WORDS.contains(token)) {
				keywords.add(token);
			}
		}
		for (String phrase : DICTIONARY) {
			if (normalized.contains(normalize(phrase))) {
				keywords.add(normalize(phrase));
			}
		}
		return new ArrayList<>(keywords);
	}

	public String normalize(String text) {
		if (!StringUtils.hasText(text)) {
			return "";
		}
		return text.toLowerCase(Locale.ROOT)
			.replaceAll("[\\p{Punct}，。！？；：、“”‘’（）()\\[\\]{}<>]", " ")
			.replaceAll("\\s+", " ")
			.trim();
	}

	public boolean containsAny(String text, List<String> keywords) {
		String normalized = normalize(text);
		for (String keyword : keywords) {
			if (StringUtils.hasText(keyword) && normalized.contains(normalize(keyword))) {
				return true;
			}
		}
		return false;
	}

	public List<String> splitSynonyms(String synonyms) {
		if (!StringUtils.hasText(synonyms)) {
			return List.of();
		}
		String normalized = synonyms.replace('，', ',').replace('、', ',').replace(';', ',');
		List<String> values = new ArrayList<>();
		for (String token : normalized.split(",")) {
			String value = normalize(token);
			if (StringUtils.hasText(value)) {
				values.add(value);
			}
		}
		return values;
	}

}
