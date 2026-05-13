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

import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldUpdateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationUpdateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableUpdateRequest;
import com.alibaba.cloud.ai.dataagent.service.semantic.SemanticModelService;
import com.alibaba.cloud.ai.dataagent.service.semantic.SemanticRelationService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticFieldVO;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticModelVO;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticRelationVO;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticTableVO;
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

/** Semantic Model Management Controller */
@Slf4j
@RestController
@RequestMapping("/api/semantic-model")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SemanticModelController {

	private final SemanticModelService semanticModelService;

	private final SemanticRelationService semanticRelationService;

	@PostMapping("/table/create")
	public ApiResponse<SemanticTableVO> createTable(@Valid @RequestBody SemanticTableCreateRequest request) {
		return ApiResponse.success("创建表语义成功", semanticModelService.createTable(request));
	}

	@GetMapping("/table/list")
	public ApiResponse<List<SemanticTableVO>> listTables(SemanticTableQueryRequest request) {
		return ApiResponse.success("获取表语义列表成功", semanticModelService.listTables(request));
	}

	@GetMapping("/table/{id}")
	public ApiResponse<SemanticTableVO> getTable(@PathVariable("id") Long id) {
		return ApiResponse.success("获取表语义详情成功", semanticModelService.getTableDetail(id));
	}

	@PutMapping("/table/{id}")
	public ApiResponse<SemanticTableVO> updateTable(@PathVariable("id") Long id,
			@Valid @RequestBody SemanticTableUpdateRequest request) {
		return ApiResponse.success("更新表语义成功", semanticModelService.updateTable(id, request));
	}

	@DeleteMapping("/table/{id}")
	public ApiResponse<Void> deleteTable(@PathVariable("id") Long id) {
		semanticModelService.deleteTable(id);
		return ApiResponse.success("删除表语义成功");
	}

	@PutMapping("/table/{id}/enable")
	public ApiResponse<SemanticTableVO> enableTable(@PathVariable("id") Long id) {
		return ApiResponse.success("启用表语义成功", semanticModelService.enableTable(id));
	}

	@PutMapping("/table/{id}/disable")
	public ApiResponse<SemanticTableVO> disableTable(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用表语义成功", semanticModelService.disableTable(id));
	}

	@PostMapping("/field/create")
	public ApiResponse<SemanticFieldVO> createField(@Valid @RequestBody SemanticFieldCreateRequest request) {
		return ApiResponse.success("创建字段语义成功", semanticModelService.createField(request));
	}

	@GetMapping("/field/list")
	public ApiResponse<List<SemanticFieldVO>> listFields(SemanticFieldQueryRequest request) {
		return ApiResponse.success("获取字段语义列表成功", semanticModelService.listFields(request));
	}

	@GetMapping("/field/{id}")
	public ApiResponse<SemanticFieldVO> getField(@PathVariable("id") Long id) {
		return ApiResponse.success("获取字段语义详情成功", semanticModelService.getFieldDetail(id));
	}

	@PutMapping("/field/{id}")
	public ApiResponse<SemanticFieldVO> updateField(@PathVariable("id") Long id,
			@Valid @RequestBody SemanticFieldUpdateRequest request) {
		return ApiResponse.success("更新字段语义成功", semanticModelService.updateField(id, request));
	}

	@DeleteMapping("/field/{id}")
	public ApiResponse<Void> deleteField(@PathVariable("id") Long id) {
		semanticModelService.deleteField(id);
		return ApiResponse.success("删除字段语义成功");
	}

	@PutMapping("/field/{id}/enable")
	public ApiResponse<SemanticFieldVO> enableField(@PathVariable("id") Long id) {
		return ApiResponse.success("启用字段语义成功", semanticModelService.enableField(id));
	}

	@PutMapping("/field/{id}/disable")
	public ApiResponse<SemanticFieldVO> disableField(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用字段语义成功", semanticModelService.disableField(id));
	}

	@GetMapping("/table/{id}/model")
	public ApiResponse<SemanticModelVO> getSemanticModel(@PathVariable("id") Long id) {
		return ApiResponse.success("获取语义模型成功", semanticModelService.getSemanticModelByTableId(id));
	}

	@GetMapping("/datasource/{datasourceId}/models")
	public ApiResponse<List<SemanticModelVO>> listSemanticModelsByDatasourceId(
			@PathVariable("datasourceId") Long datasourceId) {
		return ApiResponse.success("获取数据源语义模型成功",
				semanticModelService.listSemanticModelsByDatasourceId(datasourceId));
	}

	@PostMapping("/relation/create")
	public ApiResponse<SemanticRelationVO> createRelation(@Valid @RequestBody SemanticRelationCreateRequest request) {
		return ApiResponse.success("创建表关系成功", semanticRelationService.createRelation(request));
	}

	@GetMapping("/relation/list")
	public ApiResponse<List<SemanticRelationVO>> listRelations(SemanticRelationQueryRequest request) {
		return ApiResponse.success("获取表关系列表成功", semanticRelationService.listRelations(request));
	}

	@GetMapping("/relation/{id}")
	public ApiResponse<SemanticRelationVO> getRelation(@PathVariable("id") Long id) {
		return ApiResponse.success("获取表关系详情成功", semanticRelationService.getRelationDetail(id));
	}

	@PutMapping("/relation/{id}")
	public ApiResponse<SemanticRelationVO> updateRelation(@PathVariable("id") Long id,
			@Valid @RequestBody SemanticRelationUpdateRequest request) {
		return ApiResponse.success("更新表关系成功", semanticRelationService.updateRelation(id, request));
	}

	@DeleteMapping("/relation/{id}")
	public ApiResponse<Void> deleteRelation(@PathVariable("id") Long id) {
		semanticRelationService.deleteRelation(id);
		return ApiResponse.success("删除表关系成功");
	}

	@PutMapping("/relation/{id}/enable")
	public ApiResponse<SemanticRelationVO> enableRelation(@PathVariable("id") Long id) {
		return ApiResponse.success("启用表关系成功", semanticRelationService.enableRelation(id));
	}

	@PutMapping("/relation/{id}/disable")
	public ApiResponse<SemanticRelationVO> disableRelation(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用表关系成功", semanticRelationService.disableRelation(id));
	}

	@GetMapping("/datasource/{datasourceId}/relations")
	public ApiResponse<List<SemanticRelationVO>> listRelationsByDatasourceId(
			@PathVariable("datasourceId") Long datasourceId) {
		return ApiResponse.success("获取数据源表关系成功",
				semanticRelationService.listRelationsByDatasourceId(datasourceId));
	}

	@GetMapping("/table/{tableId}/relations")
	public ApiResponse<List<SemanticRelationVO>> listRelationsByTableId(@PathVariable("tableId") Long tableId) {
		return ApiResponse.success("获取表关系成功", semanticRelationService.listRelationsByTableId(tableId));
	}

}
