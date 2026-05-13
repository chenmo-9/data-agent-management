package com.alibaba.cloud.ai.dataagent.service.graphhuman;

import com.alibaba.cloud.ai.dataagent.dto.graph.GraphEventDTO;
import com.alibaba.cloud.ai.dataagent.dto.graphhuman.HumanCancelRequest;
import com.alibaba.cloud.ai.dataagent.dto.graphhuman.HumanConfirmRequest;
import com.alibaba.cloud.ai.dataagent.entity.GraphRunHistory;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.graph.nodes.FinishNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.Nl2SqlAnswerNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.PythonAnalyzeNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.ReportGenerateNode;
import com.alibaba.cloud.ai.dataagent.graph.nodes.SqlExecuteNode;
import com.alibaba.cloud.ai.dataagent.mapper.GraphRunHistoryMapper;
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlValidationResult;
import com.alibaba.cloud.ai.dataagent.nl2sql.SqlValidator;
import com.alibaba.cloud.ai.dataagent.security.SensitiveLogUtils;
import com.alibaba.cloud.ai.dataagent.service.graphhistory.GraphHistoryService;
import com.alibaba.cloud.ai.dataagent.vo.graph.GraphRunVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhuman.HumanConfirmVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhuman.HumanPendingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphHumanServiceImpl implements GraphHumanService {

	private final GraphRunHistoryMapper graphRunHistoryMapper;
	private final GraphHistoryService graphHistoryService;
	private final SqlValidator sqlValidator;
	private final SqlExecuteNode sqlExecuteNode;
	private final PythonAnalyzeNode pythonAnalyzeNode;
	private final ReportGenerateNode reportGenerateNode;
	private final Nl2SqlAnswerNode nl2SqlAnswerNode;
	private final FinishNode finishNode;

	@Override
	public HumanPendingVO getPending(String runId) {
		GraphRunHistory history = requirePending(runId);
		HumanPendingVO vo = new HumanPendingVO();
		vo.setRunId(history.getRunId());
		vo.setSessionId(history.getSessionId());
		vo.setQuestion(history.getQuestion());
		vo.setAgentId(history.getAgentId());
		vo.setAgentName(history.getAgentName());
		vo.setMode(history.getMode());
		vo.setConfirmSql(history.getConfirmSql());
		vo.setGeneratedSql(history.getGeneratedSql());
		vo.setValidatedSql(history.getValidatedSql());
		vo.setSanitizedSql(history.getSanitizedSql());
		vo.setSqlSecurityMessage(history.getSqlSecurityMessage());
		vo.setConfirmStatus(history.getConfirmStatus());
		vo.setCreatedAt(history.getCreatedAt());
		vo.setEvents(graphHistoryService.events(runId));
		return vo;
	}

	@Override
	public HumanConfirmVO confirm(String runId, HumanConfirmRequest request) {
		GraphRunHistory history = requirePending(runId);
		String requestSql = request == null ? null : request.getSql();
		String finalSql = requestSql == null || requestSql.isBlank() ? history.getConfirmSql() : requestSql;
		if (finalSql == null || finalSql.isBlank()) {
			throw new BusinessException("confirm sql cannot be empty");
		}
		SqlValidationResult validation = sqlValidator.validateDetailed(finalSql);
		if (!Boolean.TRUE.equals(validation.getValid())) {
			appendManualEvent(history, "human_confirm", "human_confirm_failed", "failed", validation.getMessage(),
					eventData("blockedReason", validation.getBlockedReason()));
			throw new BusinessException(validation.getMessage());
		}
		LocalDateTime now = LocalDateTime.now();
		GraphRunHistory update = new GraphRunHistory();
		update.setRunId(runId);
		update.setStatus("running");
		update.setConfirmStatus(finalSql.equals(history.getConfirmSql()) ? "confirmed" : "modified");
		update.setConfirmedSql(SensitiveLogUtils.maskSecretFields(validation.getSanitizedSql()));
		update.setConfirmedBy(request.getConfirmedBy() == null || request.getConfirmedBy().isBlank() ? "local_user"
				: SensitiveLogUtils.maskSecretFields(request.getConfirmedBy()));
		update.setConfirmedAt(now);
		update.setUpdatedAt(now);
		graphRunHistoryMapper.updateConfirm(update);
		appendManualEvent(history, "human_confirm", "human_confirmed", "success", "Human confirmed SQL execution",
				eventData("runId", runId, "confirmedSql", validation.getSanitizedSql(), "confirmedBy", update.getConfirmedBy(),
						"comment", request == null ? null : request.getComment()));

		GraphState state = restoreState(history);
		state.setValidatedSql(validation.getSanitizedSql());
		state.setSanitizedSql(validation.getSanitizedSql());
		state.setSqlSecurityMessage(buildSqlSecurityMessage(validation));
		state.setConfirmedSql(validation.getSanitizedSql());
		state.setConfirmedBy(update.getConfirmedBy());
		state.setConfirmedAt(now);
		state.setConfirmStatus(update.getConfirmStatus());
		GraphEventEmitter emitter = new GraphEventEmitter(state, null,
				event -> graphHistoryService.saveEvent(state.getRunId(), event));
		try {
			sqlExecuteNode.execute(state, emitter);
			pythonAnalyzeNode.execute(state, emitter);
			reportGenerateNode.execute(state, emitter);
			nl2SqlAnswerNode.execute(state, emitter);
			finishNode.execute(state, emitter);
		}
		catch (Exception ex) {
			state.setSuccess(false);
			state.setErrorMessage(SensitiveLogUtils.maskSecretFields(ex.getMessage()));
			emitter.emitError(state.getCurrentNode() == null ? "graph_human" : state.getCurrentNode(), state.getErrorMessage(),
					eventData("exception", ex.getClass().getSimpleName()));
		}
		GraphRunVO vo = toVO(state);
		graphHistoryService.finishRun(state, vo);
		HumanConfirmVO result = new HumanConfirmVO();
		result.setRunId(runId);
		result.setStatus(Boolean.TRUE.equals(vo.getSuccess()) ? "success" : "failed");
		result.setConfirmStatus(update.getConfirmStatus());
		result.setConfirmedSql(validation.getSanitizedSql());
		result.setAnswer(vo.getAnswer());
		result.setGraphRunVO(vo);
		result.setMessage(Boolean.TRUE.equals(vo.getSuccess()) ? "SQL executed after confirmation" : vo.getMessage());
		return result;
	}

	@Override
	public HumanConfirmVO cancel(String runId, HumanCancelRequest request) {
		GraphRunHistory history = requirePending(runId);
		GraphRunHistory update = new GraphRunHistory();
		update.setRunId(runId);
		update.setCanceledAt(LocalDateTime.now());
		update.setCancelReason(SensitiveLogUtils.maskSecretFields(request == null ? null : request.getReason()));
		update.setErrorMessage(update.getCancelReason());
		update.setUpdatedAt(LocalDateTime.now());
		graphRunHistoryMapper.updateCancel(update);
		appendManualEvent(history, "human_confirm", "human_cancel", "canceled", "Human canceled SQL execution",
				eventData("reason", request == null ? null : request.getReason()));
		HumanConfirmVO vo = new HumanConfirmVO();
		vo.setRunId(runId);
		vo.setStatus("canceled");
		vo.setConfirmStatus("canceled");
		vo.setMessage("SQL execution canceled");
		return vo;
	}

	private GraphRunHistory requirePending(String runId) {
		GraphRunHistory history = graphRunHistoryMapper.selectByRunId(runId);
		if (history == null) {
			throw new BusinessException("graph run not found: " + runId);
		}
		if (!"pending_confirm".equals(history.getStatus())) {
			throw new BusinessException("graph run status is not pending_confirm: " + runId);
		}
		if (!"pending".equals(history.getConfirmStatus())) {
			throw new BusinessException("graph run is not pending confirmation: " + runId);
		}
		if (history.getConfirmSql() == null || history.getConfirmSql().isBlank()) {
			throw new BusinessException("graph run has no confirm sql: " + runId);
		}
		return history;
	}

	private GraphState restoreState(GraphRunHistory history) {
		return GraphState.builder()
			.runId(history.getRunId())
			.sessionId(history.getSessionId())
			.agentId(history.getAgentId())
			.agentName(history.getAgentName())
			.modelConfigId(history.getModelConfigId())
			.question(history.getQuestion())
			.mode(history.getMode())
			.intent("data_query")
			.selectedDatasourceId(history.getDatasourceId())
			.datasourceName(history.getDatasourceName())
			.generatedSql(history.getGeneratedSql())
			.extractedSql(history.getExtractedSql())
			.repairedSql(history.getRepairedSql())
			.validatedSql(history.getValidatedSql())
			.sanitizedSql(history.getSanitizedSql())
			.sqlSecurityMessage(history.getSqlSecurityMessage())
			.humanConfirmRequired(true)
			.resumeRun(true)
			.success(false)
			.build();
	}

	private Map<String, Object> eventData(Object... keyValues) {
		Map<String, Object> data = new LinkedHashMap<>();
		for (int i = 0; i + 1 < keyValues.length; i += 2) {
			String key = String.valueOf(keyValues[i]);
			Object value = keyValues[i + 1];
			data.put(key, value == null ? "" : SensitiveLogUtils.maskSecretFields(String.valueOf(value)));
		}
		return data;
	}

	private String buildSqlSecurityMessage(SqlValidationResult validation) {
		if (validation == null) {
			return null;
		}
		if (Boolean.TRUE.equals(validation.getLimitApplied())) {
			return "LIMIT " + validation.getLimit() + " applied during human confirmation";
		}
		return "SQL confirmed and validated before execution";
	}

	private void appendManualEvent(GraphRunHistory history, String nodeName, String eventType, String status, String message,
			Map<String, Object> data) {
		GraphEventDTO event = GraphEventDTO.builder()
			.eventId(java.util.UUID.randomUUID().toString())
			.sessionId(history.getSessionId())
			.nodeName(nodeName)
			.eventType(eventType)
			.status(status)
			.message(SensitiveLogUtils.maskSecretFields(message))
			.data(data)
			.timestamp(LocalDateTime.now())
			.build();
		graphHistoryService.saveEvent(history.getRunId(), event);
	}

	private GraphRunVO toVO(GraphState state) {
		return GraphRunVO.builder()
			.runId(state.getRunId())
			.sessionId(state.getSessionId())
			.confirmRequired(state.getHumanConfirmRequired())
			.confirmStatus(state.getConfirmStatus())
			.confirmedSql(state.getConfirmedSql())
			.answer(state.getAnswer())
			.intent(state.getIntent())
			.datasourceId(state.getSelectedDatasourceId())
			.generatedSql(state.getGeneratedSql())
			.validatedSql(state.getValidatedSql())
			.sanitizedSql(state.getSanitizedSql())
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

}
