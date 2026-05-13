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
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FinishNode implements GraphNode {

	@Override
	public String name() {
		return "finish";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (state.getErrorMessage() == null) {
			state.setSuccess(true);
		}
		Map<String, Object> data = new HashMap<>();
		data.put("answer", state.getAnswer());
		data.put("intent", state.getIntent());
		data.put("businessRuleCandidate", state.getBusinessRuleCandidate());
		data.put("unsafeOperation", state.getUnsafeOperation());
		data.put("skipSql", state.getSkipSql());
		data.put("datasourceId", state.getSelectedDatasourceId());
		data.put("schemaRecallResult", state.getSchemaRecallResult());
		data.put("schemaRecallFallbackUsed", state.getSchemaRecallFallbackUsed());
		data.put("schemaRecallMessage", state.getSchemaRecallMessage());
		data.put("recalledTableCount", state.getRecalledTableCount());
		data.put("recalledFieldCount", state.getRecalledFieldCount());
		data.put("schemaContext", state.getSchemaContext());
		data.put("relationRecallResult", state.getRelationRecallResult());
		data.put("relationContext", state.getRelationContext());
		data.put("recalledRelationCount", state.getRecalledRelationCount());
		data.put("relationRecallMessage", state.getRelationRecallMessage());
		data.put("relationRecallFallbackUsed", state.getRelationRecallFallbackUsed());
		data.put("knowledgeRecallResult", state.getKnowledgeRecallResult());
		data.put("knowledgeContext", state.getKnowledgeContext());
		data.put("knowledgeRecallFallbackUsed", state.getKnowledgeRecallFallbackUsed());
		data.put("knowledgeRecallMessage", state.getKnowledgeRecallMessage());
		data.put("recalledKnowledgeCount", state.getRecalledKnowledgeCount());
		data.put("rawLlmSqlOutput", state.getRawLlmSqlOutput());
		data.put("extractedSql", state.getExtractedSql());
		data.put("generatedSql", state.getGeneratedSql());
		data.put("repairedSql", state.getRepairedSql());
		data.put("validatedSql", state.getValidatedSql());
		data.put("sanitizedSql", state.getSanitizedSql());
		data.put("sqlLimited", state.getSqlLimited());
		data.put("sqlLimit", state.getSqlLimit());
		data.put("sqlResultTruncated", state.getSqlResultTruncated());
		data.put("sqlQueryTimeoutSeconds", state.getSqlQueryTimeoutSeconds());
		data.put("sqlSecurityMessage", state.getSqlSecurityMessage());
		data.put("sqlRepairAttempted", state.getSqlRepairAttempted());
		data.put("sqlRepairSuccess", state.getSqlRepairSuccess());
		data.put("sqlRepairMessage", state.getSqlRepairMessage());
		data.put("sqlValidationError", state.getSqlValidationError());
		data.put("sqlExecutionError", state.getSqlExecutionError());
		data.put("sqlResult", state.getSqlResult());
		data.put("rowCount", state.getRowCount());
		data.put("analysisSummary", state.getAnalysisSummary());
		data.put("analysisResult", state.getAnalysisResult());
		data.put("pythonEngine", state.getPythonEngine());
		data.put("pythonExecuted", state.getPythonExecuted());
		data.put("pythonSuccess", state.getPythonSuccess());
		data.put("pythonCode", state.getPythonCode());
		data.put("pythonStdout", state.getPythonStdout());
		data.put("pythonStderr", state.getPythonStderr());
		data.put("pythonExitCode", state.getPythonExitCode());
		data.put("pythonDurationMs", state.getPythonDurationMs());
		data.put("pythonErrorMessage", state.getPythonErrorMessage());
		data.put("pythonFallbackUsed", state.getPythonFallbackUsed());
		data.put("reportMarkdown", state.getReportMarkdown());
		data.put("reportTitle", state.getReportTitle());
		data.put("reportSummary", resolveReportSummary(state));
		data.put("summary", resolveReportSummary(state));
		data.put("reportResult", state.getReportResult());
		data.put("chartSpec", state.getChartSpec());
		emitter.emitFinish("Graph workflow finished", data);
	}

	private String resolveReportSummary(GraphState state) {
		if (state.getReportSummary() != null && !state.getReportSummary().isBlank()) {
			return state.getReportSummary();
		}
		if (state.getReportResult() != null && state.getReportResult().getSummary() != null
				&& !state.getReportResult().getSummary().isBlank()) {
			return state.getReportResult().getSummary();
		}
		return "";
	}

}
