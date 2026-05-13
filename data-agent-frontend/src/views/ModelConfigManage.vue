<template>
  <section class="page">
    <h1 class="page-title">模型配置</h1>
    <el-card class="page-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="keyword" clearable />
        <el-select v-model="query.modelType" placeholder="modelType" clearable>
          <el-option label="chat" value="chat" />
          <el-option label="embedding" value="embedding" />
        </el-select>
        <el-select v-model="query.enabled" placeholder="enabled" clearable>
          <el-option label="true" :value="true" />
          <el-option label="false" :value="false" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openCreate">新建</el-button>
      </div>
    </el-card>
    <el-card class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="provider" label="provider" width="120" />
        <el-table-column prop="modelName" label="modelName" min-width="150" />
        <el-table-column prop="modelType" label="类型" width="110" />
        <el-table-column prop="hasApiKey" label="hasApiKey" width="110" />
        <el-table-column prop="maskedApiKey" label="apiKey" width="140" />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggle(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑模型' : '新建模型'" width="760px">
      <el-form label-width="130px" class="dialog-form">
        <div class="form-grid">
          <el-form-item label="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="provider">
            <el-select v-model="form.provider" filterable allow-create>
              <el-option v-for="item in providers" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="modelName"><el-input v-model="form.modelName" /></el-form-item>
          <el-form-item label="modelType">
            <el-select v-model="form.modelType"><el-option label="chat" value="chat" /><el-option label="embedding" value="embedding" /></el-select>
          </el-form-item>
          <el-form-item label="baseUrl"><el-input v-model="form.baseUrl" /></el-form-item>
          <el-form-item label="apiKey">
            <el-input v-model="form.apiKey" type="password" show-password :placeholder="editingId ? '留空表示不修改' : '请输入 API Key'" />
          </el-form-item>
          <el-form-item label="temperature"><el-input-number v-model="form.temperature" :step="0.1" /></el-form-item>
          <el-form-item label="maxTokens"><el-input-number v-model="form.maxTokens" :min="0" /></el-form-item>
          <el-form-item label="enabled"><el-switch v-model="form.enabled" /></el-form-item>
          <el-form-item label="description" class="form-full"><el-input v-model="form.description" type="textarea" /></el-form-item>
        </div>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createModelConfig, deleteModelConfig, disableModelConfig, enableModelConfig, listModelConfigs, updateModelConfig } from '../api/modelConfig';

const providers = ['mock', 'deepseek', 'openai', 'qwen', 'custom'];
const rows = ref([]);
const query = reactive({ keyword: '', modelType: '', enabled: '' });
const dialogVisible = ref(false);
const editingId = ref(null);
const emptyForm = () => ({ name: '', provider: 'mock', modelName: 'mock-chat', modelType: 'chat', baseUrl: 'mock', apiKey: '', temperature: 0.7, maxTokens: 2000, enabled: true, description: '' });
const form = reactive(emptyForm());
const loadData = async () => { rows.value = await listModelConfigs(query); };
const resetForm = (data = emptyForm()) => Object.assign(form, emptyForm(), data, { apiKey: '' });
const openCreate = () => { editingId.value = null; resetForm(); dialogVisible.value = true; };
const openEdit = (row) => { editingId.value = row.id; resetForm(row); dialogVisible.value = true; };
const buildPayload = () => {
  const payload = { ...form };
  delete payload.maskedApiKey;
  delete payload.hasApiKey;
  if (editingId.value && !payload.apiKey) {
    delete payload.apiKey;
  }
  return payload;
};
const save = async () => {
  const payload = buildPayload();
  editingId.value ? await updateModelConfig(editingId.value, payload) : await createModelConfig(payload);
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  loadData();
};
const toggle = async (row) => { row.enabled ? await disableModelConfig(row.id) : await enableModelConfig(row.id); ElMessage.success('操作成功'); loadData(); };
const doDelete = async (id) => { await ElMessageBox.confirm('确认删除该模型配置？', '提示'); await deleteModelConfig(id); ElMessage.success('删除成功'); loadData(); };
onMounted(loadData);
</script>
