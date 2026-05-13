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
import com.alibaba.cloud.ai.dataagent.nl2sql.RelationRecallResult;
import com.alibaba.cloud.ai.dataagent.nl2sql.RelationRecallService;
import com.alibaba.cloud.ai.dataagent.nl2sql.SchemaRelationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RelationRecallNode implements GraphNode {

	private final RelationRecallService relationRecallService;

	@Override
	public String name() {
		return "relation_recall";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent())) {
			return;
		}
		emitter.emitNodeStart(name(), "Recalling table relations", Map.of("datasourceId", state.getSelectedDatasourceId()));
		RelationRecallResult result = relationRecallService.recall(state.getSelectedDatasourceId(), state.getQuestion(),
				state.getSchemaRecallResult());
		state.setRelationRecallResult(result);
		state.setRelationContext(result.getRelationContext());
		state.setRecalledRelationCount(result.getRelationCount());
		state.setRelationRecallMessage(result.getMessage());
		state.setRelationRecallFallbackUsed(result.getFallbackUsed());
		if (StringUtils.hasText(result.getRelationContext())) {
			state.setSchemaContext(defaultString(state.getSchemaContext()) + "\n" + result.getRelationContext());
		}
		emitter.emitNodeEnd(name(), "Relations recalled", Map.of(
				"relationCount", result.getRelationCount(),
				"message", defaultString(result.getMessage()),
				"selectedRelations", simplify(result.getSelectedRelations()),
				"relationContext", defaultString(result.getRelationContext())));
	}

	private List<Map<String, Object>> simplify(List<SchemaRelationItem> relations) {
		return relations.stream()
			.map(item -> Map.of(
					"id", item.getId(),
					"source", item.getSourceTableName() + "." + item.getSourceFieldName(),
					"joinType", defaultString(item.getJoinType()),
					"target", item.getTargetTableName() + "." + item.getTargetFieldName(),
					"relationType", defaultString(item.getRelationType()),
					"score", item.getScore(),
					"matchReasons", item.getMatchReasons()))
			.collect(Collectors.toList());
	}

	private String defaultString(String value) {
		return value == null ? "" : value;
	}

}
