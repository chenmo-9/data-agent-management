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

import com.alibaba.cloud.ai.dataagent.dto.knowledge.AgentKnowledgeBindRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.AgentKnowledgeQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.BusinessKnowledgeUpdateRequest;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.AgentKnowledgeVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.BusinessKnowledgeVO;
import com.alibaba.cloud.ai.dataagent.vo.knowledge.KnowledgeChunkVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeService {

	BusinessKnowledgeVO createBusinessKnowledge(BusinessKnowledgeCreateRequest request);

	List<BusinessKnowledgeVO> listBusinessKnowledge(BusinessKnowledgeQueryRequest request);

	BusinessKnowledgeVO getBusinessKnowledgeDetail(Long id);

	BusinessKnowledgeVO updateBusinessKnowledge(Long id, BusinessKnowledgeUpdateRequest request);

	void deleteBusinessKnowledge(Long id);

	BusinessKnowledgeVO enableBusinessKnowledge(Long id);

	BusinessKnowledgeVO disableBusinessKnowledge(Long id);

	BusinessKnowledgeVO uploadBusinessKnowledgeFile(MultipartFile file, String title, String knowledgeType);

	AgentKnowledgeVO bindAgentKnowledge(AgentKnowledgeBindRequest request);

	List<AgentKnowledgeVO> listAgentKnowledge(AgentKnowledgeQueryRequest request);

	List<AgentKnowledgeVO> listKnowledgeByAgentId(Long agentId);

	void unbindAgentKnowledgeById(Long id);

	void unbindAgentKnowledge(Long agentId, Long knowledgeId);

	AgentKnowledgeVO enableAgentKnowledge(Long id);

	AgentKnowledgeVO disableAgentKnowledge(Long id);

	List<KnowledgeChunkVO> rebuildChunks(Long knowledgeId);

	List<KnowledgeChunkVO> listChunksByKnowledgeId(Long knowledgeId);

	void deleteChunksByKnowledgeId(Long knowledgeId);

}
