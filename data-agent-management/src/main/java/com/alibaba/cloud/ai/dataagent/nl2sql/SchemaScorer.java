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

import com.alibaba.cloud.ai.dataagent.entity.SemanticField;
import com.alibaba.cloud.ai.dataagent.entity.SemanticTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SchemaScorer {

	private final KeywordUtils keywordUtils;

	public SchemaRecallFieldItem scoreField(SemanticField field, List<String> keywords, String question) {
		double score = 0;
		List<String> reasons = new ArrayList<>();
		score += scoreText(field.getFieldName(), keywords, 6, "fieldName matched", reasons);
		score += scoreText(field.getBusinessName(), keywords, 10, "businessName matched", reasons);
		score += scoreSynonyms(field.getSynonyms(), keywords, 12, "field synonym matched", reasons);
		score += scoreText(field.getDescription(), keywords, 4, "field description matched", reasons);
		score += scoreText(field.getExampleValue(), keywords, 1, "exampleValue matched", reasons);
		score += scoreIntent(field, question, reasons);
		return SchemaRecallFieldItem.builder()
			.fieldId(field.getId())
			.tableId(field.getTableId())
			.tableName(field.getTableName())
			.fieldName(field.getFieldName())
			.businessName(field.getBusinessName())
			.dataType(field.getDataType())
			.description(field.getDescription())
			.synonyms(field.getSynonyms())
			.exampleValue(field.getExampleValue())
			.primaryKey(field.getPrimaryKey())
			.nullable(field.getNullable())
			.score(score)
			.matchReasons(reasons)
			.build();
	}

	public SchemaRecallItem scoreTable(SemanticTable table, List<SchemaRecallFieldItem> fields, List<String> keywords) {
		double score = 0;
		List<String> reasons = new ArrayList<>();
		score += scoreText(table.getTableName(), keywords, 5, "tableName matched", reasons);
		score += scoreText(table.getBusinessName(), keywords, 8, "table businessName matched", reasons);
		score += scoreSynonyms(table.getSynonyms(), keywords, 10, "table synonym matched", reasons);
		score += scoreText(table.getDescription(), keywords, 3, "table description matched", reasons);
		for (SchemaRecallFieldItem field : fields) {
			score += field.getScore() * 0.3d;
			if (field.getScore() > 0 && reasons.size() < 6) {
				reasons.add("field matched: " + field.getFieldName());
			}
		}
		return SchemaRecallItem.builder()
			.tableId(table.getId())
			.datasourceId(table.getDatasourceId())
			.tableName(table.getTableName())
			.businessName(table.getBusinessName())
			.description(table.getDescription())
			.synonyms(table.getSynonyms())
			.score(score)
			.matchReasons(reasons)
			.fields(fields)
			.build();
	}

	private double scoreText(String text, List<String> keywords, double weight, String reasonPrefix, List<String> reasons) {
		String normalized = keywordUtils.normalize(text);
		if (normalized.isBlank()) {
			return 0;
		}
		double score = 0;
		for (String keyword : keywords) {
			if (!keyword.isBlank() && normalized.contains(keyword)) {
				score += weight;
				reasons.add(reasonPrefix + ": " + keyword);
			}
		}
		return score;
	}

	private double scoreSynonyms(String synonyms, List<String> keywords, double weight, String reasonPrefix,
			List<String> reasons) {
		double score = 0;
		for (String synonym : keywordUtils.splitSynonyms(synonyms)) {
			for (String keyword : keywords) {
				if (!keyword.isBlank() && synonym.contains(keyword)) {
					score += weight;
					reasons.add(reasonPrefix + ": " + keyword);
				}
			}
		}
		return score;
	}

	private double scoreIntent(SemanticField field, String question, List<String> reasons) {
		String lowerQuestion = question == null ? "" : question.toLowerCase(Locale.ROOT);
		String signature = (field.getFieldName() + " " + field.getBusinessName() + " " + field.getSynonyms()).toLowerCase(Locale.ROOT);
		double score = 0;
		if ((lowerQuestion.contains("销售额") || lowerQuestion.contains("revenue") || lowerQuestion.contains("sales")
				|| lowerQuestion.contains("sum") || lowerQuestion.contains("金额"))
				&& containsAny(signature, List.of("amount", "price", "revenue", "sales", "金额", "销售额"))) {
			score += 8;
			reasons.add("aggregation intent matched amount field");
		}
		if ((lowerQuestion.contains("数量") || lowerQuestion.contains("count") || lowerQuestion.contains("多少订单"))
				&& containsAny(signature, List.of("id", "order_id", "count", "数量"))) {
			score += 4;
			reasons.add("count intent matched candidate field");
		}
		if ((lowerQuestion.contains("时间") || lowerQuestion.contains("最近") || lowerQuestion.contains("created")
				|| lowerQuestion.contains("date"))
				&& containsAny(signature, List.of("created_at", "time", "date", "时间", "下单时间", "创建时间"))) {
			score += 6;
			reasons.add("time intent matched temporal field");
		}
		return score;
	}

	private boolean containsAny(String text, List<String> candidates) {
		for (String candidate : candidates) {
			if (text.contains(candidate.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

}
