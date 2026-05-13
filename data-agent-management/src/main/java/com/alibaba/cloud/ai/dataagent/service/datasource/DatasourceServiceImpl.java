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
package com.alibaba.cloud.ai.dataagent.service.datasource;

import com.alibaba.cloud.ai.dataagent.connector.DatabaseConnectorFactory;
import com.alibaba.cloud.ai.dataagent.converter.DatasourceConverter;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceTestRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.DatasourceMapper;
import com.alibaba.cloud.ai.dataagent.security.SecretService;
import com.alibaba.cloud.ai.dataagent.vo.datasource.DatasourceTestVO;
import com.alibaba.cloud.ai.dataagent.vo.datasource.DatasourceVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Datasource Service Class
 */
@Slf4j
@Service
@AllArgsConstructor
public class DatasourceServiceImpl implements DatasourceService {

	private final DatasourceMapper datasourceMapper;

	private final DatasourceConverter datasourceConverter;

	private final DatabaseConnectorFactory databaseConnectorFactory;

	private final SecretService secretService;

	@Override
	public DatasourceVO create(DatasourceCreateRequest request) {
		validateDbType(request.getDbType());
		if (datasourceMapper.countByName(request.getName()) > 0) {
			throw new BusinessException("datasource name already exists: " + request.getName());
		}
		Datasource datasource = datasourceConverter.createRequestToEntity(request);
		LocalDateTime now = LocalDateTime.now();
		datasource.setDbType(normalizeDbType(datasource.getDbType()));
		datasource.setPassword(secretService.encryptIfNeeded(datasource.getPassword()));
		datasource.setEnabled(datasource.getEnabled() == null || datasource.getEnabled());
		datasource.setCreatedAt(now);
		datasource.setUpdatedAt(now);
		datasourceMapper.insert(datasource);
		return datasourceConverter.entityToVO(datasource);
	}

	@Override
	public List<DatasourceVO> list(DatasourceQueryRequest request) {
		String dbType = normalizeOptionalDbType(request.getDbType());
		List<Datasource> datasources = datasourceMapper.selectList(request.getKeyword(), dbType, request.getEnabled());
		return datasourceConverter.entityListToVOList(datasources);
	}

	@Override
	public DatasourceVO getDetail(Long id) {
		return datasourceConverter.entityToVO(requireDatasource(id));
	}

	@Override
	public DatasourceVO update(Long id, DatasourceUpdateRequest request) {
		Datasource existing = requireDatasource(id);
		validateDbType(request.getDbType());
		if (datasourceMapper.countByNameExcludeId(request.getName(), id) > 0) {
			throw new BusinessException("datasource name already exists: " + request.getName());
		}
		Datasource datasource = datasourceConverter.updateRequestToEntity(request);
		datasource.setId(id);
		datasource.setDbType(normalizeDbType(datasource.getDbType()));
		if (secretService.isPlaceholder(datasource.getPassword())) {
			datasource.setPassword(existing.getPassword());
		}
		else {
			datasource.setPassword(secretService.encryptIfNeeded(datasource.getPassword()));
		}
		datasource.setEnabled(datasource.getEnabled() == null || datasource.getEnabled());
		datasource.setUpdatedAt(LocalDateTime.now());
		datasourceMapper.updateById(datasource);
		return datasourceConverter.entityToVO(datasourceMapper.selectById(id));
	}

	@Override
	public void delete(Long id) {
		requireDatasource(id);
		datasourceMapper.deleteById(id);
	}

	@Override
	public DatasourceVO enable(Long id) {
		return updateEnabled(id, true);
	}

	@Override
	public DatasourceVO disable(Long id) {
		return updateEnabled(id, false);
	}

	@Override
	public DatasourceTestVO testConnection(DatasourceTestRequest request) {
		validateDbType(request.getDbType());
		Datasource datasource = datasourceConverter.testRequestToEntity(request);
		datasource.setDbType(normalizeDbType(datasource.getDbType()));
		datasource.setPassword(secretService.decryptIfNeeded(datasource.getPassword()));
		return databaseConnectorFactory.getConnector(datasource.getDbType()).testConnection(datasource);
	}

	@Override
	public DatasourceTestVO testConnection(Long id) {
		Datasource datasource = requireDatasource(id);
		datasource.setPassword(secretService.decryptIfNeeded(datasource.getPassword()));
		return databaseConnectorFactory.getConnector(datasource.getDbType()).testConnection(datasource);
	}

	private DatasourceVO updateEnabled(Long id, boolean enabled) {
		Datasource datasource = requireDatasource(id);
		datasource.setEnabled(enabled);
		datasource.setUpdatedAt(LocalDateTime.now());
		datasourceMapper.updateById(datasource);
		return datasourceConverter.entityToVO(datasourceMapper.selectById(id));
	}

	private Datasource requireDatasource(Long id) {
		Datasource datasource = datasourceMapper.selectById(id);
		if (datasource == null) {
			throw new BusinessException("datasource with id: %d not found".formatted(id));
		}
		return datasource;
	}

	private void validateDbType(String dbType) {
		String normalized = normalizeDbType(dbType);
		if (!"h2".equals(normalized) && !"mysql".equals(normalized) && !"postgresql".equals(normalized)) {
			throw new BusinessException("dbType must be h2, mysql or postgresql");
		}
	}

	private String normalizeOptionalDbType(String dbType) {
		if (dbType == null || dbType.isBlank()) {
			return null;
		}
		validateDbType(dbType);
		return normalizeDbType(dbType);
	}

	private String normalizeDbType(String dbType) {
		return dbType == null ? null : dbType.trim().toLowerCase(Locale.ROOT);
	}

}
