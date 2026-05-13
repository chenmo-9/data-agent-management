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

import com.alibaba.cloud.ai.dataagent.entity.Agent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AgentMapper {

	@Insert("""
			INSERT INTO agent (name, description, avatar, category, tags, prompt, preset_questions, status, admin_id, created_at, updated_at)
			VALUES (#{name}, #{description}, #{avatar}, #{category}, #{tags}, #{prompt}, #{presetQuestions}, #{status}, #{adminId}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(Agent agent);

	@Select("""
			SELECT * FROM agent WHERE id = #{id}
			""")
	Agent selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM agent
				<where>
					<if test='keyword != null and keyword != ""'>
						AND (name LIKE CONCAT('%', #{keyword}, '%')
							 OR description LIKE CONCAT('%', #{keyword}, '%'))
					</if>
					<if test='status != null and status != ""'>
						AND status = #{status}
					</if>
					<if test='category != null and category != ""'>
						AND category = #{category}
					</if>
				</where>
				ORDER BY created_at DESC
			</script>
			""")
	List<Agent> selectList(@Param("keyword") String keyword, @Param("status") String status,
			@Param("category") String category);

	@Update("""
			UPDATE agent
			SET name = #{name},
			    description = #{description},
			    avatar = #{avatar},
			    category = #{category},
			    tags = #{tags},
			    prompt = #{prompt},
			    preset_questions = #{presetQuestions},
			    status = #{status},
			    admin_id = #{adminId},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int updateById(Agent agent);

	@Delete("""
			DELETE FROM agent WHERE id = #{id}
			""")
	int deleteById(Long id);

}
