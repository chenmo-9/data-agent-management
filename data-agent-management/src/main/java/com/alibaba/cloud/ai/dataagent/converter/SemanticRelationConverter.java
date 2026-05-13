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

import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.SemanticRelation;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticRelationVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SemanticRelationConverter {

	public SemanticRelation createRequestToEntity(SemanticRelationCreateRequest request) {
		if (request == null) {
			return null;
		}
		SemanticRelation relation = new SemanticRelation();
		relation.setDatasourceId(request.getDatasourceId());
		relation.setSourceTableId(request.getSourceTableId());
		relation.setSourceFieldId(request.getSourceFieldId());
		relation.setTargetTableId(request.getTargetTableId());
		relation.setTargetFieldId(request.getTargetFieldId());
		relation.setRelationType(request.getRelationType());
		relation.setJoinType(request.getJoinType());
		relation.setDescription(request.getDescription());
		relation.setEnabled(request.getEnabled());
		return relation;
	}

	public SemanticRelation updateRequestToEntity(SemanticRelationUpdateRequest request) {
		if (request == null) {
			return null;
		}
		SemanticRelation relation = new SemanticRelation();
		relation.setDatasourceId(request.getDatasourceId());
		relation.setSourceTableId(request.getSourceTableId());
		relation.setSourceFieldId(request.getSourceFieldId());
		relation.setTargetTableId(request.getTargetTableId());
		relation.setTargetFieldId(request.getTargetFieldId());
		relation.setRelationType(request.getRelationType());
		relation.setJoinType(request.getJoinType());
		relation.setDescription(request.getDescription());
		relation.setEnabled(request.getEnabled());
		return relation;
	}

	public SemanticRelationVO entityToVO(SemanticRelation entity) {
		if (entity == null) {
			return null;
		}
		SemanticRelationVO vo = new SemanticRelationVO();
		vo.setId(entity.getId());
		vo.setDatasourceId(entity.getDatasourceId());
		vo.setSourceTableId(entity.getSourceTableId());
		vo.setSourceTableName(entity.getSourceTableName());
		vo.setSourceFieldId(entity.getSourceFieldId());
		vo.setSourceFieldName(entity.getSourceFieldName());
		vo.setTargetTableId(entity.getTargetTableId());
		vo.setTargetTableName(entity.getTargetTableName());
		vo.setTargetFieldId(entity.getTargetFieldId());
		vo.setTargetFieldName(entity.getTargetFieldName());
		vo.setRelationType(entity.getRelationType());
		vo.setJoinType(entity.getJoinType());
		vo.setDescription(entity.getDescription());
		vo.setEnabled(entity.getEnabled());
		vo.setCreatedAt(entity.getCreatedAt());
		vo.setUpdatedAt(entity.getUpdatedAt());
		return vo;
	}

	public List<SemanticRelationVO> entityListToVOList(List<SemanticRelation> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		return entities.stream().map(this::entityToVO).toList();
	}

}
