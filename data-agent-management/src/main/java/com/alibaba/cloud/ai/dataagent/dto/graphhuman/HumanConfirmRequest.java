package com.alibaba.cloud.ai.dataagent.dto.graphhuman;

import lombok.Data;

@Data
public class HumanConfirmRequest {

	private String sql;
	private String comment;
	private String confirmedBy;

}
