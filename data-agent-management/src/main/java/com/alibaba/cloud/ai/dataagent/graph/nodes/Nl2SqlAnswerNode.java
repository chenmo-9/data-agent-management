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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Nl2SqlAnswerNode implements GraphNode {

	@Override
	public String name() {
		return "nl2sql_answer";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (Boolean.TRUE.equals(state.getSkipSql()) && state.getAnswer() != null && !state.getAnswer().isBlank()) {
			state.setSuccess(true);
			emitter.emitNodeEnd(name(), "Knowledge answer built", Map.of("answer", state.getAnswer(), "intent", state.getIntent()));
			return;
		}
		if (!"data_query".equals(state.getIntent())) {
			state.setAnswer("当前问题不是数据查询，请切换到 chat 模式或提出数据统计类问题。");
			state.setSuccess(true);
			emitter.emitMessage(name(), state.getAnswer(), Map.of("intent", state.getIntent()));
			return;
		}
		emitter.emitNodeStart(name(), "Building answer", Map.of("rowCount", defaultNumber(state.getRowCount())));
		if (state.getSqlError() != null) {
			state.setAnswer("SQL 执行失败：" + state.getSqlError());
			emitter.emitNodeEnd(name(), "Answer built with SQL error", Map.of("message", state.getAnswer()));
			return;
		}
		String reportAnswer = resolveReportAnswer(state);
		if (reportAnswer != null && !reportAnswer.isBlank()) {
			state.setAnswer(reportAnswer);
			emitter.emitNodeEnd(name(), "Answer built with report", Map.of("answer", state.getAnswer()));
			return;
		}
		state.setAnswer(buildAnswer(state.getSqlResult(), defaultNumber(state.getRowCount())));
		emitter.emitNodeEnd(name(), "Answer built", Map.of("answer", state.getAnswer()));
	}

	private String buildAnswer(List<Map<String, Object>> rows, int rowCount) {
		if (rows != null && rows.size() == 1 && rows.get(0).size() == 1) {
			Map.Entry<String, Object> entry = rows.get(0).entrySet().iterator().next();
			return "查询结果：" + entry.getKey() + " = " + entry.getValue();
		}
		return "查询成功，返回 " + rowCount + " 行数据。";
	}

	private int defaultNumber(Integer value) {
		return value == null ? 0 : value;
	}

	private String resolveReportAnswer(GraphState state) {
		if (state.getReportSummary() != null && !state.getReportSummary().isBlank()) {
			return state.getReportSummary();
		}
		if (state.getReportResult() != null && state.getReportResult().getSummary() != null
				&& !state.getReportResult().getSummary().isBlank()) {
			return state.getReportResult().getSummary();
		}
		if (state.getReportMarkdown() != null && !state.getReportMarkdown().isBlank()) {
			return "查询完成，已生成分析报告。";
		}
		return null;
	}

}
