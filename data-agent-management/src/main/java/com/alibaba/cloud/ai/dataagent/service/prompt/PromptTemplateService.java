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
package com.alibaba.cloud.ai.dataagent.service.prompt;

import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptRenderRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateUpdateRequest;
import com.alibaba.cloud.ai.dataagent.vo.prompt.PromptRenderVO;
import com.alibaba.cloud.ai.dataagent.vo.prompt.PromptTemplateVO;

import java.util.List;

public interface PromptTemplateService {

	PromptTemplateVO create(PromptTemplateCreateRequest request);

	List<PromptTemplateVO> list(PromptTemplateQueryRequest request);

	PromptTemplateVO getDetail(Long id);

	PromptTemplateVO update(Long id, PromptTemplateUpdateRequest request);

	void delete(Long id);

	PromptTemplateVO enable(Long id);

	PromptTemplateVO disable(Long id);

	PromptRenderVO render(PromptRenderRequest request);

	List<PromptTemplateVO> initDefaultPrompts();

}
