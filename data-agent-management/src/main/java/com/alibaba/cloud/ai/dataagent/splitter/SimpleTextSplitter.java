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
package com.alibaba.cloud.ai.dataagent.splitter;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleTextSplitter implements TextSplitter {

	private static final int DEFAULT_CHUNK_SIZE = 500;

	private static final int DEFAULT_OVERLAP = 50;

	private final int chunkSize;

	private final int overlap;

	public SimpleTextSplitter() {
		this(DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
	}

	public SimpleTextSplitter(int chunkSize, int overlap) {
		this.chunkSize = chunkSize;
		this.overlap = Math.max(0, Math.min(overlap, chunkSize - 1));
	}

	@Override
	public List<String> split(String text) {
		if (text == null || text.isBlank()) {
			return List.of();
		}
		if (text.length() <= chunkSize) {
			return List.of(text);
		}
		List<String> chunks = new ArrayList<>();
		int start = 0;
		while (start < text.length()) {
			int end = Math.min(start + chunkSize, text.length());
			chunks.add(text.substring(start, end));
			if (end >= text.length()) {
				break;
			}
			start = end - overlap;
		}
		return chunks;
	}

}
