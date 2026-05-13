package com.alibaba.cloud.ai.dataagent.dto.graphhistory;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class GraphRunHistoryQueryRequest {

	private Long agentId;
	private Long modelConfigId;
	private String mode;
	private String status;
	private Boolean success;
	private String keyword;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;
	private Integer page = 1;
	private Integer pageSize = 20;

	public int offset() {
		int currentPage = page == null || page < 1 ? 1 : page;
		int size = limit();
		return (currentPage - 1) * size;
	}

	public int limit() {
		if (pageSize == null || pageSize < 1) {
			return 20;
		}
		return Math.min(pageSize, 100);
	}

}
