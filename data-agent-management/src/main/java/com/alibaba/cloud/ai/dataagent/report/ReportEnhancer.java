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
package com.alibaba.cloud.ai.dataagent.report;

import com.alibaba.cloud.ai.dataagent.dto.analysis.ReportGenerateRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ReportEnhancer {

	public ReportResult enhance(ReportGenerateRequest request) {
		try {
			String title = buildTitle(request.getQuestion());
			ChartSpec chartSpec = inferChartSpec(request.getQuestion(), request.getRows(), request.getMetrics());
			String summary = buildSummary(request, chartSpec);
			String markdown = buildMarkdown(title, summary, request, chartSpec);
			return ReportResult.builder()
				.title(title)
				.summary(summary)
				.markdown(markdown)
				.sections(buildSections(request, summary, chartSpec))
				.chartSpec(chartSpec)
				.metrics(request.getMetrics())
				.success(true)
				.message("Enhanced report generated")
				.build();
		}
		catch (Exception ex) {
			ChartSpec chartSpec = ChartSpec.none("报告增强失败，暂无可视化数据");
			return ReportResult.builder()
				.title("数据分析报告")
				.summary("报告生成失败。")
				.markdown("")
				.sections(List.of())
				.chartSpec(chartSpec)
				.metrics(request.getMetrics())
				.success(false)
				.message(ex.getMessage())
				.build();
		}
	}

	private ChartSpec inferChartSpec(String question, List<Map<String, Object>> rows, Map<String, Object> metrics) {
		List<Map<String, Object>> safeRows = rows == null ? List.of() : rows;
		if (safeRows.isEmpty()) {
			return ChartSpec.none("暂无可视化数据");
		}
		List<String> columns = new ArrayList<>(safeRows.get(0).keySet());
		if (safeRows.size() == 1 && columns.size() == 1) {
			Object value = safeRows.get(0).get(columns.get(0));
			return ChartSpec.builder()
				.chartType("single_value")
				.title("核心指标")
				.xField(columns.get(0))
				.yField(columns.get(0))
				.yData(List.of(value))
				.description("单值指标")
				.series(List.of(ChartSeries.builder().name(columns.get(0)).type("single_value").data(List.of(value)).build()))
				.build();
		}
		String categoryField = findCategoryField(safeRows, columns);
		String numericField = findNumericField(safeRows, columns);
		if (categoryField == null || numericField == null) {
			return ChartSpec.none("未找到适合图表展示的分类字段和数值字段");
		}
		String chartType = inferChartType(question, columns, categoryField);
		List<Object> xData = safeRows.stream().map(row -> row.get(categoryField)).toList();
		List<Object> yData = safeRows.stream().map(row -> (Object) toNumber(row.get(numericField))).toList();
		return ChartSpec.builder()
			.chartType(chartType)
			.title(buildChartTitle(chartType, categoryField, numericField))
			.xField(categoryField)
			.yField(numericField)
			.xData(xData)
			.yData(yData)
			.series(List.of(ChartSeries.builder().name(numericField).type(chartType).data(yData).build()))
			.description("根据 SQL 查询结果自动生成")
			.build();
	}

	private String inferChartType(String question, List<String> columns, String categoryField) {
		String normalizedQuestion = normalize(question);
		if (containsAny(normalizedQuestion, "占比", "比例", "分布", "proportion", "share")) {
			return "pie";
		}
		if (isTimeField(categoryField) || columns.stream().anyMatch(this::isTimeField)) {
			return "line";
		}
		return "bar";
	}

	private String findCategoryField(List<Map<String, Object>> rows, List<String> columns) {
		for (String column : columns) {
			if (!allNumeric(rows, column)) {
				return column;
			}
		}
		return columns.isEmpty() ? null : columns.get(0);
	}

	private String findNumericField(List<Map<String, Object>> rows, List<String> columns) {
		for (String column : columns) {
			if (allNumeric(rows, column)) {
				return column;
			}
		}
		return null;
	}

	private boolean allNumeric(List<Map<String, Object>> rows, String column) {
		return rows.stream().allMatch(row -> toNumber(row.get(column)) != null);
	}

	private Number toNumber(Object value) {
		if (value instanceof Number number) {
			return number;
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

	private boolean isTimeField(String field) {
		String normalized = normalize(field);
		return containsAny(normalized, "created_at", "date", "time", "day", "month", "时间", "日期");
	}

	private String buildTitle(String question) {
		return question == null || question.isBlank() ? "数据分析报告" : "数据分析报告：" + question;
	}

	private String buildSummary(ReportGenerateRequest request, ChartSpec chartSpec) {
		List<Map<String, Object>> rows = request.getRows();
		if (rows != null && !rows.isEmpty()) {
			if (rows.size() == 1 && rows.get(0).size() == 1) {
				String field = rows.get(0).keySet().iterator().next();
				Object value = rows.get(0).get(field);
				return buildSingleValueConclusion(field, value);
			}
			if (chartSpec != null && !"none".equals(chartSpec.getChartType())) {
				String conclusion = buildCategoryValueConclusion(rows, chartSpec.getXField(), chartSpec.getYField());
				if (conclusion != null) {
					return conclusion;
				}
			}
		}
		Map<String, Object> metrics = request.getMetrics();
		if (metrics != null && metrics.containsKey("singleValue")) {
			return "本次查询结果为 " + formatValue(metrics.get("singleValue"), null) + "。";
		}
		return request.getAnalysisSummary() == null || request.getAnalysisSummary().isBlank()
				? "暂无可用分析结论。" : request.getAnalysisSummary();
	}

	private String buildMarkdown(String title, String summary, ReportGenerateRequest request, ChartSpec chartSpec) {
		StringBuilder markdown = new StringBuilder();
		markdown.append("# ").append(title).append("\n\n");
		markdown.append("## 核心结论\n\n").append(summary).append("\n\n");
		markdown.append("## 用户问题\n\n").append(defaultText(request.getQuestion(), "未提供")).append("\n\n");
		markdown.append("## SQL\n\n```sql\n").append(defaultText(request.getSql(), "无 SQL")).append("\n```\n\n");
		markdown.append("## 查询结果概览\n\n").append(buildRowsOverview(request.getRows(), chartSpec)).append("\n\n");
		markdown.append("## 基础指标\n\n").append(buildMetrics(request.getMetrics())).append("\n\n");
		markdown.append("## 可视化建议\n\n").append(buildChartAdvice(chartSpec)).append("\n\n");
		markdown.append("## 备注\n\n本报告由安全分析模板生成，未执行任意 Python 代码。\n");
		return markdown.toString();
	}

	private List<ReportSection> buildSections(ReportGenerateRequest request, String summary, ChartSpec chartSpec) {
		List<ReportSection> sections = new ArrayList<>();
		sections.add(section("核心结论", "summary", summary, Map.of()));
		sections.add(section("SQL", "sql", defaultText(request.getSql(), "无 SQL"), Map.of()));
		sections.add(section("查询结果", "table", "返回结果表格", Map.of("rows", request.getRows() == null ? List.of() : request.getRows())));
		sections.add(section("基础指标", "metrics", "基础统计指标", request.getMetrics() == null ? Map.of() : request.getMetrics()));
		sections.add(section("可视化", "chart", chartSpec.getDescription(), Map.of("chartSpec", chartSpec)));
		sections.add(section("备注", "note", "本报告由安全分析模板生成，未执行任意 Python 代码。", Map.of()));
		return sections;
	}

	private ReportSection section(String title, String type, String content, Map<String, Object> data) {
		return ReportSection.builder().title(title).type(type).content(content).data(data).build();
	}

	private String buildRowsPreview(List<Map<String, Object>> rows) {
		if (rows == null || rows.isEmpty()) {
			return "查询结果为空。";
		}
		StringBuilder preview = new StringBuilder();
		preview.append("返回 ").append(rows.size()).append(" 行。前 10 行：\n\n");
		rows.stream().limit(10).forEach(row -> preview.append("- ").append(row).append("\n"));
		return preview.toString();
	}

	private String buildRowsOverview(List<Map<String, Object>> rows, ChartSpec chartSpec) {
		if (rows == null || rows.isEmpty()) {
			return "查询结果为空。";
		}
		StringBuilder overview = new StringBuilder();
		overview.append("本次查询返回 ").append(rows.size()).append(" 行结果");
		if (chartSpec != null && chartSpec.getXField() != null && chartSpec.getYField() != null
				&& !"none".equals(chartSpec.getChartType())) {
			overview.append("，主要按 `").append(chartSpec.getXField()).append("` 展示 `")
				.append(chartSpec.getYField()).append("`");
		}
		overview.append("。\n\n");
		overview.append(buildRowsPreview(rows));
		return overview.toString();
	}

	private String buildSingleValueConclusion(String field, Object value) {
		String label = fieldLabel(field);
		if ("销售额".equals(label)) {
			return "本次销售额为 " + formatValue(value, label) + "。";
		}
		return "本次查询结果为 " + formatValue(value, label) + "。";
	}

	private String buildCategoryValueConclusion(List<Map<String, Object>> rows, String categoryField, String valueField) {
		if (rows == null || rows.isEmpty() || categoryField == null || valueField == null) {
			return null;
		}
		List<CategoryValue> values = rows.stream()
			.map(row -> new CategoryValue(String.valueOf(row.get(categoryField)), toBigDecimal(row.get(valueField))))
			.filter(item -> item.value() != null)
			.toList();
		if (values.isEmpty()) {
			return null;
		}
		String valueLabel = fieldLabel(valueField);
		BigDecimal total = values.stream().map(CategoryValue::value).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal firstValue = values.get(0).value();
		boolean allEqual = values.stream().allMatch(item -> item.value().compareTo(firstValue) == 0);
		if (allEqual) {
			String categories = joinNames(values.stream().map(CategoryValue::name).toList());
			String categoryLabel = categoryLabel(categoryField);
			return categories + " 的" + valueLabel + "均为 " + formatValue(values.get(0).value(), valueLabel) + "，总" + valueLabel
					+ "为 " + formatValue(total, valueLabel) + "，" + countLabel(values.size()) + "个" + categoryLabel + valueLabel + "持平。";
		}
		CategoryValue max = values.stream().max((left, right) -> left.value().compareTo(right.value())).orElse(values.get(0));
		CategoryValue min = values.stream().min((left, right) -> left.value().compareTo(right.value())).orElse(values.get(0));
		return "最高的是 " + max.name() + "，" + valueLabel + "为 " + formatValue(max.value(), valueLabel)
				+ "；最低的是 " + min.name() + "，" + valueLabel + "为 " + formatValue(min.value(), valueLabel)
				+ "；总" + valueLabel + "为 " + formatValue(total, valueLabel) + "。";
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
			case 4 -> "四";
			case 5 -> "五";
			default -> String.valueOf(count);
		};
	}

	private String fieldLabel(String field) {
		String normalized = normalize(field);
		if (containsAny(normalized, "total_sales", "sales", "revenue", "销售额")) {
			return "销售额";
		}
		if (containsAny(normalized, "amount", "金额")) {
			return "金额";
		}
		if (containsAny(normalized, "order_count")) {
			return "订单数量";
		}
		if (containsAny(normalized, "count", "数量")) {
			return "数量";
		}
		if (containsAny(normalized, "name", "user", "customer", "用户", "客户")) {
			return "用户";
		}
		return field;
	}

	private String categoryLabel(String field) {
		String normalized = normalize(field);
		if (containsAny(normalized, "name", "user", "customer", "用户", "客户")) {
			return "用户";
		}
		if (containsAny(normalized, "product", "商品", "产品")) {
			return "产品";
		}
		if (containsAny(normalized, "region", "地区", "区域")) {
			return "地区";
		}
		return "分类";
	}

	private String formatValue(Object value, String label) {
		BigDecimal decimal = toBigDecimal(value);
		if (decimal != null) {
			if ("销售额".equals(label) || "金额".equals(label)) {
				return decimal.setScale(2, RoundingMode.HALF_UP).toPlainString();
			}
			return decimal.stripTrailingZeros().toPlainString();
		}
		return String.valueOf(value);
	}

	private BigDecimal toBigDecimal(Object value) {
		Number number = toNumber(value);
		if (number == null) {
			return null;
		}
		return new BigDecimal(number.toString());
	}

	private record CategoryValue(String name, BigDecimal value) {
	}

	private String buildMetrics(Map<String, Object> metrics) {
		if (metrics == null || metrics.isEmpty()) {
			return "暂无基础指标。";
		}
		StringBuilder builder = new StringBuilder();
		metrics.forEach((key, value) -> builder.append("- **").append(key).append("**: ").append(value).append("\n"));
		return builder.toString();
	}

	private String buildChartAdvice(ChartSpec chartSpec) {
		if (chartSpec == null || "none".equals(chartSpec.getChartType())) {
			return "暂无可视化图表。";
		}
		Map<String, String> labels = new LinkedHashMap<>();
		labels.put("bar", "柱状图");
		labels.put("line", "折线图");
		labels.put("pie", "饼图");
		labels.put("single_value", "指标卡");
		return "- 图表类型：" + labels.getOrDefault(chartSpec.getChartType(), chartSpec.getChartType()) + "\n"
				+ "- X 轴：" + defaultText(chartSpec.getXField(), "-") + "\n"
				+ "- Y 轴：" + defaultText(chartSpec.getYField(), "-");
	}

	private String buildChartTitle(String chartType, String xField, String yField) {
		if ("pie".equals(chartType)) {
			return yField + " 分布";
		}
		if ("line".equals(chartType)) {
			return yField + " 趋势";
		}
		return yField + " 按 " + xField + " 对比";
	}

	private boolean containsAny(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(normalize(keyword))) {
				return true;
			}
		}
		return false;
	}

	private String normalize(String text) {
		return text == null ? "" : text.toLowerCase(Locale.ROOT);
	}

	private String defaultText(String text, String defaultText) {
		return text == null || text.isBlank() ? defaultText : text;
	}

}
