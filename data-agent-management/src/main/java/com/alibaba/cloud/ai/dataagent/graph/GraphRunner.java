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
package com.alibaba.cloud.ai.dataagent.graph;

import com.alibaba.cloud.ai.dataagent.dto.graph.GraphRunRequest;
import com.alibaba.cloud.ai.dataagent.graph.nodes.CallLlmNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.FinishNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.HumanConfirmNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.IntentRecognitionNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.KnowledgeLoadNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.LoadAgentNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.LoadContextNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.Nl2SqlAnswerNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.PythonAnalyzeNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.RelationRecallNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.ReportGenerateNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.SchemaRecallNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.SqlExecuteNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.SqlGenerateNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.SqlRepairNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.SqlValidateNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.StartNode;
import com.alibaba.cloud.ai.dataagent.service.graphhistory.GraphHistoryService;
import com.alibaba.cloud.ai.dataagent.vo.graph.GraphRunVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GraphRunner {

	private final StartNode startNode;

	private final LoadAgentNode loadAgentNode;

	private final LoadContextNode loadContextNode;

	private final CallLlmNode callLlmNode;

	private final IntentRecognitionNode intentRecognitionNode;

	private final SchemaRecallNode schemaRecallNode;

	private final KnowledgeLoadNode knowledgeLoadNode;

	private final RelationRecallNode relationRecallNode;

	private final SqlGenerateNode sqlGenerateNode;

	private final SqlValidateNode sqlValidateNode;

	private final SqlRepairNode sqlRepairNode;

	private final HumanConfirmNode humanConfirmNode;

	private final SqlExecuteNode sqlExecuteNode;

	private final PythonAnalyzeNode pythonAnalyzeNode;

	private final ReportGenerateNode reportGenerateNode;

	private final Nl2SqlAnswerNode nl2SqlAnswerNode;

	private final FinishNode finishNode;

	private final GraphHistoryService graphHistoryService;

	public GraphRunVO run(GraphRunRequest request) {
		GraphState state = execute(request, null);
		return toVO(state);
	}

	public GraphState runWithEmitter(GraphRunRequest request, SseEmitter sseEmitter) {
		return execute(request, sseEmitter);
	}

	private GraphState execute(GraphRunRequest request, SseEmitter sseEmitter) {
		GraphState state = buildState(request);
		graphHistoryService.startRun(state, request);
		GraphEventEmitter emitter = new GraphEventEmitter(state, sseEmitter,
				event -> graphHistoryService.saveEvent(state.getRunId(), event));
		for (GraphNode node : nodes(state)) {
			try {
				if (shouldSkipNode(state, node)) {
					continue;
				}
				state.setCurrentNode(node.name());
				node.execute(state, emitter);
				if (Boolean.TRUE.equals(state.getPaused())) {
					graphHistoryService.markPendingConfirm(state);
					break;
				}
			}
			catch (Exception ex) {
				handleError(state, emitter, node.name(), ex);
				break;
			}
		}
		if (!Boolean.TRUE.equals(state.getPaused())) {
			graphHistoryService.finishRun(state, toVO(state));
		}
		return state;
	}

	private List<GraphNode> nodes(GraphState state) {
		if ("chat".equalsIgnoreCase(state.getMode())) {
			return List.of(startNode, loadAgentNode, loadContextNode, callLlmNode, finishNode);
		}
		return List.of(startNode, loadAgentNode, intentRecognitionNode, schemaRecallNode, relationRecallNode, knowledgeLoadNode,
				sqlGenerateNode, sqlValidateNode, sqlRepairNode, humanConfirmNode, sqlExecuteNode, pythonAnalyzeNode, reportGenerateNode,
				nl2SqlAnswerNode, finishNode);
	}

	private GraphState buildState(GraphRunRequest request) {
		return GraphState.builder()
			.sessionId(request.getSessionId())
			.agentId(request.getAgentId())
			.modelConfigId(request.getModelConfigId())
			.embeddingModelConfigId(request.getEmbeddingModelConfigId())
			.knowledgeTopK(request.getKnowledgeTopK())
			.confirmBeforeExecute(Boolean.TRUE.equals(request.getConfirmBeforeExecute()))
			.question(request.getQuestion())
			.mode(defaultMode(request.getMode()))
			.success(false)
			.build();
	}

	private String defaultMode(String mode) {
		return mode == null || mode.isBlank() ? "nl2sql" : mode;
	}

	private void handleError(GraphState state, GraphEventEmitter emitter, String nodeName, Exception ex) {
		state.setSuccess(false);
		state.setErrorMessage(ex.getMessage());
		Map<String, Object> data = new HashMap<>();
		data.put("exception", ex.getClass().getSimpleName());
		try {
			emitter.emitError(nodeName, ex.getMessage(), data);
		}
		catch (Exception ignored) {
			// The workflow result still records failure even if the SSE client has gone away.
		}
	}

	private GraphRunVO toVO(GraphState state) {
		return GraphRunVO.builder()
			.sessionId(state.getSessionId())
			.runId(state.getRunId())
			.durationMs(state.getDurationMs())
			.historySaved(state.getHistorySaved())
			.eventCount(state.getEvents() == null ? 0 : state.getEvents().size())
			.confirmRequired(state.getHumanConfirmRequired())
			.confirmStatus(state.getConfirmStatus())
			.confirmSql(state.getConfirmSql())
			.confirmedSql(state.getConfirmedSql())
			.resumeToken(state.getResumeToken())
			.paused(state.getPaused())
			.answer(state.getAnswer())
			.intent(state.getIntent())
			.businessRuleCandidate(state.getBusinessRuleCandidate())
			.unsafeOperation(state.getUnsafeOperation())
			.skipSql(state.getSkipSql())
			.datasourceId(state.getSelectedDatasourceId())
			.schemaRecallResult(state.getSchemaRecallResult())
			.schemaRecallFallbackUsed(state.getSchemaRecallFallbackUsed())
			.schemaRecallMessage(state.getSchemaRecallMessage())
			.recalledTableCount(state.getRecalledTableCount())
			.recalledFieldCount(state.getRecalledFieldCount())
			.schemaContext(state.getSchemaContext())
			.relationRecallResult(state.getRelationRecallResult())
			.relationContext(state.getRelationContext())
			.recalledRelationCount(state.getRecalledRelationCount())
			.relationRecallMessage(state.getRelationRecallMessage())
			.relationRecallFallbackUsed(state.getRelationRecallFallbackUsed())
			.knowledgeRecallResult(state.getKnowledgeRecallResult())
			.knowledgeContext(state.getKnowledgeContext())
			.knowledgeRecallFallbackUsed(state.getKnowledgeRecallFallbackUsed())
			.knowledgeRecallMessage(state.getKnowledgeRecallMessage())
			.recalledKnowledgeCount(state.getRecalledKnowledgeCount())
			.rawLlmSqlOutput(state.getRawLlmSqlOutput())
			.extractedSql(state.getExtractedSql())
			.generatedSql(state.getGeneratedSql())
			.repairedSql(state.getRepairedSql())
			.validatedSql(state.getValidatedSql())
			.sanitizedSql(state.getSanitizedSql())
			.sqlLimited(state.getSqlLimited())
			.sqlLimit(state.getSqlLimit())
			.sqlResultTruncated(state.getSqlResultTruncated())
			.sqlQueryTimeoutSeconds(state.getSqlQueryTimeoutSeconds())
			.sqlSecurityMessage(state.getSqlSecurityMessage())
			.sqlRepairAttempted(state.getSqlRepairAttempted())
			.sqlRepairSuccess(state.getSqlRepairSuccess())
			.sqlRepairMessage(state.getSqlRepairMessage())
			.sqlValidationError(state.getSqlValidationError())
			.sqlExecutionError(state.getSqlExecutionError())
			.sqlResult(state.getSqlResult())
			.rowCount(state.getRowCount())
			.analysisSummary(state.getAnalysisSummary())
			.analysisResult(state.getAnalysisResult())
			.pythonEngine(state.getPythonEngine())
			.pythonExecuted(state.getPythonExecuted())
			.pythonSuccess(state.getPythonSuccess())
			.pythonCode(state.getPythonCode())
			.pythonStdout(state.getPythonStdout())
			.pythonStderr(state.getPythonStderr())
			.pythonExitCode(state.getPythonExitCode())
			.pythonDurationMs(state.getPythonDurationMs())
			.pythonErrorMessage(state.getPythonErrorMessage())
			.pythonFallbackUsed(state.getPythonFallbackUsed())
			.reportMarkdown(state.getReportMarkdown())
			.reportTitle(state.getReportTitle())
			.reportSummary(resolveReportSummary(state))
			.summary(resolveReportSummary(state))
			.reportResult(state.getReportResult())
			.chartSpec(state.getChartSpec())
			.success(Boolean.TRUE.equals(state.getSuccess()))
			.message(Boolean.TRUE.equals(state.getSuccess()) ? "success" : state.getErrorMessage())
			.events(state.getEvents())
			.build();
	}

	private String resolveReportSummary(GraphState state) {
		if (state.getReportSummary() != null && !state.getReportSummary().isBlank()) {
			return state.getReportSummary();
		}
		if (state.getReportResult() != null && state.getReportResult().getSummary() != null
				&& !state.getReportResult().getSummary().isBlank()) {
			return state.getReportResult().getSummary();
		}
		return null;
	}

	private boolean shouldSkipNode(GraphState state, GraphNode node) {
		if (!Boolean.TRUE.equals(state.getSkipSql())) {
			return false;
		}
		String name = node.name();
		if (Boolean.TRUE.equals(state.getUnsafeOperation())) {
			return !"finish".equals(name);
		}
		return !("nl2sql_answer".equals(name) || "finish".equals(name));
	}

}
