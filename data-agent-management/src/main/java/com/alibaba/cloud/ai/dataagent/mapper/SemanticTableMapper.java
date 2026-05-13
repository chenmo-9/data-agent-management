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

import com.alibaba.cloud.ai.dataagent.entity.SemanticTable;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SemanticTableMapper {

	@Insert("""
			INSERT INTO semantic_table (datasource_id, table_name, business_name, description, synonyms, enabled,
			                            created_at, updated_at)
			VALUES (#{datasourceId}, #{tableName}, #{businessName}, #{description}, #{synonyms}, #{enabled},
			        #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(SemanticTable table);

	@Select("SELECT * FROM semantic_table WHERE id = #{id}")
	SemanticTable selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM semantic_table
				<where>
					<if test='datasourceId != null'>
						AND datasource_id = #{datasourceId}
					</if>
					<if test='keyword != null and keyword != ""'>
						AND (table_name LIKE CONCAT('%', #{keyword}, '%')
							 OR business_name LIKE CONCAT('%', #{keyword}, '%')
							 OR description LIKE CONCAT('%', #{keyword}, '%')
							 OR synonyms LIKE CONCAT('%', #{keyword}, '%'))
					</if>
					<if test='enabled != null'>
						AND enabled = #{enabled}
					</if>
				</where>
				ORDER BY created_at DESC
			</script>
			""")
	List<SemanticTable> selectList(@Param("datasourceId") Long datasourceId, @Param("keyword") String keyword,
			@Param("enabled") Boolean enabled);

	@Select("SELECT * FROM semantic_table WHERE datasource_id = #{datasourceId} ORDER BY created_at DESC")
	List<SemanticTable> selectByDatasourceId(Long datasourceId);

	@Select("SELECT * FROM semantic_table WHERE datasource_id = #{datasourceId} AND table_name = #{tableName}")
	SemanticTable selectByDatasourceIdAndTableName(@Param("datasourceId") Long datasourceId,
			@Param("tableName") String tableName);

	@Update("""
			UPDATE semantic_table
			SET datasource_id = #{datasourceId},
			    table_name = #{tableName},
			    business_name = #{businessName},
			    description = #{description},
			    synonyms = #{synonyms},
			    enabled = #{enabled},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int updateById(SemanticTable table);

	@Update("UPDATE semantic_table SET enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
	int updateEnabledById(@Param("id") Long id, @Param("enabled") Boolean enabled);

	@Delete("DELETE FROM semantic_table WHERE id = #{id}")
	int deleteById(Long id);

	@Select("SELECT COUNT(1) FROM semantic_table WHERE datasource_id = #{datasourceId} AND table_name = #{tableName}")
	int countByDatasourceIdAndTableName(@Param("datasourceId") Long datasourceId, @Param("tableName") String tableName);

	@Select("""
			SELECT COUNT(1) FROM semantic_table
			WHERE datasource_id = #{datasourceId} AND table_name = #{tableName} AND id != #{id}
			""")
	int countByDatasourceIdAndTableNameExcludeId(@Param("datasourceId") Long datasourceId,
			@Param("tableName") String tableName, @Param("id") Long id);

}
