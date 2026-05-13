package com.alibaba.cloud.ai.dataagent.service.graphhuman;

import com.alibaba.cloud.ai.dataagent.dto.graphhuman.HumanCancelRequest;
import com.alibaba.cloud.ai.dataagent.dto.graphhuman.HumanConfirmRequest;
import com.alibaba.cloud.ai.dataagent.vo.graphhuman.HumanConfirmVO;
import com.alibaba.cloud.ai.dataagent.vo.graphhuman.HumanPendingVO;

public interface GraphHumanService {

	HumanPendingVO getPending(String runId);

	HumanConfirmVO confirm(String runId, HumanConfirmRequest request);

	HumanConfirmVO cancel(String runId, HumanCancelRequest request);

}
