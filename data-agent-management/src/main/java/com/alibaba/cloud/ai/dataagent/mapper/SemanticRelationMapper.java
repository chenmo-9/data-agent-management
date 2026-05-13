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

import com.alibaba.cloud.ai.dataagent.entity.SemanticRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SemanticRelationMapper {

	@Insert("""
			INSERT INTO semantic_relation (datasource_id, source_table_id, source_table_name, source_field_id, source_field_name,
			                              target_table_id, target_table_name, target_field_id, target_field_name,
			                              relation_type, join_type, description, enabled, created_at, updated_at)
			VALUES (#{datasourceId}, #{sourceTableId}, #{sourceTableName}, #{sourceFieldId}, #{sourceFieldName},
			        #{targetTableId}, #{targetTableName}, #{targetFieldId}, #{targetFieldName},
			        #{relationType}, #{joinType}, #{description}, #{enabled}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(SemanticRelation relation);

	@Select("SELECT * FROM semantic_relation WHERE id = #{id}")
	SemanticRelation selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM semantic_relation
				<where>
					<if test='datasourceId != null'>
						AND datasource_id = #{datasourceId}
					</if>
					<if test='tableId != null'>
						AND (source_table_id = #{tableId} OR target_table_id = #{tableId})
					</if>
					<if test='enabled != null'>
						AND enabled = #{enabled}
					</if>
					<if test='keyword != null and keyword != ""'>
						AND (source_table_name LIKE CONCAT('%', #{keyword}, '%')
						     OR source_field_name LIKE CONCAT('%', #{keyword}, '%')
						     OR target_table_name LIKE CONCAT('%', #{keyword}, '%')
						     OR target_field_name LIKE CONCAT('%', #{keyword}, '%')
						     OR relation_type LIKE CONCAT('%', #{keyword}, '%')
						     OR join_type LIKE CONCAT('%', #{keyword}, '%')
						     OR description LIKE CONCAT('%', #{keyword}, '%'))
					</if>
				</where>
				ORDER BY created_at DESC
			</script>
			""")
	List<SemanticRelation> selectList(@Param("datasourceId") Long datasourceId, @Param("tableId") Long tableId,
			@Param("enabled") Boolean enabled, @Param("keyword") String keyword);

	@Select("SELECT * FROM semantic_relation WHERE datasource_id = #{datasourceId} ORDER BY created_at DESC")
	List<SemanticRelation> selectByDatasourceId(Long datasourceId);

	@Select("""
			<script>
				SELECT * FROM semantic_relation
				WHERE datasource_id = #{datasourceId}
				  AND enabled = TRUE
				  AND (
				        source_table_id IN
				        <foreach collection='tableIds' item='tableId' open='(' separator=',' close=')'>
				        	#{tableId}
				        </foreach>
				        OR target_table_id IN
				        <foreach collection='tableIds' item='tableId' open='(' separator=',' close=')'>
				        	#{tableId}
				        </foreach>
				  )
				ORDER BY created_at DESC
			</script>
			""")
	List<SemanticRelation> selectByTableIds(@Param("datasourceId") Long datasourceId,
			@Param("tableIds") List<Long> tableIds);

	@Update("""
			UPDATE semantic_relation
			SET datasource_id = #{datasourceId},
			    source_table_id = #{sourceTableId},
			    source_table_name = #{sourceTableName},
			    source_field_id = #{sourceFieldId},
			    source_field_name = #{sourceFieldName},
			    target_table_id = #{targetTableId},
			    target_table_name = #{targetTableName},
			    target_field_id = #{targetFieldId},
			    target_field_name = #{targetFieldName},
			    relation_type = #{relationType},
			    join_type = #{joinType},
			    description = #{description},
			    enabled = #{enabled},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int updateById(SemanticRelation relation);

	@Delete("DELETE FROM semantic_relation WHERE id = #{id}")
	int deleteById(Long id);

	@Delete("DELETE FROM semantic_relation WHERE source_table_id = #{tableId} OR target_table_id = #{tableId}")
	int deleteByTableId(Long tableId);

	@Delete("DELETE FROM semantic_relation WHERE source_field_id = #{fieldId} OR target_field_id = #{fieldId}")
	int deleteByFieldId(Long fieldId);

	@Update("UPDATE semantic_relation SET enabled = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
	int enableById(Long id);

	@Update("UPDATE semantic_relation SET enabled = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
	int disableById(Long id);

	@Select("""
			SELECT COUNT(1) FROM semantic_relation
			WHERE datasource_id = #{datasourceId}
			  AND (
			        (source_table_id = #{sourceTableId} AND source_field_id = #{sourceFieldId}
			         AND target_table_id = #{targetTableId} AND target_field_id = #{targetFieldId})
			        OR
			        (source_table_id = #{targetTableId} AND source_field_id = #{targetFieldId}
			         AND target_table_id = #{sourceTableId} AND target_field_id = #{sourceFieldId})
			  )
			""")
	int countDuplicate(@Param("datasourceId") Long datasourceId, @Param("sourceTableId") Long sourceTableId,
			@Param("sourceFieldId") Long sourceFieldId, @Param("targetTableId") Long targetTableId,
			@Param("targetFieldId") Long targetFieldId);

	@Select("""
			SELECT COUNT(1) FROM semantic_relation
			WHERE datasource_id = #{datasourceId}
			  AND id != #{id}
			  AND (
			        (source_table_id = #{sourceTableId} AND source_field_id = #{sourceFieldId}
			         AND target_table_id = #{targetTableId} AND target_field_id = #{targetFieldId})
			        OR
			        (source_table_id = #{targetTableId} AND source_field_id = #{targetFieldId}
			         AND target_table_id = #{sourceTableId} AND target_field_id = #{sourceFieldId})
			  )
			""")
	int countDuplicateExcludeId(@Param("id") Long id, @Param("datasourceId") Long datasourceId,
			@Param("sourceTableId") Long sourceTableId, @Param("sourceFieldId") Long sourceFieldId,
			@Param("targetTableId") Long targetTableId, @Param("targetFieldId") Long targetFieldId);

}
