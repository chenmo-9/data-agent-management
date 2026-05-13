package com.alibaba.cloud.ai.dataagent.dto.graphhuman;

import lombok.Data;

@Data
public class HumanCancelRequest {

	private String reason;
	private String canceledBy;

}
