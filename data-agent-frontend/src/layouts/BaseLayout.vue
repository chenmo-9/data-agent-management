<template>
  <el-container class="app-layout">
    <el-aside width="240px" class="app-sidebar">
      <div class="app-brand">DataAgent</div>
      <el-menu router :default-active="$route.path" class="app-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/run">运行中心</el-menu-item>
        <el-menu-item index="/graph-history">运行历史</el-menu-item>
        <el-menu-item index="/agents">Agent 管理</el-menu-item>
        <el-menu-item index="/models">模型配置</el-menu-item>
        <el-menu-item index="/datasources">数据源管理</el-menu-item>
        <el-menu-item index="/agent-datasources">Agent 数据源绑定</el-menu-item>
        <el-menu-item index="/semantic-models">语义模型</el-menu-item>
        <el-menu-item index="/knowledge">知识管理</el-menu-item>
        <el-menu-item index="/prompts">Prompt 模板</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <span>Spring AI Alibaba DataAgent 管理后台</span>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
