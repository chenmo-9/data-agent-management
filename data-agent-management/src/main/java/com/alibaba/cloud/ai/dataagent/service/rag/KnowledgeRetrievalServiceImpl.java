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
package com.alibaba.cloud.ai.dataagent.service.rag;

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
import com.alibaba.cloud.ai.dataagent.nl2sql.KeywordUtils;
import com.alibaba.cloud.ai.dataagent.rag.KnowledgeRecallItem;
import com.alibaba.cloud.ai.dataagent.rag.KnowledgeRecallResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalServiceImpl implements KnowledgeRetrievalService {

	private static final int MAX_CONTEXT_LENGTH = 3000;

	private final AgentMapper agentMapper;

	private final ModelConfigMapper modelConfigMapper;

	private final KnowledgeChunkMapper knowledgeChunkMapper;

	private final BusinessKnowledgeMapper businessKnowledgeMapper;

	private final EmbeddingModelRegistry embeddingModelRegistry;

	private final KeywordUtils keywordUtils;

	@Override
	public KnowledgeRecallResult recall(Long agentId, String question, Long embeddingModelConfigId, int topK) {
		requireAgent(agentId);
		int limit = topK <= 0 ? 5 : topK;
		ModelConfig embeddingModel = resolveEmbeddingModel(embeddingModelConfigId);
		if (embeddingModel != null) {
			try {
				KnowledgeRecallResult vectorResult = recallByVector(agentId, question, embeddingModel, limit);
				if (vectorResult != null && vectorResult.getRecalledCount() != null && vectorResult.getRecalledCount() > 0) {
					return vectorResult;
				}
			}
			catch (Exception ex) {
				return fallbackRecall(agentId, question, limit, "Embedding recall failed, fallback to keyword chunks: "
						+ ex.getMessage(), true);
			}
		}
		return fallbackRecall(agentId, question, limit,
				embeddingModelConfigId == null ? "No embedding model configured, fallback to keyword chunks"
						: "Embedding model unavailable, fallback to keyword chunks",
				true);
	}

	private KnowledgeRecallResult recallByVector(Long agentId, String question, ModelConfig embeddingModel, int topK) {
		List<KnowledgeChunk> embeddedChunks = knowledgeChunkMapper.selectEmbeddedChunksByAgentId(agentId);
		if (embeddedChunks.isEmpty()) {
			return fallbackRecall(agentId, question, topK, "No embedded chunks available, fallback to keyword chunks", true);
		}
		List<Double> questionVector = embeddingModelRegistry.embed(embeddingModel, question);
		if (!VectorUtils.isValidVector(questionVector)) {
			return fallbackRecall(agentId, question, topK, "Question embedding invalid, fallback to keyword chunks", true);
		}
		Map<Long, BusinessKnowledge> knowledgeMap = loadKnowledgeMap(embeddedChunks);
		List<KnowledgeRecallItem> items = new ArrayList<>();
		for (KnowledgeChunk chunk : embeddedChunks) {
			List<Double> chunkVector = VectorUtils.fromJson(chunk.getEmbeddingVector());
			double score = VectorUtils.cosineSimilarity(questionVector, chunkVector);
			if (score <= 0D) {
				continue;
			}
			BusinessKnowledge knowledge = knowledgeMap.get(chunk.getKnowledgeId());
			if (knowledge == null) {
				continue;
			}
			items.add(KnowledgeRecallItem.builder()
				.chunkId(chunk.getId())
				.knowledgeId(chunk.getKnowledgeId())
				.title(knowledge.getTitle())
				.content(chunk.getContent())
				.score(score)
				.sourceType(knowledge.getSourceType())
				.knowledgeType(knowledge.getKnowledgeType())
				.matchReason("vector cosine similarity")
				.build());
		}
		items.sort(Comparator.comparing(KnowledgeRecallItem::getScore).reversed());
		List<KnowledgeRecallItem> selected = items.stream().limit(topK).toList();
		if (selected.isEmpty()) {
			return fallbackRecall(agentId, question, topK, "Vector recall returned empty, fallback to keyword chunks", true);
		}
		return buildResult(agentId, question, topK, selected, false, "Vector recall success");
	}

	private KnowledgeRecallResult fallbackRecall(Long agentId, String question, int topK, String message,
			boolean fallbackUsed) {
		List<KnowledgeChunk> chunks = knowledgeChunkMapper.selectChunksByAgentId(agentId);
		Map<Long, BusinessKnowledge> knowledgeMap = loadKnowledgeMap(chunks);
		List<String> keywords = keywordUtils.extractKeywords(question);
		List<KnowledgeRecallItem> scoredItems = new ArrayList<>();
		for (KnowledgeChunk chunk : chunks) {
			BusinessKnowledge knowledge = knowledgeMap.get(chunk.getKnowledgeId());
			if (knowledge == null) {
				continue;
			}
			double score = 0D;
			List<String> reasons = new ArrayList<>();
			String title = normalize(knowledge.getTitle());
			String content = normalize(chunk.getContent());
			for (String keyword : keywords) {
				if (title.contains(keyword)) {
					score += 6D;
					reasons.add("title matched: " + keyword);
				}
				if (content.contains(keyword)) {
					score += 3D;
					reasons.add("content matched: " + keyword);
				}
			}
			scoredItems.add(KnowledgeRecallItem.builder()
				.chunkId(chunk.getId())
				.knowledgeId(chunk.getKnowledgeId())
				.title(knowledge.getTitle())
				.content(chunk.getContent())
				.score(score)
				.sourceType(knowledge.getSourceType())
				.knowledgeType(knowledge.getKnowledgeType())
				.matchReason(reasons.isEmpty() ? "fallback chunk" : String.join("; ", reasons))
				.build());
		}
		scoredItems.sort(Comparator.comparing(KnowledgeRecallItem::getScore).reversed());
		List<KnowledgeRecallItem> selected = scoredItems.stream()
			.filter(item -> item.getScore() > 0D)
			.limit(topK)
			.toList();
		if (selected.isEmpty()) {
			selected = scoredItems.stream().limit(topK).toList();
		}
		return buildResult(agentId, question, topK, selected, fallbackUsed, message);
	}

	private KnowledgeRecallResult buildResult(Long agentId, String question, int topK, List<KnowledgeRecallItem> selected,
			boolean fallbackUsed, String message) {
		return KnowledgeRecallResult.builder()
			.agentId(agentId)
			.question(question)
			.topK(topK)
			.recalledCount(selected.size())
			.fallbackUsed(fallbackUsed)
			.message(message)
			.selectedChunks(selected)
			.knowledgeContext(buildKnowledgeContext(selected))
			.build();
	}

	private String buildKnowledgeContext(List<KnowledgeRecallItem> selectedChunks) {
		if (selectedChunks == null || selectedChunks.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder("相关业务知识：\n");
		for (int i = 0; i < selectedChunks.size(); i++) {
			KnowledgeRecallItem item = selectedChunks.get(i);
			builder.append(i + 1)
				.append(". [score=")
				.append(String.format(Locale.ROOT, "%.2f", item.getScore()))
				.append("] ")
				.append(item.getTitle())
				.append("\n")
				.append(item.getContent())
				.append("\n");
			if (builder.length() >= MAX_CONTEXT_LENGTH) {
				return builder.substring(0, MAX_CONTEXT_LENGTH);
			}
		}
		return builder.toString();
	}

	private Map<Long, BusinessKnowledge> loadKnowledgeMap(List<KnowledgeChunk> chunks) {
		Map<Long, BusinessKnowledge> map = new HashMap<>();
		for (KnowledgeChunk chunk : chunks) {
			map.computeIfAbsent(chunk.getKnowledgeId(), businessKnowledgeMapper::selectById);
		}
		return map;
	}

	private ModelConfig resolveEmbeddingModel(Long embeddingModelConfigId) {
		if (embeddingModelConfigId != null) {
			ModelConfig config = modelConfigMapper.selectById(embeddingModelConfigId);
			if (config != null && "embedding".equalsIgnoreCase(config.getModelType())
					&& Boolean.TRUE.equals(config.getEnabled())) {
				return config;
			}
			return null;
		}
		List<ModelConfig> embeddingModels = modelConfigMapper.selectList(null, "embedding", true);
		return embeddingModels.isEmpty() ? null : embeddingModels.get(0);
	}

	private Agent requireAgent(Long agentId) {
		Agent agent = agentMapper.selectById(agentId);
		if (agent == null) {
			throw new BusinessException("agent with id: %d not found".formatted(agentId));
		}
		return agent;
	}

	private String normalize(String text) {
		return text == null ? "" : text.toLowerCase(Locale.ROOT);
	}

}
