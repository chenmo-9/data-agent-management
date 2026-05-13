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

import com.alibaba.cloud.ai.dataagent.converter.PromptTemplateConverter;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptRenderRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.prompt.PromptTemplateUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.PromptTemplate;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.PromptTemplateMapper;
import com.alibaba.cloud.ai.dataagent.prompt.DefaultPrompts;
import com.alibaba.cloud.ai.dataagent.prompt.PromptConstant;
import com.alibaba.cloud.ai.dataagent.prompt.PromptRenderer;
import com.alibaba.cloud.ai.dataagent.vo.prompt.PromptRenderVO;
import com.alibaba.cloud.ai.dataagent.vo.prompt.PromptTemplateVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Prompt Template Service Class
 */
@Slf4j
@Service
@AllArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {

	private static final String DEFAULT_VERSION = "v1";

	private final PromptTemplateMapper promptTemplateMapper;

	private final PromptTemplateConverter promptTemplateConverter;

	private final PromptRenderer promptRenderer;

	@Override
	public PromptTemplateVO create(PromptTemplateCreateRequest request) {
		PromptTemplate template = promptTemplateConverter.createRequestToEntity(request);
		prepareTemplate(template);
		if (promptTemplateMapper.countByPromptKeyAndVersion(template.getPromptKey(), template.getVersion()) > 0) {
			throw new BusinessException("prompt template already exists: " + template.getPromptKey() + " " + template.getVersion());
		}
		LocalDateTime now = LocalDateTime.now();
		template.setCreatedAt(now);
		template.setUpdatedAt(now);
		promptTemplateMapper.insert(template);
		return promptTemplateConverter.entityToVO(template);
	}

	@Override
	public List<PromptTemplateVO> list(PromptTemplateQueryRequest request) {
		List<PromptTemplate> templates = promptTemplateMapper.selectList(request.getKeyword(),
				normalizeOptional(request.getPromptKey()), normalizeOptional(request.getScene()), request.getEnabled());
		return promptTemplateConverter.entityListToVOList(templates);
	}

	@Override
	public PromptTemplateVO getDetail(Long id) {
		return promptTemplateConverter.entityToVO(requireTemplate(id));
	}

	@Override
	public PromptTemplateVO update(Long id, PromptTemplateUpdateRequest request) {
		requireTemplate(id);
		PromptTemplate template = promptTemplateConverter.updateRequestToEntity(request);
		prepareTemplate(template);
		if (promptTemplateMapper.countByPromptKeyAndVersionExcludeId(template.getPromptKey(), template.getVersion(), id) > 0) {
			throw new BusinessException("prompt template already exists: " + template.getPromptKey() + " " + template.getVersion());
		}
		template.setId(id);
		template.setUpdatedAt(LocalDateTime.now());
		promptTemplateMapper.updateById(template);
		return promptTemplateConverter.entityToVO(promptTemplateMapper.selectById(id));
	}

	@Override
	public void delete(Long id) {
		requireTemplate(id);
		promptTemplateMapper.deleteById(id);
	}

	@Override
	public PromptTemplateVO enable(Long id) {
		return updateEnabled(id, true);
	}

	@Override
	public PromptTemplateVO disable(Long id) {
		return updateEnabled(id, false);
	}

	@Override
	public PromptRenderVO render(PromptRenderRequest request) {
		PromptTemplate template;
		if (request.getTemplateId() != null) {
			template = requireTemplate(request.getTemplateId());
		}
		else {
			String promptKey = normalizeRequired(request.getPromptKey(), "promptKey cannot be blank");
			String version = normalizeVersion(request.getVersion());
			template = promptTemplateMapper.selectByPromptKeyAndVersion(promptKey, version);
			if (template == null) {
				throw new BusinessException("prompt template not found: " + promptKey + " " + version);
			}
		}
		PromptRenderVO vo = new PromptRenderVO();
		vo.setTemplateId(template.getId());
		vo.setPromptKey(template.getPromptKey());
		vo.setVersion(template.getVersion());
		vo.setRenderedContent(promptRenderer.render(template.getContent(), request.getVariables()));
		return vo;
	}

	@Override
	public List<PromptTemplateVO> initDefaultPrompts() {
		List<PromptTemplate> defaults = List.of(
				defaultTemplate(PromptConstant.INTENT_RECOGNITION, "意图识别 Prompt", "nl2sql",
						DefaultPrompts.defaultIntentRecognitionPrompt(), "识别用户问题意图"),
				defaultTemplate(PromptConstant.SCHEMA_RECALL, "Schema Recall Prompt", "nl2sql",
						DefaultPrompts.defaultSchemaRecallPrompt(), "根据问题召回相关表字段"),
				defaultTemplate(PromptConstant.SQL_GENERATE, "SQL 生成 Prompt", "nl2sql",
						DefaultPrompts.defaultSqlGeneratePrompt(), "生成只读 SQL"),
				defaultTemplate(PromptConstant.SQL_REPAIR, "SQL 修复 Prompt", "nl2sql",
						DefaultPrompts.defaultSqlRepairPrompt(), "修复模型输出的 SELECT SQL"),
				defaultTemplate(PromptConstant.REPORT_GENERATE, "报告生成 Prompt", "report",
						DefaultPrompts.defaultReportGeneratePrompt(), "生成分析报告"));
		List<PromptTemplate> created = new ArrayList<>();
		for (PromptTemplate template : defaults) {
			if (promptTemplateMapper.countByPromptKeyAndVersion(template.getPromptKey(), template.getVersion()) > 0) {
				continue;
			}
			promptTemplateMapper.insert(template);
			created.add(template);
		}
		return promptTemplateConverter.entityListToVOList(created);
	}

	private PromptTemplateVO updateEnabled(Long id, boolean enabled) {
		requireTemplate(id);
		promptTemplateMapper.updateEnabledById(id, enabled);
		return promptTemplateConverter.entityToVO(promptTemplateMapper.selectById(id));
	}

	private PromptTemplate defaultTemplate(String promptKey, String name, String scene, String content,
			String description) {
		LocalDateTime now = LocalDateTime.now();
		PromptTemplate template = new PromptTemplate();
		template.setPromptKey(promptKey);
		template.setName(name);
		template.setScene(scene);
		template.setContent(content);
		template.setVersion(DEFAULT_VERSION);
		template.setEnabled(true);
		template.setDescription(description);
		template.setCreatedAt(now);
		template.setUpdatedAt(now);
		return template;
	}

	private PromptTemplate requireTemplate(Long id) {
		PromptTemplate template = promptTemplateMapper.selectById(id);
		if (template == null) {
			throw new BusinessException("prompt template with id: %d not found".formatted(id));
		}
		return template;
	}

	private void prepareTemplate(PromptTemplate template) {
		template.setPromptKey(normalizeRequired(template.getPromptKey(), "promptKey cannot be blank"));
		template.setScene(normalizeOptional(template.getScene()));
		template.setVersion(normalizeVersion(template.getVersion()));
		template.setEnabled(template.getEnabled() == null || template.getEnabled());
	}

	private String normalizeVersion(String version) {
		return version == null || version.isBlank() ? DEFAULT_VERSION : version.trim();
	}

	private String normalizeRequired(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new BusinessException(message);
		}
		return value.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
	}

}
