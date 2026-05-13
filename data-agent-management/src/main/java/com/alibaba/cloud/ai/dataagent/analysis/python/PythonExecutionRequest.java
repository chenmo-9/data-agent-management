/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.alibaba.cloud.ai.dataagent.analysis.python;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonExecutionRequest {

	private String runId;

	private String question;

	private String sql;

	private List<Map<String, Object>> rows;

	private Integer timeoutSeconds;

	private Integer maxOutputChars;

	private String code;

}
