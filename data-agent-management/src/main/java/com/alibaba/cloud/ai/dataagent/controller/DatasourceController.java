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

import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceTestRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceUpdateRequest;
import com.alibaba.cloud.ai.dataagent.service.datasource.DatasourceService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.datasource.DatasourceTestVO;
import com.alibaba.cloud.ai.dataagent.vo.datasource.DatasourceVO;
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

/** Datasource Management Controller */
@Slf4j
@RestController
@RequestMapping("/api/datasource")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class DatasourceController {

	private final DatasourceService datasourceService;

	@PostMapping("/create")
	public ApiResponse<DatasourceVO> create(@Valid @RequestBody DatasourceCreateRequest request) {
		return ApiResponse.success("创建数据源成功", datasourceService.create(request));
	}

	@GetMapping("/list")
	public ApiResponse<List<DatasourceVO>> list(DatasourceQueryRequest request) {
		return ApiResponse.success("获取数据源列表成功", datasourceService.list(request));
	}

	@GetMapping("/{id}")
	public ApiResponse<DatasourceVO> get(@PathVariable("id") Long id) {
		return ApiResponse.success("获取数据源详情成功", datasourceService.getDetail(id));
	}

	@PutMapping("/{id}")
	public ApiResponse<DatasourceVO> update(@PathVariable("id") Long id,
			@Valid @RequestBody DatasourceUpdateRequest request) {
		return ApiResponse.success("更新数据源成功", datasourceService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable("id") Long id) {
		datasourceService.delete(id);
		return ApiResponse.success("删除数据源成功");
	}

	@PutMapping("/{id}/enable")
	public ApiResponse<DatasourceVO> enable(@PathVariable("id") Long id) {
		return ApiResponse.success("启用数据源成功", datasourceService.enable(id));
	}

	@PutMapping("/{id}/disable")
	public ApiResponse<DatasourceVO> disable(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用数据源成功", datasourceService.disable(id));
	}

	@PostMapping("/test")
	public ApiResponse<DatasourceTestVO> testConnection(@Valid @RequestBody DatasourceTestRequest request) {
		return ApiResponse.success("数据源连接测试完成", datasourceService.testConnection(request));
	}

	@PostMapping("/{id}/test")
	public ApiResponse<DatasourceTestVO> testConnection(@PathVariable("id") Long id) {
		return ApiResponse.success("数据源连接测试完成", datasourceService.testConnection(id));
	}

}
