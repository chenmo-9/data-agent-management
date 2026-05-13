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
package com.alibaba.cloud.ai.dataagent.service.analysis;

import com.alibaba.cloud.ai.dataagent.analysis.MarkdownReportGenerator;
import com.alibaba.cloud.ai.dataagent.analysis.SafeAnalysisService;
import com.alibaba.cloud.ai.dataagent.dto.analysis.AnalysisRequest;
import com.alibaba.cloud.ai.dataagent.dto.analysis.AnalysisResultDTO;
import com.alibaba.cloud.ai.dataagent.dto.analysis.ReportGenerateRequest;
import com.alibaba.cloud.ai.dataagent.vo.analysis.AnalysisVO;
import com.alibaba.cloud.ai.dataagent.vo.analysis.ReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

	private final SafeAnalysisService safeAnalysisService;

	private final MarkdownReportGenerator markdownReportGenerator;

	@Override
	public AnalysisVO analyze(AnalysisRequest request) {
		try {
			AnalysisResultDTO result = safeAnalysisService.analyze(request);
			return AnalysisVO.builder()
				.success(result.getSuccess())
				.summary(result.getSummary())
				.metrics(result.getMetrics())
				.message(result.getMessage())
				.engine(result.getEngine())
				.pythonEnabled(result.getPythonEnabled())
				.pythonExecuted(result.getPythonExecuted())
				.pythonSuccess(result.getPythonSuccess())
				.pythonCode(result.getPythonCode())
				.pythonStdout(result.getPythonStdout())
				.pythonStderr(result.getPythonStderr())
				.pythonExitCode(result.getPythonExitCode())
				.pythonDurationMs(result.getPythonDurationMs())
				.pythonErrorMessage(result.getPythonErrorMessage())
				.fallbackUsed(result.getFallbackUsed())
				.build();
		}
		catch (Exception ex) {
			return AnalysisVO.builder()
				.success(false)
				.summary("分析失败。")
				.metrics(Map.of())
				.message(ex.getMessage())
				.build();
		}
	}

	@Override
	public ReportVO generateReport(ReportGenerateRequest request) {
		try {
			return markdownReportGenerator.generate(request);
		}
		catch (Exception ex) {
			return ReportVO.builder().success(false).title("数据分析报告").markdown("").message(ex.getMessage()).build();
		}
	}

}
