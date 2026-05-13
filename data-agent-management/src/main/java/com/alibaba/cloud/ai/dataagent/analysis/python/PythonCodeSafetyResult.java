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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PythonCodeSafetyResult {

	private Boolean safe;

	private String blockedReason;

	private String matchedPattern;

	public static PythonCodeSafetyResult safe() {
		return PythonCodeSafetyResult.builder().safe(true).build();
	}

	public static PythonCodeSafetyResult blocked(String pattern) {
		return PythonCodeSafetyResult.builder()
			.safe(false)
			.matchedPattern(pattern)
			.blockedReason("Python code contains blocked pattern: " + pattern)
			.build();
	}

}
