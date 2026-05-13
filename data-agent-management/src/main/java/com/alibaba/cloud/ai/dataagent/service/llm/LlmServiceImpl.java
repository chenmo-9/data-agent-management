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
package com.alibaba.cloud.ai.dataagent.service.llm;

import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatMessage;
import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatRequest;
import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatResponse;
import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.entity.PromptTemplate;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.llm.AiModelRegistry;
import com.alibaba.cloud.ai.dataagent.llm.LlmClient;
import com.alibaba.cloud.ai.dataagent.mapper.ModelConfigMapper;
import com.alibaba.cloud.ai.dataagent.mapper.PromptTemplateMapper;
import com.alibaba.cloud.ai.dataagent.prompt.PromptRenderer;
import com.alibaba.cloud.ai.dataagent.vo.llm.LlmChatVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * LLM Service Class
 */
@Slf4j
@Service
@AllArgsConstructor
public class LlmServiceImpl implements LlmService {

	private static final String DEFAULT_PROMPT_VERSION = "v1";

	private final ModelConfigMapper modelConfigMapper;

	private final PromptTemplateMapper promptTemplateMapper;

	private final PromptRenderer promptRenderer;

	private final AiModelRegistry aiModelRegistry;

	@Override
	public LlmChatVO chat(LlmChatRequest request) {
		ModelConfig config = requireModelConfig(request.getModelConfigId());
		validateConfig(config);
		List<LlmChatMessage> messages = buildMessages(request);
		LlmClient client = aiModelRegistry.getLlmClient(config);
		LlmChatResponse response = client.chat(config, messages);
		return toVO(response);
	}

	private List<LlmChatMessage> buildMessages(LlmChatRequest request) {
		List<LlmChatMessage> messages = new ArrayList<>();
		String systemPrompt = renderSystemPrompt(request);
		if (systemPrompt != null && !systemPrompt.isBlank()) {
			messages.add(message("system", systemPrompt));
		}
		if (request.getMessages() != null) {
			for (LlmChatMessage message : request.getMessages()) {
				validateMessage(message);
				messages.add(message);
			}
		}
		if (request.getUserMessage() != null && !request.getUserMessage().isBlank()) {
			messages.add(message("user", request.getUserMessage()));
		}
		boolean hasUserMessage = messages.stream().anyMatch(message -> "user".equalsIgnoreCase(message.getRole())
				&& message.getContent() != null && !message.getContent().isBlank());
		if (!hasUserMessage) {
			throw new BusinessException("messages must contain at least one user message");
		}
		return messages;
	}

	private String renderSystemPrompt(LlmChatRequest request) {
		if (request.getPromptKey() == null || request.getPromptKey().isBlank()) {
			return null;
		}
		String promptKey = request.getPromptKey().trim().toLowerCase(Locale.ROOT);
		String version = request.getPromptVersion() == null || request.getPromptVersion().isBlank()
				? DEFAULT_PROMPT_VERSION : request.getPromptVersion().trim();
		PromptTemplate template = promptTemplateMapper.selectByPromptKeyAndVersion(promptKey, version);
		if (template == null) {
			throw new BusinessException("prompt template not found: " + promptKey + " " + version);
		}
		return promptRenderer.render(template.getContent(), request.getVariables());
	}

	private void validateMessage(LlmChatMessage message) {
		if (message == null || message.getRole() == null || message.getRole().isBlank()) {
			throw new BusinessException("message role cannot be blank");
		}
		String role = message.getRole().trim().toLowerCase(Locale.ROOT);
		if (!"system".equals(role) && !"user".equals(role) && !"assistant".equals(role)) {
			throw new BusinessException("message role must be system, user or assistant");
		}
		if (message.getContent() == null || message.getContent().isBlank()) {
			throw new BusinessException("message content cannot be blank");
		}
		message.setRole(role);
	}

	private ModelConfig requireModelConfig(Long id) {
		ModelConfig config = modelConfigMapper.selectById(id);
		if (config == null) {
			throw new BusinessException("model config with id: %d not found".formatted(id));
		}
		return config;
	}

	private void validateConfig(ModelConfig config) {
		if (!"chat".equalsIgnoreCase(config.getModelType())) {
			throw new BusinessException("modelType must be chat");
		}
		if (!Boolean.TRUE.equals(config.getEnabled())) {
			throw new BusinessException("model config is disabled");
		}
	}

	private LlmChatMessage message(String role, String content) {
		LlmChatMessage message = new LlmChatMessage();
		message.setRole(role);
		message.setContent(content);
		return message;
	}

	private LlmChatVO toVO(LlmChatResponse response) {
		LlmChatVO vo = new LlmChatVO();
		vo.setModelConfigId(response.getModelConfigId());
		vo.setProvider(response.getProvider());
		vo.setModelName(response.getModelName());
		vo.setContent(response.getContent());
		vo.setSuccess(response.getSuccess());
		vo.setMessage(response.getMessage());
		return vo;
	}

}
