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

import com.alibaba.cloud.ai.dataagent.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper {

	@Insert("""
			INSERT INTO knowledge_chunk (knowledge_id, chunk_index, content, embedding_model_config_id, embedding_dimension,
			                             embedding_vector, embedding_status, embedding_error, embedded_at,
			                             enabled, created_at, updated_at)
			VALUES (#{knowledgeId}, #{chunkIndex}, #{content}, #{embeddingModelConfigId}, #{embeddingDimension},
			        #{embeddingVector}, #{embeddingStatus}, #{embeddingError}, #{embeddedAt},
			        #{enabled}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(KnowledgeChunk chunk);

	@Insert("""
			<script>
				INSERT INTO knowledge_chunk (knowledge_id, chunk_index, content, embedding_model_config_id, embedding_dimension,
				                             embedding_vector, embedding_status, embedding_error, embedded_at,
				                             enabled, created_at, updated_at)
				VALUES
				<foreach collection='chunks' item='chunk' separator=','>
					(#{chunk.knowledgeId}, #{chunk.chunkIndex}, #{chunk.content},
					 #{chunk.embeddingModelConfigId}, #{chunk.embeddingDimension}, #{chunk.embeddingVector},
					 #{chunk.embeddingStatus}, #{chunk.embeddingError}, #{chunk.embeddedAt},
					 #{chunk.enabled}, #{chunk.createdAt}, #{chunk.updatedAt})
				</foreach>
			</script>
			""")
	int batchInsert(@Param("chunks") List<KnowledgeChunk> chunks);

	@Select("SELECT * FROM knowledge_chunk WHERE id = #{id}")
	KnowledgeChunk selectById(Long id);

	@Select("""
			<script>
				SELECT * FROM knowledge_chunk
				<where>
					<if test='knowledgeId != null'>
						AND knowledge_id = #{knowledgeId}
					</if>
					<if test='enabled != null'>
						AND enabled = #{enabled}
					</if>
				</where>
				ORDER BY chunk_index ASC
			</script>
			""")
	List<KnowledgeChunk> selectList(@Param("knowledgeId") Long knowledgeId, @Param("enabled") Boolean enabled);

	@Select("SELECT * FROM knowledge_chunk WHERE knowledge_id = #{knowledgeId} ORDER BY chunk_index ASC")
	List<KnowledgeChunk> selectByKnowledgeId(Long knowledgeId);

	@Select("""
			SELECT kc.*
			FROM knowledge_chunk kc
			INNER JOIN business_knowledge bk ON bk.id = kc.knowledge_id
			INNER JOIN agent_knowledge ak ON ak.knowledge_id = bk.id
			WHERE ak.agent_id = #{agentId}
			  AND ak.enabled = TRUE
			  AND bk.enabled = TRUE
			  AND kc.enabled = TRUE
			ORDER BY kc.knowledge_id ASC, kc.chunk_index ASC
			""")
	List<KnowledgeChunk> selectChunksByAgentId(Long agentId);

	@Select("""
			SELECT kc.*
			FROM knowledge_chunk kc
			INNER JOIN business_knowledge bk ON bk.id = kc.knowledge_id
			INNER JOIN agent_knowledge ak ON ak.knowledge_id = bk.id
			WHERE ak.agent_id = #{agentId}
			  AND ak.enabled = TRUE
			  AND bk.enabled = TRUE
			  AND kc.enabled = TRUE
			  AND kc.embedding_status = 'success'
			  AND kc.embedding_vector IS NOT NULL
			ORDER BY kc.knowledge_id ASC, kc.chunk_index ASC
			""")
	List<KnowledgeChunk> selectEmbeddedChunksByAgentId(Long agentId);

	@Delete("DELETE FROM knowledge_chunk WHERE id = #{id}")
	int deleteById(Long id);

	@Delete("DELETE FROM knowledge_chunk WHERE knowledge_id = #{knowledgeId}")
	int deleteByKnowledgeId(Long knowledgeId);

	@Update("UPDATE knowledge_chunk SET enabled = #{enabled}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
	int updateEnabledById(@Param("id") Long id, @Param("enabled") Boolean enabled);

	@Update("""
			UPDATE knowledge_chunk
			SET embedding_model_config_id = #{embeddingModelConfigId},
			    embedding_dimension = #{embeddingDimension},
			    embedding_vector = #{embeddingVector},
			    embedding_status = #{embeddingStatus},
			    embedding_error = #{embeddingError},
			    embedded_at = #{embeddedAt},
			    updated_at = CURRENT_TIMESTAMP
			WHERE id = #{id}
			""")
	int updateEmbedding(KnowledgeChunk chunk);

	@Update("""
			UPDATE knowledge_chunk
			SET embedding_model_config_id = NULL,
			    embedding_dimension = NULL,
			    embedding_vector = NULL,
			    embedding_status = 'none',
			    embedding_error = NULL,
			    embedded_at = NULL,
			    updated_at = CURRENT_TIMESTAMP
			WHERE knowledge_id = #{knowledgeId}
			""")
	int clearEmbeddingByKnowledgeId(Long knowledgeId);

	@Select("""
			SELECT COUNT(1)
			FROM knowledge_chunk
			WHERE knowledge_id = #{knowledgeId}
			  AND embedding_status = 'success'
			  AND embedding_vector IS NOT NULL
			""")
	int countEmbeddedByKnowledgeId(Long knowledgeId);

}
