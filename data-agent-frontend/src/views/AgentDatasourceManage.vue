<template>
  <section class="page">
    <h1 class="page-title">Agent 数据源绑定</h1>
    <el-card class="page-card">
      <div class="toolbar">
        <el-select v-model="form.agentId" placeholder="选择 Agent" filterable clearable>
          <el-option v-for="item in agents" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="form.datasourceId" placeholder="选择 Datasource" filterable clearable>
          <el-option v-for="item in datasources" :key="item.id" :label="`${item.name} (${item.dbType})`" :value="item.id" />
        </el-select>
        <el-button type="success" @click="bind">绑定</el-button>
        <el-button type="primary" @click="loadData">查询全部</el-button>
        <el-button @click="loadByAgent">按 Agent 查询</el-button>
      </div>
    </el-card>
    <el-card class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="agentName" label="Agent" min-width="150" />
        <el-table-column prop="datasourceName" label="Datasource" min-width="180" />
        <el-table-column prop="dbType" label="dbType" width="100" />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggle(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listAgents } from '../api/agent';
import { listDatasources } from '../api/datasource';
import { bindAgentDatasource, deleteAgentDatasource, disableAgentDatasource, enableAgentDatasource, listAgentDatasource, listDatasourceByAgent } from '../api/agentDatasource';

const agents = ref([]);
const datasources = ref([]);
const rows = ref([]);
const form = reactive({ agentId: null, datasourceId: null });
const loadOptions = async () => { agents.value = await listAgents({}); datasources.value = await listDatasources({}); };
const loadData = async () => { rows.value = await listAgentDatasource({}); };
const loadByAgent = async () => { rows.value = form.agentId ? await listDatasourceByAgent(form.agentId) : await listAgentDatasource({}); };
const bind = async () => { await bindAgentDatasource(form); ElMessage.success('绑定成功'); loadData(); };
const toggle = async (row) => { row.enabled ? await disableAgentDatasource(row.id) : await enableAgentDatasource(row.id); ElMessage.success('操作成功'); loadData(); };
const remove = async (id) => { await ElMessageBox.confirm('确认删除绑定？', '提示'); await deleteAgentDatasource(id); ElMessage.success('删除成功'); loadData(); };
onMounted(() => { loadOptions(); loadData(); });
</script>
