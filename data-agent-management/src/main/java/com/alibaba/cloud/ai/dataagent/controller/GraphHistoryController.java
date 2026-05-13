package com.alibaba.cloud.ai.dataagent.controller;

import com.alibaba.cloud.ai.dataagent.dto.graphhistory.GraphRunHistoryQueryRequest;
import com.alibaba.cloud.ai.dataagent.service.graphhistory.GraphHistoryService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphEventHistoryVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphRunHistoryDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/graph-history")
public class GraphHistoryController {

	private final GraphHistoryService graphHistoryService;

	@GetMapping("/list")
	public ApiResponse<Map<String, Object>> list(GraphRunHistoryQueryRequest request) {
		return ApiResponse.success("Graph history list", graphHistoryService.list(request));
	}

	@GetMapping("/{runId}")
	public ApiResponse<GraphRunHistoryDetailVO> detail(@PathVariable("runId") String runId) {
		return ApiResponse.success("Graph history detail", graphHistoryService.detail(runId));
	}

	@GetMapping("/{runId}/events")
	public ApiResponse<List<GraphEventHistoryVO>> events(@PathVariable("runId") String runId) {
		return ApiResponse.success("Graph history events", graphHistoryService.events(runId));
	}

	@DeleteMapping("/{runId}")
	public ApiResponse<Void> delete(@PathVariable("runId") String runId) {
		graphHistoryService.delete(runId);
		return ApiResponse.success(null);
	}

}
