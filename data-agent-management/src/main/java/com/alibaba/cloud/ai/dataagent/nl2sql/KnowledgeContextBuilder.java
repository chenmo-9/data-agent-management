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
package com.alibaba.cloud.ai.dataagent.nl2sql;

import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import com.alibaba.cloud.ai.dataagent.entity.BusinessKnowledge;
import com.alibaba.cloud.ai.dataagent.mapper.AgentKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.mapper.BusinessKnowledgeMapper;
import com.alibaba.cloud.ai.dataagent.rag.KnowledgeRecallItem;
import com.alibaba.cloud.ai.dataagent.rag.KnowledgeRecallResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class KnowledgeContextBuilder {

	private static final int MAX_LENGTH = 3000;

	private final AgentKnowledgeMapper agentKnowledgeMapper;

	private final BusinessKnowledgeMapper businessKnowledgeMapper;

	public String build(Long agentId) {
		StringBuilder context = new StringBuilder();
		for (AgentKnowledge relation : agentKnowledgeMapper.selectByAgentId(agentId)) {
			if (!Boolean.TRUE.equals(relation.getEnabled())) {
				continue;
			}
			BusinessKnowledge knowledge = businessKnowledgeMapper.selectById(relation.getKnowledgeId());
			if (knowledge == null || !Boolean.TRUE.equals(knowledge.getEnabled())) {
				continue;
			}
			context.append("知识：").append(knowledge.getTitle()).append("\n");
			if (StringUtils.hasText(knowledge.getContent())) {
				context.append(knowledge.getContent()).append("\n\n");
			}
			if (context.length() >= MAX_LENGTH) {
				return context.substring(0, MAX_LENGTH);
			}
		}
		return context.toString();
	}

	public String buildFromRecallResult(KnowledgeRecallResult result) {
		if (result == null || result.getSelectedChunks() == null || result.getSelectedChunks().isEmpty()) {
			return "";
		}
		StringBuilder context = new StringBuilder("相关业务知识：\n");
		for (int i = 0; i < result.getSelectedChunks().size(); i++) {
			KnowledgeRecallItem item = result.getSelectedChunks().get(i);
			context.append(i + 1).append(". ").append(item.getTitle()).append("\n");
			if (StringUtils.hasText(item.getContent())) {
				context.append(item.getContent()).append("\n\n");
			}
			if (context.length() >= MAX_LENGTH) {
				return context.substring(0, MAX_LENGTH);
			}
		}
		return context.toString();
	}

}
