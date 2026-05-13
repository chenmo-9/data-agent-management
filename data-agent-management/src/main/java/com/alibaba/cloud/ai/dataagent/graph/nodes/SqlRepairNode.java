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
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlRepairResult;
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlRepairer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlRepairNode implements GraphNode {

	private final SqlRepairer sqlRepairer;

	@Override
	public String name() {
		return "sql_repair";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent())) {
			return;
		}
		if (state.getValidatedSql() != null && !state.getValidatedSql().isBlank()) {
			state.setSqlRepairAttempted(false);
			state.setSqlRepairSuccess(true);
			state.setSqlRepairMessage("No repair needed");
			emitter.emitNodeEnd(name(), "No repair needed", Map.of("validatedSql", state.getValidatedSql()));
			return;
		}
		state.setSqlRepairAttempted(true);
		emitter.emitNodeStart(name(), "Repairing SQL", Map.of("sqlValidationError", state.getSqlValidationError()));
		SqlRepairResult rules = sqlRepairer.repairWithRules(state.getGeneratedSql(), state.getSqlValidationError(),
				state.getSchemaContext());
		if (Boolean.TRUE.equals(rules.getSuccess())) {
			applySuccess(state, emitter, rules);
			return;
		}
		SqlRepairResult llm = sqlRepairer.repairWithLlm(state.getModelConfigId(), state.getQuestion(),
				state.getSchemaContext(), state.getKnowledgeContext(), state.getGeneratedSql(),
				rules.getMessage());
		if (Boolean.TRUE.equals(llm.getSuccess())) {
			applySuccess(state, emitter, llm);
			return;
		}
		state.setSqlRepairSuccess(false);
		state.setSqlRepairMessage(llm.getMessage());
		throw new BusinessException(llm.getMessage());
	}

	private void applySuccess(GraphState state, GraphEventEmitter emitter, SqlRepairResult result) {
		state.setRepairedSql(result.getSql());
		state.setValidatedSql(result.getSql());
		state.setSqlRepairSuccess(true);
		state.setSqlRepairMessage(result.getMessage());
		state.setSqlValidationError(null);
		Map<String, Object> data = new HashMap<>();
		data.put("repairedSql", result.getSql());
		data.put("validatedSql", result.getSql());
		data.put("sqlRepairAttempted", state.getSqlRepairAttempted());
		data.put("sqlRepairSuccess", true);
		data.put("sqlRepairMessage", result.getMessage());
		data.put("source", result.getSource());
		emitter.emitNodeEnd(name(), result.getMessage(), data);
	}

}
