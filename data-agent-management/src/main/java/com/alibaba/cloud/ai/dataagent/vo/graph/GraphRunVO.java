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
package com.alibaba.cloud.ai.dataagent.vo.graph;

import com.alibaba.cloud.ai.dataagent.dto.graph.GraphEventDTO;
import com.alibaba.cloud.ai.dataagent.nl2sql.RelationRecallResult;
import com.alibaba.cloud.ai.dataagent.nl2sql.SchemaRecallResult;
import com.alibaba.cloud.ai.dataagent.rag.KnowledgeRecallResult;
import com.alibaba.cloud.ai.dataagent.report.ChartSpec;
import com.alibaba.cloud.ai.dataagent.report.ReportResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphRunVO {

	private String sessionId;

	private String runId;

	private Long durationMs;

	private Boolean historySaved;

	private Integer eventCount;

	private Boolean confirmRequired;

	private String confirmStatus;

	private String confirmSql;

	private String confirmedSql;

	private String resumeToken;

	private Boolean paused;

	private String answer;

	private String intent;

	private Boolean businessRuleCandidate;

	private Boolean unsafeOperation;

	private Boolean skipSql;

	private Long datasourceId;

	private SchemaRecallResult schemaRecallResult;

	private Boolean schemaRecallFallbackUsed;

	private String schemaRecallMessage;

	private Integer recalledTableCount;

	private Integer recalledFieldCount;

	private String schemaContext;

	private RelationRecallResult relationRecallResult;

	private String relationContext;

	private Integer recalledRelationCount;

	private String relationRecallMessage;

	private Boolean relationRecallFallbackUsed;

	private KnowledgeRecallResult knowledgeRecallResult;

	private String knowledgeContext;

	private Boolean knowledgeRecallFallbackUsed;

	private String knowledgeRecallMessage;

	private Integer recalledKnowledgeCount;

	private String rawLlmSqlOutput;

	private String extractedSql;

	private String generatedSql;

	private String repairedSql;

	private String validatedSql;

	private String sanitizedSql;

	private Boolean sqlLimited;

	private Integer sqlLimit;

	private Boolean sqlResultTruncated;

	private Integer sqlQueryTimeoutSeconds;

	private String sqlSecurityMessage;

	private Boolean sqlRepairAttempted;

	private Boolean sqlRepairSuccess;

	private String sqlRepairMessage;

	private String sqlValidationError;

	private String sqlExecutionError;

	private List<Map<String, Object>> sqlResult;

	private Integer rowCount;

	private String analysisSummary;

	private Map<String, Object> analysisResult;

	private String pythonEngine;

	private Boolean pythonExecuted;

	private Boolean pythonSuccess;

	private String pythonCode;

	private String pythonStdout;

	private String pythonStderr;

	private Integer pythonExitCode;

	private Long pythonDurationMs;

	private String pythonErrorMessage;

	private Boolean pythonFallbackUsed;

	private String reportMarkdown;

	private String reportTitle;

	private String reportSummary;

	private String summary;

	private ReportResult reportResult;

	private ChartSpec chartSpec;

	private Boolean success;

	private String message;

	private List<GraphEventDTO> events;

}
