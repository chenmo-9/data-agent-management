package com.alibaba.cloud.ai.dataagent.service.graphhistory;

import com.alibaba.cloud.ai.dataagent.dto.graph.GraphEventDTO;
import com.alibaba.cloud.ai.dataagent.dto.graph.GraphRunRequest;
import com.alibaba.cloud.ai.dataagent.dto.graphhistory.GraphRunHistoryQueryRequest;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.vo.graph.GraphRunVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphEventHistoryVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphRunHistoryDetailVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphRunHistoryVO;

import java.util.List;
import java.util.Map;

public interface GraphHistoryService {

	void startRun(GraphState state, GraphRunRequest request);

	void saveEvent(String runId, GraphEventDTO event);

	void finishRun(GraphState state, GraphRunVO vo);

	void failRun(GraphState state, Throwable ex);

	void markPendingConfirm(GraphState state);

	Map<String, Object> list(GraphRunHistoryQueryRequest request);

	GraphRunHistoryDetailVO detail(String runId);

	List<GraphEventHistoryVO> events(String runId);

	void delete(String runId);

}
