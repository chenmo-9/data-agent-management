package com.alibaba.cloud.ai.dataagent.service.graphhistory;

import com.alibaba.cloud.ai.dataagent.converter.GraphHistoryConverter;
import com.alibaba.cloud.ai.dataagent.dto.graph.GraphEventDTO;
import com.alibaba.cloud.ai.dataagent.dto.graph.GraphRunRequest;
import com.alibaba.cloud.ai.dataagent.dto.graphhistory.GraphRunHistoryQueryRequest;
import com.alibaba.cloud.ai.dataagent.entity.GraphEventHistory;
import com.alibaba.cloud.ai.dataagent.entity.GraphRunHistory;
import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.mapper.GraphEventHistoryMapper;
import com.alibaba.cloud.ai.dataagent.mapper.GraphRunHistoryMapper;
import com.alibaba.cloud.ai.dataagent.mapper.ModelConfigMapper;
import com.alibaba.cloud.ai.dataagent.report.ChartSpec;
import com.alibaba.cloud.ai.dataagent.security.SensitiveLogUtils;
import com.alibaba.cloud.ai.dataagent.vo.graph.GraphRunVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphEventHistoryVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphRunHistoryDetailVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphRunHistoryVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphHistoryServiceImpl implements GraphHistoryService {

	private static final int PREVIEW_ROWS = 20;

	private static final int MAX_VALUE_LENGTH = 500;

	private final GraphRunHistoryMapper graphRunHistoryMapper;

	private final GraphEventHistoryMapper graphEventHistoryMapper;

	private final ModelConfigMapper modelConfigMapper;

	private final GraphHistoryConverter graphHistoryConverter;

	private final ObjectMapper objectMapper;

	@Override
	public void startRun(GraphState state, GraphRunRequest request) {
		String runId = state.getRunId() == null ? UUID.randomUUID().toString() : state.getRunId();
		LocalDateTime now = LocalDateTime.now();
		state.setRunId(runId);
		state.setStartedAt(now);
		state.setGraphStatus("running");
		try {
			GraphRunHistory history = new GraphRunHistory();
			history.setRunId(runId);
			history.setSessionId(state.getSessionId());
			history.setAgentId(request.getAgentId());
			history.setModelConfigId(request.getModelConfigId());
			history.setMode(state.getMode());
			history.setQuestion(sanitize(request.getQuestion()));
			history.setStatus("running");
			history.setSuccess(false);
			history.setStartedAt(now);
			history.setCreatedAt(now);
			history.setUpdatedAt(now);
			graphRunHistoryMapper.insert(history);
			state.setHistorySaved(true);
		}
		catch (Exception ex) {
			state.setHistorySaved(false);
			log.warn("Failed to start graph history run: {}", ex.getMessage());
		}
	}

	@Override
	public void saveEvent(String runId, GraphEventDTO event) {
		if (runId == null || event == null) {
			return;
		}
		try {
			GraphEventHistory history = new GraphEventHistory();
			history.setRunId(runId);
			history.setSessionId(event.getSessionId());
			history.setEventId(event.getEventId());
			history.setNodeName(event.getNodeName());
			history.setEventType(event.getEventType());
			history.setStatus(event.getStatus());
			history.setMessage(sanitize(event.getMessage()));
			history.setDataJson(toSanitizedJson(event.getData()));
			history.setErrorMessage("failed".equals(event.getStatus()) ? sanitize(event.getMessage()) : null);
			history.setEventTime(event.getTimestamp());
			history.setCreatedAt(LocalDateTime.now());
			graphEventHistoryMapper.insert(history);
		}
		catch (Exception ex) {
			log.warn("Failed to save graph event: {}", ex.getMessage());
		}
	}

	@Override
	public void finishRun(GraphState state, GraphRunVO vo) {
		if (state.getRunId() == null) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		state.setFinishedAt(now);
		if (state.getStartedAt() != null) {
			state.setDurationMs(Duration.between(state.getStartedAt(), now).toMillis());
		}
		state.setGraphStatus(Boolean.TRUE.equals(state.getSuccess()) ? "success" : "failed");
		try {
			GraphRunHistory history = buildFinalHistory(state, vo, now);
			graphRunHistoryMapper.updateByRunId(history);
			state.setHistorySaved(true);
		}
		catch (Exception ex) {
			state.setHistorySaved(false);
			log.warn("Failed to finish graph history run: {}", ex.getMessage());
		}
	}

	@Override
	public void failRun(GraphState state, Throwable ex) {
		if (state == null || state.getRunId() == null) {
			return;
		}
		state.setSuccess(false);
		state.setErrorMessage(ex == null ? state.getErrorMessage() : sanitize(ex.getMessage()));
		state.setGraphStatus("failed");
		finishRun(state, null);
	}

	@Override
	public void markPendingConfirm(GraphState state) {
		if (state.getRunId() == null) {
			return;
		}
		try {
			GraphRunHistory history = new GraphRunHistory();
			history.setRunId(state.getRunId());
			history.setStatus("pending_confirm");
			history.setSuccess(false);
			history.setConfirmRequired(true);
			history.setConfirmStatus("pending");
			history.setConfirmSql(sanitize(state.getConfirmSql()));
			history.setResumeToken(state.getResumeToken());
			history.setPendingPayloadJson(toSanitizedJson(buildPendingPayload(state)));
			history.setDatasourceId(state.getSelectedDatasourceId());
			history.setDatasourceName(state.getDatasourceName());
			history.setGeneratedSql(sanitize(state.getGeneratedSql()));
			history.setExtractedSql(sanitize(state.getExtractedSql()));
			history.setRepairedSql(sanitize(state.getRepairedSql()));
			history.setValidatedSql(sanitize(state.getValidatedSql()));
			history.setSanitizedSql(sanitize(state.getSanitizedSql()));
			history.setSqlSecurityMessage(sanitize(state.getSqlSecurityMessage()));
			history.setUpdatedAt(LocalDateTime.now());
			graphRunHistoryMapper.updatePendingConfirm(history);
			state.setHistorySaved(true);
		}
		catch (Exception ex) {
			state.setHistorySaved(false);
			log.warn("Failed to mark graph run pending confirm: {}", ex.getMessage());
		}
	}

	@Override
	public Map<String, Object> list(GraphRunHistoryQueryRequest request) {
		int total = graphRunHistoryMapper.countList(request.getAgentId(), request.getModelConfigId(), request.getMode(),
				request.getStatus(), request.getSuccess(), request.getKeyword(), request.getStartTime(), request.getEndTime());
		List<GraphRunHistory> runs = graphRunHistoryMapper.selectList(request.getAgentId(), request.getModelConfigId(),
				request.getMode(), request.getStatus(), request.getSuccess(), request.getKeyword(), request.getStartTime(),
				request.getEndTime(), request.limit(), request.offset());
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("total", total);
		result.put("page", request.getPage());
		result.put("pageSize", request.limit());
		result.put("records", graphHistoryConverter.runListToVOList(runs));
		return result;
	}

	@Override
	public GraphRunHistoryDetailVO detail(String runId) {
		GraphRunHistory run = graphRunHistoryMapper.selectByRunId(runId);
		if (run == null) {
			throw new BusinessException("graph run history not found: " + runId);
		}
		return graphHistoryConverter.toDetail(run, graphEventHistoryMapper.selectByRunId(runId));
	}

	@Override
	public List<GraphEventHistoryVO> events(String runId) {
		return graphHistoryConverter.eventListToVOList(graphEventHistoryMapper.selectByRunId(runId));
	}

	@Override
	public void delete(String runId) {
		graphEventHistoryMapper.deleteByRunId(runId);
		graphRunHistoryMapper.deleteByRunId(runId);
	}

	private GraphRunHistory buildFinalHistory(GraphState state, GraphRunVO vo, LocalDateTime now) {
		GraphRunHistory history = new GraphRunHistory();
		history.setRunId(state.getRunId());
		history.setSessionId(state.getSessionId());
		history.setAgentId(state.getAgentId());
		history.setAgentName(state.getAgentName());
		history.setModelConfigId(state.getModelConfigId());
		history.setModelName(resolveModelName(state.getModelConfigId()));
		history.setDatasourceId(state.getSelectedDatasourceId());
		history.setDatasourceName(state.getDatasourceName());
		history.setMode(state.getMode());
		history.setQuestion(sanitize(state.getQuestion()));
		history.setAnswer(sanitize(state.getAnswer()));
		history.setStatus(Boolean.TRUE.equals(state.getUnsafeOperation()) ? "blocked" : Boolean.TRUE.equals(state.getSuccess()) ? "success" : "failed");
		history.setSuccess(Boolean.TRUE.equals(state.getSuccess()));
		history.setStartedAt(state.getStartedAt());
		history.setFinishedAt(now);
		history.setDurationMs(state.getDurationMs());
		history.setEventCount(state.getEvents() == null ? 0 : state.getEvents().size());
		history.setFailedNode(Boolean.TRUE.equals(state.getSuccess()) ? null : inferFailedNode(state));
		history.setErrorMessage(sanitize(state.getErrorMessage()));
		history.setGeneratedSql(sanitize(state.getGeneratedSql()));
		history.setExtractedSql(sanitize(state.getExtractedSql()));
		history.setRepairedSql(sanitize(state.getRepairedSql()));
		history.setValidatedSql(sanitize(state.getValidatedSql()));
		history.setSanitizedSql(sanitize(state.getSanitizedSql()));
		history.setSqlLimited(state.getSqlLimited());
		history.setSqlLimit(state.getSqlLimit());
		history.setSqlResultTruncated(state.getSqlResultTruncated());
		history.setSqlQueryTimeoutSeconds(state.getSqlQueryTimeoutSeconds());
		history.setSqlSecurityMessage(sanitize(state.getSqlSecurityMessage()));
		history.setRowCount(state.getRowCount());
		history.setResultPreviewJson(buildResultPreview(state.getSqlResult()));
		history.setRecalledTableCount(state.getRecalledTableCount());
		history.setRecalledFieldCount(state.getRecalledFieldCount());
		history.setRecalledRelationCount(state.getRecalledRelationCount());
		history.setRecalledKnowledgeCount(state.getRecalledKnowledgeCount());
		history.setReportTitle(sanitize(state.getReportTitle()));
		history.setReportMarkdown(sanitize(state.getReportMarkdown()));
		history.setChartType(resolveChartType(state));
		history.setReportSummary(sanitize(resolveReportSummary(state)));
		history.setSqlValidationError(sanitize(state.getSqlValidationError()));
		history.setSqlExecutionError(sanitize(state.getSqlExecutionError()));
		history.setPythonEngine(state.getPythonEngine());
		history.setPythonExecuted(state.getPythonExecuted());
		history.setPythonSuccess(state.getPythonSuccess());
		history.setPythonDurationMs(state.getPythonDurationMs());
		history.setPythonFallbackUsed(state.getPythonFallbackUsed());
		history.setPythonErrorMessage(sanitize(state.getPythonErrorMessage()));
		history.setConfirmRequired(state.getHumanConfirmRequired());
		history.setConfirmStatus(state.getConfirmStatus());
		history.setConfirmSql(sanitize(state.getConfirmSql()));
		history.setConfirmedSql(sanitize(state.getConfirmedSql()));
		history.setConfirmedBy(sanitize(state.getConfirmedBy()));
		history.setConfirmedAt(state.getConfirmedAt());
		history.setResumeToken(state.getResumeToken());
		history.setPendingPayloadJson(sanitize(state.getPendingPayloadJson()));
		history.setUpdatedAt(now);
		return history;
	}

	private Map<String, Object> buildPendingPayload(GraphState state) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("runId", state.getRunId());
		payload.put("sessionId", state.getSessionId());
		payload.put("agentId", state.getAgentId());
		payload.put("agentName", state.getAgentName());
		payload.put("modelConfigId", state.getModelConfigId());
		payload.put("question", state.getQuestion());
		payload.put("mode", state.getMode());
		payload.put("selectedDatasourceId", state.getSelectedDatasourceId());
		payload.put("datasourceName", state.getDatasourceName());
		payload.put("validatedSql", state.getValidatedSql());
		payload.put("sanitizedSql", state.getSanitizedSql());
		payload.put("sqlSecurityMessage", state.getSqlSecurityMessage());
		return payload;
	}

	private String resolveModelName(Long modelConfigId) {
		if (modelConfigId == null) {
			return null;
		}
		ModelConfig config = modelConfigMapper.selectById(modelConfigId);
		return config == null ? null : config.getModelName();
	}

	private String inferFailedNode(GraphState state) {
		if (state.getEvents() == null || state.getEvents().isEmpty()) {
			return state.getCurrentNode();
		}
		return state.getEvents()
			.stream()
			.filter(event -> "failed".equals(event.getStatus()) || "error".equals(event.getEventType()))
			.reduce((first, second) -> second)
			.map(GraphEventDTO::getNodeName)
			.orElse(state.getCurrentNode());
	}

	private String resolveChartType(GraphState state) {
		ChartSpec chartSpec = state.getChartSpec();
		return chartSpec == null ? null : chartSpec.getChartType();
	}

	private String resolveReportSummary(GraphState state) {
		if (state.getReportSummary() != null && !state.getReportSummary().isBlank()) {
			return state.getReportSummary();
		}
		if (state.getReportResult() != null && state.getReportResult().getSummary() != null
				&& !state.getReportResult().getSummary().isBlank()) {
			return state.getReportResult().getSummary();
		}
		return state.getAnalysisSummary();
	}

	private String buildResultPreview(List<Map<String, Object>> rows) {
		if (rows == null || rows.isEmpty()) {
			return "[]";
		}
		List<Map<String, Object>> preview = rows.stream().limit(PREVIEW_ROWS).map(this::truncateRow).toList();
		return toSanitizedJson(preview);
	}

	private Map<String, Object> truncateRow(Map<String, Object> row) {
		Map<String, Object> result = new LinkedHashMap<>();
		row.forEach((key, value) -> {
			if (value instanceof String text && text.length() > MAX_VALUE_LENGTH) {
				result.put(key, text.substring(0, MAX_VALUE_LENGTH) + "...");
			}
			else {
				result.put(key, value);
			}
		});
		return result;
	}

	private String toSanitizedJson(Object value) {
		try {
			return sanitize(objectMapper.writeValueAsString(value));
		}
		catch (JsonProcessingException ex) {
			return "{}";
		}
	}

	private String sanitize(String text) {
		return SensitiveLogUtils.maskSecretFields(text);
	}

}
