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

import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldUpdateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.SemanticField;
import com.alibaba.cloud.ai.dataagent.entity.SemanticTable;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticFieldVO;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticTableVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SemanticModelConverter {

	public SemanticTable tableCreateRequestToEntity(SemanticTableCreateRequest request) {
		if (request == null) {
			return null;
		}
		SemanticTable table = new SemanticTable();
		table.setDatasourceId(request.getDatasourceId());
		table.setTableName(request.getTableName());
		table.setBusinessName(request.getBusinessName());
		table.setDescription(request.getDescription());
		table.setSynonyms(request.getSynonyms());
		table.setEnabled(request.getEnabled());
		return table;
	}

	public SemanticTable tableUpdateRequestToEntity(SemanticTableUpdateRequest request) {
		if (request == null) {
			return null;
		}
		SemanticTable table = new SemanticTable();
		table.setDatasourceId(request.getDatasourceId());
		table.setTableName(request.getTableName());
		table.setBusinessName(request.getBusinessName());
		table.setDescription(request.getDescription());
		table.setSynonyms(request.getSynonyms());
		table.setEnabled(request.getEnabled());
		return table;
	}

	public SemanticTableVO tableEntityToVO(SemanticTable entity) {
		if (entity == null) {
			return null;
		}
		SemanticTableVO vo = new SemanticTableVO();
		vo.setId(entity.getId());
		vo.setDatasourceId(entity.getDatasourceId());
		vo.setTableName(entity.getTableName());
		vo.setBusinessName(entity.getBusinessName());
		vo.setDescription(entity.getDescription());
		vo.setSynonyms(entity.getSynonyms());
		vo.setEnabled(entity.getEnabled());
		vo.setCreatedAt(entity.getCreatedAt());
		vo.setUpdatedAt(entity.getUpdatedAt());
		return vo;
	}

	public List<SemanticTableVO> tableEntityListToVOList(List<SemanticTable> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		return entities.stream().map(this::tableEntityToVO).toList();
	}

	public SemanticField fieldCreateRequestToEntity(SemanticFieldCreateRequest request) {
		if (request == null) {
			return null;
		}
		SemanticField field = new SemanticField();
		field.setTableId(request.getTableId());
		field.setFieldName(request.getFieldName());
		field.setBusinessName(request.getBusinessName());
		field.setDataType(request.getDataType());
		field.setDescription(request.getDescription());
		field.setSynonyms(request.getSynonyms());
		field.setExampleValue(request.getExampleValue());
		field.setPrimaryKey(request.getPrimaryKey());
		field.setNullable(request.getNullable());
		field.setEnabled(request.getEnabled());
		return field;
	}

	public SemanticField fieldUpdateRequestToEntity(SemanticFieldUpdateRequest request) {
		if (request == null) {
			return null;
		}
		SemanticField field = new SemanticField();
		field.setTableId(request.getTableId());
		field.setFieldName(request.getFieldName());
		field.setBusinessName(request.getBusinessName());
		field.setDataType(request.getDataType());
		field.setDescription(request.getDescription());
		field.setSynonyms(request.getSynonyms());
		field.setExampleValue(request.getExampleValue());
		field.setPrimaryKey(request.getPrimaryKey());
		field.setNullable(request.getNullable());
		field.setEnabled(request.getEnabled());
		return field;
	}

	public SemanticFieldVO fieldEntityToVO(SemanticField entity) {
		if (entity == null) {
			return null;
		}
		SemanticFieldVO vo = new SemanticFieldVO();
		vo.setId(entity.getId());
		vo.setTableId(entity.getTableId());
		vo.setDatasourceId(entity.getDatasourceId());
		vo.setTableName(entity.getTableName());
		vo.setFieldName(entity.getFieldName());
		vo.setBusinessName(entity.getBusinessName());
		vo.setDataType(entity.getDataType());
		vo.setDescription(entity.getDescription());
		vo.setSynonyms(entity.getSynonyms());
		vo.setExampleValue(entity.getExampleValue());
		vo.setPrimaryKey(entity.getPrimaryKey());
		vo.setNullable(entity.getNullable());
		vo.setEnabled(entity.getEnabled());
		vo.setCreatedAt(entity.getCreatedAt());
		vo.setUpdatedAt(entity.getUpdatedAt());
		return vo;
	}

	public List<SemanticFieldVO> fieldEntityListToVOList(List<SemanticField> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		return entities.stream().map(this::fieldEntityToVO).toList();
	}

}
