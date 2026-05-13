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

import com.alibaba.cloud.ai.dataagent.dto.nl2sql.SqlExecutionResultDTO;
import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.mapper.DatasourceMapper;
import com.alibaba.cloud.ai.dataagent.security.SecretService;
import com.alibaba.cloud.ai.dataagent.security.SecurityProperties;
import com.alibaba.cloud.ai.dataagent.security.SensitiveLogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlExecutor {

	private final DatasourceMapper datasourceMapper;

	private final SecretService secretService;

	private final SecurityProperties securityProperties;

	private final SqlValidator sqlValidator;

	public SqlExecutionResultDTO execute(Long datasourceId, String sql) {
		Datasource datasource = datasourceMapper.selectById(datasourceId);
		if (datasource == null) {
			return failed(sql, "Datasource not found: " + datasourceId);
		}
		if (!Boolean.TRUE.equals(datasource.getEnabled())) {
			return failed(sql, "Datasource disabled: " + datasourceId);
		}
		if (!supports(datasource.getDbType())) {
			return failed(sql, "Unsupported datasource type: " + datasource.getDbType());
		}
		SqlValidationResult validation = sqlValidator.validateDetailed(sql);
		if (!Boolean.TRUE.equals(validation.getValid())) {
			return failed(sql, validation.getMessage());
		}
		String sanitizedSql = validation.getSanitizedSql();
		int maxRows = securityProperties.getSql().getMaxRows();
		int timeoutSeconds = securityProperties.getSql().getQueryTimeoutSeconds();
		try (Connection connection = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(),
				secretService.decryptIfNeeded(datasource.getPassword())); Statement statement = connection.createStatement()) {
			statement.setMaxRows(maxRows);
			statement.setQueryTimeout(timeoutSeconds);
			try (ResultSet resultSet = statement.executeQuery(sanitizedSql)) {
				List<Map<String, Object>> rows = readRows(resultSet, maxRows);
				boolean truncated = rows.size() >= maxRows;
				log.debug("SQL executed datasourceId={}, sqlLength={}, rowCount={}, truncated={}", datasourceId,
						sanitizedSql.length(), rows.size(), truncated);
				return SqlExecutionResultDTO.builder()
					.success(true)
					.sql(sanitizedSql)
					.sanitizedSql(sanitizedSql)
					.rows(rows)
					.rowCount(rows.size())
					.sqlLimited(validation.getLimitApplied())
					.sqlLimit(validation.getLimit())
					.truncated(truncated)
					.queryTimeoutSeconds(timeoutSeconds)
					.securityMessage(buildSecurityMessage(validation, truncated, maxRows, timeoutSeconds))
					.message("SQL executed successfully")
					.build();
			}
		}
		catch (Exception ex) {
			return failed(sanitizedSql, SensitiveLogUtils.maskSecretFields(ex.getMessage()));
		}
	}

	private List<Map<String, Object>> readRows(ResultSet resultSet, int maxRows) throws Exception {
		List<Map<String, Object>> rows = new ArrayList<>();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		while (resultSet.next() && rows.size() < maxRows) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 1; i <= columnCount; i++) {
				String label = metaData.getColumnLabel(i);
				if (label == null || label.isBlank()) {
					label = metaData.getColumnName(i);
				}
				row.put(label, resultSet.getObject(i));
			}
			rows.add(row);
		}
		return rows;
	}

	private boolean supports(String dbType) {
		if (dbType == null) {
			return false;
		}
		String normalized = dbType.toLowerCase(Locale.ROOT);
		return "h2".equals(normalized) || "mysql".equals(normalized);
	}

	private SqlExecutionResultDTO failed(String sql, String message) {
		return SqlExecutionResultDTO.builder()
			.success(false)
			.sql(sql)
			.sanitizedSql(sql)
			.rows(List.of())
			.rowCount(0)
			.truncated(false)
			.queryTimeoutSeconds(securityProperties.getSql().getQueryTimeoutSeconds())
			.securityMessage("SQL execution blocked or failed by safety guard")
			.message(SensitiveLogUtils.maskSecretFields(message))
			.build();
	}

	private String buildSecurityMessage(SqlValidationResult validation, boolean truncated, int maxRows, int timeoutSeconds) {
		List<String> messages = new ArrayList<>();
		if (Boolean.TRUE.equals(validation.getLimitApplied())) {
			messages.add("LIMIT " + validation.getLimit() + " applied");
		}
		if (truncated) {
			messages.add("result truncated to " + maxRows + " rows");
		}
		messages.add("query timeout " + timeoutSeconds + "s");
		return String.join("; ", messages);
	}

}
