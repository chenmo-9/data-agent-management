package com.alibaba.cloud.ai.dataagent.vo.graphhistory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GraphRunHistoryVO {

	private Long id;
	private String runId;
	private String sessionId;
	private Long agentId;
	private String agentName;
	private String mode;
	private String question;
	private String status;
	private Boolean success;
	private Long durationMs;
	private Integer rowCount;
	private String chartType;
	private Boolean confirmRequired;
	private String confirmStatus;
	private String failedNode;
	private String errorMessage;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime startedAt;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime finishedAt;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdAt;

}
