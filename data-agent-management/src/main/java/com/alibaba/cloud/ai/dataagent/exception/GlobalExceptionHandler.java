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
package com.alibaba.cloud.ai.dataagent.exception;

import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ApiResponse<Void> handleBusinessException(BusinessException exception) {
		return ApiResponse.error(exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream().findFirst()
			.map(error -> error.getDefaultMessage())
			.orElse("Invalid request");
		return ApiResponse.error(message);
	}

	@ExceptionHandler(Exception.class)
	public ApiResponse<Void> handleException(Exception exception) {
		log.error("Unexpected error", exception);
		return ApiResponse.error("Internal server error");
	}

}
