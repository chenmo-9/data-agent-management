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

import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
@AllArgsConstructor
public class AiModelRegistry {

	private static final Set<String> OPENAI_COMPATIBLE_PROVIDERS = Set.of("deepseek", "openai", "qwen", "custom");

	private final MockLlmClient mockLlmClient;

	private final OpenAiCompatibleLlmClient openAiCompatibleLlmClient;

	public LlmClient getLlmClient(ModelConfig config) {
		validateChatConfig(config);
		String provider = config.getProvider().trim().toLowerCase(Locale.ROOT);
		if ("mock".equals(provider)) {
			return mockLlmClient;
		}
		if (OPENAI_COMPATIBLE_PROVIDERS.contains(provider)) {
			return openAiCompatibleLlmClient;
		}
		throw new BusinessException("unsupported model provider: " + config.getProvider());
	}

	private void validateChatConfig(ModelConfig config) {
		if (config == null) {
			throw new BusinessException("model config cannot be null");
		}
		if (!"chat".equalsIgnoreCase(config.getModelType())) {
			throw new BusinessException("modelType must be chat");
		}
		if (!Boolean.TRUE.equals(config.getEnabled())) {
			throw new BusinessException("model config is disabled");
		}
	}

}
