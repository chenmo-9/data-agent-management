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

import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticRelationUpdateRequest;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticRelationVO;

import java.util.List;

public interface SemanticRelationService {

	SemanticRelationVO createRelation(SemanticRelationCreateRequest request);

	List<SemanticRelationVO> listRelations(SemanticRelationQueryRequest request);

	SemanticRelationVO getRelationDetail(Long id);

	SemanticRelationVO updateRelation(Long id, SemanticRelationUpdateRequest request);

	void deleteRelation(Long id);

	SemanticRelationVO enableRelation(Long id);

	SemanticRelationVO disableRelation(Long id);

	List<SemanticRelationVO> listRelationsByDatasourceId(Long datasourceId);

	List<SemanticRelationVO> listRelationsByTableId(Long tableId);

}
