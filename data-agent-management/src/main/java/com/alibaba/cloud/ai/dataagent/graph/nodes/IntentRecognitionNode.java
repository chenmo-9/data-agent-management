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
package com.alibaba.cloud.ai.dataagent.graph.nodes;

import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.nl2sql.IntentRecognizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class IntentRecognitionNode implements GraphNode {

	private final IntentRecognizer intentRecognizer;

	@Override
	public String name() {
		return "intent_recognition";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		emitter.emitNodeStart(name(), "Recognizing intent", Map.of("question", state.getQuestion()));
		if (isUnsafeOperation(state.getQuestion())) {
			String answer = "检测到危险操作意图，系统只允许 SELECT 只读查询，已拒绝执行。";
			state.setIntent("unsafe_operation");
			state.setUnsafeOperation(true);
			state.setSkipSql(true);
			state.setSuccess(false);
			state.setAnswer(answer);
			state.setErrorMessage(answer);
			state.setSqlSecurityMessage("Unsafe user intent blocked before SQL generation");
			emitter.emitNodeEnd(name(), "Unsafe user intent blocked",
					Map.of("intent", "unsafe_operation", "blocked", true, "sqlSecurityMessage", state.getSqlSecurityMessage()));
			return;
		}
		String intent = intentRecognizer.recognize(state.getQuestion());
		state.setIntent(intent);
		boolean businessRuleCandidate = isBusinessRuleCandidate(state.getQuestion());
		state.setBusinessRuleCandidate(businessRuleCandidate);
		emitter.emitNodeEnd(name(), "Intent recognized", Map.of("intent", intent, "businessRuleCandidate", businessRuleCandidate));
	}

	private boolean isBusinessRuleCandidate(String question) {
		if (question == null || question.isBlank()) {
			return false;
		}
		return containsAny(question, "是否", "计不计入", "是否计入", "要不要", "规则", "口径", "是否排除", "退款", "不计入", "包含", "不包含");
	}

	private boolean isUnsafeOperation(String question) {
		if (question == null || question.isBlank()) {
			return false;
		}
		String normalized = question.toLowerCase();
		return containsAny(normalized, "删除", "清空", "修改", "更新", "插入", "新增", "写入", "删除所有", "清空所有", "删库", "建表",
				"改表", "授权", "删除订单", "delete", "drop", "truncate", "update", "insert", "alter", "create",
				"grant", "revoke", "replace", "merge");
	}

	private boolean containsAny(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

}
