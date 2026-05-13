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
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlGenerationResult;
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlGenerateNode implements GraphNode {

	private final SqlGenerator sqlGenerator;

	@Override
	public String name() {
		return "sql_generate";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent())) {
			return;
		}
		emitter.emitNodeStart(name(), "Generating SQL", Map.of("modelConfigId", state.getModelConfigId()));
		SqlGenerationResult result = sqlGenerator.generateResult(state.getModelConfigId(), state.getQuestion(), state.getSchemaContext(),
				state.getKnowledgeContext());
		state.setRawLlmSqlOutput(result.getRawLlmSqlOutput());
		state.setExtractedSql(result.getExtractedSql());
		state.setGeneratedSql(result.getGeneratedSql());
		Map<String, Object> data = new HashMap<>();
		data.put("rawLlmSqlOutput", result.getRawLlmSqlOutput());
		data.put("extractedSql", result.getExtractedSql());
		data.put("generatedSql", result.getGeneratedSql());
		emitter.emitNodeEnd(name(), "SQL generated", data);
	}

}
