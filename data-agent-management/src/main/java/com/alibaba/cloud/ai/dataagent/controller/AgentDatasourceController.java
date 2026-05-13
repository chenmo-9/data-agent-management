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

import com.alibaba.cloud.ai.dataagent.dto.agentdatasource.AgentDatasourceBindRequest;
import com.alibaba.cloud.ai.dataagent.dto.agentdatasource.AgentDatasourceQueryRequest;
import com.alibaba.cloud.ai.dataagent.service.agentdatasource.AgentDatasourceService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.agentdatasource.AgentDatasourceVO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Agent Datasource Management Controller */
@Slf4j
@RestController
@RequestMapping("/api/agent-datasource")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AgentDatasourceController {

	private final AgentDatasourceService agentDatasourceService;

	@PostMapping("/bind")
	public ApiResponse<AgentDatasourceVO> bind(@Valid @RequestBody AgentDatasourceBindRequest request) {
		return ApiResponse.success("绑定数据源成功", agentDatasourceService.bind(request));
	}

	@GetMapping("/list")
	public ApiResponse<List<AgentDatasourceVO>> list(AgentDatasourceQueryRequest request) {
		return ApiResponse.success("获取绑定关系列表成功", agentDatasourceService.list(request));
	}

	@GetMapping("/agent/{agentId}")
	public ApiResponse<List<AgentDatasourceVO>> listByAgentId(@PathVariable("agentId") Long agentId) {
		return ApiResponse.success("获取 Agent 绑定数据源成功", agentDatasourceService.listByAgentId(agentId));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> unbindById(@PathVariable("id") Long id) {
		agentDatasourceService.unbindById(id);
		return ApiResponse.success("解绑数据源成功");
	}

	@DeleteMapping("/unbind")
	public ApiResponse<Void> unbind(@RequestParam("agentId") Long agentId,
			@RequestParam("datasourceId") Long datasourceId) {
		agentDatasourceService.unbind(agentId, datasourceId);
		return ApiResponse.success("解绑数据源成功");
	}

	@PutMapping("/{id}/enable")
	public ApiResponse<AgentDatasourceVO> enable(@PathVariable("id") Long id) {
		return ApiResponse.success("启用绑定关系成功", agentDatasourceService.enable(id));
	}

	@PutMapping("/{id}/disable")
	public ApiResponse<AgentDatasourceVO> disable(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用绑定关系成功", agentDatasourceService.disable(id));
	}

}
