# DataAgent Rebuild v2.0 最终验收清单

## 1. 环境验收

- JDK 17+
- Maven 可用，项目使用 `.mvn/settings.xml`
- Node.js / npm 可用
- MySQL 可用，账号示例：`root / 123456`
- Python 可选，默认沙箱关闭

## 2. 编译验收

后端：

```powershell
cd "D:\Downloads\DataAgent-main\DataAgent-main\spring ai"
mvn -gs .mvn\settings.xml -pl data-agent-management compile
```

前端：

```powershell
cd "D:\Downloads\DataAgent-main\DataAgent-main\spring ai\data-agent-frontend"
npm.cmd run build
```

最终验收记录：

- 后端 compile：通过，2026-05-08 18:14，`BUILD SUCCESS`
- 前端 build：通过，2026-05-08 18:14，`vite build` 成功

## 3. 数据库验收

管理库 `dataagent_management` 应包含：

- `agent`
- `model_config`
- `datasource`
- `agent_datasource`
- `semantic_table`
- `semantic_field`
- `semantic_relation`
- `business_knowledge`
- `knowledge_chunk`
- `agent_knowledge`
- `prompt_template`
- `graph_run`
- `graph_event`

重点字段：

- `knowledge_chunk` 包含 embedding 字段。
- `graph_run` 包含 confirm 字段。
- `graph_run` 包含 python 分析字段。

业务库 `test` 应包含：

- `orders`
- `users`

## 4. 前端页面验收

- 首页
- 运行中心
- 运行历史
- Agent 管理
- 模型配置
- 数据源管理
- Agent 数据源绑定
- 语义模型和表关系
- 知识管理
- Prompt 模板

## 5. 核心功能验收

| 场景 | 输入 | 预期 |
| --- | --- | --- |
| 单表销售额 | 最近销售额是多少？ | `total_sales=600.00`，图表为 single_value |
| 多表 JOIN | 每个用户的销售额是多少？ | Alice 300.00 / Bob 300.00，图表为 bar |
| RAG 知识 | 退款订单是否计入销售额？ | Knowledge Recall 命中退款规则 |
| 危险 SQL | 删除所有订单 | SqlValidator 拦截，不执行 DELETE |
| 人工确认 | 开启确认后问销售额 | pending_confirm，确认后继续执行 |
| 取消执行 | pending 后取消 | graph_run 为 canceled |
| 运行历史 | 任意运行完成后查看 | 能看到 SQL、事件、报告、结果预览 |
| Python 默认关闭 | 默认配置运行 | engine=java_safe |
| Python 开启 | enabled=true | engine=python_sandbox 或 python_sandbox_fallback |

## 6. 安全验收

- `GET /api/model-config/list` 不返回真实 API Key。
- `GET /api/datasource/list` 不返回真实 password。
- `model_config.api_key` 新数据以 `ENC:` 开头。
- `datasource.password` 新数据以 `ENC:` 开头。
- `DELETE / DROP / ALTER / TRUNCATE / SLEEP / 多语句` 不执行。
- `graph_run` 和 `graph_event` 不保存真实 API Key、password、Bearer token 或 `DATAAGENT_SECRET_KEY`。

## 7. 最终通过标准

- 后端 compile 通过。
- 前端 build 通过。
- MySQL profile 能启动。
- 首页、运行中心、运行历史能访问。
- 单表、多表、RAG、安全拦截、人工确认、运行历史和 Python fallback 全部可演示。

## 8. 路径验收

本轮所有修改均位于：

```text
D:\Downloads\DataAgent-main\DataAgent-main\spring ai
```

父目录中存在官方源项目原始目录 `data-agent-management`、`data-agent-frontend`，本轮未修改这些目录，也未新建 `spring-ai` 或 `spring_ai` 错误模块。
