package com.alibaba.cloud.ai.dataagent.vo.graphhistory;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GraphRunHistoryDetailVO {

	private GraphRunHistoryVO run;
	private List<GraphEventHistoryVO> events;
	private String answer;
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
	private Integer recalledTableCount;
	private Integer recalledFieldCount;
	private Integer recalledRelationCount;
	private Integer recalledKnowledgeCount;
	private String resultPreviewJson;
	private String reportTitle;
	private String reportMarkdown;
	private String chartType;
	private String reportSummary;

}
