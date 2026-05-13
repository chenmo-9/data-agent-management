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
package com.alibaba.cloud.ai.dataagent.analysis;

import com.alibaba.cloud.ai.dataagent.analysis.python.PythonCodeGenerator;
import com.alibaba.cloud.ai.dataagent.analysis.python.PythonCodeSafetyChecker;
import com.alibaba.cloud.ai.dataagent.analysis.python.PythonCodeSafetyResult;
import com.alibaba.cloud.ai.dataagent.analysis.python.PythonExecutionRequest;
import com.alibaba.cloud.ai.dataagent.analysis.python.PythonExecutionResult;
import com.alibaba.cloud.ai.dataagent.analysis.python.PythonSandboxExecutor;
import com.alibaba.cloud.ai.dataagent.dto.analysis.AnalysisRequest;
import com.alibaba.cloud.ai.dataagent.dto.analysis.AnalysisResultDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PythonAnalysisExecutor {

	private final PythonSandboxProperties properties;

	private final PythonCodeGenerator codeGenerator;

	private final PythonCodeSafetyChecker safetyChecker;

	private final PythonSandboxExecutor sandboxExecutor;

	private final ObjectMapper objectMapper;

	public AnalysisResultDTO executeAnalysis(AnalysisRequest request) {
		List<Map<String, Object>> rows = request == null ? List.of() : request.getRows();
		if (!Boolean.TRUE.equals(properties.getEnabled())) {
			AnalysisResultDTO result = executeBasicAnalysis(rows);
			result.setEngine("java_safe");
			result.setPythonEnabled(false);
			result.setPythonExecuted(false);
			result.setPythonSuccess(false);
			result.setFallbackUsed(false);
			return result;
		}
		String code = codeGenerator.generate(request == null ? null : request.getQuestion(), request == null ? null : request.getSql(),
				rows);
		PythonCodeSafetyResult safety = safetyChecker.check(code);
		if (!Boolean.TRUE.equals(safety.getSafe())) {
			return fallbackOrFailure(rows, code, safety.getBlockedReason(), null);
		}
		PythonExecutionResult execution = sandboxExecutor.execute(PythonExecutionRequest.builder()
			.runId("analysis")
			.question(request == null ? null : request.getQuestion())
			.sql(request == null ? null : request.getSql())
			.rows(rows)
			.timeoutSeconds(properties.getTimeoutSeconds())
			.maxOutputChars(properties.getMaxOutputChars())
			.code(code)
			.build());
		if (Boolean.TRUE.equals(execution.getSuccess())) {
			return fromPythonResult(execution, rows);
		}
		return fallbackOrFailure(rows, code, execution.getErrorMessage(), execution);
	}

	public AnalysisResultDTO executeBasicAnalysis(List<Map<String, Object>> rows) {
		List<Map<String, Object>> safeRows = rows == null ? List.of() : rows;
		Map<String, Object> metrics = new LinkedHashMap<>();
		metrics.put("rowCount", safeRows.size());
		if (safeRows.isEmpty()) {
			metrics.put("columnCount", 0);
			metrics.put("columns", List.of());
			return AnalysisResultDTO.builder()
				.success(true)
				.summary("查询结果为空，暂无可统计的数据。")
				.metrics(metrics)
				.message("Analysis completed")
				.engine("java_safe")
				.build();
		}

		List<String> columns = new ArrayList<>(safeRows.get(0).keySet());
		metrics.put("columnCount", columns.size());
		metrics.put("columns", columns);
		if (safeRows.size() == 1 && columns.size() == 1) {
			metrics.put("singleValue", safeRows.get(0).get(columns.get(0)));
		}

		Map<String, Object> numericStats = buildNumericStats(safeRows, columns);
		metrics.put("numericStats", numericStats);
		return AnalysisResultDTO.builder()
			.success(true)
			.summary(buildSummary(safeRows.size(), columns.size(), metrics))
			.metrics(metrics)
			.message("Analysis completed")
			.engine("java_safe")
			.build();
	}

	private AnalysisResultDTO fromPythonResult(PythonExecutionResult execution, List<Map<String, Object>> rows) {
		try {
			Map<String, Object> metrics = objectMapper.readValue(execution.getStdout(), new TypeReference<>() {
			});
			String summary = metrics.get("summary") == null ? "Python 沙箱分析完成。" : String.valueOf(metrics.get("summary"));
			return AnalysisResultDTO.builder()
				.success(true)
				.summary(summary)
				.metrics(metrics)
				.message("Python sandbox analysis completed")
				.engine("python_sandbox")
				.pythonEnabled(true)
				.pythonExecuted(true)
				.pythonSuccess(true)
				.pythonCode(execution.getCode())
				.pythonStdout(execution.getStdout())
				.pythonStderr(execution.getStderr())
				.pythonExitCode(execution.getExitCode())
				.pythonDurationMs(execution.getDurationMs())
				.fallbackUsed(false)
				.build();
		}
		catch (Exception ex) {
			return fallbackOrFailure(rows, execution.getCode(), "Failed to parse python stdout: " + ex.getMessage(), execution);
		}
	}

	private AnalysisResultDTO fallbackOrFailure(List<Map<String, Object>> rows, String code, String errorMessage,
			PythonExecutionResult execution) {
		if (Boolean.TRUE.equals(properties.getFallbackToSafeAnalysis())) {
			AnalysisResultDTO fallback = executeBasicAnalysis(rows);
			fallback.setEngine("python_sandbox_fallback");
			fallback.setPythonEnabled(true);
			fallback.setPythonExecuted(execution != null);
			fallback.setPythonSuccess(false);
			fallback.setPythonCode(code);
			fallback.setPythonStdout(execution == null ? null : execution.getStdout());
			fallback.setPythonStderr(execution == null ? null : execution.getStderr());
			fallback.setPythonExitCode(execution == null ? null : execution.getExitCode());
			fallback.setPythonDurationMs(execution == null ? null : execution.getDurationMs());
			fallback.setPythonErrorMessage(errorMessage);
			fallback.setFallbackUsed(true);
			fallback.setMessage("Python sandbox failed, fallback to Java safe analysis");
			return fallback;
		}
		return AnalysisResultDTO.builder()
			.success(false)
			.summary("Python 沙箱分析失败。")
			.metrics(Map.of())
			.message(errorMessage)
			.engine("python_sandbox")
			.pythonEnabled(true)
			.pythonExecuted(execution != null)
			.pythonSuccess(false)
			.pythonCode(code)
			.pythonStdout(execution == null ? null : execution.getStdout())
			.pythonStderr(execution == null ? null : execution.getStderr())
			.pythonExitCode(execution == null ? null : execution.getExitCode())
			.pythonDurationMs(execution == null ? null : execution.getDurationMs())
			.pythonErrorMessage(errorMessage)
			.fallbackUsed(false)
			.build();
	}

	private Map<String, Object> buildNumericStats(List<Map<String, Object>> rows, List<String> columns) {
		Map<String, Object> numericStats = new LinkedHashMap<>();
		for (String column : columns) {
			List<BigDecimal> values = rows.stream()
				.map(row -> toBigDecimal(row.get(column)))
				.filter(value -> value != null)
				.toList();
			if (values.isEmpty()) {
				continue;
			}
			BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal avg = sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
			BigDecimal min = values.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
			BigDecimal max = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
			Map<String, Object> stat = new LinkedHashMap<>();
			stat.put("sum", sum);
			stat.put("avg", avg);
			stat.put("min", min);
			stat.put("max", max);
			numericStats.put(column, stat);
		}
		return numericStats;
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

	private String buildSummary(int rowCount, int columnCount, Map<String, Object> metrics) {
		if (metrics.containsKey("singleValue")) {
			return "查询返回 1 行 1 列，核心结果为 " + metrics.get("singleValue") + "。";
		}
		return "查询返回 " + rowCount + " 行、" + columnCount + " 列，已完成基础统计。";
	}

}
