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

import com.alibaba.cloud.ai.dataagent.converter.SemanticRelationConverter;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.entity.SemanticField;
import com.alibaba.cloud.ai.dataagent.entity.SemanticRelation;
import com.alibaba.cloud.ai.dataagent.entity.SemanticTable;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.DatasourceMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticFieldMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticRelationMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticTableMapper;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticRelationVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@AllArgsConstructor
public class SemanticRelationServiceImpl implements SemanticRelationService {

	private static final String DEFAULT_JOIN_TYPE = "INNER JOIN";

	private static final String DEFAULT_RELATION_TYPE = "logical";

	private final SemanticRelationMapper semanticRelationMapper;

	private final DatasourceMapper datasourceMapper;

	private final SemanticTableMapper semanticTableMapper;

	private final SemanticFieldMapper semanticFieldMapper;

	private final SemanticRelationConverter semanticRelationConverter;

	@Override
	public SemanticRelationVO createRelation(SemanticRelationCreateRequest request) {
		ResolvedRelation resolvedRelation = validateAndResolve(request.getDatasourceId(), request.getSourceTableId(),
				request.getSourceFieldId(), request.getTargetTableId(), request.getTargetFieldId(), null);
		SemanticRelation relation = semanticRelationConverter.createRequestToEntity(request);
		fillResolvedFields(relation, resolvedRelation);
		prepareDefaults(relation);
		LocalDateTime now = LocalDateTime.now();
		relation.setCreatedAt(now);
		relation.setUpdatedAt(now);
		semanticRelationMapper.insert(relation);
		return semanticRelationConverter.entityToVO(relation);
	}

	@Override
	public List<SemanticRelationVO> listRelations(SemanticRelationQueryRequest request) {
		return semanticRelationConverter.entityListToVOList(
				semanticRelationMapper.selectList(request.getDatasourceId(), request.getTableId(), request.getEnabled(),
						request.getKeyword()));
	}

	@Override
	public SemanticRelationVO getRelationDetail(Long id) {
		return semanticRelationConverter.entityToVO(requireRelation(id));
	}

	@Override
	public SemanticRelationVO updateRelation(Long id, SemanticRelationUpdateRequest request) {
		requireRelation(id);
		ResolvedRelation resolvedRelation = validateAndResolve(request.getDatasourceId(), request.getSourceTableId(),
				request.getSourceFieldId(), request.getTargetTableId(), request.getTargetFieldId(), id);
		SemanticRelation relation = semanticRelationConverter.updateRequestToEntity(request);
		relation.setId(id);
		fillResolvedFields(relation, resolvedRelation);
		prepareDefaults(relation);
		relation.setUpdatedAt(LocalDateTime.now());
		semanticRelationMapper.updateById(relation);
		return semanticRelationConverter.entityToVO(semanticRelationMapper.selectById(id));
	}

	@Override
	public void deleteRelation(Long id) {
		requireRelation(id);
		semanticRelationMapper.deleteById(id);
	}

	@Override
	public SemanticRelationVO enableRelation(Long id) {
		requireRelation(id);
		semanticRelationMapper.enableById(id);
		return semanticRelationConverter.entityToVO(semanticRelationMapper.selectById(id));
	}

	@Override
	public SemanticRelationVO disableRelation(Long id) {
		requireRelation(id);
		semanticRelationMapper.disableById(id);
		return semanticRelationConverter.entityToVO(semanticRelationMapper.selectById(id));
	}

	@Override
	public List<SemanticRelationVO> listRelationsByDatasourceId(Long datasourceId) {
		requireDatasource(datasourceId);
		return semanticRelationConverter.entityListToVOList(semanticRelationMapper.selectByDatasourceId(datasourceId));
	}

	@Override
	public List<SemanticRelationVO> listRelationsByTableId(Long tableId) {
		SemanticTable table = requireTable(tableId);
		return semanticRelationConverter.entityListToVOList(
				semanticRelationMapper.selectList(table.getDatasourceId(), tableId, null, null));
	}

	private void prepareDefaults(SemanticRelation relation) {
		relation.setJoinType(normalizeJoinType(relation.getJoinType()));
		relation.setRelationType(normalizeRelationType(relation.getRelationType()));
		relation.setEnabled(relation.getEnabled() == null || relation.getEnabled());
	}

	private String normalizeJoinType(String joinType) {
		if (!StringUtils.hasText(joinType)) {
			return DEFAULT_JOIN_TYPE;
		}
		return joinType.trim().toUpperCase(Locale.ROOT);
	}

	private String normalizeRelationType(String relationType) {
		if (!StringUtils.hasText(relationType)) {
			return DEFAULT_RELATION_TYPE;
		}
		return relationType.trim().toLowerCase(Locale.ROOT);
	}

	private void fillResolvedFields(SemanticRelation relation, ResolvedRelation resolvedRelation) {
		relation.setDatasourceId(resolvedRelation.datasource().getId());
		relation.setSourceTableId(resolvedRelation.sourceTable().getId());
		relation.setSourceTableName(resolvedRelation.sourceTable().getTableName());
		relation.setSourceFieldId(resolvedRelation.sourceField().getId());
		relation.setSourceFieldName(resolvedRelation.sourceField().getFieldName());
		relation.setTargetTableId(resolvedRelation.targetTable().getId());
		relation.setTargetTableName(resolvedRelation.targetTable().getTableName());
		relation.setTargetFieldId(resolvedRelation.targetField().getId());
		relation.setTargetFieldName(resolvedRelation.targetField().getFieldName());
	}

	private ResolvedRelation validateAndResolve(Long datasourceId, Long sourceTableId, Long sourceFieldId,
			Long targetTableId, Long targetFieldId, Long currentId) {
		Datasource datasource = requireDatasource(datasourceId);
		SemanticTable sourceTable = requireTable(sourceTableId);
		SemanticTable targetTable = requireTable(targetTableId);
		SemanticField sourceField = requireField(sourceFieldId);
		SemanticField targetField = requireField(targetFieldId);
		if (!datasource.getId().equals(sourceTable.getDatasourceId()) || !datasource.getId().equals(targetTable.getDatasourceId())) {
			throw new BusinessException("sourceTable and targetTable must belong to datasource: " + datasourceId);
		}
		if (!sourceTable.getId().equals(sourceField.getTableId())) {
			throw new BusinessException("sourceField does not belong to sourceTable");
		}
		if (!targetTable.getId().equals(targetField.getTableId())) {
			throw new BusinessException("targetField does not belong to targetTable");
		}
		if (sourceTableId.equals(targetTableId) && sourceFieldId.equals(targetFieldId)) {
			throw new BusinessException("source and target relation endpoints cannot be identical");
		}
		int duplicates = currentId == null
				? semanticRelationMapper.countDuplicate(datasourceId, sourceTableId, sourceFieldId, targetTableId, targetFieldId)
				: semanticRelationMapper.countDuplicateExcludeId(currentId, datasourceId, sourceTableId, sourceFieldId,
						targetTableId, targetFieldId);
		if (duplicates > 0) {
			throw new BusinessException("semantic relation already exists");
		}
		return new ResolvedRelation(datasource, sourceTable, sourceField, targetTable, targetField);
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

	private SemanticRelation requireRelation(Long id) {
		SemanticRelation relation = semanticRelationMapper.selectById(id);
		if (relation == null) {
			throw new BusinessException("semantic relation with id: %d not found".formatted(id));
		}
		return relation;
	}

	private record ResolvedRelation(Datasource datasource, SemanticTable sourceTable, SemanticField sourceField,
			SemanticTable targetTable, SemanticField targetField) {
	}

}
