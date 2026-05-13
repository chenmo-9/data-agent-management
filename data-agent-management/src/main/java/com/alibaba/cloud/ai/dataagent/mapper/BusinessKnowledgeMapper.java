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

import com.alibaba.cloud.ai.dataagent.entity.BusinessKnowledge;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BusinessKnowledgeMapper {

	@Insert("""
			INSERT INTO business_knowledge (title, content, knowledge_type, source_type, file_name, file_path, file_size,
			                                enabled, created_at, updated_at)
			VALUES (#{title}, #{content}, #{knowledgeType}, #{sourceType}, #{fileName}, #{filePath}, #{fileSize},
			        #{enabled}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(BusinessKnowledge knowledge);

	@Select("SELECT * FROM business_knowledge WHERE id = #{id}")
	BusinessKnowledge selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM business_knowledge
				<where>
					<if test='keyword != null and keyword != ""'>
						AND (title LIKE CONCAT('%', #{keyword}, '%')
							 OR content LIKE CONCAT('%', #{keyword}, '%')
							 OR knowledge_type LIKE CONCAT('%', #{keyword}, '%'))
					</if>
					<if test='knowledgeType != null and knowledgeType != ""'>
						AND knowledge_type = #{knowledgeType}
					</if>
					<if test='sourceType != null and sourceType != ""'>
						AND source_type = #{sourceType}
					</if>
					<if test='enabled != null'>
						AND enabled = #{enabled}
					</if>
				</where>
				ORDER BY created_at DESC
			</script>
			""")
	List<BusinessKnowledge> selectList(@Param("keyword") String keyword, @Param("knowledgeType") String knowledgeType,
			@Param("sourceType") String sourceType, @Param("enabled") Boolean enabled);

	@Update("""
			UPDATE business_knowledge
			SET title = #{title},
			    content = #{content},
			    knowledge_type = #{knowledgeType},
			    source_type = #{sourceType},
			    file_name = #{fileName},
			    file_path = #{filePath},
			    file_size = #{fileSize},
			    enabled = #{enabled},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int updateById(BusinessKnowledge knowledge);

	@Update("UPDATE business_knowledge SET enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
	int updateEnabledById(@Param("id") Long id, @Param("enabled") Boolean enabled);

	@Delete("DELETE FROM business_knowledge WHERE id = #{id}")
	int deleteById(Long id);

	@Select("SELECT COUNT(1) FROM business_knowledge WHERE title = #{title}")
	int countByTitle(String title);

	@Select("SELECT COUNT(1) FROM business_knowledge WHERE title = #{title} AND id != #{id}")
	int countByTitleExcludeId(@Param("title") String title, @Param("id") Long id);

}
