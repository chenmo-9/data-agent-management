/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.alibaba.cloud.ai.dataagent.analysis.python;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class PythonCodeSafetyChecker {

	private static final List<String> BLOCKED_PATTERNS = List.of("import os", "import sys", "import subprocess",
			"import socket", "import pathlib", "import shutil", "import requests", "import urllib", "import http",
			"import multiprocessing", "import threading", "open(", "eval(", "exec(", "compile(", "__import__",
			"globals(", "locals(", "input(", "dir(", "vars(", "getattr(", "setattr(", "delattr(", "__",
			"while true", "fork", "system(", "popen", "remove(", "rmdir(", "unlink(");

	public PythonCodeSafetyResult check(String code) {
		if (code == null || code.isBlank()) {
			return PythonCodeSafetyResult.blocked("empty code");
		}
		String normalized = code.toLowerCase(Locale.ROOT);
		for (String pattern : BLOCKED_PATTERNS) {
			if (normalized.contains(pattern)) {
				return PythonCodeSafetyResult.blocked(pattern);
			}
		}
		return PythonCodeSafetyResult.safe();
	}

}
