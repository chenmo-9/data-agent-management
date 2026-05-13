package com.alibaba.cloud.ai.dataagent.mapper;

import com.alibaba.cloud.ai.dataagent.entity.GraphEventHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GraphEventHistoryMapper {

	@Insert("""
			INSERT INTO graph_event (run_id, session_id, event_id, node_name, event_type, status, message,
			                         data_json, error_message, event_time, created_at)
			VALUES (#{runId}, #{sessionId}, #{eventId}, #{nodeName}, #{eventType}, #{status}, #{message},
			        #{dataJson}, #{errorMessage}, #{eventTime}, #{createdAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(GraphEventHistory event);

	@Select("SELECT * FROM graph_event WHERE run_id = #{runId} ORDER BY id ASC")
	List<GraphEventHistory> selectByRunId(String runId);

	@Delete("DELETE FROM graph_event WHERE run_id = #{runId}")
	int deleteByRunId(String runId);

}
