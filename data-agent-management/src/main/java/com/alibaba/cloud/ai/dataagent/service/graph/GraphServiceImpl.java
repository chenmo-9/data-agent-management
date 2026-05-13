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
package com.alibaba.cloud.ai.dataagent.service.graph;

import com.alibaba.cloud.ai.dataagent.dto.graph.GraphRunRequest;
import com.alibaba.cloud.ai.dataagent.dto.graph.GraphEventDTO;
import com.alibaba.cloud.ai.dataagent.graph.GraphRunner;
import com.alibaba.cloud.ai.dataagent.vo.graph.GraphRunVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class GraphServiceImpl implements GraphService {

	private static final long SSE_TIMEOUT = 5 * 60 * 1000L;

	private final GraphRunner graphRunner;

	private final ObjectMapper objectMapper;

	@Override
	public GraphRunVO run(GraphRunRequest request) {
		return graphRunner.run(request);
	}

	@Override
	public SseEmitter stream(GraphRunRequest request) {
		SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
		CompletableFuture.runAsync(() -> {
			try {
				graphRunner.runWithEmitter(request, emitter);
				emitter.complete();
			}
			catch (Exception ex) {
				emitter.complete();
			}
		});
		return emitter;
	}

	@Override
	public StreamingResponseBody streamBody(GraphRunRequest request) {
		return outputStream -> {
			GraphRunVO result = graphRunner.run(request);
			Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			for (GraphEventDTO event : result.getEvents()) {
				writer.write("id:" + event.getEventId() + "\n");
				writer.write("event:" + event.getEventType() + "\n");
				writer.write("data:" + objectMapper.writeValueAsString(event) + "\n\n");
				writer.flush();
				try {
					Thread.sleep(30);
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		};
	}

}
