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

import com.alibaba.cloud.ai.dataagent.converter.KnowledgeConverter;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.AgentKnowledgeBindRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.AgentKnowledgeQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.Agent;
import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.BusinessKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.KnowledgeChunk;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.AgentKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.mapper.AgentMapper;
import com.alibaba.cloud.ai.dataagent.mapper.BusinessKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.mapper.KnowledgeChunkMapper;
import com.alibaba.cloud.ai.dataagent.splitter.SimpleTextSplitter;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.AgentKnowledgeVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.BusinessKnowledgeVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.KnowledgeChunkVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Knowledge Service Class
 */
@Slf4j
@Service
@AllArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

	private static final Path KNOWLEDGE_UPLOAD_DIR = Path.of("data", "uploads", "knowledge");

	private final BusinessKnowledgeMapper businessKnowledgeMapper;

	private final AgentKnowledgeMapper agentKnowledgeMapper;

	private final KnowledgeChunkMapper knowledgeChunkMapper;

	private final AgentMapper agentMapper;

	private final KnowledgeConverter knowledgeConverter;

	private final SimpleTextSplitter simpleTextSplitter;

	@Override
	public BusinessKnowledgeVO createBusinessKnowledge(BusinessKnowledgeCreateRequest request) {
		if (businessKnowledgeMapper.countByTitle(request.getTitle()) > 0) {
			throw new BusinessException("business knowledge title already exists: " + request.getTitle());
		}
		BusinessKnowledge knowledge = knowledgeConverter.businessCreateRequestToEntity(request);
		prepareBusinessKnowledge(knowledge);
		validateBusinessKnowledgeContent(knowledge);
		LocalDateTime now = LocalDateTime.now();
		knowledge.setCreatedAt(now);
		knowledge.setUpdatedAt(now);
		businessKnowledgeMapper.insert(knowledge);
		rebuildChunksIfContentPresent(knowledge);
		return knowledgeConverter.businessEntityToVO(knowledge);
	}

	@Override
	public List<BusinessKnowledgeVO> listBusinessKnowledge(BusinessKnowledgeQueryRequest request) {
		List<BusinessKnowledge> knowledgeList = businessKnowledgeMapper.selectList(request.getKeyword(),
				normalizeOptional(request.getKnowledgeType()), normalizeOptionalSourceType(request.getSourceType()),
				request.getEnabled());
		return knowledgeConverter.businessEntityListToVOList(knowledgeList);
	}

	@Override
	public BusinessKnowledgeVO getBusinessKnowledgeDetail(Long id) {
		return knowledgeConverter.businessEntityToVO(requireBusinessKnowledge(id));
	}

	@Override
	public BusinessKnowledgeVO updateBusinessKnowledge(Long id, BusinessKnowledgeUpdateRequest request) {
		BusinessKnowledge existing = requireBusinessKnowledge(id);
		if (businessKnowledgeMapper.countByTitleExcludeId(request.getTitle(), id) > 0) {
			throw new BusinessException("business knowledge title already exists: " + request.getTitle());
		}
		BusinessKnowledge knowledge = knowledgeConverter.businessUpdateRequestToEntity(request);
		knowledge.setId(id);
		knowledge.setFileName(existing.getFileName());
		knowledge.setFilePath(existing.getFilePath());
		knowledge.setFileSize(existing.getFileSize());
		prepareBusinessKnowledge(knowledge);
		validateBusinessKnowledgeContent(knowledge);
		knowledge.setUpdatedAt(LocalDateTime.now());
		businessKnowledgeMapper.updateById(knowledge);
		if (!Objects.equals(existing.getContent(), knowledge.getContent())) {
			rebuildChunks(id);
		}
		return knowledgeConverter.businessEntityToVO(businessKnowledgeMapper.selectById(id));
	}

	@Override
	public void deleteBusinessKnowledge(Long id) {
		requireBusinessKnowledge(id);
		agentKnowledgeMapper.deleteByKnowledgeId(id);
		knowledgeChunkMapper.deleteByKnowledgeId(id);
		businessKnowledgeMapper.deleteById(id);
	}

	@Override
	public BusinessKnowledgeVO enableBusinessKnowledge(Long id) {
		return updateBusinessKnowledgeEnabled(id, true);
	}

	@Override
	public BusinessKnowledgeVO disableBusinessKnowledge(Long id) {
		return updateBusinessKnowledgeEnabled(id, false);
	}

	@Override
	public BusinessKnowledgeVO uploadBusinessKnowledgeFile(MultipartFile file, String title, String knowledgeType) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("upload file cannot be empty");
		}
		String originalFileName = file.getOriginalFilename();
		String safeFileName = sanitizeFileName(originalFileName == null || originalFileName.isBlank() ? "knowledge.txt"
				: originalFileName);
		String finalTitle = title == null || title.isBlank() ? safeFileName : title;
		if (businessKnowledgeMapper.countByTitle(finalTitle) > 0) {
			throw new BusinessException("business knowledge title already exists: " + finalTitle);
		}
		try {
			Files.createDirectories(KNOWLEDGE_UPLOAD_DIR);
			String storedFileName = System.currentTimeMillis() + "-" + safeFileName;
			Path target = KNOWLEDGE_UPLOAD_DIR.resolve(storedFileName).normalize();
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

			BusinessKnowledge knowledge = new BusinessKnowledge();
			knowledge.setTitle(finalTitle);
			knowledge.setKnowledgeType(knowledgeType == null || knowledgeType.isBlank() ? "document" : knowledgeType);
			knowledge.setSourceType("file");
			knowledge.setFileName(safeFileName);
			knowledge.setFilePath(target.toString());
			knowledge.setFileSize(file.getSize());
			knowledge.setContent(readTextFileIfSupported(target, safeFileName));
			knowledge.setEnabled(true);
			LocalDateTime now = LocalDateTime.now();
			knowledge.setCreatedAt(now);
			knowledge.setUpdatedAt(now);
			businessKnowledgeMapper.insert(knowledge);
			rebuildChunksIfContentPresent(knowledge);
			return knowledgeConverter.businessEntityToVO(knowledge);
		}
		catch (IOException ex) {
			throw new BusinessException("failed to save upload file: " + ex.getMessage());
		}
	}

	@Override
	public AgentKnowledgeVO bindAgentKnowledge(AgentKnowledgeBindRequest request) {
		Agent agent = requireAgent(request.getAgentId());
		BusinessKnowledge knowledge = requireBusinessKnowledge(request.getKnowledgeId());
		if (!Boolean.TRUE.equals(knowledge.getEnabled())) {
			throw new BusinessException("business knowledge with id: %d is disabled".formatted(request.getKnowledgeId()));
		}
		if (agentKnowledgeMapper.countByAgentIdAndKnowledgeId(request.getAgentId(), request.getKnowledgeId()) > 0) {
			throw new BusinessException("agent knowledge relation already exists");
		}
		AgentKnowledge relation = new AgentKnowledge();
		LocalDateTime now = LocalDateTime.now();
		relation.setAgentId(request.getAgentId());
		relation.setKnowledgeId(request.getKnowledgeId());
		relation.setKnowledgeTitle(knowledge.getTitle());
		relation.setEnabled(true);
		relation.setCreatedAt(now);
		relation.setUpdatedAt(now);
		agentKnowledgeMapper.insert(relation);
		return fillAgentName(knowledgeConverter.agentKnowledgeEntityToVO(relation), agent);
	}

	@Override
	public List<AgentKnowledgeVO> listAgentKnowledge(AgentKnowledgeQueryRequest request) {
		return fillAgentNames(knowledgeConverter.agentKnowledgeEntityListToVOList(agentKnowledgeMapper
			.selectList(request.getAgentId(), request.getKnowledgeId(), request.getEnabled())));
	}

	@Override
	public List<AgentKnowledgeVO> listKnowledgeByAgentId(Long agentId) {
		requireAgent(agentId);
		return fillAgentNames(knowledgeConverter.agentKnowledgeEntityListToVOList(agentKnowledgeMapper
			.selectByAgentId(agentId)));
	}

	@Override
	public void unbindAgentKnowledgeById(Long id) {
		requireAgentKnowledge(id);
		agentKnowledgeMapper.deleteById(id);
	}

	@Override
	public void unbindAgentKnowledge(Long agentId, Long knowledgeId) {
		if (agentKnowledgeMapper.countByAgentIdAndKnowledgeId(agentId, knowledgeId) == 0) {
			throw new BusinessException("agent knowledge relation not found");
		}
		agentKnowledgeMapper.deleteByAgentIdAndKnowledgeId(agentId, knowledgeId);
	}

	@Override
	public AgentKnowledgeVO enableAgentKnowledge(Long id) {
		return updateAgentKnowledgeEnabled(id, true);
	}

	@Override
	public AgentKnowledgeVO disableAgentKnowledge(Long id) {
		return updateAgentKnowledgeEnabled(id, false);
	}

	@Override
	public List<KnowledgeChunkVO> rebuildChunks(Long knowledgeId) {
		BusinessKnowledge knowledge = requireBusinessKnowledge(knowledgeId);
		knowledgeChunkMapper.deleteByKnowledgeId(knowledgeId);
		rebuildChunksIfContentPresent(knowledge);
		return listChunksByKnowledgeId(knowledgeId);
	}

	@Override
	public List<KnowledgeChunkVO> listChunksByKnowledgeId(Long knowledgeId) {
		requireBusinessKnowledge(knowledgeId);
		return knowledgeConverter.chunkEntityListToVOList(knowledgeChunkMapper.selectByKnowledgeId(knowledgeId));
	}

	@Override
	public void deleteChunksByKnowledgeId(Long knowledgeId) {
		requireBusinessKnowledge(knowledgeId);
		knowledgeChunkMapper.deleteByKnowledgeId(knowledgeId);
	}

	private BusinessKnowledgeVO updateBusinessKnowledgeEnabled(Long id, boolean enabled) {
		requireBusinessKnowledge(id);
		businessKnowledgeMapper.updateEnabledById(id, enabled);
		return knowledgeConverter.businessEntityToVO(businessKnowledgeMapper.selectById(id));
	}

	private AgentKnowledgeVO updateAgentKnowledgeEnabled(Long id, boolean enabled) {
		requireAgentKnowledge(id);
		agentKnowledgeMapper.updateEnabledById(id, enabled);
		return fillAgentName(knowledgeConverter.agentKnowledgeEntityToVO(agentKnowledgeMapper.selectById(id)));
	}

	private void rebuildChunksIfContentPresent(BusinessKnowledge knowledge) {
		if (knowledge.getContent() == null || knowledge.getContent().isBlank()) {
			return;
		}
		List<String> contents = simpleTextSplitter.split(knowledge.getContent());
		if (contents.isEmpty()) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		List<KnowledgeChunk> chunks = new java.util.ArrayList<>();
		for (int i = 0; i < contents.size(); i++) {
			KnowledgeChunk chunk = new KnowledgeChunk();
			chunk.setKnowledgeId(knowledge.getId());
			chunk.setChunkIndex(i);
			chunk.setContent(contents.get(i));
			chunk.setEmbeddingStatus("none");
			chunk.setEnabled(true);
			chunk.setCreatedAt(now);
			chunk.setUpdatedAt(now);
			chunks.add(chunk);
		}
		knowledgeChunkMapper.batchInsert(chunks);
	}

	private void prepareBusinessKnowledge(BusinessKnowledge knowledge) {
		knowledge.setSourceType(knowledge.getSourceType() == null || knowledge.getSourceType().isBlank() ? "text"
				: knowledge.getSourceType().trim().toLowerCase(Locale.ROOT));
		knowledge.setKnowledgeType(normalizeOptional(knowledge.getKnowledgeType()));
		knowledge.setEnabled(knowledge.getEnabled() == null || knowledge.getEnabled());
	}

	private void validateBusinessKnowledgeContent(BusinessKnowledge knowledge) {
		if ("text".equals(knowledge.getSourceType()) && (knowledge.getContent() == null || knowledge.getContent().isBlank())) {
			throw new BusinessException("content cannot be blank when sourceType is text");
		}
		if (!"text".equals(knowledge.getSourceType()) && !"file".equals(knowledge.getSourceType())) {
			throw new BusinessException("sourceType must be text or file");
		}
	}

	private String readTextFileIfSupported(Path target, String fileName) throws IOException {
		if (fileName.toLowerCase(Locale.ROOT).endsWith(".txt")) {
			return Files.readString(target, StandardCharsets.UTF_8);
		}
		return null;
	}

	private String sanitizeFileName(String fileName) {
		return fileName.replace("\\", "_").replace("/", "_").replace(":", "_");
	}

	private String normalizeOptional(String value) {
		return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeOptionalSourceType(String value) {
		return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
	}

	private BusinessKnowledge requireBusinessKnowledge(Long id) {
		BusinessKnowledge knowledge = businessKnowledgeMapper.selectById(id);
		if (knowledge == null) {
			throw new BusinessException("business knowledge with id: %d not found".formatted(id));
		}
		return knowledge;
	}

	private AgentKnowledge requireAgentKnowledge(Long id) {
		AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
		if (knowledge == null) {
			throw new BusinessException("agent knowledge relation with id: %d not found".formatted(id));
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

	private List<AgentKnowledgeVO> fillAgentNames(List<AgentKnowledgeVO> vos) {
		vos.forEach(this::fillAgentName);
		return vos;
	}

	private AgentKnowledgeVO fillAgentName(AgentKnowledgeVO vo) {
		if (vo == null || vo.getAgentId() == null) {
			return vo;
		}
		return fillAgentName(vo, agentMapper.selectById(vo.getAgentId()));
	}

	private AgentKnowledgeVO fillAgentName(AgentKnowledgeVO vo, Agent agent) {
		if (vo != null && agent != null) {
			vo.setAgentName(agent.getName());
		}
		return vo;
	}

}
