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
package com.alibaba.cloud.ai.dataagent.graph;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphState {

	private String sessionId;

	private String runId;

	private LocalDateTime startedAt;

	private LocalDateTime finishedAt;

	private Long durationMs;

	private String currentNode;

	private String graphStatus;

	private Boolean historySaved;

	private Boolean confirmBeforeExecute;

	private Boolean humanConfirmRequired;

	private String confirmStatus;

	private String confirmSql;

	private String confirmedSql;

	private String confirmedBy;

	private LocalDateTime confirmedAt;

	private String cancelReason;

	private String resumeToken;

	private String pendingPayloadJson;

	private Boolean paused;

	private Boolean resumeRun;

	private Long agentId;

	private Long modelConfigId;

	private String question;

	private String mode;

	private String answer;

	private String agentName;

	private String agentPrompt;

	private Integer datasourceCount;

	private Integer semanticModelCount;

	private Integer knowledgeCount;

	private String intent;

	private Boolean businessRuleCandidate;

	private Boolean unsafeOperation;

	private Boolean skipSql;

	private Long selectedDatasourceId;

	private String datasourceName;

	private String dbType;

	private String schemaContext;

	private SchemaRecallResult schemaRecallResult;

	private Boolean schemaRecallFallbackUsed;

	private String schemaRecallMessage;

	private Integer recalledTableCount;

	private Integer recalledFieldCount;

	private RelationRecallResult relationRecallResult;

	private String relationContext;

	private Integer recalledRelationCount;

	private String relationRecallMessage;

	private Boolean relationRecallFallbackUsed;

	private Long embeddingModelConfigId;

	private Integer knowledgeTopK;

	private KnowledgeRecallResult knowledgeRecallResult;

	private Boolean knowledgeRecallFallbackUsed;

	private String knowledgeRecallMessage;

	private Integer recalledKnowledgeCount;

	private String knowledgeContext;

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

	private String sqlError;

	private Integer rowCount;

	private String analysisSummary;

	private String analysisCode;

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

	private ReportResult reportResult;

	private ChartSpec chartSpec;

	@Builder.Default
	private List<GraphEventDTO> events = new ArrayList<>();

	private Boolean success;

	private String errorMessage;

}
