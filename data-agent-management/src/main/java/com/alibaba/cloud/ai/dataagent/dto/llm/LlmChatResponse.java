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
package com.alibaba.cloud.ai.dataagent.dto.llm;

import lombok.Data;

@Data
public class LlmChatResponse {

	private Long modelConfigId;

	private String provider;

	private String modelName;

	private String content;

	private Boolean success;

	private String message;

	public static LlmChatResponse success(Long modelConfigId, String provider, String modelName, String content) {
		LlmChatResponse response = new LlmChatResponse();
		response.setModelConfigId(modelConfigId);
		response.setProvider(provider);
		response.setModelName(modelName);
		response.setContent(content);
		response.setSuccess(true);
		response.setMessage("success");
		return response;
	}

	public static LlmChatResponse error(Long modelConfigId, String provider, String modelName, String message) {
		LlmChatResponse response = new LlmChatResponse();
		response.setModelConfigId(modelConfigId);
		response.setProvider(provider);
		response.setModelName(modelName);
		response.setContent("");
		response.setSuccess(false);
		response.setMessage(message);
		return response;
	}

}
