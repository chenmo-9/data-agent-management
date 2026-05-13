# API Overview

所有接口默认前缀为后端服务 `http://127.0.0.1:8065`，前端通过 Vite `/api` 代理访问。

## Agent

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/agent/create` | 创建 Agent |
| GET | `/api/agent/list` | 查询 Agent 列表 |
| GET | `/api/agent/{id}` | 查询 Agent 详情 |
| PUT | `/api/agent/{id}` | 更新 Agent |
| DELETE | `/api/agent/{id}` | 删除 Agent |
| PUT | `/api/agent/{id}/publish` | 发布 Agent |
| PUT | `/api/agent/{id}/offline` | 下线 Agent |

## ModelConfig

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/model-config/create` | 创建模型配置 |
| GET | `/api/model-config/list` | 查询模型配置 |
| GET | `/api/model-config/{id}` | 查询模型详情 |
| PUT | `/api/model-config/{id}` | 更新模型配置 |
| DELETE | `/api/model-config/{id}` | 删除模型配置 |
| PUT | `/api/model-config/{id}/enable` | 启用模型 |
| PUT | `/api/model-config/{id}/disable` | 禁用模型 |

返回对象不会包含真实 `apiKey`，只返回：

- `hasApiKey`
- `maskedApiKey`

编辑时如果不传 `apiKey` 或传空值，后端会保留原密钥。

## Datasource

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/datasource/create` | 创建数据源 |
| GET | `/api/datasource/list` | 查询数据源 |
| GET | `/api/datasource/{id}` | 查询数据源详情 |
| PUT | `/api/datasource/{id}` | 更新数据源 |
| DELETE | `/api/datasource/{id}` | 删除数据源 |
| PUT | `/api/datasource/{id}/enable` | 启用数据源 |
| PUT | `/api/datasource/{id}/disable` | 禁用数据源 |
| POST | `/api/datasource/test` | 临时测试连接 |
| POST | `/api/datasource/{id}/test` | 测试已保存数据源 |

返回对象不会包含真实 `password`，只返回：

- `hasPassword`
- `maskedPassword`

保存到管理库的 password 会加密；连接测试和 SQL 执行前由后端临时解密使用。

## AgentDatasource

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/agent-datasource/bind` | 绑定 Agent 与数据源 |
| GET | `/api/agent-datasource/list` | 查询绑定列表 |
| GET | `/api/agent-datasource/agent/{agentId}` | 查询某 Agent 的数据源 |
| DELETE | `/api/agent-datasource/{id}` | 删除绑定关系 |
| DELETE | `/api/agent-datasource/unbind` | 按 agentId + datasourceId 解绑 |
| PUT | `/api/agent-datasource/{id}/enable` | 启用绑定关系 |
| PUT | `/api/agent-datasource/{id}/disable` | 禁用绑定关系 |

## SemanticModel

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/semantic-model/table/create` | 创建表语义 |
| GET | `/api/semantic-model/table/list` | 查询表语义 |
| GET | `/api/semantic-model/table/{id}` | 查询表语义详情 |
| PUT | `/api/semantic-model/table/{id}` | 更新表语义 |
| DELETE | `/api/semantic-model/table/{id}` | 删除表语义 |
| PUT | `/api/semantic-model/table/{id}/enable` | 启用表语义 |
| PUT | `/api/semantic-model/table/{id}/disable` | 禁用表语义 |
| POST | `/api/semantic-model/field/create` | 创建字段语义 |
| GET | `/api/semantic-model/field/list` | 查询字段语义 |
| GET | `/api/semantic-model/field/{id}` | 查询字段详情 |
| PUT | `/api/semantic-model/field/{id}` | 更新字段语义 |
| DELETE | `/api/semantic-model/field/{id}` | 删除字段语义 |
| PUT | `/api/semantic-model/field/{id}/enable` | 启用字段语义 |
| PUT | `/api/semantic-model/field/{id}/disable` | 禁用字段语义 |
| POST | `/api/semantic-model/relation/create` | 创建表关系 |
| GET | `/api/semantic-model/relation/list` | 查询表关系 |
| GET | `/api/semantic-model/relation/{id}` | 查询表关系详情 |
| PUT | `/api/semantic-model/relation/{id}` | 更新表关系 |
| DELETE | `/api/semantic-model/relation/{id}` | 删除表关系 |
| PUT | `/api/semantic-model/relation/{id}/enable` | 启用表关系 |
| PUT | `/api/semantic-model/relation/{id}/disable` | 禁用表关系 |
| GET | `/api/semantic-model/table/{id}/model` | 查询某表完整语义模型 |
| GET | `/api/semantic-model/datasource/{datasourceId}/models` | 查询某数据源的语义模型 |
| GET | `/api/semantic-model/datasource/{datasourceId}/relations` | 查询某数据源的表关系 |
| GET | `/api/semantic-model/table/{tableId}/relations` | 查询某张表的表关系 |

## Knowledge

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/knowledge/business/create` | 创建业务知识 |
| GET | `/api/knowledge/business/list` | 查询业务知识 |
| GET | `/api/knowledge/business/{id}` | 查询业务知识详情 |
| PUT | `/api/knowledge/business/{id}` | 更新业务知识 |
| DELETE | `/api/knowledge/business/{id}` | 删除业务知识 |
| PUT | `/api/knowledge/business/{id}/enable` | 启用业务知识 |
| PUT | `/api/knowledge/business/{id}/disable` | 禁用业务知识 |
| POST | `/api/knowledge/business/upload` | 上传 txt 知识文件 |
| POST | `/api/knowledge/agent/bind` | 绑定 Agent 与知识 |
| GET | `/api/knowledge/agent/list` | 查询知识绑定 |
| GET | `/api/knowledge/agent/{agentId}` | 查询某 Agent 的知识 |
| DELETE | `/api/knowledge/agent/{id}` | 删除知识绑定 |
| DELETE | `/api/knowledge/agent/unbind` | 按 agentId + knowledgeId 解绑 |
| PUT | `/api/knowledge/agent/{id}/enable` | 启用知识绑定 |
| PUT | `/api/knowledge/agent/{id}/disable` | 禁用知识绑定 |
| POST | `/api/knowledge/business/{id}/chunks/rebuild` | 重建知识切片 |
| GET | `/api/knowledge/business/{id}/chunks` | 查询知识切片 |
| DELETE | `/api/knowledge/business/{id}/chunks` | 删除知识切片 |
| POST | `/api/knowledge/business/{knowledgeId}/embedding/rebuild` | 重建单条知识 chunks 的 embedding |
| POST | `/api/knowledge/agent/{agentId}/embedding/rebuild` | 重建某 Agent 绑定知识的 embedding |
| DELETE | `/api/knowledge/business/{knowledgeId}/embedding` | 清除单条知识的 embedding |

## PromptTemplate

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/prompt-template/create` | 创建 Prompt 模板 |
| GET | `/api/prompt-template/list` | 查询 Prompt 模板 |
| GET | `/api/prompt-template/{id}` | 查询 Prompt 详情 |
| PUT | `/api/prompt-template/{id}` | 更新 Prompt |
| DELETE | `/api/prompt-template/{id}` | 删除 Prompt |
| PUT | `/api/prompt-template/{id}/enable` | 启用 Prompt |
| PUT | `/api/prompt-template/{id}/disable` | 禁用 Prompt |
| POST | `/api/prompt-template/render` | 渲染 Prompt |
| POST | `/api/prompt-template/init-defaults` | 初始化默认 Prompt |

## LLM

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/llm/chat` | 调用 LLM 抽象层，支持 mock 和 OpenAI-compatible |

## Graph

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/graph/run` | 普通 Graph 运行，返回完整结果 |
| POST | `/api/graph/stream` | POST SSE 流式运行 |
| GET | `/api/graph/stream-get` | GET SSE 流式运行，方便浏览器和 curl 测试 |

运行 Graph 时，除了 `agentId`、`modelConfigId`、`question`、`mode` 外，还支持：

- `embeddingModelConfigId`：指定知识召回使用的 embedding 模型
- `knowledgeTopK`：知识召回返回的 chunk 数量，默认 5
- `confirmBeforeExecute`：是否在 SQL 执行前进入人工确认
- `sessionId`：可选会话 ID

`/api/graph/run` 的 NL2SQL 返回结果包含 SQL 清洗和修复字段：

- `rawLlmSqlOutput`
- `extractedSql`
- `generatedSql`
- `repairedSql`
- `validatedSql`
- `sqlRepairAttempted`
- `sqlRepairSuccess`
- `sqlRepairMessage`
- `sqlValidationError`
- `sqlExecutionError`

同时返回 SQL 安全执行字段：

- `sanitizedSql`
- `sqlLimited`
- `sqlLimit`
- `sqlResultTruncated`
- `sqlQueryTimeoutSeconds`
- `sqlSecurityMessage`
- `runId`
- `durationMs`
- `historySaved`
- `eventCount`

同时返回 Schema Recall 相关字段：

- `schemaRecallResult`
- `schemaRecallFallbackUsed`
- `schemaRecallMessage`
- `recalledTableCount`
- `recalledFieldCount`
- `schemaContext`

同时返回 Relation Recall 相关字段：

- `relationRecallResult`
- `relationContext`
- `recalledRelationCount`
- `relationRecallMessage`
- `relationRecallFallbackUsed`

同时返回 Knowledge Recall 相关字段：

- `knowledgeRecallResult`
- `knowledgeContext`
- `knowledgeRecallFallbackUsed`
- `knowledgeRecallMessage`
- `recalledKnowledgeCount`

同时返回报告增强相关字段：

- `reportTitle`
- `reportMarkdown`
- `reportResult`
- `chartSpec`
- `analysisSummary`
- `analysisResult`
- `pythonEngine`
- `pythonExecuted`
- `pythonSuccess`
- `pythonDurationMs`
- `pythonFallbackUsed`
- `pythonErrorMessage`

`chartSpec` 用于前端图表展示，当前支持：

- `single_value`：一行一列结果的指标卡
- `bar`：分类字段 + 数值字段的柱状图
- `line`：时间字段 + 数值字段的折线图
- `pie`：比例、占比、分布类问题的饼图
- `none`：暂无可视化图表

## Analysis

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/analysis/analyze` | 对 SQL 查询结果做安全统计分析 |
| POST | `/api/analysis/report` | 根据查询结果和分析生成 Markdown、ReportResult 和 ChartSpec |

`/api/analysis/report` 保留旧字段：

- `title`
- `markdown`
- `success`
- `message`

同时新增：

- `chartSpec`
- `sections`
- `metrics`
- `reportResult`

`/api/analysis/analyze` 会根据 `dataagent.analysis.python.enabled` 决定使用 Java 安全统计或受限 Python 沙箱。Python 沙箱返回字段包括 `engine`、`pythonExecuted`、`pythonSuccess`、`pythonDurationMs`、`pythonErrorMessage` 和 `fallbackUsed`。

## GraphHistory

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/graph-history/list` | 查询运行历史列表 |
| GET | `/api/graph-history/{runId}` | 查询某次运行详情 |
| GET | `/api/graph-history/{runId}/events` | 查询某次运行节点事件 |
| DELETE | `/api/graph-history/{runId}` | 删除某次运行历史和事件 |

列表查询支持：

- `keyword`：搜索 question / answer / SQL
- `agentId`
- `modelConfigId`
- `mode`
- `status`
- `success`
- `startTime`
- `endTime`
- `page`
- `pageSize`

运行详情包含 SQL、安全信息、Recall 统计、结果预览、报告和节点事件。历史保存前会做敏感字段脱敏，不应包含 API Key、数据库密码、Bearer Token 或 `DATAAGENT_SECRET_KEY`。

## GraphHuman

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/graph-human/{runId}` | 查询待确认运行 |
| POST | `/api/graph-human/{runId}/confirm` | 确认或修改 SQL 后继续执行 |
| POST | `/api/graph-human/{runId}/cancel` | 取消待确认运行 |

`confirm` 请求体：

- `sql`：可选。为空时使用 `confirmSql`；非空时作为人工修改 SQL。
- `comment`：可选确认说明。
- `confirmedBy`：可选，本轮默认 `local_user`。

确认接口会重新执行 SQL 安全校验和 LIMIT 处理，不会直接执行用户输入 SQL。取消接口会把 `graph_run.status` 更新为 `canceled`，并写入 `human_cancel` 事件。

## 最终 API 验收重点

- 管理类接口统一返回 `ApiResponse`。
- ModelConfig / Datasource 不返回真实 `apiKey` 或 `password`。
- Graph Run 返回 `runId`，可在 GraphHistory 中复盘。
- Graph SSE 使用 GET `/api/graph/stream-get` 时适合浏览器 `EventSource`。
- GraphHuman 确认接口不会绕过 SQL 安全规则。
- Analysis 接口支持 Java 安全分析和可选 Python 沙箱分析。
