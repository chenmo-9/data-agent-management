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
package com.alibaba.cloud.ai.dataagent.service.agent;

import com.alibaba.cloud.ai.dataagent.converter.AgentConverter;
import com.alibaba.cloud.ai.dataagent.dto.agent.AgentCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.agent.AgentQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.agent.AgentUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.Agent;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.AgentMapper;
import com.alibaba.cloud.ai.dataagent.vo.agent.AgentVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent Service Class
 */
@Slf4j
@Service
@AllArgsConstructor
public class AgentServiceImpl implements AgentService {

	private final AgentMapper agentMapper;

	private final AgentConverter agentConverter;

	@Override
	public AgentVO create(AgentCreateRequest request) {
		Agent agent = agentConverter.createRequestToEntity(request);
		LocalDateTime now = LocalDateTime.now();
		agent.setStatus(defaultStatus(agent.getStatus()));
		agent.setCreatedAt(now);
		agent.setUpdatedAt(now);
		agentMapper.insert(agent);
		return agentConverter.entityToVO(agent);
	}

	@Override
	public List<AgentVO> list(AgentQueryRequest request) {
		List<Agent> agents = agentMapper.selectList(request.getKeyword(), request.getStatus(), request.getCategory());
		return agentConverter.entityListToVOList(agents);
	}

	@Override
	public AgentVO getDetail(Long id) {
		return agentConverter.entityToVO(requireAgent(id));
	}

	@Override
	public AgentVO update(Long id, AgentUpdateRequest request) {
		Agent existing = requireAgent(id);
		Agent agent = agentConverter.updateRequestToEntity(request);
		agent.setId(id);
		agent.setStatus(defaultStatusOrExisting(agent.getStatus(), existing.getStatus()));
		agent.setUpdatedAt(LocalDateTime.now());
		agentMapper.updateById(agent);
		return agentConverter.entityToVO(agentMapper.selectById(id));
	}

	@Override
	public void delete(Long id) {
		requireAgent(id);
		agentMapper.deleteById(id);
	}

	@Override
	public AgentVO publish(Long id) {
		return updateStatus(id, "published");
	}

	@Override
	public AgentVO offline(Long id) {
		return updateStatus(id, "offline");
	}

	private Agent requireAgent(Long id) {
		Agent agent = agentMapper.selectById(id);
		if (agent == null) {
			throw new BusinessException("agent with id: %d not found".formatted(id));
		}
		return agent;
	}

	private AgentVO updateStatus(Long id, String status) {
		Agent agent = requireAgent(id);
		agent.setStatus(status);
		agent.setUpdatedAt(LocalDateTime.now());
		agentMapper.updateById(agent);
		return agentConverter.entityToVO(agentMapper.selectById(id));
	}

	private String defaultStatus(String status) {
		return status == null || status.isBlank() ? "draft" : status;
	}

	private String defaultStatusOrExisting(String status, String existingStatus) {
		return status == null || status.isBlank() ? defaultStatus(existingStatus) : status;
	}

}
