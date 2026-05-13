/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.alibaba.cloud.ai.dataagent.analysis.python;

import com.alibaba.cloud.ai.dataagent.analysis.PythonSandboxProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PythonCodeGenerator {

	private static final int MAX_VALUE_LENGTH = 500;

	private final PythonSandboxProperties properties;

	private final ObjectMapper objectMapper;

	public String generate(String question, String sql, List<Map<String, Object>> rows) {
		String rowsJson = toJson(limitRows(rows));
		String questionJson = toJson(question == null ? "" : question);
		String sqlJson = toJson(sql == null ? "" : sql);
		String rowsLiteral = toJson(rowsJson);
		String questionLiteral = toJson(questionJson);
		String sqlLiteral = toJson(sqlJson);
		return """
				import json
				import statistics
				from decimal import Decimal

				question = json.loads(%s)
				sql = json.loads(%s)
				rows = json.loads(%s)

				def is_number(value):
				    if value is None or isinstance(value, bool):
				        return False
				    try:
				        Decimal(str(value))
				        return True
				    except Exception:
				        return False

				columns = list(rows[0].keys()) if rows else []
				result = {
				    "rowCount": len(rows),
				    "columnCount": len(columns),
				    "columns": columns,
				    "numericStats": {},
				    "categoryValues": {},
				    "singleValue": None,
				    "question": question,
				    "sql": sql,
				    "summary": ""
				}

				if len(rows) == 1 and len(columns) == 1:
				    result["singleValue"] = rows[0].get(columns[0])

				for column in columns:
				    numeric_values = []
				    category_values = []
				    for row in rows:
				        value = row.get(column)
				        if is_number(value):
				            numeric_values.append(Decimal(str(value)))
				        elif value is not None and len(category_values) < 20:
				            category_values.append(str(value)[:100])
				    if numeric_values:
				        total = sum(numeric_values)
				        result["numericStats"][column] = {
				            "sum": float(total),
				            "avg": float(total / Decimal(len(numeric_values))),
				            "min": float(min(numeric_values)),
				            "max": float(max(numeric_values)),
				            "count": len(numeric_values)
				        }
				    elif category_values:
				        result["categoryValues"][column] = category_values

				if result["singleValue"] is not None:
				    result["summary"] = "查询返回 1 个核心指标：" + str(result["singleValue"])
				else:
				    result["summary"] = "查询返回 " + str(len(rows)) + " 行，包含 " + str(len(columns)) + " 个字段。"

				print(json.dumps(result, ensure_ascii=False))
				""".formatted(questionLiteral, sqlLiteral, rowsLiteral);
	}

	private List<Map<String, Object>> limitRows(List<Map<String, Object>> rows) {
		if (rows == null || rows.isEmpty()) {
			return List.of();
		}
		int maxRows = properties.getMaxRows() == null ? 100 : properties.getMaxRows();
		return rows.stream().limit(maxRows).map(this::truncateRow).toList();
	}

	private Map<String, Object> truncateRow(Map<String, Object> row) {
		Map<String, Object> result = new LinkedHashMap<>();
		row.forEach((key, value) -> {
			if (value instanceof String text && text.length() > MAX_VALUE_LENGTH) {
				result.put(key, text.substring(0, MAX_VALUE_LENGTH));
			}
			else {
				result.put(key, value);
			}
		});
		return result;
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		}
		catch (JsonProcessingException ex) {
			throw new PythonSandboxException("Failed to serialize python input", ex);
		}
	}

}
