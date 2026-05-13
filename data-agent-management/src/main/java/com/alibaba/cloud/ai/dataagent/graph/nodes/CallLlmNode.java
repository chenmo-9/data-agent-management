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
package com.alibaba.cloud.ai.dataagent.graph.nodes;

import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatMessage;
import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatRequest;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.service.llm.LlmService;
import com.alibaba.cloud.ai.dataagent.vo.llm.LlmChatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CallLlmNode implements GraphNode {

	private final LlmService llmService;

	@Override
	public String name() {
		return "call_llm";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		Map<String, Object> startData = new HashMap<>();
		startData.put("modelConfigId", state.getModelConfigId());
		emitter.emitNodeStart(name(), "Calling LLM", startData);
		if (state.getModelConfigId() == null) {
			throw new BusinessException("modelConfigId cannot be null");
		}

		LlmChatRequest request = new LlmChatRequest();
		request.setModelConfigId(state.getModelConfigId());
		request.setMessages(buildMessages(state));
		LlmChatVO response = llmService.chat(request);
		if (!Boolean.TRUE.equals(response.getSuccess())) {
			throw new BusinessException(response.getMessage());
		}
		state.setAnswer(response.getContent());
		Map<String, Object> endData = new HashMap<>();
		endData.put("provider", response.getProvider());
		endData.put("modelName", response.getModelName());
		emitter.emitNodeEnd(name(), "LLM call completed", endData);
	}

	private List<LlmChatMessage> buildMessages(GraphState state) {
		List<LlmChatMessage> messages = new ArrayList<>();
		if (StringUtils.hasText(state.getAgentPrompt())) {
			LlmChatMessage systemMessage = new LlmChatMessage();
			systemMessage.setRole("system");
			systemMessage.setContent(state.getAgentPrompt() + "\n\nContext summary: datasourceCount="
					+ defaultNumber(state.getDatasourceCount()) + ", semanticModelCount="
					+ defaultNumber(state.getSemanticModelCount()) + ", knowledgeCount="
					+ defaultNumber(state.getKnowledgeCount()) + ".");
			messages.add(systemMessage);
		}
		LlmChatMessage userMessage = new LlmChatMessage();
		userMessage.setRole("user");
		userMessage.setContent(state.getQuestion());
		messages.add(userMessage);
		return messages;
	}

	private int defaultNumber(Integer value) {
		return value == null ? 0 : value;
	}

}
