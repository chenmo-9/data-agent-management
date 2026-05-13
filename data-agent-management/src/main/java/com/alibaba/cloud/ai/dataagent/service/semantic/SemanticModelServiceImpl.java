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
package com.alibaba.cloud.ai.dataagent.service.semantic;

import com.alibaba.cloud.ai.dataagent.converter.SemanticModelConverter;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldUpdateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.entity.SemanticField;
import com.alibaba.cloud.ai.dataagent.entity.SemanticTable;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.DatasourceMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticFieldMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticRelationMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticTableMapper;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticFieldVO;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticModelVO;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticTableVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Semantic Model Service Class
 */
@Slf4j
@Service
@AllArgsConstructor
public class SemanticModelServiceImpl implements SemanticModelService {

	private final SemanticTableMapper semanticTableMapper;

	private final SemanticFieldMapper semanticFieldMapper;

	private final SemanticRelationMapper semanticRelationMapper;

	private final DatasourceMapper datasourceMapper;

	private final SemanticModelConverter semanticModelConverter;

	@Override
	public SemanticTableVO createTable(SemanticTableCreateRequest request) {
		requireDatasource(request.getDatasourceId());
		if (semanticTableMapper.countByDatasourceIdAndTableName(request.getDatasourceId(), request.getTableName()) > 0) {
			throw new BusinessException("semantic table already exists");
		}
		SemanticTable table = semanticModelConverter.tableCreateRequestToEntity(request);
		LocalDateTime now = LocalDateTime.now();
		table.setEnabled(table.getEnabled() == null || table.getEnabled());
		table.setCreatedAt(now);
		table.setUpdatedAt(now);
		semanticTableMapper.insert(table);
		return semanticModelConverter.tableEntityToVO(table);
	}

	@Override
	public List<SemanticTableVO> listTables(SemanticTableQueryRequest request) {
		List<SemanticTable> tables = semanticTableMapper.selectList(request.getDatasourceId(), request.getKeyword(),
				request.getEnabled());
		return semanticModelConverter.tableEntityListToVOList(tables);
	}

	@Override
	public SemanticTableVO getTableDetail(Long id) {
		return semanticModelConverter.tableEntityToVO(requireTable(id));
	}

	@Override
	public SemanticTableVO updateTable(Long id, SemanticTableUpdateRequest request) {
		requireTable(id);
		requireDatasource(request.getDatasourceId());
		if (semanticTableMapper.countByDatasourceIdAndTableNameExcludeId(request.getDatasourceId(),
				request.getTableName(), id) > 0) {
			throw new BusinessException("semantic table already exists");
		}
		SemanticTable table = semanticModelConverter.tableUpdateRequestToEntity(request);
		table.setId(id);
		table.setEnabled(table.getEnabled() == null || table.getEnabled());
		table.setUpdatedAt(LocalDateTime.now());
		semanticTableMapper.updateById(table);
		return semanticModelConverter.tableEntityToVO(semanticTableMapper.selectById(id));
	}

	@Override
	public void deleteTable(Long id) {
		requireTable(id);
		semanticRelationMapper.deleteByTableId(id);
		semanticFieldMapper.deleteByTableId(id);
		semanticTableMapper.deleteById(id);
	}

	@Override
	public SemanticTableVO enableTable(Long id) {
		return updateTableEnabled(id, true);
	}

	@Override
	public SemanticTableVO disableTable(Long id) {
		return updateTableEnabled(id, false);
	}

	@Override
	public SemanticFieldVO createField(SemanticFieldCreateRequest request) {
		SemanticTable table = requireTable(request.getTableId());
		if (semanticFieldMapper.countByTableIdAndFieldName(request.getTableId(), request.getFieldName()) > 0) {
			throw new BusinessException("semantic field already exists");
		}
		SemanticField field = semanticModelConverter.fieldCreateRequestToEntity(request);
		LocalDateTime now = LocalDateTime.now();
		field.setDatasourceId(table.getDatasourceId());
		field.setTableName(table.getTableName());
		field.setPrimaryKey(Boolean.TRUE.equals(field.getPrimaryKey()));
		field.setNullable(field.getNullable() == null || field.getNullable());
		field.setEnabled(field.getEnabled() == null || field.getEnabled());
		field.setCreatedAt(now);
		field.setUpdatedAt(now);
		semanticFieldMapper.insert(field);
		return semanticModelConverter.fieldEntityToVO(field);
	}

	@Override
	public List<SemanticFieldVO> listFields(SemanticFieldQueryRequest request) {
		List<SemanticField> fields = semanticFieldMapper.selectList(request.getTableId(), request.getDatasourceId(),
				request.getTableName(), request.getKeyword(), request.getEnabled());
		return semanticModelConverter.fieldEntityListToVOList(fields);
	}

	@Override
	public SemanticFieldVO getFieldDetail(Long id) {
		return semanticModelConverter.fieldEntityToVO(requireField(id));
	}

	@Override
	public SemanticFieldVO updateField(Long id, SemanticFieldUpdateRequest request) {
		requireField(id);
		SemanticTable table = requireTable(request.getTableId());
		if (semanticFieldMapper.countByTableIdAndFieldNameExcludeId(request.getTableId(), request.getFieldName(), id) > 0) {
			throw new BusinessException("semantic field already exists");
		}
		SemanticField field = semanticModelConverter.fieldUpdateRequestToEntity(request);
		field.setId(id);
		field.setDatasourceId(table.getDatasourceId());
		field.setTableName(table.getTableName());
		field.setPrimaryKey(Boolean.TRUE.equals(field.getPrimaryKey()));
		field.setNullable(field.getNullable() == null || field.getNullable());
		field.setEnabled(field.getEnabled() == null || field.getEnabled());
		field.setUpdatedAt(LocalDateTime.now());
		semanticFieldMapper.updateById(field);
		return semanticModelConverter.fieldEntityToVO(semanticFieldMapper.selectById(id));
	}

	@Override
	public void deleteField(Long id) {
		requireField(id);
		semanticRelationMapper.deleteByFieldId(id);
		semanticFieldMapper.deleteById(id);
	}

	@Override
	public SemanticFieldVO enableField(Long id) {
		return updateFieldEnabled(id, true);
	}

	@Override
	public SemanticFieldVO disableField(Long id) {
		return updateFieldEnabled(id, false);
	}

	@Override
	public SemanticModelVO getSemanticModelByTableId(Long tableId) {
		SemanticTable table = requireTable(tableId);
		return buildSemanticModel(table);
	}

	@Override
	public List<SemanticModelVO> listSemanticModelsByDatasourceId(Long datasourceId) {
		requireDatasource(datasourceId);
		return semanticTableMapper.selectByDatasourceId(datasourceId).stream().map(this::buildSemanticModel).toList();
	}

	private SemanticTableVO updateTableEnabled(Long id, boolean enabled) {
		requireTable(id);
		semanticTableMapper.updateEnabledById(id, enabled);
		return semanticModelConverter.tableEntityToVO(semanticTableMapper.selectById(id));
	}

	private SemanticFieldVO updateFieldEnabled(Long id, boolean enabled) {
		requireField(id);
		semanticFieldMapper.updateEnabledById(id, enabled);
		return semanticModelConverter.fieldEntityToVO(semanticFieldMapper.selectById(id));
	}

	private SemanticModelVO buildSemanticModel(SemanticTable table) {
		SemanticModelVO vo = new SemanticModelVO();
		vo.setTable(semanticModelConverter.tableEntityToVO(table));
		vo.setFields(semanticModelConverter.fieldEntityListToVOList(semanticFieldMapper.selectByTableId(table.getId())));
		return vo;
	}

	private Datasource requireDatasource(Long id) {
		Datasource datasource = datasourceMapper.selectById(id);
		if (datasource == null) {
			throw new BusinessException("datasource with id: %d not found".formatted(id));
		}
		return datasource;
	}

	private SemanticTable requireTable(Long id) {
		SemanticTable table = semanticTableMapper.selectById(id);
		if (table == null) {
			throw new BusinessException("semantic table with id: %d not found".formatted(id));
		}
		return table;
	}

	private SemanticField requireField(Long id) {
		SemanticField field = semanticFieldMapper.selectById(id);
		if (field == null) {
			throw new BusinessException("semantic field with id: %d not found".formatted(id));
		}
		return field;
	}

}
