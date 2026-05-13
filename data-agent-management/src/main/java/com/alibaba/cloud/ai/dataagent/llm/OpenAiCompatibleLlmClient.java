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
package com.alibaba.cloud.ai.dataagent.llm;

import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatMessage;
import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatResponse;
import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.security.SecretService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiCompatibleLlmClient implements LlmClient {

	private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final SecretService secretService;

	@Override
	public LlmChatResponse chat(ModelConfig config, List<LlmChatMessage> messages) {
		try {
			String requestBody = objectMapper.writeValueAsString(buildRequestBody(config, messages));
			HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(resolveChatCompletionsUrl(config.getBaseUrl())))
				.timeout(Duration.ofSeconds(60))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody));
			String apiKey = secretService.decryptIfNeeded(config.getApiKey());
			if (apiKey != null && !apiKey.isBlank()) {
				builder.header("Authorization", "Bearer " + apiKey);
			}
			HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				return LlmChatResponse.error(config.getId(), config.getProvider(), config.getModelName(),
						"LLM request failed with status " + response.statusCode() + ": " + response.body());
			}
			JsonNode root = objectMapper.readTree(response.body());
			String content = root.path("choices").path(0).path("message").path("content").asText();
			return LlmChatResponse.success(config.getId(), config.getProvider(), config.getModelName(), content);
		}
		catch (Exception ex) {
			return LlmChatResponse.error(config.getId(), config.getProvider(), config.getModelName(),
					"LLM request failed: " + ex.getMessage());
		}
	}

	private Map<String, Object> buildRequestBody(ModelConfig config, List<LlmChatMessage> messages) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("model", config.getModelName());
		body.put("messages", messages.stream().map(message -> {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("role", message.getRole());
			item.put("content", message.getContent());
			return item;
		}).toList());
		if (config.getTemperature() != null) {
			body.put("temperature", config.getTemperature());
		}
		if (config.getMaxTokens() != null) {
			body.put("max_tokens", config.getMaxTokens());
		}
		return body;
	}

	private String resolveChatCompletionsUrl(String baseUrl) {
		String normalized = baseUrl == null ? "" : baseUrl.trim();
		if (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		if (normalized.endsWith("/chat/completions")) {
			return normalized;
		}
		return normalized + "/chat/completions";
	}

}
