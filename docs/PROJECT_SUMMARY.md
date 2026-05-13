# DataAgent Rebuild v2.0 项目总结

## 项目目标

DataAgent Rebuild v2.0 是一个参考 Spring AI Alibaba DataAgent 架构思路的学习型重建项目。目标是把企业级智能数据分析 Agent 的核心链路拆成可理解、可运行、可演示的模块。

## 最终完成内容

项目完成了从自然语言问数到 SQL 生成、SQL 安全校验、业务库查询、受限 Python 分析、Markdown / ECharts 报告和运行历史复盘的端到端闭环。

## 和官方 DataAgent 的关系

本项目不是官方源码复制，也不是官方完整生产版本。它参考官方 DataAgent 的核心架构方向，按模块逐步实现一个学习型 MVP。

## 核心模块

- Agent 管理
- ModelConfig 管理
- Datasource 管理
- SemanticModel 和 SemanticRelation
- Knowledge 和 KnowledgeChunk RAG
- PromptTemplate
- LLM / Embedding 抽象层
- Graph / SSE
- NL2SQL
- SQL 安全与修复
- Human-in-the-loop
- Python 受限沙箱
- ReportResult / ChartSpec
- Graph History
- Vue 管理后台和运行中心

## 核心链路

```text
用户问题
-> Start / LoadAgent
-> IntentRecognition
-> SchemaRecall
-> RelationRecall
-> KnowledgeLoad
-> SqlGenerate
-> SqlValidate
-> SqlRepair
-> HumanConfirm
-> SqlExecute
-> PythonAnalyze
-> ReportGenerate
-> Nl2SqlAnswer
-> Finish
```

## 技术难点

- 将复杂问数流程拆成可观测 Graph 节点。
- 让 Schema、Relation、Knowledge 三类上下文共同服务 NL2SQL。
- 处理真实模型输出的 Markdown、解释文本和异常 SQL。
- 在自动执行 SQL 前加入安全校验、LIMIT、timeout 和人工确认。
- 将运行过程持久化，方便复盘成功和失败链路。
- 在不执行任意用户代码的前提下提供受限 Python 分析能力。

## 已知限制

- 没有生产级登录、权限和租户隔离。
- Python 沙箱不是生产级强隔离。
- RAG 使用简化向量 JSON 存储，没有外部向量库。
- 主要验证 MySQL，其他数据库能力有限。
- 前端是演示型管理后台，不是完整 BI 产品。

## 后续规划

- MCP Server。
- Milvus / pgvector / Elasticsearch。
- Docker / microVM Python 沙箱。
- SQL AST 解析和更强多表规划。
- 图表配置器和报告导出。
- 登录、RBAC、审计和部署体系。
