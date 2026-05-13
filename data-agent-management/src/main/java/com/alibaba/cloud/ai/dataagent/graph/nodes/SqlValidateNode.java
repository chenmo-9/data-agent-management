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
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlValidator;
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlValidateNode implements GraphNode {

	private final SqlValidator sqlValidator;

	@Override
	public String name() {
		return "sql_validate";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent())) {
			return;
		}
		emitter.emitNodeStart(name(), "Validating SQL", Map.of("generatedSql", state.getGeneratedSql()));
		try {
			SqlValidationResult result = sqlValidator.validateDetailed(state.getGeneratedSql());
			if (!Boolean.TRUE.equals(result.getValid())) {
				throw new BusinessException(result.getMessage());
			}
			state.setValidatedSql(result.getSanitizedSql());
			state.setSqlLimited(result.getLimitApplied());
			state.setSqlLimit(result.getLimit());
			state.setSqlSecurityMessage(result.getMessage());
			state.setSqlValidationError(null);
			Map<String, Object> data = new HashMap<>();
			data.put("validatedSql", result.getSanitizedSql());
			data.put("sqlLimited", result.getLimitApplied());
			data.put("sqlLimit", result.getLimit());
			data.put("sqlSecurityMessage", result.getMessage());
			emitter.emitNodeEnd(name(), "SQL validated", data);
		}
		catch (BusinessException ex) {
			state.setValidatedSql(null);
			state.setSqlValidationError(ex.getMessage());
			Map<String, Object> data = new HashMap<>();
			data.put("generatedSql", state.getGeneratedSql());
			data.put("sqlValidationError", ex.getMessage());
			emitter.emitNodeEnd(name(), "SQL validation failed, repair will be attempted", data);
		}
	}

}
