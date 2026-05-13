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

import com.alibaba.cloud.ai.dataagent.entity.SemanticField;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SemanticFieldMapper {

	@Insert("""
			INSERT INTO semantic_field (table_id, datasource_id, table_name, field_name, business_name, data_type,
			                            description, synonyms, example_value, primary_key, nullable, enabled,
			                            created_at, updated_at)
			VALUES (#{tableId}, #{datasourceId}, #{tableName}, #{fieldName}, #{businessName}, #{dataType},
			        #{description}, #{synonyms}, #{exampleValue}, #{primaryKey}, #{nullable}, #{enabled},
			        #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(SemanticField field);

	@Select("SELECT * FROM semantic_field WHERE id = #{id}")
	SemanticField selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM semantic_field
				<where>
					<if test='tableId != null'>
						AND table_id = #{tableId}
					</if>
					<if test='datasourceId != null'>
						AND datasource_id = #{datasourceId}
					</if>
					<if test='tableName != null and tableName != ""'>
						AND table_name = #{tableName}
					</if>
					<if test='keyword != null and keyword != ""'>
						AND (field_name LIKE CONCAT('%', #{keyword}, '%')
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
	List<SemanticField> selectList(@Param("tableId") Long tableId, @Param("datasourceId") Long datasourceId,
			@Param("tableName") String tableName, @Param("keyword") String keyword, @Param("enabled") Boolean enabled);

	@Select("SELECT * FROM semantic_field WHERE table_id = #{tableId} ORDER BY created_at DESC")
	List<SemanticField> selectByTableId(Long tableId);

	@Select("SELECT * FROM semantic_field WHERE datasource_id = #{datasourceId} ORDER BY created_at DESC")
	List<SemanticField> selectByDatasourceId(Long datasourceId);

	@Select("SELECT * FROM semantic_field WHERE table_id = #{tableId} AND field_name = #{fieldName}")
	SemanticField selectByTableIdAndFieldName(@Param("tableId") Long tableId, @Param("fieldName") String fieldName);

	@Update("""
			UPDATE semantic_field
			SET table_id = #{tableId},
			    datasource_id = #{datasourceId},
			    table_name = #{tableName},
			    field_name = #{fieldName},
			    business_name = #{businessName},
			    data_type = #{dataType},
			    description = #{description},
			    synonyms = #{synonyms},
			    example_value = #{exampleValue},
			    primary_key = #{primaryKey},
			    nullable = #{nullable},
			    enabled = #{enabled},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int updateById(SemanticField field);

	@Update("UPDATE semantic_field SET enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
	int updateEnabledById(@Param("id") Long id, @Param("enabled") Boolean enabled);

	@Delete("DELETE FROM semantic_field WHERE id = #{id}")
	int deleteById(Long id);

	@Delete("DELETE FROM semantic_field WHERE table_id = #{tableId}")
	int deleteByTableId(Long tableId);

	@Select("SELECT COUNT(1) FROM semantic_field WHERE table_id = #{tableId} AND field_name = #{fieldName}")
	int countByTableIdAndFieldName(@Param("tableId") Long tableId, @Param("fieldName") String fieldName);

	@Select("SELECT COUNT(1) FROM semantic_field WHERE table_id = #{tableId} AND field_name = #{fieldName} AND id != #{id}")
	int countByTableIdAndFieldNameExcludeId(@Param("tableId") Long tableId, @Param("fieldName") String fieldName,
			@Param("id") Long id);

}
