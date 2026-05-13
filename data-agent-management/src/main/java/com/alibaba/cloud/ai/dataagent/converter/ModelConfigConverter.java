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

import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigCreateRequest;
import com.alibaba.cloud.ai.dataagent.dto.modelconfig.ModelConfigUpdateRequest;
import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.security.SecretService;
import com.alibaba.cloud.ai.dataagent.vo.modelconfig.ModelConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Model Config Converter
 */
@Component
@RequiredArgsConstructor
public class ModelConfigConverter {

	private final SecretService secretService;

	public ModelConfig createRequestToEntity(ModelConfigCreateRequest request) {
		ModelConfig config = new ModelConfig();
		config.setName(request.getName());
		config.setProvider(request.getProvider());
		config.setModelName(request.getModelName());
		config.setModelType(request.getModelType());
		config.setBaseUrl(request.getBaseUrl());
		config.setApiKey(request.getApiKey());
		config.setTemperature(request.getTemperature());
		config.setMaxTokens(request.getMaxTokens());
		config.setEnabled(request.getEnabled());
		config.setDescription(request.getDescription());
		return config;
	}

	public ModelConfig updateRequestToEntity(ModelConfigUpdateRequest request) {
		ModelConfig config = new ModelConfig();
		config.setName(request.getName());
		config.setProvider(request.getProvider());
		config.setModelName(request.getModelName());
		config.setModelType(request.getModelType());
		config.setBaseUrl(request.getBaseUrl());
		config.setApiKey(request.getApiKey());
		config.setTemperature(request.getTemperature());
		config.setMaxTokens(request.getMaxTokens());
		config.setEnabled(request.getEnabled());
		config.setDescription(request.getDescription());
		return config;
	}

	public ModelConfigVO entityToVO(ModelConfig config) {
		if (config == null) {
			return null;
		}
		ModelConfigVO vo = new ModelConfigVO();
		vo.setId(config.getId());
		vo.setName(config.getName());
		vo.setProvider(config.getProvider());
		vo.setModelName(config.getModelName());
		vo.setModelType(config.getModelType());
		vo.setBaseUrl(config.getBaseUrl());
		vo.setHasApiKey(config.getApiKey() != null && !config.getApiKey().isBlank());
		vo.setMaskedApiKey(secretService.maskSecret(config.getApiKey()));
		vo.setTemperature(config.getTemperature());
		vo.setMaxTokens(config.getMaxTokens());
		vo.setEnabled(config.getEnabled());
		vo.setDescription(config.getDescription());
		vo.setCreatedAt(config.getCreatedAt());
		vo.setUpdatedAt(config.getUpdatedAt());
		return vo;
	}

	public List<ModelConfigVO> entityListToVOList(List<ModelConfig> configs) {
		return configs.stream().map(this::entityToVO).toList();
	}

}
