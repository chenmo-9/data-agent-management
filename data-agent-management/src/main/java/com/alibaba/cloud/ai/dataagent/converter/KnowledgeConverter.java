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
package com.alibaba.cloud.ai.dataagent.converter;

import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.BusinessKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.KnowledgeChunk;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.AgentKnowledgeVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.BusinessKnowledgeVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.KnowledgeChunkVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class KnowledgeConverter {

	public BusinessKnowledge businessCreateRequestToEntity(BusinessKnowledgeCreateRequest request) {
		if (request == null) {
			return null;
		}
		BusinessKnowledge knowledge = new BusinessKnowledge();
		knowledge.setTitle(request.getTitle());
		knowledge.setContent(request.getContent());
		knowledge.setKnowledgeType(request.getKnowledgeType());
		knowledge.setSourceType(request.getSourceType());
		knowledge.setEnabled(request.getEnabled());
		return knowledge;
	}

	public BusinessKnowledge businessUpdateRequestToEntity(BusinessKnowledgeUpdateRequest request) {
		if (request == null) {
			return null;
		}
		BusinessKnowledge knowledge = new BusinessKnowledge();
		knowledge.setTitle(request.getTitle());
		knowledge.setContent(request.getContent());
		knowledge.setKnowledgeType(request.getKnowledgeType());
		knowledge.setSourceType(request.getSourceType());
		knowledge.setEnabled(request.getEnabled());
		return knowledge;
	}

	public BusinessKnowledgeVO businessEntityToVO(BusinessKnowledge entity) {
		if (entity == null) {
			return null;
		}
		BusinessKnowledgeVO vo = new BusinessKnowledgeVO();
		vo.setId(entity.getId());
		vo.setTitle(entity.getTitle());
		vo.setContent(entity.getContent());
		vo.setKnowledgeType(entity.getKnowledgeType());
		vo.setSourceType(entity.getSourceType());
		vo.setFileName(entity.getFileName());
		vo.setFilePath(entity.getFilePath());
		vo.setFileSize(entity.getFileSize());
		vo.setEnabled(entity.getEnabled());
		vo.setCreatedAt(entity.getCreatedAt());
		vo.setUpdatedAt(entity.getUpdatedAt());
		return vo;
	}

	public List<BusinessKnowledgeVO> businessEntityListToVOList(List<BusinessKnowledge> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		return entities.stream().map(this::businessEntityToVO).toList();
	}

	public AgentKnowledgeVO agentKnowledgeEntityToVO(AgentKnowledge entity) {
		if (entity == null) {
			return null;
		}
		AgentKnowledgeVO vo = new AgentKnowledgeVO();
		vo.setId(entity.getId());
		vo.setAgentId(entity.getAgentId());
		vo.setKnowledgeId(entity.getKnowledgeId());
		vo.setKnowledgeTitle(entity.getKnowledgeTitle());
		vo.setEnabled(entity.getEnabled());
		vo.setCreatedAt(entity.getCreatedAt());
		vo.setUpdatedAt(entity.getUpdatedAt());
		return vo;
	}

	public List<AgentKnowledgeVO> agentKnowledgeEntityListToVOList(List<AgentKnowledge> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		return entities.stream().map(this::agentKnowledgeEntityToVO).toList();
	}

	public KnowledgeChunkVO chunkEntityToVO(KnowledgeChunk entity) {
		if (entity == null) {
			return null;
		}
		KnowledgeChunkVO vo = new KnowledgeChunkVO();
		vo.setId(entity.getId());
		vo.setKnowledgeId(entity.getKnowledgeId());
		vo.setChunkIndex(entity.getChunkIndex());
		vo.setContent(entity.getContent());
		vo.setEmbeddingModelConfigId(entity.getEmbeddingModelConfigId());
		vo.setEmbeddingDimension(entity.getEmbeddingDimension());
		vo.setEmbeddingStatus(entity.getEmbeddingStatus());
		vo.setEmbeddingError(entity.getEmbeddingError());
		vo.setHasEmbedding(entity.getEmbeddingVector() != null && !entity.getEmbeddingVector().isBlank());
		vo.setEmbeddedAt(entity.getEmbeddedAt());
		vo.setEnabled(entity.getEnabled());
		vo.setCreatedAt(entity.getCreatedAt());
		vo.setUpdatedAt(entity.getUpdatedAt());
		return vo;
	}

	public List<KnowledgeChunkVO> chunkEntityListToVOList(List<KnowledgeChunk> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		return entities.stream().map(this::chunkEntityToVO).toList();
	}

}
