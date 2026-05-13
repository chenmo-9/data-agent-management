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

import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class StartNode implements GraphNode {

	@Override
	public String name() {
		return "start";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!StringUtils.hasText(state.getSessionId())) {
			state.setSessionId(UUID.randomUUID().toString());
		}
		Map<String, Object> data = new HashMap<>();
		data.put("agentId", state.getAgentId());
		data.put("modelConfigId", state.getModelConfigId());
		emitter.emitNodeStart(name(), "Start graph workflow", data);
		if (!StringUtils.hasText(state.getQuestion())) {
			throw new BusinessException("Question cannot be empty");
		}
		emitter.emitNodeEnd(name(), "Graph workflow started", Map.of("sessionId", state.getSessionId()));
	}

}
