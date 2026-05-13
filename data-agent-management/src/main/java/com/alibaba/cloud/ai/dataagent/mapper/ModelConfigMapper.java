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

import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ModelConfigMapper {

	@Insert("""
			INSERT INTO model_config (name, provider, model_name, model_type, base_url, api_key, temperature,
			                          max_tokens, enabled, description, created_at, updated_at)
			VALUES (#{name}, #{provider}, #{modelName}, #{modelType}, #{baseUrl}, #{apiKey}, #{temperature},
			        #{maxTokens}, #{enabled}, #{description}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(ModelConfig modelConfig);

	@Select("""
			SELECT * FROM model_config WHERE id = #{id}
			""")
	ModelConfig selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM model_config
				<where>
					<if test='keyword != null and keyword != ""'>
						AND (name LIKE CONCAT('%', #{keyword}, '%')
							 OR provider LIKE CONCAT('%', #{keyword}, '%')
							 OR model_name LIKE CONCAT('%', #{keyword}, '%'))
					</if>
					<if test='modelType != null and modelType != ""'>
						AND model_type = #{modelType}
					</if>
					<if test='enabled != null'>
						AND enabled = #{enabled}
					</if>
				</where>
				ORDER BY created_at DESC
			</script>
			""")
	List<ModelConfig> selectList(@Param("keyword") String keyword, @Param("modelType") String modelType,
			@Param("enabled") Boolean enabled);

	@Update("""
			UPDATE model_config
			SET name = #{name},
			    provider = #{provider},
			    model_name = #{modelName},
			    model_type = #{modelType},
			    base_url = #{baseUrl},
			    api_key = #{apiKey},
			    temperature = #{temperature},
			    max_tokens = #{maxTokens},
			    enabled = #{enabled},
			    description = #{description},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int updateById(ModelConfig modelConfig);

	@Delete("""
			DELETE FROM model_config WHERE id = #{id}
			""")
	int deleteById(Long id);

	@Select("""
			SELECT COUNT(1) FROM model_config WHERE name = #{name}
			""")
	int countByName(String name);

	@Select("""
			SELECT COUNT(1) FROM model_config WHERE name = #{name} AND id != #{id}
			""")
	int countByNameExcludeId(@Param("name") String name, @Param("id") Long id);

}
