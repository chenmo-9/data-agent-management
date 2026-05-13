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
package com.alibaba.cloud.ai.dataagent.service.agentdatasource;

import com.alibaba.cloud.ai.dataagent.converter.AgentDatasourceConverter;
import com.alibaba.cloud.ai.dataagent.dto.agentdatasource.AgentDatasourceBindRequest;
import com.alibaba.cloud.ai.dataagent.dto.agentdatasource.AgentDatasourceQueryRequest;
import com.alibaba.cloud.ai.dataagent.entity.Agent;
import com.alibaba.cloud.ai.dataagent.entity.AgentDatasource;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.AgentDatasourceMapper;
import com.alibaba.cloud.ai.dataagent.mapper.AgentMapper;
import com.alibaba.cloud.ai.dataagent.mapper.DatasourceMapper;
import com.alibaba.cloud.ai.dataagent.vo.agentdatasource.AgentDatasourceVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Agent Datasource Service Class
 */
@Slf4j
@Service
@AllArgsConstructor
public class AgentDatasourceServiceImpl implements AgentDatasourceService {

	private final AgentDatasourceMapper agentDatasourceMapper;

	private final AgentMapper agentMapper;

	private final DatasourceMapper datasourceMapper;

	private final AgentDatasourceConverter agentDatasourceConverter;

	@Override
	public AgentDatasourceVO bind(AgentDatasourceBindRequest request) {
		Agent agent = requireAgent(request.getAgentId());
		Datasource datasource = requireDatasource(request.getDatasourceId());
		if (!Boolean.TRUE.equals(datasource.getEnabled())) {
			throw new BusinessException("datasource with id: %d is disabled".formatted(request.getDatasourceId()));
		}
		if (agentDatasourceMapper.countByAgentIdAndDatasourceId(request.getAgentId(), request.getDatasourceId()) > 0) {
			throw new BusinessException("agent datasource relation already exists");
		}
		AgentDatasource relation = agentDatasourceConverter.bindRequestToEntity(request);
		LocalDateTime now = LocalDateTime.now();
		relation.setDatasourceName(datasource.getName());
		relation.setDbType(normalizeDbType(datasource.getDbType()));
		relation.setEnabled(true);
		relation.setCreatedAt(now);
		relation.setUpdatedAt(now);
		agentDatasourceMapper.insert(relation);
		return fillAgentName(agentDatasourceConverter.entityToVO(relation), agent);
	}

	@Override
	public List<AgentDatasourceVO> list(AgentDatasourceQueryRequest request) {
		String dbType = normalizeOptionalDbType(request.getDbType());
		List<AgentDatasource> relations = agentDatasourceMapper.selectList(request.getAgentId(), request.getDatasourceId(),
				dbType, request.getEnabled());
		return fillAgentNames(agentDatasourceConverter.entityListToVOList(relations));
	}

	@Override
	public List<AgentDatasourceVO> listByAgentId(Long agentId) {
		requireAgent(agentId);
		return fillAgentNames(agentDatasourceConverter.entityListToVOList(agentDatasourceMapper.selectByAgentId(agentId)));
	}

	@Override
	public void unbindById(Long id) {
		requireRelation(id);
		agentDatasourceMapper.deleteById(id);
	}

	@Override
	public void unbind(Long agentId, Long datasourceId) {
		AgentDatasource relation = agentDatasourceMapper.selectByAgentIdAndDatasourceId(agentId, datasourceId);
		if (relation == null) {
			throw new BusinessException("agent datasource relation not found");
		}
		agentDatasourceMapper.deleteByAgentIdAndDatasourceId(agentId, datasourceId);
	}

	@Override
	public AgentDatasourceVO enable(Long id) {
		return updateEnabled(id, true);
	}

	@Override
	public AgentDatasourceVO disable(Long id) {
		return updateEnabled(id, false);
	}

	private AgentDatasourceVO updateEnabled(Long id, boolean enabled) {
		requireRelation(id);
		agentDatasourceMapper.updateEnabledById(id, enabled);
		return fillAgentName(agentDatasourceConverter.entityToVO(agentDatasourceMapper.selectById(id)));
	}

	private Agent requireAgent(Long id) {
		Agent agent = agentMapper.selectById(id);
		if (agent == null) {
			throw new BusinessException("agent with id: %d not found".formatted(id));
		}
		return agent;
	}

	private Datasource requireDatasource(Long id) {
		Datasource datasource = datasourceMapper.selectById(id);
		if (datasource == null) {
			throw new BusinessException("datasource with id: %d not found".formatted(id));
		}
		return datasource;
	}

	private AgentDatasource requireRelation(Long id) {
		AgentDatasource relation = agentDatasourceMapper.selectById(id);
		if (relation == null) {
			throw new BusinessException("agent datasource relation with id: %d not found".formatted(id));
		}
		return relation;
	}

	private List<AgentDatasourceVO> fillAgentNames(List<AgentDatasourceVO> vos) {
		vos.forEach(this::fillAgentName);
		return vos;
	}

	private AgentDatasourceVO fillAgentName(AgentDatasourceVO vo) {
		if (vo == null || vo.getAgentId() == null) {
			return vo;
		}
		return fillAgentName(vo, agentMapper.selectById(vo.getAgentId()));
	}

	private AgentDatasourceVO fillAgentName(AgentDatasourceVO vo, Agent agent) {
		if (vo != null && agent != null) {
			vo.setAgentName(agent.getName());
		}
		return vo;
	}

	private String normalizeOptionalDbType(String dbType) {
		if (dbType == null || dbType.isBlank()) {
			return null;
		}
		return normalizeDbType(dbType);
	}

	private String normalizeDbType(String dbType) {
		return dbType == null ? null : dbType.trim().toLowerCase(Locale.ROOT);
	}

}
