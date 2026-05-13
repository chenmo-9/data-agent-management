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

import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.PromptTemplate;
import com.alibaba.cloud.ai.dataagent.vo.prompt.PromptTemplateVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PromptTemplateConverter {

	public PromptTemplate createRequestToEntity(PromptTemplateCreateRequest request) {
		if (request == null) {
			return null;
		}
		PromptTemplate template = new PromptTemplate();
		template.setPromptKey(request.getPromptKey());
		template.setName(request.getName());
		template.setScene(request.getScene());
		template.setContent(request.getContent());
		template.setVersion(request.getVersion());
		template.setEnabled(request.getEnabled());
		template.setDescription(request.getDescription());
		return template;
	}

	public PromptTemplate updateRequestToEntity(PromptTemplateUpdateRequest request) {
		if (request == null) {
			return null;
		}
		PromptTemplate template = new PromptTemplate();
		template.setPromptKey(request.getPromptKey());
		template.setName(request.getName());
		template.setScene(request.getScene());
		template.setContent(request.getContent());
		template.setVersion(request.getVersion());
		template.setEnabled(request.getEnabled());
		template.setDescription(request.getDescription());
		return template;
	}

	public PromptTemplateVO entityToVO(PromptTemplate entity) {
		if (entity == null) {
			return null;
		}
		PromptTemplateVO vo = new PromptTemplateVO();
		vo.setId(entity.getId());
		vo.setPromptKey(entity.getPromptKey());
		vo.setName(entity.getName());
		vo.setScene(entity.getScene());
		vo.setContent(entity.getContent());
		vo.setVersion(entity.getVersion());
		vo.setEnabled(entity.getEnabled());
		vo.setDescription(entity.getDescription());
		vo.setCreatedAt(entity.getCreatedAt());
		vo.setUpdatedAt(entity.getUpdatedAt());
		return vo;
	}

	public List<PromptTemplateVO> entityListToVOList(List<PromptTemplate> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		return entities.stream().map(this::entityToVO).toList();
	}

}
