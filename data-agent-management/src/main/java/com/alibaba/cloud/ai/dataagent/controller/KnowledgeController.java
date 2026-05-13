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

import com.alibaba.cloud.ai.dataagent.dto.knowledge.AgentKnowledgeBindRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.AgentKnowledgeQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeUpdateRequest;
import com.alibaba.cloud.ai.dataagent.service.knowledge.KnowledgeEmbeddingService;
import com.alibaba.cloud.ai.dataagent.service.knowledge.KnowledgeService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.AgentKnowledgeVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.BusinessKnowledgeVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.KnowledgeEmbeddingVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.KnowledgeChunkVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Knowledge Management Controller */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class KnowledgeController {

	private final KnowledgeService knowledgeService;

	private final KnowledgeEmbeddingService knowledgeEmbeddingService;

	@PostMapping("/business/create")
	public ApiResponse<BusinessKnowledgeVO> createBusinessKnowledge(
			@Valid @RequestBody BusinessKnowledgeCreateRequest request) {
		return ApiResponse.success("创建业务知识成功", knowledgeService.createBusinessKnowledge(request));
	}

	@GetMapping("/business/list")
	public ApiResponse<List<BusinessKnowledgeVO>> listBusinessKnowledge(BusinessKnowledgeQueryRequest request) {
		return ApiResponse.success("获取业务知识列表成功", knowledgeService.listBusinessKnowledge(request));
	}

	@GetMapping("/business/{id}")
	public ApiResponse<BusinessKnowledgeVO> getBusinessKnowledge(@PathVariable("id") Long id) {
		return ApiResponse.success("获取业务知识详情成功", knowledgeService.getBusinessKnowledgeDetail(id));
	}

	@PutMapping("/business/{id}")
	public ApiResponse<BusinessKnowledgeVO> updateBusinessKnowledge(@PathVariable("id") Long id,
			@Valid @RequestBody BusinessKnowledgeUpdateRequest request) {
		return ApiResponse.success("更新业务知识成功", knowledgeService.updateBusinessKnowledge(id, request));
	}

	@DeleteMapping("/business/{id}")
	public ApiResponse<Void> deleteBusinessKnowledge(@PathVariable("id") Long id) {
		knowledgeService.deleteBusinessKnowledge(id);
		return ApiResponse.success("删除业务知识成功");
	}

	@PutMapping("/business/{id}/enable")
	public ApiResponse<BusinessKnowledgeVO> enableBusinessKnowledge(@PathVariable("id") Long id) {
		return ApiResponse.success("启用业务知识成功", knowledgeService.enableBusinessKnowledge(id));
	}

	@PutMapping("/business/{id}/disable")
	public ApiResponse<BusinessKnowledgeVO> disableBusinessKnowledge(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用业务知识成功", knowledgeService.disableBusinessKnowledge(id));
	}

	@PostMapping("/business/upload")
	public ApiResponse<BusinessKnowledgeVO> uploadBusinessKnowledgeFile(@RequestPart("file") MultipartFile file,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "knowledgeType", required = false) String knowledgeType) {
		return ApiResponse.success("上传业务知识文件成功",
				knowledgeService.uploadBusinessKnowledgeFile(file, title, knowledgeType));
	}

	@PostMapping("/agent/bind")
	public ApiResponse<AgentKnowledgeVO> bindAgentKnowledge(@Valid @RequestBody AgentKnowledgeBindRequest request) {
		return ApiResponse.success("绑定智能体知识成功", knowledgeService.bindAgentKnowledge(request));
	}

	@GetMapping("/agent/list")
	public ApiResponse<List<AgentKnowledgeVO>> listAgentKnowledge(AgentKnowledgeQueryRequest request) {
		return ApiResponse.success("获取智能体知识绑定列表成功", knowledgeService.listAgentKnowledge(request));
	}

	@GetMapping("/agent/{agentId}")
	public ApiResponse<List<AgentKnowledgeVO>> listKnowledgeByAgentId(@PathVariable("agentId") Long agentId) {
		return ApiResponse.success("获取智能体知识列表成功", knowledgeService.listKnowledgeByAgentId(agentId));
	}

	@DeleteMapping("/agent/{id}")
	public ApiResponse<Void> unbindAgentKnowledgeById(@PathVariable("id") Long id) {
		knowledgeService.unbindAgentKnowledgeById(id);
		return ApiResponse.success("解绑智能体知识成功");
	}

	@DeleteMapping("/agent/unbind")
	public ApiResponse<Void> unbindAgentKnowledge(@RequestParam("agentId") Long agentId,
			@RequestParam("knowledgeId") Long knowledgeId) {
		knowledgeService.unbindAgentKnowledge(agentId, knowledgeId);
		return ApiResponse.success("解绑智能体知识成功");
	}

	@PutMapping("/agent/{id}/enable")
	public ApiResponse<AgentKnowledgeVO> enableAgentKnowledge(@PathVariable("id") Long id) {
		return ApiResponse.success("启用智能体知识绑定成功", knowledgeService.enableAgentKnowledge(id));
	}

	@PutMapping("/agent/{id}/disable")
	public ApiResponse<AgentKnowledgeVO> disableAgentKnowledge(@PathVariable("id") Long id) {
		return ApiResponse.success("禁用智能体知识绑定成功", knowledgeService.disableAgentKnowledge(id));
	}

	@PostMapping("/business/{id}/chunks/rebuild")
	public ApiResponse<List<KnowledgeChunkVO>> rebuildChunks(@PathVariable("id") Long id) {
		return ApiResponse.success("重建知识切片成功", knowledgeService.rebuildChunks(id));
	}

	@GetMapping("/business/{id}/chunks")
	public ApiResponse<List<KnowledgeChunkVO>> listChunksByKnowledgeId(@PathVariable("id") Long id) {
		return ApiResponse.success("获取知识切片成功", knowledgeService.listChunksByKnowledgeId(id));
	}

	@DeleteMapping("/business/{id}/chunks")
	public ApiResponse<Void> deleteChunksByKnowledgeId(@PathVariable("id") Long id) {
		knowledgeService.deleteChunksByKnowledgeId(id);
		return ApiResponse.success("删除知识切片成功");
	}

	@PostMapping("/business/{knowledgeId}/embedding/rebuild")
	public ApiResponse<KnowledgeEmbeddingVO> rebuildKnowledgeEmbedding(@PathVariable("knowledgeId") Long knowledgeId,
			@RequestParam("modelConfigId") Long modelConfigId) {
		return ApiResponse.success("重建知识向量成功",
				knowledgeEmbeddingService.embedKnowledge(knowledgeId, modelConfigId));
	}

	@PostMapping("/agent/{agentId}/embedding/rebuild")
	public ApiResponse<KnowledgeEmbeddingVO> rebuildAgentKnowledgeEmbedding(@PathVariable("agentId") Long agentId,
			@RequestParam("modelConfigId") Long modelConfigId) {
		return ApiResponse.success("重建智能体知识向量成功",
				knowledgeEmbeddingService.embedAgentKnowledge(agentId, modelConfigId));
	}

	@DeleteMapping("/business/{knowledgeId}/embedding")
	public ApiResponse<Void> clearKnowledgeEmbedding(@PathVariable("knowledgeId") Long knowledgeId) {
		knowledgeEmbeddingService.clearKnowledgeEmbedding(knowledgeId);
		return ApiResponse.success("清除知识向量成功");
	}

}
