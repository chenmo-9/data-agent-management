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
package com.alibaba.cloud.ai.dataagent.controller;

import com.alibaba.cloud.ai.dataagent.dto.analysis.AnalysisRequest;
import com.alibaba.cloud.ai.dataagent.dto.analysis.ReportGenerateRequest;
import com.alibaba.cloud.ai.dataagent.service.analysis.AnalysisService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.analysis.AnalysisVO;
import com.alibaba.cloud.ai.dataagent.vo.analysis.ReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {

	private final AnalysisService analysisService;

	@PostMapping("/analyze")
	public ApiResponse<AnalysisVO> analyze(@RequestBody AnalysisRequest request) {
		return ApiResponse.success("Analysis completed", analysisService.analyze(request));
	}

	@PostMapping("/report")
	public ApiResponse<ReportVO> report(@RequestBody ReportGenerateRequest request) {
		return ApiResponse.success("Report generated", analysisService.generateReport(request));
	}

}
