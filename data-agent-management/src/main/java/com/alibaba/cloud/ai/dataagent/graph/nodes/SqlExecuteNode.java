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

import com.alibaba.cloud.ai.dataagent.dto.nl2sql.SqlExecutionResultDTO;
import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlExecuteNode implements GraphNode {

	private final SqlExecutor sqlExecutor;

	@Override
	public String name() {
		return "sql_execute";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent())) {
			return;
		}
		if (state.getValidatedSql() == null || state.getValidatedSql().isBlank()) {
			throw new IllegalStateException("validated SQL cannot be empty");
		}
		emitter.emitNodeStart(name(), "Executing SQL", Map.of("datasourceId", state.getSelectedDatasourceId()));
		SqlExecutionResultDTO result = sqlExecutor.execute(state.getSelectedDatasourceId(), state.getValidatedSql());
		state.setSqlResult(result.getRows());
		state.setRowCount(result.getRowCount());
		state.setSanitizedSql(result.getSanitizedSql());
		state.setSqlLimited(result.getSqlLimited());
		state.setSqlLimit(result.getSqlLimit());
		state.setSqlResultTruncated(result.getTruncated());
		state.setSqlQueryTimeoutSeconds(result.getQueryTimeoutSeconds());
		state.setSqlSecurityMessage(result.getSecurityMessage());
		Map<String, Object> data = new HashMap<>();
		data.put("rowCount", result.getRowCount());
		data.put("message", result.getMessage());
		data.put("sanitizedSql", result.getSanitizedSql());
		data.put("sqlLimited", result.getSqlLimited());
		data.put("sqlLimit", result.getSqlLimit());
		data.put("sqlResultTruncated", result.getTruncated());
		data.put("sqlQueryTimeoutSeconds", result.getQueryTimeoutSeconds());
		data.put("sqlSecurityMessage", result.getSecurityMessage());
		if (Boolean.TRUE.equals(result.getSuccess())) {
			emitter.emitNodeEnd(name(), "SQL executed", data);
		}
		else {
			state.setSuccess(false);
			state.setSqlError(result.getMessage());
			state.setSqlExecutionError(result.getMessage());
			state.setErrorMessage(result.getMessage());
			emitter.emitError(name(), result.getMessage(), data);
		}
	}

}
