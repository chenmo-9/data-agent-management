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

import com.alibaba.cloud.ai.dataagent.entity.AgentKnowledge;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AgentKnowledgeMapper {

	@Insert("""
			INSERT INTO agent_knowledge (agent_id, knowledge_id, knowledge_title, enabled, created_at, updated_at)
			VALUES (#{agentId}, #{knowledgeId}, #{knowledgeTitle}, #{enabled}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(AgentKnowledge knowledge);

	@Select("SELECT * FROM agent_knowledge WHERE id = #{id}")
	AgentKnowledge selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM agent_knowledge
				<where>
					<if test='agentId != null'>
						AND agent_id = #{agentId}
					</if>
					<if test='knowledgeId != null'>
						AND knowledge_id = #{knowledgeId}
					</if>
					<if test='enabled != null'>
						AND enabled = #{enabled}
					</if>
				</where>
				ORDER BY created_at DESC
			</script>
			""")
	List<AgentKnowledge> selectList(@Param("agentId") Long agentId, @Param("knowledgeId") Long knowledgeId,
			@Param("enabled") Boolean enabled);

	@Select("SELECT * FROM agent_knowledge WHERE agent_id = #{agentId} ORDER BY created_at DESC")
	List<AgentKnowledge> selectByAgentId(Long agentId);

	@Select("SELECT * FROM agent_knowledge WHERE knowledge_id = #{knowledgeId} ORDER BY created_at DESC")
	List<AgentKnowledge> selectByKnowledgeId(Long knowledgeId);

	@Select("SELECT COUNT(1) FROM agent_knowledge WHERE agent_id = #{agentId} AND knowledge_id = #{knowledgeId}")
	int countByAgentIdAndKnowledgeId(@Param("agentId") Long agentId, @Param("knowledgeId") Long knowledgeId);

	@Update("UPDATE agent_knowledge SET enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
	int updateEnabledById(@Param("id") Long id, @Param("enabled") Boolean enabled);

	@Delete("DELETE FROM agent_knowledge WHERE id = #{id}")
	int deleteById(Long id);

	@Delete("DELETE FROM agent_knowledge WHERE agent_id = #{agentId} AND knowledge_id = #{knowledgeId}")
	int deleteByAgentIdAndKnowledgeId(@Param("agentId") Long agentId, @Param("knowledgeId") Long knowledgeId);

	@Delete("DELETE FROM agent_knowledge WHERE knowledge_id = #{knowledgeId}")
	int deleteByKnowledgeId(Long knowledgeId);

}
