package com.alibaba.cloud.ai.dataagent.vo.graphhuman;

import com.alibaba.cloud.ai.dataagent.vo.graph.GraphRunVO;
import lombok.Data;

@Data
public class HumanConfirmVO {

	private String runId;
	private String status;
	private String confirmStatus;
	private String confirmedSql;
	private String answer;
	private GraphRunVO graphRunVO;
	private String message;

}
