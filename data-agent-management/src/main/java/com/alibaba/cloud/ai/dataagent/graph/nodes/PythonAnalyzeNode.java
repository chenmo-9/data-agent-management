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

import com.alibaba.cloud.ai.dataagent.dto.analysis.AnalysisRequest;
import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import com.alibaba.cloud.ai.dataagent.service.analysis.AnalysisService;
import com.alibaba.cloud.ai.dataagent.vo.analysis.AnalysisVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PythonAnalyzeNode implements GraphNode {

	private final AnalysisService analysisService;

	@Override
	public String name() {
		return "python_analyze";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent()) || state.getSqlError() != null) {
			return;
		}
		emitter.emitNodeStart(name(), "Analyzing SQL result", Map.of("rowCount", defaultNumber(state.getRowCount())));
		AnalysisRequest request = new AnalysisRequest();
		request.setQuestion(state.getQuestion());
		request.setSql(state.getValidatedSql());
		request.setRows(state.getSqlResult());
		request.setRowCount(state.getRowCount());
		AnalysisVO result = analysisService.analyze(request);
		state.setAnalysisCode(result.getPythonCode() == null ? "java_safe_analysis_template" : result.getPythonCode());
		state.setAnalysisSummary(result.getSummary());
		state.setAnalysisResult(result.getMetrics());
		state.setPythonEngine(result.getEngine());
		state.setPythonExecuted(result.getPythonExecuted());
		state.setPythonSuccess(result.getPythonSuccess());
		state.setPythonCode(result.getPythonCode());
		state.setPythonStdout(result.getPythonStdout());
		state.setPythonStderr(result.getPythonStderr());
		state.setPythonExitCode(result.getPythonExitCode());
		state.setPythonDurationMs(result.getPythonDurationMs());
		state.setPythonErrorMessage(result.getPythonErrorMessage());
		state.setPythonFallbackUsed(result.getFallbackUsed());
		if (!Boolean.TRUE.equals(result.getSuccess())) {
			state.setErrorMessage(result.getMessage());
			emitter.emitError(name(), result.getMessage(), pythonData(result));
			return;
		}
		String message = Boolean.TRUE.equals(result.getFallbackUsed())
				? "Python sandbox failed, fallback to Java safe analysis" : "Analysis completed";
		emitter.emitNodeEnd(name(), message, pythonData(result));
	}

	private int defaultNumber(Integer value) {
		return value == null ? 0 : value;
	}

	private Map<String, Object> pythonData(AnalysisVO result) {
		return Map.of("analysisSummary", nullToEmpty(result.getSummary()), "metrics", nullToEmpty(result.getMetrics()),
				"engine", nullToEmpty(result.getEngine()), "pythonExecuted", defaultBool(result.getPythonExecuted()),
				"pythonSuccess", defaultBool(result.getPythonSuccess()), "pythonDurationMs",
				result.getPythonDurationMs() == null ? 0L : result.getPythonDurationMs(), "fallbackUsed",
				defaultBool(result.getFallbackUsed()), "pythonErrorMessage", nullToEmpty(result.getPythonErrorMessage()));
	}

	private Object nullToEmpty(Object value) {
		return value == null ? "" : value;
	}

	private boolean defaultBool(Boolean value) {
		return Boolean.TRUE.equals(value);
	}

}
