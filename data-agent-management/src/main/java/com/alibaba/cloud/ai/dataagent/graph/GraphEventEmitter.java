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
package com.alibaba.cloud.ai.dataagent.graph;

import com.alibaba.cloud.ai.dataagent.dto.graph.GraphEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class GraphEventEmitter {

	private final GraphState state;

	private final SseEmitter sseEmitter;

	private final Consumer<GraphEventDTO> eventSink;

	public GraphEventDTO emitNodeStart(String nodeName, String message, Map<String, Object> data) {
		return emit(nodeName, "node_start", "running", message, data);
	}

	public GraphEventDTO emitNodeEnd(String nodeName, String message, Map<String, Object> data) {
		return emit(nodeName, "node_end", "success", message, data);
	}

	public GraphEventDTO emitMessage(String nodeName, String message, Map<String, Object> data) {
		return emit(nodeName, "message", "running", message, data);
	}

	public GraphEventDTO emitError(String nodeName, String message, Map<String, Object> data) {
		return emit(nodeName, "error", "failed", message, data);
	}

	public GraphEventDTO emitFinish(String message, Map<String, Object> data) {
		return emit("finish", "finish", "success", message, data);
	}

	public GraphEventDTO emit(String nodeName, String eventType, String status, String message, Map<String, Object> data) {
		GraphEventDTO event = GraphEventDTO.builder()
			.eventId(UUID.randomUUID().toString())
			.sessionId(state.getSessionId())
			.nodeName(nodeName)
			.eventType(eventType)
			.status(status)
			.message(message)
			.data(data)
			.timestamp(LocalDateTime.now())
			.build();
		state.getEvents().add(event);
		if (eventSink != null) {
			eventSink.accept(event);
		}
		if (sseEmitter != null) {
			try {
				sseEmitter.send(SseEmitter.event().id(event.getEventId()).name(eventType).data(event));
			}
			catch (IOException ex) {
				throw new IllegalStateException("Failed to send graph event", ex);
			}
		}
		return event;
	}

}
