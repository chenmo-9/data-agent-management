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

import com.alibaba.cloud.ai.dataagent.entity.SemanticRelation;
import com.alibaba.cloud.ai.dataagent.mapper.SemanticRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationRecallService {

	private static final int MAX_RELATIONS = 5;

	private final SemanticRelationMapper semanticRelationMapper;

	private final KeywordUtils keywordUtils;

	public RelationRecallResult recall(Long datasourceId, String question, SchemaRecallResult schemaRecallResult) {
		if (datasourceId == null || schemaRecallResult == null || schemaRecallResult.getSelectedTables().isEmpty()) {
			return emptyResult(datasourceId, "No schema recalled");
		}
		List<Long> tableIds = schemaRecallResult.getSelectedTables().stream().map(SchemaRecallItem::getTableId).toList();
		if (tableIds.isEmpty()) {
			return emptyResult(datasourceId, "No selected tables");
		}
		List<SemanticRelation> relations = semanticRelationMapper.selectByTableIds(datasourceId, tableIds);
		if (relations.isEmpty()) {
			return emptyResult(datasourceId, "No relation matched");
		}
		Set<Long> selectedTableIds = Set.copyOf(tableIds);
		List<String> keywords = keywordUtils.extractKeywords(question);
		List<SchemaRelationItem> selectedRelations = relations.stream()
			.map(relation -> scoreRelation(relation, selectedTableIds, keywords))
			.sorted(Comparator.comparing(SchemaRelationItem::getScore, Comparator.nullsLast(Double::compareTo)).reversed())
			.limit(MAX_RELATIONS)
			.collect(Collectors.toCollection(ArrayList::new));
		String relationContext = buildRelationContext(selectedRelations);
		return RelationRecallResult.builder()
			.datasourceId(datasourceId)
			.relationCount(selectedRelations.size())
			.selectedRelations(selectedRelations)
			.relationContext(relationContext)
			.fallbackUsed(false)
			.message(selectedRelations.isEmpty() ? "No relation matched" : "Relations recalled by selected tables")
			.build();
	}

	private SchemaRelationItem scoreRelation(SemanticRelation relation, Set<Long> selectedTableIds, List<String> keywords) {
		double score = 0;
		List<String> reasons = new ArrayList<>();
		boolean sourceSelected = selectedTableIds.contains(relation.getSourceTableId());
		boolean targetSelected = selectedTableIds.contains(relation.getTargetTableId());
		if (sourceSelected && targetSelected) {
			score += 10;
			reasons.add("source and target tables both recalled");
		}
		else if (sourceSelected || targetSelected) {
			score += 5;
			reasons.add("one side table recalled");
		}
		String description = keywordUtils.normalize(relation.getDescription());
		for (String keyword : keywords) {
			if (description.contains(keyword)) {
				score += 4;
				reasons.add("relation description matched: " + keyword);
				break;
			}
		}
		if ("foreign_key".equalsIgnoreCase(relation.getRelationType())) {
			score += 2;
			reasons.add("foreign_key relation");
		}
		if ("INNER JOIN".equalsIgnoreCase(relation.getJoinType())) {
			score += 1;
			reasons.add("preferred inner join");
		}
		return SchemaRelationItem.builder()
			.id(relation.getId())
			.datasourceId(relation.getDatasourceId())
			.sourceTableName(relation.getSourceTableName())
			.sourceFieldName(relation.getSourceFieldName())
			.targetTableName(relation.getTargetTableName())
			.targetFieldName(relation.getTargetFieldName())
			.relationType(relation.getRelationType())
			.joinType(relation.getJoinType())
			.description(relation.getDescription())
			.score(score)
			.matchReasons(reasons)
			.build();
	}

	private String buildRelationContext(List<SchemaRelationItem> relations) {
		if (relations.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder("表关系：\n");
		for (SchemaRelationItem relation : relations) {
			builder.append("- ")
				.append(relation.getSourceTableName()).append('.').append(relation.getSourceFieldName())
				.append(' ').append(defaultJoinType(relation.getJoinType())).append(' ')
				.append(relation.getTargetTableName()).append('.').append(relation.getTargetFieldName())
				.append("（").append(defaultString(relation.getRelationType())).append("）");
			if (StringUtils.hasText(relation.getDescription())) {
				builder.append("：").append(relation.getDescription());
			}
			builder.append(" score=").append(String.format(Locale.ROOT, "%.1f", relation.getScore() == null ? 0D : relation.getScore()));
			if (!relation.getMatchReasons().isEmpty()) {
				builder.append("，匹配原因 ").append(String.join("; ", relation.getMatchReasons()));
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	private RelationRecallResult emptyResult(Long datasourceId, String message) {
		return RelationRecallResult.builder()
			.datasourceId(datasourceId)
			.relationCount(0)
			.selectedRelations(new ArrayList<>())
			.relationContext("")
			.fallbackUsed(false)
			.message(message)
			.build();
	}

	private String defaultJoinType(String joinType) {
		return StringUtils.hasText(joinType) ? joinType : "INNER JOIN";
	}

	private String defaultString(String value) {
		return value == null ? "" : value;
	}

}
