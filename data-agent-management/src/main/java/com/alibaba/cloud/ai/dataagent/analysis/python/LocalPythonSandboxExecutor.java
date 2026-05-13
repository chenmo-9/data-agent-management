/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.alibaba.cloud.ai.dataagent.analysis.python;

import com.alibaba.cloud.ai.dataagent.analysis.PythonSandboxProperties;
import com.alibaba.cloud.ai.dataagent.security.SensitiveLogUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class LocalPythonSandboxExecutor implements PythonSandboxExecutor {

	private static final List<String> SENSITIVE_ENV_KEYS = List.of("DATAAGENT_SECRET_KEY", "API_KEY", "DEEPSEEK_API_KEY",
			"OPENAI_API_KEY");

	private final PythonSandboxProperties properties;

	private final PythonCodeSafetyChecker safetyChecker;

	@Override
	public PythonExecutionResult execute(PythonExecutionRequest request) {
		if (!Boolean.TRUE.equals(properties.getEnabled())) {
			return PythonExecutionResult.builder()
				.success(false)
				.code(request.getCode())
				.errorMessage("Python sandbox is disabled")
				.build();
		}
		String code = request.getCode();
		PythonCodeSafetyResult safety = safetyChecker.check(code);
		if (!Boolean.TRUE.equals(safety.getSafe())) {
			return PythonExecutionResult.builder()
				.success(false)
				.code(code)
				.errorMessage(safety.getBlockedReason())
				.build();
		}
		Path workRoot = Path.of(properties.getWorkDir()).toAbsolutePath().normalize();
		Path runDir = workRoot.resolve("run-" + safeRunId(request.getRunId()) + "-" + UUID.randomUUID()).normalize();
		Instant started = Instant.now();
		try {
			Files.createDirectories(runDir);
			Path script = runDir.resolve("analysis.py");
			Files.writeString(script, code, StandardCharsets.UTF_8);
			ProcessBuilder builder = new ProcessBuilder(properties.getPythonCommand(), script.getFileName().toString());
			builder.directory(runDir.toFile());
			cleanEnvironment(builder.environment());
			Process process = builder.start();
			int timeout = request.getTimeoutSeconds() == null ? properties.getTimeoutSeconds() : request.getTimeoutSeconds();
			boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				return PythonExecutionResult.builder()
					.success(false)
					.code(code)
					.durationMs(Duration.between(started, Instant.now()).toMillis())
					.errorMessage("Python execution timed out after " + timeout + " seconds")
					.build();
			}
			String stdout = truncate(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8),
					request.getMaxOutputChars());
			String stderr = truncate(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8),
					request.getMaxOutputChars());
			return PythonExecutionResult.builder()
				.success(process.exitValue() == 0)
				.code(code)
				.stdout(SensitiveLogUtils.maskSecretFields(stdout))
				.stderr(SensitiveLogUtils.maskSecretFields(stderr))
				.exitCode(process.exitValue())
				.durationMs(Duration.between(started, Instant.now()).toMillis())
				.errorMessage(process.exitValue() == 0 ? null : SensitiveLogUtils.maskSecretFields(stderr))
				.build();
		}
		catch (Exception ex) {
			return PythonExecutionResult.builder()
				.success(false)
				.code(code)
				.durationMs(Duration.between(started, Instant.now()).toMillis())
				.errorMessage(SensitiveLogUtils.maskSecretFields(ex.getMessage()))
				.build();
		}
		finally {
			cleanup(runDir, workRoot);
		}
	}

	private void cleanEnvironment(Map<String, String> environment) {
		SENSITIVE_ENV_KEYS.forEach(environment::remove);
		environment.entrySet().removeIf(entry -> entry.getKey().toUpperCase().contains("TOKEN")
				|| entry.getKey().toUpperCase().contains("SECRET") || entry.getKey().toUpperCase().contains("PASSWORD"));
	}

	private String truncate(String text, Integer maxOutputChars) {
		if (text == null) {
			return null;
		}
		int max = maxOutputChars == null ? properties.getMaxOutputChars() : maxOutputChars;
		return text.length() <= max ? text : text.substring(0, max) + "...";
	}

	private String safeRunId(String runId) {
		return runId == null ? "manual" : runId.replaceAll("[^a-zA-Z0-9_-]", "");
	}

	private void cleanup(Path runDir, Path workRoot) {
		if (runDir == null || !runDir.normalize().startsWith(workRoot.normalize())) {
			return;
		}
		try (Stream<Path> stream = Files.walk(runDir)) {
			stream.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				}
				catch (IOException ignored) {
					// Temporary sandbox cleanup is best-effort.
				}
			});
		}
		catch (IOException ignored) {
			// Temporary sandbox cleanup is best-effort.
		}
	}

}
