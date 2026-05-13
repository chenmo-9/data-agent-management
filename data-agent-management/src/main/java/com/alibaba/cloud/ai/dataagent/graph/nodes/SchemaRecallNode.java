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

import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.nl2sql.SchemaRecallItem;
import com.alibaba.cloud.ai.dataagent.nl2sql.SchemaRecallResult;
import com.alibaba.cloud.ai.dataagent.nl2sql.SchemaRecallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SchemaRecallNode implements GraphNode {

	private final SchemaRecallService schemaRecallService;

	@Override
	public String name() {
		return "schema_recall";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent())) {
			return;
		}
		emitter.emitNodeStart(name(), "Recalling schema", Map.of("agentId", state.getAgentId()));
		SchemaRecallResult result = schemaRecallService.recall(state.getAgentId(), state.getQuestion());
		state.setSelectedDatasourceId(result.getDatasourceId());
		state.setDatasourceName(result.getDatasourceName());
		state.setDbType(result.getDbType());
		state.setSchemaContext(result.getSchemaContext());
		state.setSchemaRecallResult(result);
		state.setSchemaRecallFallbackUsed(result.getFallbackUsed());
		state.setSchemaRecallMessage(result.getMessage());
		state.setRecalledTableCount(result.getTableCount());
		state.setRecalledFieldCount(result.getFieldCount());
		emitter.emitNodeEnd(name(), "Schema recalled", Map.of(
				"datasourceId", result.getDatasourceId(),
				"tableCount", result.getTableCount(),
				"fieldCount", result.getFieldCount(),
				"fallbackUsed", result.getFallbackUsed(),
				"selectedTables", simplify(result.getSelectedTables())));
	}

	private List<Map<String, Object>> simplify(List<SchemaRecallItem> items) {
		return items.stream()
			.map(item -> Map.of(
					"tableId", item.getTableId(),
					"tableName", item.getTableName(),
					"businessName", defaultString(item.getBusinessName()),
					"score", item.getScore(),
					"matchReasons", item.getMatchReasons(),
					"fieldCount", item.getFields().size()))
			.collect(Collectors.toList());
	}

	private String defaultString(String value) {
		return value == null ? "" : value;
	}

}
