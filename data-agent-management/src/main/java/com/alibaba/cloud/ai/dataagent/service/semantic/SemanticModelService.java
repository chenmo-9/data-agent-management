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

import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticFieldUpdateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.semantic.SemanticTableUpdateRequest;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticFieldVO;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticModelVO;
import com.alibaba.cloud.ai.dataagent.vo.semantic.SemanticTableVO;

import java.util.List;

public interface SemanticModelService {

	SemanticTableVO createTable(SemanticTableCreateRequest request);

	List<SemanticTableVO> listTables(SemanticTableQueryRequest request);

	SemanticTableVO getTableDetail(Long id);

	SemanticTableVO updateTable(Long id, SemanticTableUpdateRequest request);

	void deleteTable(Long id);

	SemanticTableVO enableTable(Long id);

	SemanticTableVO disableTable(Long id);

	SemanticFieldVO createField(SemanticFieldCreateRequest request);

	List<SemanticFieldVO> listFields(SemanticFieldQueryRequest request);

	SemanticFieldVO getFieldDetail(Long id);

	SemanticFieldVO updateField(Long id, SemanticFieldUpdateRequest request);

	void deleteField(Long id);

	SemanticFieldVO enableField(Long id);

	SemanticFieldVO disableField(Long id);

	SemanticModelVO getSemanticModelByTableId(Long tableId);

	List<SemanticModelVO> listSemanticModelsByDatasourceId(Long datasourceId);

}
