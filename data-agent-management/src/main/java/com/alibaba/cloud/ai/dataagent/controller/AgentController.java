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

import com.alibaba.cloud.ai.dataagent.dto.agent.AgentCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.agent.AgentQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.agent.AgentUpdateRequest;
import com.alibaba.cloud.ai.dataagent.service.agent.AgentService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.agent.AgentVO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Agent Management Controller */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AgentController {

	private final AgentService agentService;

	/** Create agent */
	@PostMapping("/create")
	public ApiResponse<AgentVO> create(@Valid @RequestBody AgentCreateRequest request) {
		return ApiResponse.success("创建 Agent 成功", agentService.create(request));
	}

	/** Get agent list */
	@GetMapping("/list")
	public ApiResponse<List<AgentVO>> list(AgentQueryRequest request) {
		return ApiResponse.success("获取 Agent 列表成功", agentService.list(request));
	}

	/** Get agent details by ID */
	@GetMapping("/{id}")
	public ApiResponse<AgentVO> get(@PathVariable("id") Long id) {
		return ApiResponse.success("获取 Agent 详情成功", agentService.getDetail(id));
	}

	/** Update agent */
	@PutMapping("/{id}")
	public ApiResponse<AgentVO> update(@PathVariable("id") Long id, @Valid @RequestBody AgentUpdateRequest request) {
		return ApiResponse.success("更新 Agent 成功", agentService.update(id, request));
	}

	/** Delete agent */
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable("id") Long id) {
		agentService.delete(id);
		return ApiResponse.success("删除 Agent 成功");
	}

	/** Publish agent */
	@PutMapping("/{id}/publish")
	public ApiResponse<AgentVO> publish(@PathVariable("id") Long id) {
		return ApiResponse.success("发布 Agent 成功", agentService.publish(id));
	}

	/** Offline agent */
	@PutMapping("/{id}/offline")
	public ApiResponse<AgentVO> offline(@PathVariable("id") Long id) {
		return ApiResponse.success("下线 Agent 成功", agentService.offline(id));
	}

}
