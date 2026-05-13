package com.alibaba.cloud.ai.dataagent.converter;

import com.alibaba.cloud.ai.dataagent.entity.GraphEventHistory;
import com.alibaba.cloud.ai.dataagent.entity.GraphRunHistory;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphEventHistoryVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphRunHistoryDetailVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphRunHistoryVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GraphHistoryConverter {

	public GraphRunHistoryVO runToVO(GraphRunHistory history) {
		if (history == null) {
			return null;
		}
		GraphRunHistoryVO vo = new GraphRunHistoryVO();
		vo.setId(history.getId());
		vo.setRunId(history.getRunId());
		vo.setSessionId(history.getSessionId());
		vo.setAgentId(history.getAgentId());
		vo.setAgentName(history.getAgentName());
		vo.setMode(history.getMode());
		vo.setQuestion(history.getQuestion());
		vo.setStatus(history.getStatus());
		vo.setSuccess(history.getSuccess());
		vo.setDurationMs(history.getDurationMs());
		vo.setRowCount(history.getRowCount());
		vo.setChartType(history.getChartType());
		vo.setConfirmRequired(history.getConfirmRequired());
		vo.setConfirmStatus(history.getConfirmStatus());
		vo.setFailedNode(history.getFailedNode());
		vo.setErrorMessage(history.getErrorMessage());
		vo.setStartedAt(history.getStartedAt());
		vo.setFinishedAt(history.getFinishedAt());
		vo.setCreatedAt(history.getCreatedAt());
		return vo;
	}

	public GraphEventHistoryVO eventToVO(GraphEventHistory event) {
		if (event == null) {
			return null;
		}
		GraphEventHistoryVO vo = new GraphEventHistoryVO();
		vo.setId(event.getId());
		vo.setRunId(event.getRunId());
		vo.setEventId(event.getEventId());
		vo.setNodeName(event.getNodeName());
		vo.setEventType(event.getEventType());
		vo.setStatus(event.getStatus());
		vo.setMessage(event.getMessage());
		vo.setDataJson(event.getDataJson());
		vo.setErrorMessage(event.getErrorMessage());
		vo.setEventTime(event.getEventTime());
		vo.setCreatedAt(event.getCreatedAt());
		return vo;
	}

	public List<GraphRunHistoryVO> runListToVOList(List<GraphRunHistory> histories) {
		return histories.stream().map(this::runToVO).toList();
	}

	public List<GraphEventHistoryVO> eventListToVOList(List<GraphEventHistory> events) {
		return events.stream().map(this::eventToVO).toList();
	}

	public GraphRunHistoryDetailVO toDetail(GraphRunHistory history, List<GraphEventHistory> events) {
		GraphRunHistoryDetailVO detail = new GraphRunHistoryDetailVO();
		detail.setRun(runToVO(history));
		detail.setEvents(eventListToVOList(events));
		detail.setAnswer(history.getAnswer());
		detail.setGeneratedSql(history.getGeneratedSql());
		detail.setExtractedSql(history.getExtractedSql());
		detail.setRepairedSql(history.getRepairedSql());
		detail.setValidatedSql(history.getValidatedSql());
		detail.setSanitizedSql(history.getSanitizedSql());
		detail.setSqlLimited(history.getSqlLimited());
		detail.setSqlLimit(history.getSqlLimit());
		detail.setSqlResultTruncated(history.getSqlResultTruncated());
		detail.setSqlQueryTimeoutSeconds(history.getSqlQueryTimeoutSeconds());
		detail.setSqlSecurityMessage(history.getSqlSecurityMessage());
		detail.setSqlValidationError(history.getSqlValidationError());
		detail.setSqlExecutionError(history.getSqlExecutionError());
		detail.setPythonEngine(history.getPythonEngine());
		detail.setPythonExecuted(history.getPythonExecuted());
		detail.setPythonSuccess(history.getPythonSuccess());
		detail.setPythonDurationMs(history.getPythonDurationMs());
		detail.setPythonFallbackUsed(history.getPythonFallbackUsed());
		detail.setPythonErrorMessage(history.getPythonErrorMessage());
		detail.setConfirmRequired(history.getConfirmRequired());
		detail.setConfirmStatus(history.getConfirmStatus());
		detail.setConfirmSql(history.getConfirmSql());
		detail.setConfirmedSql(history.getConfirmedSql());
		detail.setConfirmedBy(history.getConfirmedBy());
		detail.setConfirmedAt(history.getConfirmedAt());
		detail.setCanceledAt(history.getCanceledAt());
		detail.setCancelReason(history.getCancelReason());
		detail.setRecalledTableCount(history.getRecalledTableCount());
		detail.setRecalledFieldCount(history.getRecalledFieldCount());
		detail.setRecalledRelationCount(history.getRecalledRelationCount());
		detail.setRecalledKnowledgeCount(history.getRecalledKnowledgeCount());
		detail.setResultPreviewJson(history.getResultPreviewJson());
		detail.setReportTitle(history.getReportTitle());
		detail.setReportMarkdown(history.getReportMarkdown());
		detail.setChartType(history.getChartType());
		detail.setReportSummary(history.getReportSummary());
		return detail;
	}

}
