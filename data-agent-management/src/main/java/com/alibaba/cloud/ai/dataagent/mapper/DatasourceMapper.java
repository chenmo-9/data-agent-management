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

import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DatasourceMapper {

	@Insert("""
			INSERT INTO datasource (name, db_type, url, username, password, database_name, host, port,
			                        enabled, description, created_at, updated_at)
			VALUES (#{name}, #{dbType}, #{url}, #{username}, #{password}, #{databaseName}, #{host}, #{port},
			        #{enabled}, #{description}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(Datasource datasource);

	@Select("""
			SELECT * FROM datasource WHERE id = #{id}
			""")
	Datasource selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM datasource
				<where>
					<if test='keyword != null and keyword != ""'>
						AND (name LIKE CONCAT('%', #{keyword}, '%')
							 OR db_type LIKE CONCAT('%', #{keyword}, '%')
							 OR database_name LIKE CONCAT('%', #{keyword}, '%')
							 OR host LIKE CONCAT('%', #{keyword}, '%'))
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
	List<Datasource> selectList(@Param("keyword") String keyword, @Param("dbType") String dbType,
			@Param("enabled") Boolean enabled);

	@Update("""
			UPDATE datasource
			SET name = #{name},
			    db_type = #{dbType},
			    url = #{url},
			    username = #{username},
			    password = #{password},
			    database_name = #{databaseName},
			    host = #{host},
			    port = #{port},
			    enabled = #{enabled},
			    description = #{description},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int updateById(Datasource datasource);

	@Delete("""
			DELETE FROM datasource WHERE id = #{id}
			""")
	int deleteById(Long id);

	@Select("""
			SELECT COUNT(1) FROM datasource WHERE name = #{name}
			""")
	int countByName(String name);

	@Select("""
			SELECT COUNT(1) FROM datasource WHERE name = #{name} AND id != #{id}
			""")
	int countByNameExcludeId(@Param("name") String name, @Param("id") Long id);

}
