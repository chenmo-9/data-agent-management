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

import com.alibaba.cloud.ai.dataagent.dto.agent.AgentCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.agent.AgentUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.Agent;
import com.alibaba.cloud.ai.dataagent.vo.agent.AgentVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Agent Converter
 */
@Component
public class AgentConverter {

	public Agent createRequestToEntity(AgentCreateRequest request) {
		Agent agent = new Agent();
		agent.setName(request.getName());
		agent.setDescription(request.getDescription());
		agent.setAvatar(request.getAvatar());
		agent.setCategory(request.getCategory());
		agent.setTags(request.getTags());
		agent.setPrompt(request.getPrompt());
		agent.setPresetQuestions(request.getPresetQuestions());
		agent.setStatus(request.getStatus());
		agent.setAdminId(request.getAdminId());
		return agent;
	}

	public Agent updateRequestToEntity(AgentUpdateRequest request) {
		Agent agent = new Agent();
		agent.setName(request.getName());
		agent.setDescription(request.getDescription());
		agent.setAvatar(request.getAvatar());
		agent.setCategory(request.getCategory());
		agent.setTags(request.getTags());
		agent.setPrompt(request.getPrompt());
		agent.setPresetQuestions(request.getPresetQuestions());
		agent.setStatus(request.getStatus());
		agent.setAdminId(request.getAdminId());
		return agent;
	}

	public AgentVO entityToVO(Agent agent) {
		if (agent == null) {
			return null;
		}
		AgentVO vo = new AgentVO();
		vo.setId(agent.getId());
		vo.setName(agent.getName());
		vo.setDescription(agent.getDescription());
		vo.setAvatar(agent.getAvatar());
		vo.setCategory(agent.getCategory());
		vo.setTags(agent.getTags());
		vo.setPrompt(agent.getPrompt());
		vo.setPresetQuestions(agent.getPresetQuestions());
		vo.setStatus(agent.getStatus());
		vo.setAdminId(agent.getAdminId());
		vo.setCreatedAt(agent.getCreatedAt());
		vo.setUpdatedAt(agent.getUpdatedAt());
		return vo;
	}

	public List<AgentVO> entityListToVOList(List<Agent> agents) {
		return agents.stream().map(this::entityToVO).toList();
	}

}
