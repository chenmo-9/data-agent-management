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

import com.alibaba.cloud.ai.dataagent.converter.ModelConfigConverter;
import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigQueryRequest;
import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.ModelConfigMapper;
import com.alibaba.cloud.ai.dataagent.security.SecretService;
import com.alibaba.cloud.ai.dataagent.vo.modelconfig.ModelConfigVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Model Config Service Class
 */
@Slf4j
@Service
@AllArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {

	private final ModelConfigMapper modelConfigMapper;

	private final ModelConfigConverter modelConfigConverter;

	private final SecretService secretService;

	@Override
	public ModelConfigVO create(ModelConfigCreateRequest request) {
		validateModelType(request.getModelType());
		if (modelConfigMapper.countByName(request.getName()) > 0) {
			throw new BusinessException("model config name already exists: " + request.getName());
		}
		ModelConfig config = modelConfigConverter.createRequestToEntity(request);
		LocalDateTime now = LocalDateTime.now();
		config.setModelType(normalizeModelType(config.getModelType()));
		config.setApiKey(secretService.encryptIfNeeded(config.getApiKey()));
		config.setEnabled(config.getEnabled() == null || config.getEnabled());
		config.setCreatedAt(now);
		config.setUpdatedAt(now);
		modelConfigMapper.insert(config);
		return modelConfigConverter.entityToVO(config);
	}

	@Override
	public List<ModelConfigVO> list(ModelConfigQueryRequest request) {
		String modelType = normalizeOptionalModelType(request.getModelType());
		List<ModelConfig> configs = modelConfigMapper.selectList(request.getKeyword(), modelType, request.getEnabled());
		return modelConfigConverter.entityListToVOList(configs);
	}

	@Override
	public ModelConfigVO getDetail(Long id) {
		return modelConfigConverter.entityToVO(requireModelConfig(id));
	}

	@Override
	public ModelConfigVO update(Long id, ModelConfigUpdateRequest request) {
		ModelConfig existing = requireModelConfig(id);
		validateModelType(request.getModelType());
		if (modelConfigMapper.countByNameExcludeId(request.getName(), id) > 0) {
			throw new BusinessException("model config name already exists: " + request.getName());
		}
		ModelConfig config = modelConfigConverter.updateRequestToEntity(request);
		config.setId(id);
		config.setModelType(normalizeModelType(config.getModelType()));
		if (secretService.isPlaceholder(config.getApiKey())) {
			config.setApiKey(existing.getApiKey());
		}
		else {
			config.setApiKey(secretService.encryptIfNeeded(config.getApiKey()));
		}
		config.setEnabled(config.getEnabled() == null || config.getEnabled());
		config.setUpdatedAt(LocalDateTime.now());
		modelConfigMapper.updateById(config);
		return modelConfigConverter.entityToVO(modelConfigMapper.selectById(id));
	}

	@Override
	public void delete(Long id) {
		requireModelConfig(id);
		modelConfigMapper.deleteById(id);
	}

	@Override
	public ModelConfigVO enable(Long id) {
		return updateEnabled(id, true);
	}

	@Override
	public ModelConfigVO disable(Long id) {
		return updateEnabled(id, false);
	}

	private ModelConfigVO updateEnabled(Long id, boolean enabled) {
		ModelConfig config = requireModelConfig(id);
		config.setEnabled(enabled);
		config.setUpdatedAt(LocalDateTime.now());
		modelConfigMapper.updateById(config);
		return modelConfigConverter.entityToVO(modelConfigMapper.selectById(id));
	}

	private ModelConfig requireModelConfig(Long id) {
		ModelConfig config = modelConfigMapper.selectById(id);
		if (config == null) {
			throw new BusinessException("model config with id: %d not found".formatted(id));
		}
		return config;
	}

	private void validateModelType(String modelType) {
		String normalized = normalizeModelType(modelType);
		if (!"chat".equals(normalized) && !"embedding".equals(normalized)) {
			throw new BusinessException("modelType must be chat or embedding");
		}
	}

	private String normalizeOptionalModelType(String modelType) {
		if (modelType == null || modelType.isBlank()) {
			return null;
		}
		validateModelType(modelType);
		return normalizeModelType(modelType);
	}

	private String normalizeModelType(String modelType) {
		return modelType == null ? null : modelType.trim().toLowerCase(Locale.ROOT);
	}

}
