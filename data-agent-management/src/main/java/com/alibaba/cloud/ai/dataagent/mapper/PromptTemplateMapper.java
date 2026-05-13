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

import com.alibaba.cloud.ai.dataagent.entity.PromptTemplate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PromptTemplateMapper {

	@Insert("""
			INSERT INTO prompt_template (prompt_key, name, scene, content, version, enabled, description, created_at, updated_at)
			VALUES (#{promptKey}, #{name}, #{scene}, #{content}, #{version}, #{enabled}, #{description}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(PromptTemplate template);

	@Select("SELECT * FROM prompt_template WHERE id = #{id}")
	PromptTemplate selectById(Long id);

	@Select("SELECT * FROM prompt_template WHERE prompt_key = #{promptKey} AND version = #{version}")
	PromptTemplate selectByPromptKeyAndVersion(@Param("promptKey") String promptKey, @Param("version") String version);

	@Select("""
			<script>
				SELECT * FROM prompt_template
				<where>
					<if test='keyword != null and keyword != ""'>
						AND (prompt_key LIKE CONCAT('%', #{keyword}, '%')
							 OR name LIKE CONCAT('%', #{keyword}, '%')
							 OR scene LIKE CONCAT('%', #{keyword}, '%')
							 OR description LIKE CONCAT('%', #{keyword}, '%'))
					</if>
					<if test='promptKey != null and promptKey != ""'>
						AND prompt_key = #{promptKey}
					</if>
					<if test='scene != null and scene != ""'>
						AND scene = #{scene}
					</if>
					<if test='enabled != null'>
						AND enabled = #{enabled}
					</if>
				</where>
				ORDER BY created_at DESC
			</script>
			""")
	List<PromptTemplate> selectList(@Param("keyword") String keyword, @Param("promptKey") String promptKey,
			@Param("scene") String scene, @Param("enabled") Boolean enabled);

	@Update("""
			UPDATE prompt_template
			SET prompt_key = #{promptKey},
			    name = #{name},
			    scene = #{scene},
			    content = #{content},
			    version = #{version},
			    enabled = #{enabled},
			    description = #{description},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int updateById(PromptTemplate template);

	@Update("UPDATE prompt_template SET enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
	int updateEnabledById(@Param("id") Long id, @Param("enabled") Boolean enabled);

	@Delete("DELETE FROM prompt_template WHERE id = #{id}")
	int deleteById(Long id);

	@Select("SELECT COUNT(1) FROM prompt_template WHERE prompt_key = #{promptKey} AND version = #{version}")
	int countByPromptKeyAndVersion(@Param("promptKey") String promptKey, @Param("version") String version);

	@Select("""
			SELECT COUNT(1) FROM prompt_template
			WHERE prompt_key = #{promptKey} AND version = #{version} AND id != #{id}
			""")
	int countByPromptKeyAndVersionExcludeId(@Param("promptKey") String promptKey, @Param("version") String version,
			@Param("id") Long id);

}
