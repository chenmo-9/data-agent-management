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
import com.alibaba.cloud.ai.dataagent.nl2sql.KnowledgeContextBuilder;
import com.alibaba.cloud.ai.dataagent.rag.KnowledgeRecallItem;
import com.alibaba.cloud.ai.dataagent.rag.KnowledgeRecallResult;
import com.alibaba.cloud.ai.dataagent.report.ChartSpec;
import com.alibaba.cloud.ai.dataagent.report.ReportResult;
import com.alibaba.cloud.ai.dataagent.service.rag.KnowledgeRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KnowledgeLoadNode implements GraphNode {

	private final KnowledgeContextBuilder knowledgeContextBuilder;

	private final KnowledgeRetrievalService knowledgeRetrievalService;

	@Override
	public String name() {
		return "knowledge_load";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent())) {
			return;
		}
		emitter.emitNodeStart(name(), "Loading knowledge context", Map.of("agentId", state.getAgentId()));
		KnowledgeRecallResult recallResult = knowledgeRetrievalService.recall(state.getAgentId(), state.getQuestion(),
				state.getEmbeddingModelConfigId(), state.getKnowledgeTopK() == null ? 5 : state.getKnowledgeTopK());
		String knowledgeContext = recallResult.getKnowledgeContext();
		if (knowledgeContext == null || knowledgeContext.isBlank()) {
			knowledgeContext = knowledgeContextBuilder.build(state.getAgentId());
		}
		state.setKnowledgeRecallResult(recallResult);
		state.setKnowledgeRecallFallbackUsed(recallResult.getFallbackUsed());
		state.setKnowledgeRecallMessage(recallResult.getMessage());
		state.setRecalledKnowledgeCount(recallResult.getRecalledCount());
		state.setKnowledgeContext(knowledgeContext);
		if (Boolean.TRUE.equals(state.getBusinessRuleCandidate()) && hasRelevantKnowledge(recallResult, knowledgeContext)) {
			buildKnowledgeAnswer(state, recallResult, knowledgeContext);
		}
		emitter.emitNodeEnd(name(), "Knowledge context loaded",
				Map.of("knowledgeLength", knowledgeContext.length(), "recalledKnowledgeCount", recallResult.getRecalledCount(),
						"fallbackUsed", recallResult.getFallbackUsed(), "selectedChunks", recallResult.getSelectedChunks(),
						"knowledgeRecallMessage", recallResult.getMessage(), "skipSql", Boolean.TRUE.equals(state.getSkipSql())));
	}

	private boolean hasRelevantKnowledge(KnowledgeRecallResult recallResult, String knowledgeContext) {
		boolean hasChunks = recallResult != null && recallResult.getSelectedChunks() != null && !recallResult.getSelectedChunks().isEmpty();
		String context = knowledgeContext == null ? "" : knowledgeContext;
		return (hasChunks || !context.isBlank()) && containsAny(context, "退款", "销售额", "计入", "不计入", "排除");
	}

	private void buildKnowledgeAnswer(GraphState state, KnowledgeRecallResult recallResult, String knowledgeContext) {
		String answer = buildBusinessRuleAnswer(knowledgeContext);
		String markdown = buildKnowledgeReport(state.getQuestion(), answer, recallResult.getSelectedChunks());
		ChartSpec chartSpec = ChartSpec.none("业务规则类问题不生成可视化图表");
		state.setIntent("business_rule");
		state.setSkipSql(true);
		state.setAnswer(answer);
		state.setReportSummary(answer);
		state.setReportTitle("数据分析报告：" + state.getQuestion());
		state.setReportMarkdown(markdown);
		state.setChartSpec(chartSpec);
		state.setReportResult(ReportResult.builder()
			.title(state.getReportTitle())
			.summary(answer)
			.markdown(markdown)
			.sections(List.of())
			.chartSpec(chartSpec)
			.metrics(Map.of())
			.success(true)
			.message("Knowledge answer generated")
			.build());
	}

	private String buildBusinessRuleAnswer(String knowledgeContext) {
		String context = knowledgeContext == null ? "" : knowledgeContext;
		if (context.contains("退款订单不计入销售额")) {
			StringBuilder answer = new StringBuilder("退款订单不计入销售额。");
			if (context.contains("status='refunded'")) {
				answer.append("如果订单状态字段存在，应排除 status='refunded' 的订单");
				if (context.contains("暂时没有 status 字段")) {
					answer.append("；当前演示库暂时没有 status 字段");
				}
				answer.append("。");
			}
			return answer.toString();
		}
		return firstUsefulSentence(context);
	}

	private String firstUsefulSentence(String text) {
		if (text == null || text.isBlank()) {
			return "已根据召回知识生成业务规则回答。";
		}
		String normalized = text.replace("\r", "\n");
		for (String line : normalized.split("\n")) {
			String cleaned = line.replaceAll("^\\s*\\d+\\.\\s*", "").replaceAll("^\\s*-\\s*", "").trim();
			if (!cleaned.isBlank() && containsAny(cleaned, "规则", "口径", "计入", "不计入", "退款", "销售额")) {
				return cleaned.endsWith("。") ? cleaned : cleaned + "。";
			}
		}
		return "已根据召回知识生成业务规则回答。";
	}

	private String buildKnowledgeReport(String question, String answer, List<KnowledgeRecallItem> chunks) {
		StringBuilder markdown = new StringBuilder();
		markdown.append("# 数据分析报告：").append(question == null ? "" : question).append("\n\n");
		markdown.append("## 核心结论\n\n").append(answer).append("\n\n");
		markdown.append("## 依据知识\n\n");
		if (chunks == null || chunks.isEmpty()) {
			markdown.append("- 暂无召回知识。\n");
		}
		else {
			chunks.forEach(chunk -> markdown.append("- ").append(defaultText(chunk.getTitle(), "业务知识")).append("：")
				.append(defaultText(chunk.getContent(), "")).append("\n"));
		}
		return markdown.toString();
	}

	private String defaultText(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value;
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
