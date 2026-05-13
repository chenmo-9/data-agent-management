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

import com.alibaba.cloud.ai.dataagent.entity.Datasource;
import com.alibaba.cloud.ai.dataagent.vo.datasource.DatasourceTestVO;
import org.springframework.stereotype.Component;

import java.sql.DriverManager;

@Component
public class H2DatabaseConnector implements DatabaseConnector {

	@Override
	public DatasourceTestVO testConnection(Datasource datasource) {
		try (var ignored = DriverManager.getConnection(datasource.getUrl(), datasource.getUsername(),
				datasource.getPassword())) {
			return new DatasourceTestVO(true, "H2 connection test succeeded", datasource.getDbType(),
					datasource.getUrl());
		}
		catch (Exception exception) {
			return new DatasourceTestVO(false, "H2 connection test failed: " + exception.getMessage(),
					datasource.getDbType(), datasource.getUrl());
		}
	}

}
