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

import com.alibaba.cloud.ai.dataagent.dto.analysis.AnalysisRequest;
import com.alibaba.cloud.ai.dataagent.dto.analysis.AnalysisResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SafeAnalysisService {

	private final PythonAnalysisExecutor pythonAnalysisExecutor;

	public AnalysisResultDTO analyze(AnalysisRequest request) {
		AnalysisResultDTO result = pythonAnalysisExecutor.executeAnalysis(request);
		result.setSummary(enrichSummary(request, result));
		return result;
	}

	private String enrichSummary(AnalysisRequest request, AnalysisResultDTO result) {
		String question = request == null || request.getQuestion() == null ? "未提供问题" : request.getQuestion();
		return "针对问题「" + question + "」：" + result.getSummary();
	}

}
