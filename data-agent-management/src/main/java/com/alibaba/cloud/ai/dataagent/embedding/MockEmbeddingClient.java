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
package com.alibaba.cloud.ai.dataagent.embedding;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MockEmbeddingClient implements EmbeddingClient {

	private static final int DIMENSION = 16;

	@Override
	public List<Double> embed(String text) {
		double[] values = new double[DIMENSION];
		String content = text == null ? "" : text;
		for (int i = 0; i < content.length(); i++) {
			int codePoint = content.charAt(i);
			int index = Math.abs((codePoint + i) % DIMENSION);
			values[index] += (codePoint % 97) + 1D;
		}
		double norm = 0D;
		for (double value : values) {
			norm += value * value;
		}
		norm = Math.sqrt(norm);
		List<Double> vector = new ArrayList<>(DIMENSION);
		for (double value : values) {
			vector.add(norm == 0D ? 0D : value / norm);
		}
		return vector;
	}

}
