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

import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigUpdateRequest;
import com.alibaba.cloud.ai.dataagent.service.aimodelconfig.ModelConfigService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.modelconfig.ModelConfigVO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/model-config")
public class ModelConfigController {

	private final ModelConfigService modelConfigService;

	@PostMapping("/create")
	public ApiResponse<ModelConfigVO> create(@Valid @RequestBody ModelConfigCreateRequest request) {
		return ApiResponse.success("创建模型配置成功", modelConfigService.create(request));
	}

	@GetMapping("/list")
	public ApiResponse<List<ModelConfigVO>> list(ModelConfigQueryRequest request) {
		return ApiResponse.success("获取模型配置列表成功", modelConfigService.list(request));
	}

	@GetMapping("/{id}")
	public ApiResponse<ModelConfigVO> get(@PathVariable("id") Long id) {
		return ApiResponse.success("获取模型配置详情成功", modelConfigService.getDetail(id));
	}

	@PutMapping("/{id}")
	public ApiResponse<ModelConfigVO> update(@PathVariable("id") Long id,
			@Valid @RequestBody ModelConfigUpdateRequest request) {
		return ApiResponse.success("更新模型配置成功", modelConfigService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable("id") Long id) {
		modelConfigService.delete(id);
		return ApiResponse.success("删除模型配置成功");
	}

	@PutMapping("/{id}/enable")
	public ApiResponse<ModelConfigVO> enable(@PathVariable("id") Long id) {
		return ApiResponse.success("启用模型配置成功", modelConfigService.enable(id));
	}

	@PutMapping("/{id}/disable")
	public ApiResponse<ModelConfigVO> disable(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用模型配置成功", modelConfigService.disable(id));
	}

}
