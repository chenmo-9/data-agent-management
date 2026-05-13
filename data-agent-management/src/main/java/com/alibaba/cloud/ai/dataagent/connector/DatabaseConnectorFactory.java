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
package com.alibaba.cloud.ai.dataagent.connector;

import com.alibaba.cloud.ai.dataagent.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@AllArgsConstructor
public class DatabaseConnectorFactory {

	private final H2DatabaseConnector h2DatabaseConnector;

	private final MySQLDatabaseConnector mySQLDatabaseConnector;

	private final PostgreSQLDatabaseConnector postgreSQLDatabaseConnector;

	public DatabaseConnector getConnector(String dbType) {
		String normalized = dbType == null ? "" : dbType.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "h2" -> h2DatabaseConnector;
			case "mysql" -> mySQLDatabaseConnector;
			case "postgresql" -> postgreSQLDatabaseConnector;
			default -> throw new BusinessException("dbType must be h2, mysql or postgresql");
		};
	}

}
