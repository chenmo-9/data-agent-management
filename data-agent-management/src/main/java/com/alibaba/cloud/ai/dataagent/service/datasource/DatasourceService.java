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

import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceTestRequest;
import com.alibaba.cloud.ai.dataagent.dto.datasource.DatasourceUpdateRequest;
import com.alibaba.cloud.ai.dataagent.vo.datasource.DatasourceTestVO;
import com.alibaba.cloud.ai.dataagent.vo.datasource.DatasourceVO;

import java.util.List;

public interface DatasourceService {

	DatasourceVO create(DatasourceCreateRequest request);

	List<DatasourceVO> list(DatasourceQueryRequest request);

	DatasourceVO getDetail(Long id);

	DatasourceVO update(Long id, DatasourceUpdateRequest request);

	void delete(Long id);

	DatasourceVO enable(Long id);

	DatasourceVO disable(Long id);

	DatasourceTestVO testConnection(DatasourceTestRequest request);

	DatasourceTestVO testConnection(Long id);

}
