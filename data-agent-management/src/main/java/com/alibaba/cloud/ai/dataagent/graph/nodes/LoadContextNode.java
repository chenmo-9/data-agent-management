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

import com.alibaba.cloud.ai.dataagent.entity.AgentDatasource;
import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.SemanticTable;
import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.mapper.AgentDatasourceMapper;
import com.alibaba.cloud.ai.dataagent.mapper.AgentKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticTableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoadContextNode implements GraphNode {

	private final AgentDatasourceMapper agentDatasourceMapper;

	private final SemanticTableMapper semanticTableMapper;

	private final AgentKnowledgeMapper agentKnowledgeMapper;

	@Override
	public String name() {
		return "load_context";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		Map<String, Object> startData = new HashMap<>();
		startData.put("agentId", state.getAgentId());
		emitter.emitNodeStart(name(), "Loading context summary", startData);

		List<AgentDatasource> datasourceRelations = agentDatasourceMapper.selectByAgentId(state.getAgentId())
			.stream()
			.filter(item -> Boolean.TRUE.equals(item.getEnabled()))
			.toList();
		int semanticModelCount = datasourceRelations.stream()
			.map(AgentDatasource::getDatasourceId)
			.map(semanticTableMapper::selectByDatasourceId)
			.flatMap(List::stream)
			.filter(table -> Boolean.TRUE.equals(table.getEnabled()))
			.mapToInt(table -> 1)
			.sum();
		List<AgentKnowledge> knowledgeRelations = agentKnowledgeMapper.selectByAgentId(state.getAgentId())
			.stream()
			.filter(item -> Boolean.TRUE.equals(item.getEnabled()))
			.toList();

		state.setDatasourceCount(datasourceRelations.size());
		state.setSemanticModelCount(semanticModelCount);
		state.setKnowledgeCount(knowledgeRelations.size());
		emitter.emitNodeEnd(name(), "Context summary loaded", Map.of("datasourceCount", datasourceRelations.size(),
				"semanticModelCount", semanticModelCount, "knowledgeCount", knowledgeRelations.size()));
	}

}
