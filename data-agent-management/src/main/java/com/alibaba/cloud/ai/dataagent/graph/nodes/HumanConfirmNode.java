package com.alibaba.cloud.ai.dataagent.graph.nodes;

import com.alibaba.cloud.ai.dataagent.graph.GraphEventEmitter;
import com.alibaba.cloud.ai.dataagent.graph.GraphNode;
import com.alibaba.cloud.ai.dataagent.graph.GraphState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HumanConfirmNode implements GraphNode {

	@Override
	public String name() {
		return "human_confirm";
	}

	@Override
	public void execute(GraphState state, GraphEventEmitter emitter) {
		if (!"data_query".equals(state.getIntent())) {
			return;
		}
		if (!Boolean.TRUE.equals(state.getConfirmBeforeExecute())) {
			emitter.emitNodeEnd(name(), "Human confirmation skipped", Map.of("confirmBeforeExecute", false));
			return;
		}
		String confirmSql = state.getSanitizedSql();
		if (confirmSql == null || confirmSql.isBlank()) {
			confirmSql = state.getValidatedSql();
		}
		state.setHumanConfirmRequired(true);
		state.setConfirmStatus("pending");
		state.setConfirmSql(confirmSql);
		state.setResumeToken(UUID.randomUUID().toString());
		state.setPaused(true);
		state.setGraphStatus("pending_confirm");
		state.setAnswer("SQL 已生成，等待人工确认后执行。");
		Map<String, Object> data = new HashMap<>();
		data.put("runId", state.getRunId());
		data.put("confirmRequired", true);
		data.put("confirmStatus", "pending");
		data.put("confirmSql", confirmSql);
		data.put("resumeToken", state.getResumeToken());
		data.put("sqlSecurityMessage", state.getSqlSecurityMessage());
		emitter.emit("human_confirm", "human_confirm_required", "pending",
				"Waiting for human confirmation before SQL execution", data);
	}

}
