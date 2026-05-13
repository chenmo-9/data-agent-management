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

import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptRenderRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateUpdateRequest;
import com.alibaba.cloud.ai.dataagent.service.prompt.PromptTemplateService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.prompt.PromptRenderVO;
import com.alibaba.cloud.ai.dataagent.vo.prompt.PromptTemplateVO;
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

/** Prompt Template Management Controller */
@Slf4j
@RestController
@RequestMapping("/api/prompt-template")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class PromptTemplateController {

	private final PromptTemplateService promptTemplateService;

	@PostMapping("/create")
	public ApiResponse<PromptTemplateVO> create(@Valid @RequestBody PromptTemplateCreateRequest request) {
		return ApiResponse.success("创建 Prompt 模板成功", promptTemplateService.create(request));
	}

	@GetMapping("/list")
	public ApiResponse<List<PromptTemplateVO>> list(PromptTemplateQueryRequest request) {
		return ApiResponse.success("获取 Prompt 模板列表成功", promptTemplateService.list(request));
	}

	@GetMapping("/{id}")
	public ApiResponse<PromptTemplateVO> get(@PathVariable("id") Long id) {
		return ApiResponse.success("获取 Prompt 模板详情成功", promptTemplateService.getDetail(id));
	}

	@PutMapping("/{id}")
	public ApiResponse<PromptTemplateVO> update(@PathVariable("id") Long id,
			@Valid @RequestBody PromptTemplateUpdateRequest request) {
		return ApiResponse.success("更新 Prompt 模板成功", promptTemplateService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable("id") Long id) {
		promptTemplateService.delete(id);
		return ApiResponse.success("删除 Prompt 模板成功");
	}

	@PutMapping("/{id}/enable")
	public ApiResponse<PromptTemplateVO> enable(@PathVariable("id") Long id) {
		return ApiResponse.success("启用 Prompt 模板成功", promptTemplateService.enable(id));
	}

	@PutMapping("/{id}/disable")
	public ApiResponse<PromptTemplateVO> disable(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用 Prompt 模板成功", promptTemplateService.disable(id));
	}

	@PostMapping("/render")
	public ApiResponse<PromptRenderVO> render(@RequestBody PromptRenderRequest request) {
		return ApiResponse.success("渲染 Prompt 模板成功", promptTemplateService.render(request));
	}

	@PostMapping("/init-defaults")
	public ApiResponse<List<PromptTemplateVO>> initDefaultPrompts() {
		return ApiResponse.success("初始化默认 Prompt 模板成功", promptTemplateService.initDefaultPrompts());
	}

}
