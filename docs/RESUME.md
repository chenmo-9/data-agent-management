# 简历材料：DataAgent Rebuild

## 项目标题

DataAgent Rebuild：基于 Spring Boot + Vue 的智能数据分析 Agent

## 一句话描述

基于 Spring Boot、Vue3、MyBatis、MySQL 和 OpenAI-compatible LLM 架构，重建 Spring AI Alibaba DataAgent 核心链路，实现自然语言问数、SQL 生成、安全执行、分析报告和运行历史追踪的完整闭环。

## 技术栈

- Java 17 / Spring Boot 3.4.8
- MyBatis / H2 / MySQL
- Vue 3 / Vite / Element Plus / Axios
- SSE / ECharts / Markdown
- OpenAI-compatible API / DeepSeek / mock model

## 项目职责

- 设计并实现 Agent、模型、数据源、语义模型、知识库和 Prompt 管理模块。
- 设计 Graph 节点化 NL2SQL 工作流，支持普通运行和 SSE 流式事件。
- 实现 Schema Recall、Relation Recall、Knowledge RAG 三层上下文召回。
- 实现 SQL 输出清洗、SQL 修复、安全校验、LIMIT、timeout 和危险 SQL 拦截。
- 实现 Human-in-the-loop SQL 执行确认和运行历史持久化。
- 实现受限 Python 沙箱分析、Markdown 报告和 ECharts 图表展示。
- 实现 API Key / datasource password 加密存储和前端脱敏。

## 技术亮点

- Graph 节点化 NL2SQL：把问数流程拆成可观测、可扩展、可复盘的节点。
- 三层上下文召回：Schema / Relation / Knowledge 协同降低 Prompt 噪声。
- SQL 安全闭环：extract -> validate -> repair -> confirm -> execute。
- Human-in-the-loop：执行前可确认、修改或取消 SQL。
- 运行历史：graph_run / graph_event 保存每次运行和节点事件。
- Python 受限沙箱：模板生成代码、静态检查、超时控制、输出截断和 Java fallback。
- 前端产品化：运行中心展示事件、召回、SQL、表格、图表、报告和历史详情。

## 简历精简版

DataAgent Rebuild：基于 Spring Boot + Vue3 重建智能数据分析 Agent，支持 Agent/模型/数据源/语义模型/知识库/Prompt 管理，实现 Graph 节点化 NL2SQL、Schema/Relation/Knowledge 召回、SQL 清洗修复与安全执行、Human-in-the-loop、运行历史、Python 受限分析和 ECharts 报告展示。

## 简历详细版

基于 Spring Boot、Vue3、MyBatis、MySQL、DeepSeek API 重建 Spring AI Alibaba DataAgent 核心链路，实现从自然语言问题到 SQL 生成、SQL 安全校验、数据查询、Python 分析、ECharts 报告和运行历史追踪的完整闭环。项目支持 Agent/模型/数据源/语义模型/知识库/Prompt 管理，内置 Schema Recall、Relation Recall、RAG 检索增强、Human-in-the-loop 人工确认、API Key 加密和受限 Python 沙箱分析。

## 面试可讲问题

- 为什么 NL2SQL 要拆成 Graph 节点？
- Schema Recall 如何降低模型幻觉？
- Relation Recall 如何帮助 JOIN？
- Knowledge RAG 为什么比全量拼接更好？
- SQL 安全校验如何防止危险查询？
- Human-in-the-loop 如何恢复后半段执行？
- Python 沙箱为什么默认关闭？
- 运行历史如何保存且避免泄漏敏感信息？
