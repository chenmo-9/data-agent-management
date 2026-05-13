# DataAgent Rebuild 演示脚本

## 1. 演示前准备

确认本机已安装：

- JDK 17+
- Maven
- Node.js / npm
- MySQL 8.x

确认 MySQL 账号：

- host: `localhost`
- port: `3306`
- username: `root`
- password: `123456`

## 2. 初始化 MySQL 管理库

```powershell
cd "D:\Downloads\DataAgent-main\DataAgent-main\spring ai"
Get-Content .\scripts\create-management-db.sql -Encoding UTF8 | mysql -uroot -p123456
```

管理库 `dataagent_management` 用于保存 DataAgent 的配置数据，例如 Agent、模型配置、数据源配置、语义模型、知识和 Prompt。

## 3. 初始化业务演示数据

```powershell
Get-Content .\scripts\init-demo-mysql.sql -Encoding UTF8 | mysql -uroot -p123456
Get-Content .\scripts\check-demo-data.sql -Encoding UTF8 | mysql -uroot -p123456
```

预期结果：

- `order_count = 3`
- `total_sales = 600.00`

业务库 `test` 用于模拟真实业务数据，本项目运行时会通过 DataSource 配置连接它。

## 4. 启动后端

推荐使用 MySQL 持久化模式：

```powershell
$env:DATAAGENT_SECRET_KEY="please-change-this-to-a-random-32-byte-secret"
.\scripts\start-backend-mysql.ps1
```

`DATAAGENT_SECRET_KEY` 用于加密模型 API Key 和数据源密码。如果不设置，后端会使用 dev key 并打印 warning，只适合本地演示。

开发回退时可以使用 H2：

```powershell
.\scripts\start-backend-h2.ps1
```

后端默认端口：

```text
http://127.0.0.1:8065
```

## 5. 启动前端

```powershell
.\scripts\start-frontend.ps1
```

浏览器访问：

```text
http://127.0.0.1:3000
```

## 6. 浏览器配置步骤

1. 打开首页，说明这是 DataAgent 智能数据分析工作台。
2. 进入 Agent 管理，创建“销售分析 Agent”。
3. 进入模型配置，创建 mock chat 模型：
   - name: `Mock Chat`
   - provider: `mock`
   - modelName: `mock-chat`
   - modelType: `chat`
   - baseUrl: `mock`
   - apiKey: `empty`
   - enabled: `true`
4. 进入数据源管理，创建 MySQL 业务数据源：
   - name: `test@localhost`
   - dbType: `mysql`
   - url: `jdbc:mysql://localhost:3306/test`
   - username: `root`
   - password: `123456`
   - enabled: `true`
5. 点击测试连接，确认连接成功。
6. 进入 Agent 数据源绑定，绑定“销售分析 Agent”和 `test@localhost`。
7. 进入语义模型，选择 `test@localhost` 数据源。
8. 创建表语义：
   - tableName: `orders`
   - businessName: `订单表`
   - description: `保存订单交易记录`
   - synonyms: `订单,销售单,交易记录`
9. 再创建表语义：
   - tableName: `users`
   - businessName: `用户表`
   - description: `保存用户主数据`
   - synonyms: `用户,客户,customer`
10. 选中 `orders` 表，创建字段语义：
   - `user_id`，businessName: `用户ID`，dataType: `bigint`，synonyms: `user_id,user,customer,用户`
   - `amount`，businessName: `销售额`，dataType: `decimal`，synonyms: `金额,订单金额,销售额`
   - `created_at`，businessName: `下单时间`，dataType: `datetime`，synonyms: `创建时间,交易时间`
11. 选中 `users` 表，创建字段语义：
   - `id`，businessName: `用户ID`，dataType: `bigint`，synonyms: `id,user_id,用户ID`
   - `name`，businessName: `用户名称`，dataType: `varchar`，synonyms: `name,username,customer name,用户名称`
12. 在语义模型页创建表关系：
   - `orders.user_id INNER JOIN users.id`
   - relationType: `logical`
   - description: `订单所属用户`
13. 进入知识管理，创建业务知识：
    - title: `销售额统计口径`
    - knowledgeType: `metric`
    - sourceType: `text`
    - content: `销售额包含已支付订单金额，不包含退款订单。按用户统计销售额时，使用 orders.user_id 关联 users.id。`
14. 再创建：
    - title: `退款规则`
    - knowledgeType: `business_rule`
    - sourceType: `text`
    - content: `退款订单不计入销售额。如果订单状态字段存在，应排除 status='refunded' 的订单。本演示库暂时没有 status 字段。`
15. 绑定这两条知识到“销售分析 Agent”。
16. 在模型配置中创建 mock embedding 模型：
    - name: `Mock Embedding`
    - provider: `mock`
    - modelName: `mock-embedding`
    - modelType: `embedding`
    - baseUrl: `mock`
    - apiKey: `empty`
    - enabled: `true`
17. 在知识管理页点击“重建向量”，给知识 chunks 生成 embedding。
18. 进入 Prompt 模板，点击初始化默认 Prompt。

## 7. 运行中心演示

进入运行中心：

- Agent: `销售分析 Agent`
- 模型配置: `Mock Chat`
- mode: `nl2sql`
- question: `最近销售额是多少？`

点击“普通运行”或“SSE 流式运行”。

## 8. 演示话术

这里选择一个销售分析 Agent，它绑定了 MySQL `test` 数据源，并配置了 `orders`、`users` 两张表的语义模型和表关系。当用户输入“最近销售额是多少？”或“每个用户的销售额是多少？”时，系统会先召回语义模型、表关系和业务知识，再生成 SELECT SQL，经过安全校验后执行，最后把结果整理成分析报告。

在事件流中可以看到一次问答被拆成多个节点：加载 Agent、意图识别、Schema 召回、Relation Recall、知识加载、SQL 生成、SQL 校验、SQL 执行、分析和报告生成。当前版本的 Schema Recall 已经支持关键词打分，Relation Recall 会继续把 `orders.user_id -> users.id` 这类 JOIN 条件送进 Prompt，而 KnowledgeLoadNode 也已经从“整包拼接知识”升级成“优先检索相关 KnowledgeChunk”，让业务规则更聚焦。报告生成节点会输出 Markdown、结构化 `ReportResult` 和 `ChartSpec`，前端运行中心会把单值结果显示成指标卡，把按用户聚合的数据显示成 ECharts 柱状图。

## 9. 预期结果

- 事件流出现 `intent_recognition`、`schema_recall`、`sql_generate`、`sql_validate`、`sql_repair`、`sql_execute`、`python_analyze`、`report_generate`、`finish`。
- Schema 召回面板中 `orders` 分数高于 `users`。
- 表关系召回面板中出现 `orders.user_id INNER JOIN users.id`。
- 知识召回面板中出现 `销售额统计口径`，并带有 score。
- SQL 结果中出现 `total_sales = 600.00`。
- 分析摘要有值。
- Markdown 报告展示本次查询结论，并提供预览和原文两种视图。
- 单值查询显示指标卡，多行分类结果显示 ECharts 图表。
- SQL 区域展示 `sanitizedSql`、自动 LIMIT、查询超时和截断信息。
- 模型配置和数据源管理页只显示 `maskedApiKey` / `maskedPassword`，不会回显真实密钥。
- 运行状态区展示 `runId`，侧边菜单可以进入“运行历史”查看本次运行详情和节点事件。
- 开启“人工确认”时，事件流会停在 `human_confirm_required`，确认后才执行 SQL。
- 使用 DeepSeek / OpenAI-compatible 模型时，运行中心可以查看 `rawLlmSqlOutput`、`extractedSql`、`repairedSql` 和 `sql_repair` 事件。
- Python 默认关闭时，Python 分析面板显示 `java_safe`；开启沙箱后显示 `python_sandbox` 或失败后的 `python_sandbox_fallback`。

## 10. DeepSeek SQL 清洗演示

真实模型可能返回：

````text
下面是 SQL：
```sql
SELECT SUM(amount) AS total_sales FROM orders;
```
这个查询用于统计销售额。
````

系统会先通过 `SqlExtractor` 提取：

```sql
SELECT SUM(amount) AS total_sales FROM orders
```

如果提取后的 SQL 无法通过校验，会进入 `sql_repair` 节点。修复后的 SQL 仍必须通过 `SqlValidator`，不会执行未校验 SQL。

## 11. Schema Recall 打分演示

为 `orders` 配置：

- table synonyms: `orders,sales,transactions,订单,销售`
- amount synonyms: `sales,amount,revenue,total_sales,销售额,金额,订单金额`

再额外建一个不相关的 `users` 表语义。

当问题是 `最近销售额是多少？` 时，运行中心里的 Schema 召回面板应当显示：

- `orders` 排在前面
- `amount` 字段高分命中
- `users` 要么不被召回，要么分数明显更低

再问 `每个用户的销售额是多少？` 时，应当看到：

- `orders` 和 `users` 都被召回
- 表关系召回面板里出现 `orders.user_id INNER JOIN users.id`
- 知识召回面板优先命中“销售额统计口径”
- mock fallback 能返回按用户聚合的 JOIN SQL

再问 `退款订单是否计入销售额？` 时，应当看到：

- 知识召回面板优先命中“退款规则”
- 即使 SQL 不一定执行，仍能从 recall 结果看到系统找到了正确业务知识

## 12. 报告和图表演示

问题 `最近销售额是多少？` 的结果通常是一行一列：

- 后端生成 `chartSpec.chartType=single_value`
- 前端展示大号指标卡，例如 `600.00`
- Markdown 预览中展示核心结论、SQL、结果概览和备注

问题 `每个用户的销售额是多少？` 的结果通常是多行两列：

- 后端生成 `chartSpec.chartType=bar`
- 前端展示柱状图，X 轴是用户名称，Y 轴是销售额
- SQL 结果表格和 Markdown 报告仍然保留，方便交叉验证

报告增强默认使用 Java 安全分析；如果开启 Python 沙箱，则使用后端模板生成的受限 Python 代码分析结果，失败会 fallback。图表配置仍由后端规则生成，不依赖 LLM。

## 13. 安全增强演示

创建新的模型配置后，可以在 MySQL 管理库查看：

```sql
SELECT id, name, api_key FROM dataagent_management.model_config;
```

新的 `api_key` 应以 `ENC:` 开头。创建新的数据源后也可以查看：

```sql
SELECT id, name, password FROM dataagent_management.datasource;
```

新的 `password` 应以 `ENC:` 开头。接口层验证：

```powershell
curl.exe http://127.0.0.1:8065/api/model-config/list
curl.exe http://127.0.0.1:8065/api/datasource/list
```

返回中只应看到 `hasApiKey`、`maskedApiKey`、`hasPassword`、`maskedPassword`，不应出现真实 Key 或数据库密码。

SQL 安全验证可以尝试诱导：

```text
DELETE FROM orders
SELECT * FROM orders; DROP TABLE orders;
SELECT SLEEP(10)
```

预期都被拦截，不会执行。正常查询会自动追加 LIMIT，并在运行中心展示 `sqlSecurityMessage`。

## 14. 运行历史演示

普通运行或 SSE 流式运行完成后，运行中心会显示 `runId`。点击“查看本次历史”，进入运行历史详情页。

详情页应能看到：

- 基本信息：runId、sessionId、状态、耗时、问题和回答
- SQL 信息：generatedSql、validatedSql、sanitizedSql
- 安全信息：自动 LIMIT、查询超时、截断状态、校验错误、执行错误
- 召回摘要：表、字段、关系、知识的召回数量
- 结果预览：只保存前 20 行
- 报告：reportTitle、reportMarkdown、chartType
- 节点事件：每个节点的 eventType、status、message、dataJson

MySQL 管理库可以直接验证：

```sql
SELECT run_id, question, status, success, row_count, chart_type, created_at
FROM dataagent_management.graph_run
ORDER BY id DESC
LIMIT 5;

SELECT run_id, node_name, event_type, status, message
FROM dataagent_management.graph_event
WHERE run_id = '替换成实际 runId'
ORDER BY id;
```

历史表中的 `data_json` 和错误信息已经做敏感字段脱敏，不应出现真实 API Key、数据库密码、Bearer Token 或 `DATAAGENT_SECRET_KEY`。

## 15. Human-in-the-loop 演示

在运行中心打开“人工确认”，再输入：

```text
最近销售额是多少？
```

预期：

- Graph 停在 `human_confirm_required`
- `graph_run.status=pending_confirm`
- 页面展示待确认 SQL
- 点击“确认执行”后继续 SQL 执行、分析和报告

修改 SQL 验证：

```sql
SELECT COUNT(*) AS order_count FROM orders
```

确认后应返回 `order_count=3`，历史中的 `confirmedSql` 保存修改后的 SQL。

危险 SQL 验证：

```sql
DELETE FROM orders
```

确认接口应被 `SqlValidator` 拦截，SQL 不会执行，订单数据不变。

取消验证：

- pending 后点击取消执行
- `graph_run.status=canceled`
- `confirm_status=canceled`
- `graph_event` 中出现 `human_cancel`

## 16. 常见问题

## 16. Python 沙箱演示

默认情况下 Python 沙箱关闭，运行中心的 Python 分析面板会显示 `java_safe`，旧链路不受影响。

如需演示受限 Python 分析，可以用 MySQL profile 启动时打开：

```powershell
mvn -gs .mvn\settings.xml -pl data-agent-management spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=mysql --dataagent.analysis.python.enabled=true"
```

如果本机命令是 `py`，追加 `--dataagent.analysis.python.python-command=py`。运行“每个用户的销售额是多少？”后，预期 Python 分析面板显示 `python_sandbox`、`pythonExecuted=true`、`pythonSuccess=true` 和执行耗时。如果配置了不存在的 Python 命令，系统会 fallback 到 `python_sandbox_fallback`，报告仍能生成。

## 17. 常见问题

### 前端无法请求后端

确认后端已启动在 `8065`，前端 Vite 代理 `/api` 指向 `http://127.0.0.1:8065`。

### MySQL 数据源连接失败

确认 MySQL 已启动，账号密码为 `root/123456`，业务库 `test` 已创建。

### 运行中心没有模型可选

确认已创建 `modelType=chat` 且 `enabled=true` 的模型配置。

### 运行时提示没有绑定数据源

进入 Agent 数据源绑定页面，为当前 Agent 绑定启用的数据源。

### 真实 DeepSeek 输出不是纯 SQL

当前版本已经支持 SQL 输出清洗与 `sql_repair` 节点。真实模型输出如果包含 Markdown、解释文本或非 SELECT SQL，会先提取、校验和修复；仍无法通过时会返回明确错误，不会执行危险 SQL。
