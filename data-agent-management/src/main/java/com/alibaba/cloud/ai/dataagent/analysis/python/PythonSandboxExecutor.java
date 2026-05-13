/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.alibaba.cloud.ai.dataagent.analysis.python;

public interface PythonSandboxExecutor {

	PythonExecutionResult execute(PythonExecutionRequest request);

}
