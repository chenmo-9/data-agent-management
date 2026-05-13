# 截图 / 录屏清单

## 建议截图

- 首页：DataAgent Rebuild v2.0 工作台。
- Agent 管理。
- 模型配置：mock chat / mock embedding / DeepSeek。
- 数据源管理：MySQL datasource 和连接测试。
- 语义模型：orders / users 表字段。
- 表关系：orders.user_id -> users.id。
- 知识管理：销售额口径、退款规则、重建向量。
- Prompt 模板。
- 运行中心：最近销售额是多少，single_value 指标卡。
- 运行中心：每个用户销售额，bar 柱状图。
- Schema Recall 面板。
- Relation Recall 面板。
- Knowledge Recall 面板。
- Human Confirm 面板。
- Python 分析面板。
- 运行历史列表。
- 运行历史详情。
- 危险 SQL 被拦截。

## 建议录屏流程

1. 打开首页。
2. 快速展示管理后台菜单。
3. 运行“最近销售额是多少？”。
4. 展示 SQL、结果、指标卡和 Markdown 报告。
5. 运行“每个用户的销售额是多少？”。
6. 展示 Relation Recall、JOIN SQL 和柱状图。
7. 展示 Knowledge Recall 命中退款规则。
8. 开启人工确认，展示 pending_confirm。
9. 修改 SQL 后执行。
10. 打开运行历史详情。
11. 输入危险 SQL 问题，展示拦截效果。
