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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EmbeddingModelRegistry {

	private static final Set<String> OPENAI_COMPATIBLE_PROVIDERS = Set.of("deepseek", "openai", "qwen", "custom");

	private final MockEmbeddingClient mockEmbeddingClient;

	private final OpenAiCompatibleEmbeddingClient openAiCompatibleEmbeddingClient;

	public List<Double> embed(ModelConfig config, String text) {
		validateEmbeddingConfig(config);
		String provider = config.getProvider().trim().toLowerCase(Locale.ROOT);
		if ("mock".equals(provider)) {
			return mockEmbeddingClient.embed(text);
		}
		if (OPENAI_COMPATIBLE_PROVIDERS.contains(provider)) {
			return openAiCompatibleEmbeddingClient.embed(config, text);
		}
		throw new BusinessException("unsupported embedding provider: " + config.getProvider());
	}

	private void validateEmbeddingConfig(ModelConfig config) {
		if (config == null) {
			throw new BusinessException("embedding model config cannot be null");
		}
		if (!"embedding".equalsIgnoreCase(config.getModelType())) {
			throw new BusinessException("modelType must be embedding");
		}
		if (!Boolean.TRUE.equals(config.getEnabled())) {
			throw new BusinessException("embedding model config is disabled");
		}
	}

}
