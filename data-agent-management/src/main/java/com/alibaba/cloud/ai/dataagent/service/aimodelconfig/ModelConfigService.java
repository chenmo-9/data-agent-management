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
package com.alibaba.cloud.ai.dataagent.service.aimodelconfig;

import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigUpdateRequest;
import com.alibaba.cloud.ai.dataagent.vo.modelconfig.ModelConfigVO;

import java.util.List;

public interface ModelConfigService {

	ModelConfigVO create(ModelConfigCreateRequest request);

	List<ModelConfigVO> list(ModelConfigQueryRequest request);

	ModelConfigVO getDetail(Long id);

	ModelConfigVO update(Long id, ModelConfigUpdateRequest request);

	void delete(Long id);

	ModelConfigVO enable(Long id);

	ModelConfigVO disable(Long id);

}
