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
package com.alibaba.cloud.ai.dataagent.service.knowledge;

import com.alibaba.cloud.ai.dataagent.embedding.EmbeddingModelRegistry;
import com.alibaba.cloud.ai.dataagent.embedding.VectorUtils;
import com.alibaba.cloud.ai.dataagent.entity.Agent;
import com.alibaba.cloud.ai.dataagent.entity.BusinessKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.KnowledgeChunk;
import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.AgentMapper;
import com.alibaba.cloud.ai.dataagent.mapper.BusinessKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.mapper.KnowledgeChunkMapper;
import com.alibaba.cloud.ai.dataagent.mapper.ModelConfigMapper;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.KnowledgeEmbeddingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeEmbeddingServiceImpl implements KnowledgeEmbeddingService {

	private final KnowledgeChunkMapper knowledgeChunkMapper;

	private final BusinessKnowledgeMapper businessKnowledgeMapper;

	private final AgentMapper agentMapper;

	private final ModelConfigMapper modelConfigMapper;

	private final EmbeddingModelRegistry embeddingModelRegistry;

	@Override
	public KnowledgeEmbeddingVO embedKnowledge(Long knowledgeId, Long modelConfigId) {
		ModelConfig modelConfig = requireEmbeddingModel(modelConfigId);
		requireBusinessKnowledge(knowledgeId);
		List<KnowledgeChunk> chunks = knowledgeChunkMapper.selectByKnowledgeId(knowledgeId);
		return embedChunks(chunks, modelConfig, "Knowledge embedding rebuilt");
	}

	@Override
	public KnowledgeEmbeddingVO embedAgentKnowledge(Long agentId, Long modelConfigId) {
		ModelConfig modelConfig = requireEmbeddingModel(modelConfigId);
		requireAgent(agentId);
		List<KnowledgeChunk> chunks = knowledgeChunkMapper.selectChunksByAgentId(agentId);
		return embedChunks(chunks, modelConfig, "Agent knowledge embedding rebuilt");
	}

	@Override
	public void clearKnowledgeEmbedding(Long knowledgeId) {
		requireBusinessKnowledge(knowledgeId);
		knowledgeChunkMapper.clearEmbeddingByKnowledgeId(knowledgeId);
	}

	private KnowledgeEmbeddingVO embedChunks(List<KnowledgeChunk> chunks, ModelConfig modelConfig, String message) {
		int total = chunks.size();
		int success = 0;
		int failed = 0;
		for (KnowledgeChunk chunk : chunks) {
			try {
				List<Double> vector = embeddingModelRegistry.embed(modelConfig, chunk.getContent());
				if (!VectorUtils.isValidVector(vector)) {
					throw new BusinessException("invalid embedding vector");
				}
				chunk.setEmbeddingModelConfigId(modelConfig.getId());
				chunk.setEmbeddingDimension(vector.size());
				chunk.setEmbeddingVector(VectorUtils.toJson(vector));
				chunk.setEmbeddingStatus("success");
				chunk.setEmbeddingError(null);
				chunk.setEmbeddedAt(LocalDateTime.now());
				knowledgeChunkMapper.updateEmbedding(chunk);
				success++;
			}
			catch (Exception ex) {
				chunk.setEmbeddingModelConfigId(modelConfig.getId());
				chunk.setEmbeddingDimension(null);
				chunk.setEmbeddingVector(null);
				chunk.setEmbeddingStatus("failed");
				chunk.setEmbeddingError(ex.getMessage());
				chunk.setEmbeddedAt(LocalDateTime.now());
				knowledgeChunkMapper.updateEmbedding(chunk);
				failed++;
			}
		}
		return new KnowledgeEmbeddingVO(total, success, failed, message);
	}

	private BusinessKnowledge requireBusinessKnowledge(Long id) {
		BusinessKnowledge knowledge = businessKnowledgeMapper.selectById(id);
		if (knowledge == null) {
			throw new BusinessException("business knowledge with id: %d not found".formatted(id));
		}
		return knowledge;
	}

	private Agent requireAgent(Long id) {
		Agent agent = agentMapper.selectById(id);
		if (agent == null) {
			throw new BusinessException("agent with id: %d not found".formatted(id));
		}
		return agent;
	}

	private ModelConfig requireEmbeddingModel(Long id) {
		ModelConfig modelConfig = modelConfigMapper.selectById(id);
		if (modelConfig == null) {
			throw new BusinessException("model config with id: %d not found".formatted(id));
		}
		if (!"embedding".equalsIgnoreCase(modelConfig.getModelType())) {
			throw new BusinessException("modelType must be embedding");
		}
		if (!Boolean.TRUE.equals(modelConfig.getEnabled())) {
			throw new BusinessException("embedding model config is disabled");
		}
		return modelConfig;
	}

}
