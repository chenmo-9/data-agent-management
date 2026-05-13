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
package com.alibaba.cloud.ai.dataagent.nl2sql;

import com.alibaba.cloud.ai.dataagent.dto.llm.LlmChatRequest;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.prompt.PromptConstant;
import com.alibaba.cloud.ai.dataagent.service.llm.LlmService;
import com.alibaba.cloud.ai.dataagent.vo.llm.LlmChatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlRepairer {

	private final SqlExtractor sqlExtractor;

	private final SqlValidator sqlValidator;

	private final LlmService llmService;

	public SqlRepairResult repairWithRules(String sql, String errorMessage, String schemaContext) {
		String repaired = sqlExtractor.extract(clean(sql));
		if (!StringUtils.hasText(repaired)) {
			return failed("rules", "Rule repair failed: no SELECT SQL extracted");
		}
		try {
			return success("rules", sqlValidator.validate(repaired), "Rule repair succeeded");
		}
		catch (BusinessException ex) {
			return failed("rules", "Rule repair failed: " + ex.getMessage());
		}
	}

	public SqlRepairResult repairWithLlm(Long modelConfigId, String question, String schemaContext,
			String knowledgeContext, String badSql, String errorMessage) {
		try {
			LlmChatRequest request = new LlmChatRequest();
			request.setModelConfigId(modelConfigId);
			request.setPromptKey(PromptConstant.SQL_REPAIR);
			request.setPromptVersion("v1");
			Map<String, Object> variables = new HashMap<>();
			variables.put("question", question);
			variables.put("schema", schemaContext);
			variables.put("knowledge", knowledgeContext);
			variables.put("bad_sql", badSql);
			variables.put("error", errorMessage);
			request.setVariables(variables);
			request.setUserMessage("请只返回修复后的 SELECT SQL，不要解释。");
			LlmChatVO response = llmService.chat(request);
			String extracted = sqlExtractor.extract(response.getContent());
			if (!StringUtils.hasText(extracted)) {
				return failed("llm", "LLM repair failed: no SELECT SQL extracted");
			}
			return success("llm", sqlValidator.validate(extracted), "LLM repair succeeded");
		}
		catch (Exception ex) {
			return failed("llm", "LLM repair failed: " + ex.getMessage());
		}
	}

	private String clean(String sql) {
		if (sql == null) {
			return "";
		}
		return sql.replace("```sql", "")
			.replace("```SQL", "")
			.replace("```", "")
			.replaceFirst("(?is)^\\s*(sql|SQL|查询语句)\\s*[:：]\\s*", "")
			.replace("。", "")
			.trim();
	}

	private SqlRepairResult success(String source, String sql, String message) {
		return SqlRepairResult.builder().success(true).source(source).sql(sql).message(message).build();
	}

	private SqlRepairResult failed(String source, String message) {
		return SqlRepairResult.builder().success(false).source(source).message(message).build();
	}

}
