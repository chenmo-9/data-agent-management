/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.mapper;

import com.alibaba.cloud.ai.dataagent.entity.AgentDatasource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AgentDatasourceMapper {

	@Insert("""
			INSERT INTO agent_datasource (agent_id, datasource_id, datasource_name, db_type, enabled, created_at, updated_at)
			VALUES (#{agentId}, #{datasourceId}, #{datasourceName}, #{dbType}, #{enabled}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(AgentDatasource agentDatasource);

	@Select("""
			SELECT * FROM agent_datasource WHERE id = #{id}
			""")
	AgentDatasource selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM agent_datasource
				<where>
					<if test='agentId != null'>
						AND agent_id = #{agentId}
					</if>
					<if test='datasourceId != null'>
						AND datasource_id = #{datasourceId}
					</if>
					<if test='dbType != null and dbType != ""'>
						AND db_type = #{dbType}
					</if>
					<if test='enabled != null'>
						AND enabled = #{enabled}
					</if>
				</where>
				ORDER BY created_at DESC
			</script>
			""")
	List<AgentDatasource> selectList(@Param("agentId") Long agentId, @Param("datasourceId") Long datasourceId,
			@Param("dbType") String dbType, @Param("enabled") Boolean enabled);

	@Select("""
			SELECT * FROM agent_datasource WHERE agent_id = #{agentId} ORDER BY created_at DESC
			""")
	List<AgentDatasource> selectByAgentId(Long agentId);

	@Select("""
			SELECT * FROM agent_datasource WHERE datasource_id = #{datasourceId} ORDER BY created_at DESC
			""")
	List<AgentDatasource> selectByDatasourceId(Long datasourceId);

	@Select("""
			SELECT * FROM agent_datasource WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}
			""")
	AgentDatasource selectByAgentIdAndDatasourceId(@Param("agentId") Long agentId,
			@Param("datasourceId") Long datasourceId);

	@Update("""
			UPDATE agent_datasource
			SET enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP
			WHERE id = #{id}
			""")
	int updateEnabledById(@Param("id") Long id, @Param("enabled") Boolean enabled);

	@Delete("""
			DELETE FROM agent_datasource WHERE id = #{id}
			""")
	int deleteById(Long id);

	@Delete("""
			DELETE FROM agent_datasource WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}
			""")
	int deleteByAgentIdAndDatasourceId(@Param("agentId") Long agentId, @Param("datasourceId") Long datasourceId);

	@Select("""
			SELECT COUNT(1) FROM agent_datasource WHERE agent_id = #{agentId} AND datasource_id = #{datasourceId}
			""")
	int countByAgentIdAndDatasourceId(@Param("agentId") Long agentId, @Param("datasourceId") Long datasourceId);

}
