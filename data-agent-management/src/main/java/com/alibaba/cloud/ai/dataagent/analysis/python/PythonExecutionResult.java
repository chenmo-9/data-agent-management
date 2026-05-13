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

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonExecutionResult {

	private Boolean success;

	private String code;

	private String stdout;

	private String stderr;

	private Integer exitCode;

	private Long durationMs;

	private String errorMessage;

	private Map<String, Object> metrics;

	private Boolean fallbackUsed;

}
