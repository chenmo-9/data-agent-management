package com.alibaba.cloud.ai.dataagent.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GraphRunHistory {

	private Long id;
	private String runId;
	private String sessionId;
	private Long agentId;
	private String agentName;
	private Long modelConfigId;
	private String modelName;
	private Long datasourceId;
	private String datasourceName;
	private String mode;
	private String question;
	private String answer;
	private String status;
	private Boolean success;
	private LocalDateTime startedAt;
	private LocalDateTime finishedAt;
	private Long durationMs;
	private Integer eventCount;
	private String failedNode;
	private String errorMessage;
	private String generatedSql;
	private String extractedSql;
	private String repairedSql;
	private String validatedSql;
	private String sanitizedSql;
	private Boolean sqlLimited;
	private Integer sqlLimit;
	private Boolean sqlResultTruncated;
	private Integer sqlQueryTimeoutSeconds;
	private String sqlSecurityMessage;
	private Integer rowCount;
	private String resultPreviewJson;
	private Integer recalledTableCount;
	private Integer recalledFieldCount;
	private Integer recalledRelationCount;
	private Integer recalledKnowledgeCount;
	private String reportTitle;
	private String reportMarkdown;
	private String chartType;
	private String reportSummary;
	private String sqlValidationError;
	private String sqlExecutionError;
	private String pythonEngine;
	private Boolean pythonExecuted;
	private Boolean pythonSuccess;
	private Long pythonDurationMs;
	private Boolean pythonFallbackUsed;
	private String pythonErrorMessage;
	private Boolean confirmRequired;
	private String confirmStatus;
	private String confirmSql;
	private String confirmedSql;
	private String confirmedBy;
	private LocalDateTime confirmedAt;
	private LocalDateTime canceledAt;
	private String cancelReason;
	private String resumeToken;
	private String pendingPayloadJson;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
