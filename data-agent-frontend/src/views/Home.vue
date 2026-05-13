<template>
  <section class="home-page workbench-home">
    <el-card class="home-hero" shadow="never">
      <div class="home-hero-content">
        <div>
          <h1>DataAgent Rebuild v2.0</h1>
          <p>
            基于 Spring AI Alibaba DataAgent 架构思路重建的智能数据分析工作台，已完成自然语言问数、NL2SQL、
            SQL 安全执行、RAG 知识召回、人工确认、Python 受限分析、ECharts 报告和运行历史闭环。
          </p>
          <div class="home-tags">
            <el-tag type="success">第 27 轮最终版</el-tag>
            <el-tag type="info">学习型重建</el-tag>
            <el-tag type="warning">演示级安全边界</el-tag>
          </div>
        </div>
        <div class="quick-actions">
          <el-button type="primary" size="large" @click="$router.push('/run')">进入运行中心</el-button>
          <el-button size="large" @click="$router.push('/graph-history')">查看运行历史</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="page-card" shadow="never">
      <template #header>
        <span>核心能力</span>
      </template>
      <el-row :gutter="16">
        <el-col v-for="item in capabilities" :key="item.title" :xs="24" :sm="12" :lg="8">
          <div class="capability-item">
            <el-tag :type="item.type" effect="light">{{ item.tag }}</el-tag>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="page-card" shadow="never">
      <template #header>
        <span>DataAgent Rebuild 完成时间线</span>
      </template>
      <el-timeline>
        <el-timeline-item v-for="item in timeline" :key="item.round" :timestamp="item.round" placement="top">
          <el-card shadow="never" class="timeline-card">
            <strong>{{ item.title }}</strong>
            <p>{{ item.description }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <el-card class="page-card" shadow="never">
      <template #header>
        <span>推荐演示流程</span>
      </template>
      <div class="workflow-steps">
        <template v-for="(step, index) in workflow" :key="step">
          <el-tag size="large">{{ step }}</el-tag>
          <span v-if="index < workflow.length - 1" class="workflow-arrow">→</span>
        </template>
      </div>
    </el-card>

    <el-card class="page-card" shadow="never">
      <template #header>
        <span>快速入口</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/run')">进入运行中心</el-button>
        <el-button @click="$router.push('/graph-history')">运行历史</el-button>
        <el-button @click="$router.push('/agents')">Agent 管理</el-button>
        <el-button @click="$router.push('/datasources')">数据源管理</el-button>
        <el-button @click="$router.push('/semantic-models')">语义模型</el-button>
        <el-button @click="$router.push('/knowledge')">知识管理</el-button>
        <el-button @click="$router.push('/prompts')">Prompt 模板</el-button>
      </div>
    </el-card>

    <el-alert
      title="已知限制：本项目是学习型重建版，Python 沙箱、RAG、权限和部署能力均为演示级实现，不建议直接用于生产。"
      type="warning"
      show-icon
      :closable="false"
    />
  </section>
</template>

<script setup>
const capabilities = [
  {
    title: '智能体管理',
    tag: 'Agent',
    type: 'success',
    description: '配置不同业务场景的数据分析 Agent。',
  },
  {
    title: '模型配置',
    tag: 'Model',
    type: 'primary',
    description: '管理 mock、DeepSeek、OpenAI-compatible 模型。',
  },
  {
    title: '数据源管理',
    tag: 'Datasource',
    type: 'warning',
    description: '维护 MySQL / H2 等业务数据源。',
  },
  {
    title: '语义模型',
    tag: 'Semantic',
    type: 'info',
    description: '维护表字段业务含义，辅助 NL2SQL。',
  },
  {
    title: '知识管理',
    tag: 'Knowledge',
    type: 'danger',
    description: '维护业务规则、指标口径和 FAQ。',
  },
  {
    title: '运行中心',
    tag: 'Run',
    type: 'success',
    description: '输入自然语言问题，查看 Graph 节点、SQL、图表、报告和人工确认。',
  },
  {
    title: '运行历史',
    tag: 'History',
    type: 'info',
    description: '持久化 graph_run / graph_event，支持复盘成功和失败链路。',
  },
  {
    title: '安全增强',
    tag: 'Security',
    type: 'warning',
    description: 'API Key 加密、SQL 拦截、LIMIT、timeout 和敏感日志脱敏。',
  },
];

const workflow = [
  '创建 Agent',
  '配置模型',
  '配置数据源',
  '绑定数据源',
  '维护语义模型',
  '维护知识',
  '运行问数',
];

const timeline = [
  {
    round: '1-15',
    title: 'v1.0 功能闭环',
    description: '完成后端管理模块、Graph/NL2SQL、分析报告、前端管理后台和运行中心。',
  },
  {
    round: '16-18',
    title: '持久化与真实模型稳定性',
    description: '支持 MySQL 管理库、项目文档、DeepSeek SQL 输出清洗和 SQL 修复节点。',
  },
  {
    round: '19-22',
    title: '召回、JOIN、RAG 和报告增强',
    description: '实现 Schema Recall、Relation Recall、Knowledge RAG、Markdown 美化和 ECharts 图表。',
  },
  {
    round: '23-26',
    title: '安全、历史、人工确认和 Python 沙箱',
    description: '实现密钥加密、SQL 安全、运行历史、Human-in-the-loop 和受限 Python 分析。',
  },
  {
    round: '27',
    title: '最终收尾',
    description: '补齐验收清单、演示脚本、面试材料、简历描述和截图录屏清单。',
  },
];
</script>
