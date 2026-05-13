package com.alibaba.cloud.ai.dataagent.vo.graphhuman;

import com.alibaba.cloud.ai.dataagent.vo.graphhistory.GraphEventHistoryVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HumanPendingVO {

	private String runId;
	private String sessionId;
	private String question;
	private Long agentId;
	private String agentName;
	private String mode;
	private String confirmSql;
	private String generatedSql;
	private String validatedSql;
	private String sanitizedSql;
	private String sqlSecurityMessage;
	private String confirmStatus;
	private LocalDateTime createdAt;
	private List<GraphEventHistoryVO> events;

}
