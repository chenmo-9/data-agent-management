package com.alibaba.cloud.ai.dataagent.controller;

import com.alibaba.cloud.ai.dataagent.dto.graphhuman.HumanCancelRequest;
import com.alibaba.cloud.ai.dataagent.dto.graphhuman.HumanConfirmRequest;
import com.alibaba.cloud.ai.dataagent.service.graphhuman.GraphHumanService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.graphhuman.HumanConfirmVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhuman.HumanPendingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/graph-human")
public class GraphHumanController {

	private final GraphHumanService graphHumanService;

	@GetMapping("/{runId}")
	public ApiResponse<HumanPendingVO> getPending(@PathVariable("runId") String runId) {
		return ApiResponse.success("Human confirmation pending run", graphHumanService.getPending(runId));
	}

	@PostMapping("/{runId}/confirm")
	public ApiResponse<HumanConfirmVO> confirm(@PathVariable("runId") String runId, @RequestBody HumanConfirmRequest request) {
		return ApiResponse.success("Human confirmation completed", graphHumanService.confirm(runId, request));
	}

	@PostMapping("/{runId}/cancel")
	public ApiResponse<HumanConfirmVO> cancel(@PathVariable("runId") String runId, @RequestBody HumanCancelRequest request) {
		return ApiResponse.success("Human confirmation canceled", graphHumanService.cancel(runId, request));
	}

}
