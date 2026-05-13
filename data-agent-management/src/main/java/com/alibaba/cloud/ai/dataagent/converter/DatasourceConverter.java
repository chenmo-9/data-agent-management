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

import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceTestRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.security.SecretService;
import com.alibaba.cloud.ai.dataagent.vo.datasource.DatasourceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Datasource Converter
 */
@Component
@RequiredArgsConstructor
public class DatasourceConverter {

	private final SecretService secretService;

	public Datasource createRequestToEntity(DatasourceCreateRequest request) {
		Datasource datasource = new Datasource();
		datasource.setName(request.getName());
		datasource.setDbType(request.getDbType());
		datasource.setUrl(request.getUrl());
		datasource.setUsername(request.getUsername());
		datasource.setPassword(request.getPassword());
		datasource.setDatabaseName(request.getDatabaseName());
		datasource.setHost(request.getHost());
		datasource.setPort(request.getPort());
		datasource.setEnabled(request.getEnabled());
		datasource.setDescription(request.getDescription());
		return datasource;
	}

	public Datasource updateRequestToEntity(DatasourceUpdateRequest request) {
		Datasource datasource = new Datasource();
		datasource.setName(request.getName());
		datasource.setDbType(request.getDbType());
		datasource.setUrl(request.getUrl());
		datasource.setUsername(request.getUsername());
		datasource.setPassword(request.getPassword());
		datasource.setDatabaseName(request.getDatabaseName());
		datasource.setHost(request.getHost());
		datasource.setPort(request.getPort());
		datasource.setEnabled(request.getEnabled());
		datasource.setDescription(request.getDescription());
		return datasource;
	}

	public Datasource testRequestToEntity(DatasourceTestRequest request) {
		Datasource datasource = new Datasource();
		datasource.setDbType(request.getDbType());
		datasource.setUrl(request.getUrl());
		datasource.setUsername(request.getUsername());
		datasource.setPassword(request.getPassword());
		datasource.setDatabaseName(request.getDatabaseName());
		datasource.setHost(request.getHost());
		datasource.setPort(request.getPort());
		return datasource;
	}

	public DatasourceVO entityToVO(Datasource datasource) {
		if (datasource == null) {
			return null;
		}
		DatasourceVO vo = new DatasourceVO();
		vo.setId(datasource.getId());
		vo.setName(datasource.getName());
		vo.setDbType(datasource.getDbType());
		vo.setUrl(datasource.getUrl());
		vo.setUsername(datasource.getUsername());
		vo.setHasPassword(datasource.getPassword() != null && !datasource.getPassword().isBlank());
		vo.setMaskedPassword(secretService.maskSecret(datasource.getPassword()));
		vo.setDatabaseName(datasource.getDatabaseName());
		vo.setHost(datasource.getHost());
		vo.setPort(datasource.getPort());
		vo.setEnabled(datasource.getEnabled());
		vo.setDescription(datasource.getDescription());
		vo.setCreatedAt(datasource.getCreatedAt());
		vo.setUpdatedAt(datasource.getUpdatedAt());
		return vo;
	}

	public List<DatasourceVO> entityListToVOList(List<Datasource> datasources) {
		return datasources.stream().map(this::entityToVO).toList();
	}

}
