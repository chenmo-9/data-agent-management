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

import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatRequest;
import com.alibaba.cloud.ai.dataagent.service.llm.LlmService;
import com.alibaba.cloud.ai.dataagent.vo.llm.LlmChatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlGenerator {

	private final LlmService llmService;

	private final SqlExtractor sqlExtractor;

	public String generate(Long modelConfigId, String question, String schemaContext, String knowledgeContext) {
		return generateResult(modelConfigId, question, schemaContext, knowledgeContext).getGeneratedSql();
	}

	public SqlGenerationResult generateResult(Long modelConfigId, String question, String schemaContext,
			String knowledgeContext) {
		if (looksLikeRawSql(question)) {
			String extracted = sqlExtractor.extract(question);
			return SqlGenerationResult.builder()
				.rawLlmSqlOutput(question)
				.extractedSql(extracted)
				.generatedSql(extracted)
				.build();
		}
		LlmChatRequest request = new LlmChatRequest();
		request.setModelConfigId(modelConfigId);
		request.setPromptKey("sql_generate");
		request.setPromptVersion("v1");
		Map<String, Object> variables = new HashMap<>();
		variables.put("question", question);
		variables.put("schema", schemaContext);
		variables.put("knowledge", knowledgeContext);
		request.setVariables(variables);
		request.setUserMessage("请根据上下文生成 SQL");

		LlmChatVO response = llmService.chat(request);
		String raw = response.getContent();
		String extracted = sqlExtractor.extract(raw);
		if (StringUtils.hasText(extracted)) {
			return SqlGenerationResult.builder().rawLlmSqlOutput(raw).extractedSql(extracted).generatedSql(extracted).build();
		}
		if (shouldUseFallback(response, raw)) {
			String fallback = fallbackSql(question, schemaContext);
			return SqlGenerationResult.builder().rawLlmSqlOutput(raw).extractedSql("").generatedSql(fallback).build();
		}
		return SqlGenerationResult.builder().rawLlmSqlOutput(raw).extractedSql("").generatedSql("").build();
	}

	private boolean shouldUseFallback(LlmChatVO response, String raw) {
		String provider = response.getProvider() == null ? "" : response.getProvider().toLowerCase(Locale.ROOT);
		String content = raw == null ? "" : raw.toLowerCase(Locale.ROOT);
		return "mock".equals(provider) || content.startsWith("mock response:");
	}

	private boolean looksLikeRawSql(String question) {
		if (!StringUtils.hasText(question)) {
			return false;
		}
		String normalized = question.trim().toLowerCase(Locale.ROOT);
		return normalized.startsWith("select ") || normalized.startsWith("insert ") || normalized.startsWith("update ")
				|| normalized.startsWith("delete ") || normalized.startsWith("drop ") || normalized.startsWith("alter ")
				|| normalized.startsWith("truncate ") || normalized.startsWith("create ");
	}

	private String fallbackSql(String question, String schemaContext) {
		String lowerQuestion = question == null ? "" : question.toLowerCase(Locale.ROOT);
		String lowerSchema = schemaContext == null ? "" : schemaContext.toLowerCase(Locale.ROOT);
		// Mock 模型不会真正生成 SQL。这里提供一个极小 fallback，只用于本地端到端验证。
		if ((lowerQuestion.contains("每个用户") || lowerQuestion.contains("按用户") || lowerQuestion.contains("用户销售额")
				|| lowerQuestion.contains("each user") || lowerQuestion.contains("by user"))
				&& lowerSchema.contains("orders") && lowerSchema.contains("users") && lowerSchema.contains("user_id")) {
			return """
					SELECT u.name, SUM(o.amount) AS total_sales
					FROM users u
					JOIN orders o ON o.user_id = u.id
					GROUP BY u.name
					""";
		}
		if (lowerSchema.contains("orders") && lowerSchema.contains("amount")) {
			if (lowerQuestion.contains("count") || lowerQuestion.contains("数量") || lowerQuestion.contains("多少订单")) {
				return "SELECT COUNT(*) AS total_count FROM orders";
			}
			return "SELECT SUM(amount) AS total_sales FROM orders";
		}
		return generatedEmptyFallback();
	}

	private String generatedEmptyFallback() {
		return "SELECT 1 AS result";
	}

}
