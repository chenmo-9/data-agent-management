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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public final class VectorUtils {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private VectorUtils() {
	}

	public static String toJson(List<Double> vector) {
		try {
			return OBJECT_MAPPER.writeValueAsString(vector == null ? Collections.emptyList() : vector);
		}
		catch (Exception ex) {
			throw new IllegalStateException("failed to serialize vector", ex);
		}
	}

	public static List<Double> fromJson(String json) {
		if (json == null || json.isBlank()) {
			return Collections.emptyList();
		}
		try {
			return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
			});
		}
		catch (Exception ex) {
			return Collections.emptyList();
		}
	}

	public static double cosineSimilarity(List<Double> left, List<Double> right) {
		if (!isValidVector(left) || !isValidVector(right) || left.size() != right.size()) {
			return 0D;
		}
		double dot = 0D;
		double leftNorm = 0D;
		double rightNorm = 0D;
		for (int i = 0; i < left.size(); i++) {
			double l = left.get(i);
			double r = right.get(i);
			dot += l * r;
			leftNorm += l * l;
			rightNorm += r * r;
		}
		if (leftNorm == 0D || rightNorm == 0D) {
			return 0D;
		}
		return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
	}

	public static boolean isValidVector(List<Double> vector) {
		if (vector == null || vector.isEmpty()) {
			return false;
		}
		for (Double value : vector) {
			if (value == null || value.isNaN() || value.isInfinite()) {
				return false;
			}
		}
		return true;
	}

}
