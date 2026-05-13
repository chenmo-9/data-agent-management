package com.alibaba.cloud.ai.dataagent.mapper;

import com.alibaba.cloud.ai.dataagent.entity.GraphRunHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GraphRunHistoryMapper {

	@Insert("""
			INSERT INTO graph_run (run_id, session_id, agent_id, model_config_id, mode, question, status, success,
			                       started_at, created_at, updated_at)
			VALUES (#{runId}, #{sessionId}, #{agentId}, #{modelConfigId}, #{mode}, #{question}, #{status}, #{success},
			        #{startedAt}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(GraphRunHistory history);

	@Update("""
			UPDATE graph_run
			SET session_id = #{sessionId},
			    agent_id = #{agentId},
			    agent_name = #{agentName},
			    model_config_id = #{modelConfigId},
			    model_name = #{modelName},
			    datasource_id = #{datasourceId},
			    datasource_name = #{datasourceName},
			    mode = #{mode},
			    question = #{question},
			    answer = #{answer},
			    status = #{status},
			    success = #{success},
			    started_at = #{startedAt},
			    finished_at = #{finishedAt},
			    duration_ms = #{durationMs},
			    event_count = #{eventCount},
			    failed_node = #{failedNode},
			    error_message = #{errorMessage},
			    generated_sql = #{generatedSql},
			    extracted_sql = #{extractedSql},
			    repaired_sql = #{repairedSql},
			    validated_sql = #{validatedSql},
			    sanitized_sql = #{sanitizedSql},
			    sql_limited = #{sqlLimited},
			    sql_limit = #{sqlLimit},
			    sql_result_truncated = #{sqlResultTruncated},
			    sql_query_timeout_seconds = #{sqlQueryTimeoutSeconds},
			    sql_security_message = #{sqlSecurityMessage},
			    row_count = #{rowCount},
			    result_preview_json = #{resultPreviewJson},
			    recalled_table_count = #{recalledTableCount},
			    recalled_field_count = #{recalledFieldCount},
			    recalled_relation_count = #{recalledRelationCount},
			    recalled_knowledge_count = #{recalledKnowledgeCount},
			    report_title = #{reportTitle},
			    report_markdown = #{reportMarkdown},
			    chart_type = #{chartType},
			    report_summary = #{reportSummary},
			    sql_validation_error = #{sqlValidationError},
			    sql_execution_error = #{sqlExecutionError},
			    python_engine = #{pythonEngine},
			    python_executed = #{pythonExecuted},
			    python_success = #{pythonSuccess},
			    python_duration_ms = #{pythonDurationMs},
			    python_fallback_used = #{pythonFallbackUsed},
			    python_error_message = #{pythonErrorMessage},
			    confirm_required = #{confirmRequired},
			    confirm_status = #{confirmStatus},
			    confirm_sql = #{confirmSql},
			    confirmed_sql = #{confirmedSql},
			    confirmed_by = #{confirmedBy},
			    confirmed_at = #{confirmedAt},
			    canceled_at = #{canceledAt},
			    cancel_reason = #{cancelReason},
			    resume_token = #{resumeToken},
			    pending_payload_json = #{pendingPayloadJson},
			    updated_at = #{updatedAt}
			WHERE run_id = #{runId}
			""")
	int updateByRunId(GraphRunHistory history);

	@Update("""
			UPDATE graph_run
			SET status = #{status},
			    success = #{success},
			    confirm_required = #{confirmRequired},
			    confirm_status = #{confirmStatus},
			    confirm_sql = #{confirmSql},
			    resume_token = #{resumeToken},
			    pending_payload_json = #{pendingPayloadJson},
			    datasource_id = #{datasourceId},
			    datasource_name = #{datasourceName},
			    generated_sql = #{generatedSql},
			    extracted_sql = #{extractedSql},
			    repaired_sql = #{repairedSql},
			    validated_sql = #{validatedSql},
			    sanitized_sql = #{sanitizedSql},
			    sql_security_message = #{sqlSecurityMessage},
			    updated_at = #{updatedAt}
			WHERE run_id = #{runId}
			""")
	int updatePendingConfirm(GraphRunHistory history);

	@Update("""
			UPDATE graph_run
			SET status = #{status},
			    confirm_status = #{confirmStatus},
			    confirmed_sql = #{confirmedSql},
			    confirmed_by = #{confirmedBy},
			    confirmed_at = #{confirmedAt},
			    updated_at = #{updatedAt}
			WHERE run_id = #{runId}
			""")
	int updateConfirm(GraphRunHistory history);

	@Update("""
			UPDATE graph_run
			SET status = 'canceled',
			    success = FALSE,
			    confirm_status = 'canceled',
			    canceled_at = #{canceledAt},
			    cancel_reason = #{cancelReason},
			    error_message = #{errorMessage},
			    updated_at = #{updatedAt}
			WHERE run_id = #{runId}
			""")
	int updateCancel(GraphRunHistory history);

	@Select("SELECT * FROM graph_run WHERE run_id = #{runId}")
	GraphRunHistory selectByRunId(String runId);

	@Select("SELECT * FROM graph_run WHERE id = #{id}")
	GraphRunHistory selectById(Long id);

	@Select("""
			<script>
			SELECT * FROM graph_run
			<where>
				<if test='agentId != null'>AND agent_id = #{agentId}</if>
				<if test='modelConfigId != null'>AND model_config_id = #{modelConfigId}</if>
				<if test='mode != null and mode != ""'>AND mode = #{mode}</if>
				<if test='status != null and status != ""'>AND status = #{status}</if>
				<if test='success != null'>AND success = #{success}</if>
				<if test='keyword != null and keyword != ""'>
					AND (question LIKE CONCAT('%', #{keyword}, '%')
						OR answer LIKE CONCAT('%', #{keyword}, '%')
						OR generated_sql LIKE CONCAT('%', #{keyword}, '%')
						OR validated_sql LIKE CONCAT('%', #{keyword}, '%'))
				</if>
				<if test='startTime != null'>AND created_at &gt;= #{startTime}</if>
				<if test='endTime != null'>AND created_at &lt;= #{endTime}</if>
			</where>
			ORDER BY created_at DESC
			LIMIT #{limit} OFFSET #{offset}
			</script>
			""")
	List<GraphRunHistory> selectList(@Param("agentId") Long agentId, @Param("modelConfigId") Long modelConfigId,
			@Param("mode") String mode, @Param("status") String status, @Param("success") Boolean success,
			@Param("keyword") String keyword, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("limit") int limit, @Param("offset") int offset);

	@Select("""
			<script>
			SELECT COUNT(1) FROM graph_run
			<where>
				<if test='agentId != null'>AND agent_id = #{agentId}</if>
				<if test='modelConfigId != null'>AND model_config_id = #{modelConfigId}</if>
				<if test='mode != null and mode != ""'>AND mode = #{mode}</if>
				<if test='status != null and status != ""'>AND status = #{status}</if>
				<if test='success != null'>AND success = #{success}</if>
				<if test='keyword != null and keyword != ""'>
					AND (question LIKE CONCAT('%', #{keyword}, '%')
						OR answer LIKE CONCAT('%', #{keyword}, '%')
						OR generated_sql LIKE CONCAT('%', #{keyword}, '%')
						OR validated_sql LIKE CONCAT('%', #{keyword}, '%'))
				</if>
				<if test='startTime != null'>AND created_at &gt;= #{startTime}</if>
				<if test='endTime != null'>AND created_at &lt;= #{endTime}</if>
			</where>
			</script>
			""")
	int countList(@Param("agentId") Long agentId, @Param("modelConfigId") Long modelConfigId, @Param("mode") String mode,
			@Param("status") String status, @Param("success") Boolean success, @Param("keyword") String keyword,
			@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

	@Delete("DELETE FROM graph_run WHERE run_id = #{runId}")
	int deleteByRunId(String runId);

}
