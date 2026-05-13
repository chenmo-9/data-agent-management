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

import com.alibaba.cloud.ai.dataagent.dto.analysis.ReportGenerateRequest;
import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.service.analysis.AnalysisService;
import com.alibaba.cloud.ai.dataagent.vo.analysis.ReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReportGenerateNode implements GraphNode {

	private final AnalysisService analysisService;

	@Override
	public String name() {
		return "report_generate";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent()) || state.getSqlError() != null) {
			return;
		}
		emitter.emitNodeStart(name(), "Generating markdown report", Map.of("rowCount", defaultNumber(state.getRowCount())));
		ReportGenerateRequest request = new ReportGenerateRequest();
		request.setQuestion(state.getQuestion());
		request.setSql(state.getValidatedSql());
		request.setRows(state.getSqlResult());
		request.setAnalysisSummary(state.getAnalysisSummary());
		request.setMetrics(state.getAnalysisResult());
		ReportVO report = analysisService.generateReport(request);
		String reportSummary = resolveReportSummary(report, state);
		state.setReportTitle(report.getTitle());
		state.setReportMarkdown(report.getMarkdown());
		state.setReportSummary(reportSummary);
		state.setReportResult(report.getReportResult());
		state.setChartSpec(report.getChartSpec());
		if (!Boolean.TRUE.equals(report.getSuccess())) {
			state.setErrorMessage(report.getMessage());
			emitter.emitError(name(), report.getMessage(), Map.of("reportTitle", report.getTitle()));
			return;
		}
		emitter.emitNodeEnd(name(), "Markdown report generated",
				Map.of("reportTitle", defaultText(report.getTitle()), "reportSummary", defaultText(reportSummary),
						"markdownLength", report.getMarkdown() == null ? 0 : report.getMarkdown().length(),
						"chartSpec", report.getChartSpec(), "reportResult", report.getReportResult()));
	}

	private int defaultNumber(Integer value) {
		return value == null ? 0 : value;
	}

	private String defaultText(String value) {
		return value == null ? "" : value;
	}

	private String resolveReportSummary(ReportVO report, GraphState state) {
		if (report == null) {
			return fallbackSummaryFromRows(state.getSqlResult());
		}
		if (report.getSummary() != null && !report.getSummary().isBlank()) {
			return report.getSummary();
		}
		if (report.getReportResult() != null && report.getReportResult().getSummary() != null
				&& !report.getReportResult().getSummary().isBlank()) {
			return report.getReportResult().getSummary();
		}
		return fallbackSummaryFromRows(state.getSqlResult());
	}

	private String fallbackSummaryFromRows(List<Map<String, Object>> rows) {
		if (rows == null || rows.isEmpty()) {
			return "";
		}
		if (rows.size() == 1 && rows.get(0).size() == 1) {
			String field = rows.get(0).keySet().iterator().next();
			return "本次" + fieldLabel(field) + "为 " + formatValue(rows.get(0).get(field), fieldLabel(field)) + "。";
		}
		List<String> columns = rows.get(0).keySet().stream().toList();
		if (columns.size() < 2) {
			return "";
		}
		String categoryField = columns.stream().filter(column -> toBigDecimal(rows.get(0).get(column)) == null).findFirst().orElse(columns.get(0));
		String valueField = columns.stream().filter(column -> toBigDecimal(rows.get(0).get(column)) != null).findFirst()
			.orElse(columns.size() > 1 ? columns.get(1) : columns.get(0));
		List<CategoryValue> values = rows.stream()
			.map(row -> new CategoryValue(String.valueOf(row.get(categoryField)), toBigDecimal(row.get(valueField))))
			.filter(item -> item.value() != null)
			.toList();
		if (values.isEmpty()) {
			return "";
		}
		String label = fieldLabel(valueField);
		BigDecimal total = values.stream().map(CategoryValue::value).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal first = values.get(0).value();
		boolean allEqual = values.stream().allMatch(item -> item.value().compareTo(first) == 0);
		if (allEqual) {
			return joinNames(values.stream().map(CategoryValue::name).toList()) + " 的" + label + "均为 "
					+ formatValue(first, label) + "，总" + label + "为 " + formatValue(total, label) + "，"
					+ countLabel(values.size()) + "个" + categoryLabel(categoryField) + label + "持平。";
		}
		CategoryValue max = values.stream().max((left, right) -> left.value().compareTo(right.value())).orElse(values.get(0));
		CategoryValue min = values.stream().min((left, right) -> left.value().compareTo(right.value())).orElse(values.get(0));
		return "最高的是 " + max.name() + "，" + label + "为 " + formatValue(max.value(), label) + "；最低的是 "
				+ min.name() + "，" + label + "为 " + formatValue(min.value(), label) + "；总" + label + "为 "
				+ formatValue(total, label) + "。";
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value instanceof Number number) {
			return new BigDecimal(number.toString());
		}
		if (value instanceof String text) {
			try {
				return new BigDecimal(text);
			}
			catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}

	private String formatValue(Object value, String label) {
		BigDecimal decimal = toBigDecimal(value);
		if (decimal == null) {
			return String.valueOf(value);
		}
		if ("销售额".equals(label) || "金额".equals(label)) {
			return decimal.setScale(2, RoundingMode.HALF_UP).toPlainString();
		}
		return decimal.stripTrailingZeros().toPlainString();
	}

	private String fieldLabel(String field) {
		String normalized = field == null ? "" : field.toLowerCase();
		if (normalized.contains("total_sales") || normalized.contains("sales") || normalized.contains("revenue")) {
			return "销售额";
		}
		if (normalized.contains("amount")) {
			return "金额";
		}
		if (normalized.contains("order_count")) {
			return "订单数量";
		}
		return field == null || field.isBlank() ? "查询结果" : field;
	}

	private String categoryLabel(String field) {
		String normalized = field == null ? "" : field.toLowerCase();
		if (normalized.contains("name") || normalized.contains("user") || normalized.contains("customer")) {
			return "用户";
		}
		return "分类";
	}

	private String joinNames(List<String> names) {
		if (names.size() <= 2) {
			return String.join(" 和 ", names);
		}
		return String.join("、", names.subList(0, names.size() - 1)) + "和" + names.get(names.size() - 1);
	}

	private String countLabel(int count) {
		return switch (count) {
			case 1 -> "一";
			case 2 -> "两";
			case 3 -> "三";
			default -> String.valueOf(count);
		};
	}

	private record CategoryValue(String name, BigDecimal value) {
	}

}
