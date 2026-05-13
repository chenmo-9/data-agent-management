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
package com.alibaba.cloud.ai.dataagent.controller;

import com.alibaba.cloud.ai.dataagent.dto.graph.GraphRunRequest;
import com.alibaba.cloud.ai.dataagent.service.graph.GraphService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.graph.GraphRunVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/graph")
public class GraphController {

	private final GraphService graphService;

	@PostMapping("/run")
	public ApiResponse<GraphRunVO> run(@Valid @RequestBody GraphRunRequest request) {
		return ApiResponse.success("Graph run completed", graphService.run(request));
	}

	@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(@Valid @RequestBody GraphRunRequest request) {
		return graphService.stream(request);
	}

	@GetMapping(value = "/stream-get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<StreamingResponseBody> streamGet(@RequestParam("agentId") Long agentId,
			@RequestParam("modelConfigId") Long modelConfigId, @RequestParam("question") String question,
			@RequestParam(value = "sessionId", required = false) String sessionId,
			@RequestParam(value = "mode", required = false) String mode,
			@RequestParam(value = "embeddingModelConfigId", required = false) Long embeddingModelConfigId,
			@RequestParam(value = "knowledgeTopK", required = false) Integer knowledgeTopK,
			@RequestParam(value = "confirmBeforeExecute", required = false) Boolean confirmBeforeExecute) {
		GraphRunRequest request = GraphRunRequest.builder()
			.agentId(agentId)
			.modelConfigId(modelConfigId)
			.question(question)
			.sessionId(sessionId)
			.mode(mode)
			.embeddingModelConfigId(embeddingModelConfigId)
			.knowledgeTopK(knowledgeTopK)
			.confirmBeforeExecute(confirmBeforeExecute)
			.build();
		return ResponseEntity.ok()
			.header(HttpHeaders.CACHE_CONTROL, "no-cache")
			.header(HttpHeaders.CONNECTION, "keep-alive")
			.contentType(MediaType.TEXT_EVENT_STREAM)
			.body(graphService.streamBody(request));
	}

}
