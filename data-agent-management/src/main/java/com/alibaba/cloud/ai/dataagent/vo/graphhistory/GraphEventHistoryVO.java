package com.alibaba.cloud.ai.dataagent.vo.graphhistory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GraphEventHistoryVO {

	private Long id;
	private String runId;
	private String eventId;
	private String nodeName;
	private String eventType;
	private String status;
	private String message;
	private String dataJson;
	private String errorMessage;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime eventTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdAt;

}
