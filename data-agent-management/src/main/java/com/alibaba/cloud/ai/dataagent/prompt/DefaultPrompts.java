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
package com.alibaba.cloud.ai.dataagent.prompt;

public final class DefaultPrompts {

	private DefaultPrompts() {
	}

	public static String defaultIntentRecognitionPrompt() {
		return """
				你是一个意图识别助手。
				请根据用户问题判断用户意图。
				用户问题：
				{{question}}

				请输出意图类型和简短原因。
				""";
	}

	public static String defaultSchemaRecallPrompt() {
		return """
				你是一个 Schema Recall 助手。
				请根据用户问题、语义模型和业务知识选择可能相关的表和字段。
				用户问题：
				{{question}}

				语义模型：
				{{semanticModel}}

				业务知识：
				{{knowledge}}
				""";
	}

	public static String defaultSqlGeneratePrompt() {
		return """
				你是一个 SQL 生成助手。
				请根据用户问题、数据源信息、语义模型和业务知识生成只读 SQL。
				用户问题：
				{{question}}

				可用表结构：
				{{schema}}

				业务知识：
				{{knowledge}}

				要求：
				1. 只输出 SQL
				2. 不要输出解释
				3. 只允许 SELECT 查询
				4. 如果问题涉及多个表，请优先使用“表关系”中提供的连接条件
				5. 不要凭空猜测 JOIN 条件
				6. 如果没有相关表关系，尽量使用单表查询
				7. 不返回 Markdown
				8. knowledge 中只包含检索出的相关业务知识，生成 SQL 时优先遵守其中的指标口径
				9. 如果知识和 schema 冲突，以 schema 的表字段为准，知识用于口径解释
				""";
	}

	public static String defaultSqlRepairPrompt() {
		return """
				你是 SQL 修复助手。
				请根据用户问题、数据库 Schema、业务知识和错误信息，修复给定 SQL。

				用户问题：
				{{question}}

				数据库 Schema：
				{{schema}}

				业务知识：
				{{knowledge}}

				待修复 SQL：
				{{bad_sql}}

				错误信息：
				{{error}}

				要求：
				1. 只返回一条 SELECT SQL
				2. 不要返回 Markdown
				3. 不要解释
				4. 不要使用 INSERT/UPDATE/DELETE/DROP/ALTER/TRUNCATE/CREATE
				5. 不要多语句
				6. 如果无法修复，返回空字符串
				""";
	}

	public static String defaultReportGeneratePrompt() {
		return """
				你是一个数据分析报告生成助手。
				请根据用户问题、分析结果和建议生成结构化报告。
				用户问题：
				{{question}}

				分析结果：
				{{analysis}}

				建议：
				{{recommendations}}
				""";
	}

}
