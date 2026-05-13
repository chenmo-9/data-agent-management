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
package com.alibaba.cloud.ai.dataagent.nl2sql;

import com.alibaba.cloud.ai.dataagent.entity.AgentDatasource;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.entity.SemanticField;
import com.alibaba.cloud.ai.dataagent.entity.SemanticTable;
import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import com.alibaba.cloud.ai.dataagent.mapper.AgentDatasourceMapper;
import com.alibaba.cloud.ai.dataagent.mapper.DatasourceMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticFieldMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticTableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchemaRecallService {

	private static final int MAX_RECALL_TABLES = 3;

	private static final int MAX_FIELDS_PER_TABLE = 8;

	private static final int MIN_SCORE = 1;

	private final AgentDatasourceMapper agentDatasourceMapper;

	private final DatasourceMapper datasourceMapper;

	private final SemanticTableMapper semanticTableMapper;

	private final SemanticFieldMapper semanticFieldMapper;

	private final KeywordUtils keywordUtils;

	private final SchemaScorer schemaScorer;

	public SchemaRecallResult recall(Long agentId, String question) {
		List<AgentDatasource> relations = agentDatasourceMapper.selectByAgentId(agentId)
			.stream()
			.filter(item -> Boolean.TRUE.equals(item.getEnabled()))
			.toList();
		if (relations.isEmpty()) {
			throw new BusinessException("No enabled datasource bound to agent: " + agentId);
		}
		AgentDatasource relation = relations.get(0);
		Datasource datasource = datasourceMapper.selectById(relation.getDatasourceId());
		if (datasource == null) {
			throw new BusinessException("Datasource not found: " + relation.getDatasourceId());
		}
		if (!Boolean.TRUE.equals(datasource.getEnabled())) {
			throw new BusinessException("Datasource disabled: " + relation.getDatasourceId());
		}

		List<String> keywords = keywordUtils.extractKeywords(question);
		List<SemanticTable> tables = semanticTableMapper.selectByDatasourceId(datasource.getId())
			.stream()
			.filter(item -> Boolean.TRUE.equals(item.getEnabled()))
			.toList();
		List<SchemaRecallItem> scoredTables = new ArrayList<>();
		for (SemanticTable table : tables) {
			List<SchemaRecallFieldItem> scoredFields = semanticFieldMapper.selectByTableId(table.getId())
				.stream()
				.filter(item -> Boolean.TRUE.equals(item.getEnabled()))
				.map(field -> schemaScorer.scoreField(field, keywords, question))
				.sorted(Comparator.comparing(SchemaRecallFieldItem::getScore, Comparator.nullsLast(Double::compareTo)).reversed())
				.collect(Collectors.toCollection(ArrayList::new));
			scoredTables.add(schemaScorer.scoreTable(table, scoredFields, keywords));
		}

		boolean fallbackUsed = scoredTables.stream().noneMatch(item -> defaultScore(item.getScore()) >= MIN_SCORE);
		List<SchemaRecallItem> selectedTables = selectTables(scoredTables, fallbackUsed);
		int fieldCount = selectedTables.stream().mapToInt(item -> item.getFields().size()).sum();
		String schemaContext = buildSchemaContext(datasource, question, selectedTables, fallbackUsed);
		String message = fallbackUsed ? "No keyword match, fallback to enabled schema" : "Schema recalled by keyword scoring";
		return SchemaRecallResult.builder()
			.datasourceId(datasource.getId())
			.datasourceName(datasource.getName())
			.dbType(datasource.getDbType())
			.question(question)
			.tableCount(selectedTables.size())
			.fieldCount(fieldCount)
			.selectedTables(selectedTables)
			.schemaContext(schemaContext)
			.fallbackUsed(fallbackUsed)
			.message(message)
			.build();
	}

	private List<SchemaRecallItem> selectTables(List<SchemaRecallItem> scoredTables, boolean fallbackUsed) {
		return scoredTables.stream()
			.sorted(Comparator.comparing(SchemaRecallItem::getScore, Comparator.nullsLast(Double::compareTo)).reversed())
			.limit(MAX_RECALL_TABLES)
			.map(item -> filterFields(item, fallbackUsed))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	private SchemaRecallItem filterFields(SchemaRecallItem item, boolean fallbackUsed) {
		List<SchemaRecallFieldItem> selectedFields = item.getFields()
			.stream()
			.filter(field -> fallbackUsed || defaultScore(field.getScore()) >= MIN_SCORE)
			.limit(MAX_FIELDS_PER_TABLE)
			.collect(Collectors.toCollection(ArrayList::new));
		if (selectedFields.isEmpty()) {
			selectedFields = item.getFields().stream().limit(MAX_FIELDS_PER_TABLE).collect(Collectors.toCollection(ArrayList::new));
		}
		item.setFields(selectedFields);
		return item;
	}

	private String buildSchemaContext(Datasource datasource, String question, List<SchemaRecallItem> selectedTables,
			boolean fallbackUsed) {
		StringBuilder schema = new StringBuilder();
		schema.append("数据源：").append(defaultString(datasource.getName())).append("（").append(defaultString(datasource.getDbType()))
			.append("）\n");
		schema.append("用户问题：").append(defaultString(question)).append("\n");
		if (fallbackUsed) {
			schema.append("说明：No keyword match, fallback to enabled schema\n");
		}
		if (selectedTables.isEmpty()) {
			schema.append("暂无启用语义模型。\n");
			return schema.toString();
		}
		schema.append("召回表：\n");
		int index = 1;
		for (SchemaRecallItem table : selectedTables) {
			schema.append(index++).append(". ").append(table.getTableName()).append("（").append(defaultString(table.getBusinessName()))
				.append("） score=").append(formatScore(table.getScore())).append("\n");
			if (StringUtils.hasText(table.getDescription())) {
				schema.append("说明：").append(table.getDescription()).append("\n");
			}
			if (!table.getMatchReasons().isEmpty()) {
				schema.append("匹配原因：").append(String.join("; ", table.getMatchReasons())).append("\n");
			}
			schema.append("字段：\n");
			for (SchemaRecallFieldItem field : table.getFields()) {
				schema.append("- ").append(field.getFieldName()).append("：").append(defaultString(field.getBusinessName()));
				if (StringUtils.hasText(field.getDataType())) {
					schema.append("，类型 ").append(field.getDataType());
				}
				if (StringUtils.hasText(field.getSynonyms())) {
					schema.append("，同义词 ").append(field.getSynonyms());
				}
				schema.append("，score=").append(formatScore(field.getScore()));
				if (!field.getMatchReasons().isEmpty()) {
					schema.append("，匹配原因 ").append(String.join("; ", field.getMatchReasons()));
				}
				schema.append("\n");
			}
			schema.append("\n");
		}
		return schema.toString();
	}

	private double defaultScore(Double score) {
		return score == null ? 0 : score;
	}

	private String formatScore(Double score) {
		return String.format("%.1f", defaultScore(score));
	}

	private String defaultString(String value) {
		return value == null ? "" : value;
	}

}
