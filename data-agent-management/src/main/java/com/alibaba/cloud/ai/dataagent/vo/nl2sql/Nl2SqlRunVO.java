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
package com.alibaba.cloud.ai.dataagent.vo.nl2sql;

import com.alibaba.cloud.ai.dataagent.dto.graph.GraphEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nl2SqlRunVO {

	private String sessionId;

	private String question;

	private String intent;

	private Long datasourceId;

	private String schemaContext;

	private String knowledgeContext;

	private String generatedSql;

	private String validatedSql;

	private List<Map<String, Object>> sqlResult;

	private Integer rowCount;

	private String answer;

	private Boolean success;

	private String message;

	private List<GraphEventDTO> events;

}
