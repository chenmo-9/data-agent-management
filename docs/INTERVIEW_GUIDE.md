# DataAgent Rebuild 面试讲解指南

## 1. 项目一句话介绍

这是一个参考 Spring AI Alibaba DataAgent 架构重建的智能数据分析 Agent MVP，支持用户用自然语言提问，系统自动完成 Schema 召回、Relation Recall、Knowledge Recall、SQL 生成、SQL 校验、SQL 执行、结果分析和 Markdown 报告展示。

## 2. 为什么做这个项目

我想系统学习企业级数据分析 Agent 的工程拆分方式，而不是只写一个简单的 Chat Demo。这个项目把 Agent 配置、模型配置、数据源、语义模型、知识、Prompt、Graph 工作流和前端运行中心都串起来，形成一个可演示的端到端系统。

## 3. 整体架构

前端使用 Vue 3 + Element Plus，提供管理后台和运行中心。后端使用 Spring Boot + MyBatis，管理数据存储在 H2 或 MySQL。核心问答链路由 GraphRunner 编排，节点包括意图识别、Schema 关键词召回、Relation Recall、Knowledge Recall、SQL 生成、SQL 提取、SQL 校验、SQL 修复、SQL 执行、分析、结构化报告和图表展示。

## 4. 一次 NL2SQL 请求如何流转

1. 前端运行中心提交 Agent、模型和问题。
2. GraphController 接收请求。
3. GraphRunner 初始化 GraphState。
4. StartNode 校验问题。
5. LoadAgentNode 加载 Agent。
6. IntentRecognitionNode 判断是否是数据查询。
7. SchemaRecallNode 根据 Agent 绑定的数据源加载语义模型，并按关键词做表字段打分召回。
8. RelationRecallNode 根据已召回表补充 JOIN 关系。
9. KnowledgeLoadNode 优先检索相关 KnowledgeChunk，没有 embedding 时再回退到绑定知识拼接。
10. SqlGenerateNode 调用 LlmService 或 mock fallback 生成 SQL，并保留 raw output。
11. SqlExtractor 提取第一条 SELECT SQL。
12. SqlValidateNode 校验 SQL 安全性。
13. SqlRepairNode 在校验失败时做规则修复或 LLM 修复。
14. HumanConfirmNode 可在执行前暂停，等待用户确认、修改 SQL 或取消。
15. SqlExecuteNode 连接业务库执行 SELECT。
16. PythonAnalyzeNode 默认用 Java 安全统计；开启沙箱后执行后端模板生成的受限 Python 分析代码，失败自动 fallback。
17. ReportGenerateNode 生成 Markdown、结构化 ReportResult 和 ChartSpec。
18. FinishNode 输出最终结果。
19. GraphHistoryService 保存运行主记录和节点事件。
20. 前端展示事件、Schema Recall、Relation Recall、Knowledge Recall、raw/extracted/repaired SQL、表格、分析、Markdown 预览和 ECharts 图表。

## 5. 为什么要 Agent / Datasource / SemanticModel / Knowledge / Prompt

- Agent：代表一个业务场景，例如销售分析、运营分析。
- Datasource：描述业务数据库连接，是 Agent 可访问数据的入口。
- SemanticModel：把数据库表字段翻译成业务语言，帮助模型理解 `amount` 是销售额。
- Knowledge：保存业务规则、指标口径和 FAQ，补充数据库结构本身没有的信息。
- Prompt：把不同场景的提示词模板独立管理，方便后续迭代。

## 6. 为什么要 Graph/SSE

自然语言问数不是一个单步操作。拆成 Graph 节点后，每一步职责清晰，可调试、可观测、可扩展。SSE 可以让前端实时看到节点执行过程，而不是等后端全部执行完才返回。

## 7. 为什么 SQL 要校验

LLM 输出不可完全信任。为了避免误执行危险 SQL，SqlValidator 只允许 SELECT / WITH SELECT，并禁止 INSERT、UPDATE、DELETE、DROP、ALTER、TRUNCATE、CREATE、CALL、EXEC、多语句、注释和 SLEEP、BENCHMARK、LOAD_FILE 这类危险函数。这是数据分析 Agent 的底线安全措施。即使 SQL 经过 LLM 修复，也必须再次通过 SqlValidator 才能执行。执行阶段还会自动追加 LIMIT、限制最大行数并设置 JDBC query timeout。

## 8. 为什么当前不执行任意 Python

执行用户或模型生成的 Python 代码风险很高，可能访问文件系统、执行系统命令或联网。当前版本新增了“受限 Python 沙箱 MVP”，但仍然不执行用户任意代码，只执行后端模板生成的统计代码。执行前会做静态安全检查，拦截 `os/subprocess/socket/open/eval/exec/__import__` 等危险能力；执行时使用临时目录、超时和 stdout/stderr 截断。如果 Python 不可用或执行失败，会 fallback 到 Java 安全统计，保证主链路不崩。

## 9. 为什么报告要结构化和图表化

原始 Markdown 可以表达结论，但演示时不够直观。现在后端会把报告拆成 `ReportResult`、`ReportSection` 和 `ChartSpec`，前端按 ChartSpec 渲染指标卡、柱状图、折线图或饼图。这样既保留 Markdown 报告，又能把多行结果快速变成可视化。

## 10. 为什么要做运行历史

Graph/SSE 只能让当前页面看到过程，刷新后就丢了。运行历史把一次问数的主记录保存到 `graph_run`，把每个节点事件保存到 `graph_event`，失败时也能回看卡在哪个节点、模型输出了什么、SQL 为什么被拦截。它也是后续 Human-in-the-loop 的基础，因为人工确认需要保存 pending/running 状态。

## 11. 为什么要 Human-in-the-loop

真实数据分析系统里，即使 SQL 通过了安全校验，也可能存在业务风险，比如复杂 JOIN、口径不确定或真实模型生成的 SQL 不够直观。Human-in-the-loop 让系统在执行 SQL 前暂停，用户可以确认、修改或取消。它降低了自动执行风险，但不会绕过安全规则：修改后的 SQL 仍必须重新经过 SqlValidator、LIMIT、maxRows 和 timeout。

## 12. H2 和 MySQL profile 的区别

H2 profile 适合快速开发，启动简单，但管理数据可能随重启丢失。MySQL profile 连接 `dataagent_management`，可以持久保存 Agent、模型、数据源、语义模型、知识和 Prompt 配置。

## 13. API Key 和数据源密码怎么保护

当前版本做的是开发演示级基础保护：模型 API Key 和数据源密码在创建、更新时通过 AES-GCM 加密后存入管理库，主密钥来自 `DATAAGENT_SECRET_KEY` 环境变量。接口返回时只给 `hasApiKey/hasPassword` 和脱敏值，前端编辑时留空表示不修改。历史明文数据仍能兼容读取，重新保存后会转为加密格式。

这还不是完整生产级密钥管理，生产环境还需要 KMS/Vault、权限隔离、审计和密钥轮换。

## 14. 项目难点

- 如何把官方 DataAgent 的复杂能力拆成适合学习实现的模块。
- 如何设计 DTO / VO / Converter，让 Controller 不直接暴露 Entity。
- 如何把 Agent、Datasource、SemanticModel、Knowledge 串成 NL2SQL 上下文。
- 如何让 Graph 节点既能普通返回，又能 SSE 流式展示。
- 如何保证 SQL 执行安全。
- 如何处理 API Key、数据源密码这类敏感字段，避免入库和接口回显明文。
- 如何把运行历史做成旁路能力，既能复盘，又不影响主链路稳定性。
- 如何让人工确认恢复后半段执行，同时不绕过 SQL 安全校验。
- 如何区分管理库和业务库，避免混淆。

## 15. 还能怎么优化

- 将当前简化向量召回升级为 Milvus / pgvector / Elasticsearch 等生产级向量检索。
- 支持更多多表 JOIN 和关系规划。
- 支持更复杂图表展示和报告导出。
- Human-in-the-loop 人工确认。
- 增加登录、权限、多用户隔离。
- 接入 KMS/Vault、密钥轮换和审计。
- 将本地 Python 沙箱升级为 Docker / microVM / K8s sandbox，并增加 CPU / 内存 / 只读文件系统等生产级隔离。
- 对接 MCP Server。

## 16. 可能被问的问题和参考回答

### 这个项目和官方 DataAgent 是什么关系？

这是参考官方 DataAgent 架构思路做的学习型重建，不是官方完整版本。目标是理解核心模块和端到端链路。

### 为什么不用一个 Controller 直接调用模型？

因为真实数据分析链路包含 Agent 配置、数据源、语义模型、知识、Prompt、SQL 校验和执行等多个步骤。放在 Controller 里会让职责混乱，也不利于测试和扩展。

### mock 模型有什么意义？

mock 模型让本地演示不依赖真实 API Key。它可以稳定验证 Graph、NL2SQL、SQL 执行、分析和报告链路。

### 真实 DeepSeek 接入后会遇到什么问题？

真实模型可能输出解释文本、Markdown 或带代码块的 SQL。本项目已经增加 SqlExtractor 和 SqlRepairNode：先提取第一条 SELECT，校验失败时先规则修复，再尝试 `sql_repair` Prompt 修复，并且修复后仍要过安全校验。

### 为什么要语义模型？

数据库字段名通常偏技术，例如 `amount`、`created_at`。语义模型把它们映射到业务含义，例如销售额、下单时间，帮助模型生成更准确的 SQL。

### 为什么要做 Schema Recall 增强？

如果把一个数据源下所有表字段都拼进 Prompt，真实模型很容易被噪声干扰。现在会根据问题关键词、业务名、同义词和描述做打分，只保留最相关的 topN 表和 topM 字段，这样更稳，也为后续多表 JOIN 和向量召回留了接口。

### 为什么还要单独做表关系管理？

即使召回到了 `orders` 和 `users`，模型也不应该凭空猜 `orders.user_id = users.id`。我们把 JOIN 条件单独建模成 `semantic_relation`，再通过 `RelationRecallNode` 把这些关系送进 Prompt，这样多表查询更稳定，也更容易继续扩展到复杂 JOIN。

### 为什么要知识模块？

很多指标口径不在数据库结构里，例如“销售额是否含退款”。知识模块用于补充这些业务规则。

### 为什么还要做知识检索增强？

如果把 Agent 绑定的所有知识都拼进 Prompt，知识一多就会出现噪声。现在 Knowledge 模块会先把 `KnowledgeChunk` 向量化，再按问题召回 topK 相关片段；没有 embedding 模型时仍会回退到关键词匹配和绑定知识拼接。这样既能体现 RAG 思路，也能保持本地可运行。

### 管理库和业务库为什么分开？

管理库保存系统配置，业务库保存被分析的数据。分开后 DataAgent 可以管理多个业务数据源，也不会污染业务库结构。

### 这个项目现在能不能生产使用？

当前版本适合学习和演示，不建议直接生产使用。生产环境还需要权限、加密、审计、连接池、限流、沙箱和更完整测试。
