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

import java.util.List;

@Component
public class IntentRecognizer {

	private static final List<String> DATA_QUERY_KEYWORDS = List.of("查询", "统计", "多少", "销售额", "订单", "金额",
			"数量", "排名", "revenue", "count", "sum", "total", "orders");

	public String recognize(String question) {
		if (!StringUtils.hasText(question)) {
			return "unknown";
		}
		String normalized = question.toLowerCase();
		return DATA_QUERY_KEYWORDS.stream().anyMatch(normalized::contains) ? "data_query" : "chat";
	}

}
