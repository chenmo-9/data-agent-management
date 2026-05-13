package com.alibaba.cloud.ai.dataagent.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GraphEventHistory {

	private Long id;
	private String runId;
	private String sessionId;
	private String eventId;
	private String nodeName;
	private String eventType;
	private String status;
	private String message;
	private String dataJson;
	private String errorMessage;
	private LocalDateTime eventTime;
	private LocalDateTime createdAt;

}
