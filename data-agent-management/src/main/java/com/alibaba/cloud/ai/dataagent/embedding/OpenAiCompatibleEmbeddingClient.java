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
package com.alibaba.cloud.ai.dataagent.embedding;

import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiCompatibleEmbeddingClient {

	private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final SecretService secretService;

	public List<Double> embed(ModelConfig config, String text) {
		try {
			String requestBody = objectMapper.writeValueAsString(buildRequestBody(config, text));
			HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(resolveEmbeddingsUrl(config.getBaseUrl())))
				.timeout(Duration.ofSeconds(60))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody));
			String apiKey = secretService.decryptIfNeeded(config.getApiKey());
			if (apiKey != null && !apiKey.isBlank()) {
				builder.header("Authorization", "Bearer " + apiKey);
			}
			HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new BusinessException("embedding request failed with status " + response.statusCode() + ": "
						+ response.body());
			}
			JsonNode embeddingNode = objectMapper.readTree(response.body()).path("data").path(0).path("embedding");
			if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
				throw new BusinessException("embedding response missing embedding array");
			}
			List<Double> vector = new ArrayList<>();
			embeddingNode.forEach(node -> vector.add(node.asDouble()));
			return vector;
		}
		catch (BusinessException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new BusinessException("embedding request failed: " + ex.getMessage());
		}
	}

	private Map<String, Object> buildRequestBody(ModelConfig config, String text) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("model", config.getModelName());
		body.put("input", text == null ? "" : text);
		return body;
	}

	private String resolveEmbeddingsUrl(String baseUrl) {
		String normalized = baseUrl == null ? "" : baseUrl.trim();
		if (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		if (normalized.endsWith("/embeddings")) {
			return normalized;
		}
		return normalized + "/embeddings";
	}

}
