<template>
  <section class="page">
    <h1 class="page-title">Prompt 模板</h1>
    <el-card class="page-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="keyword" clearable />
        <el-input v-model="query.promptKey" placeholder="promptKey" clearable />
        <el-input v-model="query.scene" placeholder="scene" clearable />
        <el-select v-model="query.enabled" placeholder="enabled" clearable><el-option label="true" :value="true" /><el-option label="false" :value="false" /></el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openForm()">新建</el-button>
        <el-button @click="initDefaults">init-defaults</el-button>
      </div>
    </el-card>
    <el-card class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="promptKey" label="promptKey" min-width="160" />
        <el-table-column prop="name" label="name" min-width="160" />
        <el-table-column prop="scene" label="scene" width="110" />
        <el-table-column prop="version" label="version" width="100" />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openForm(row)">编辑</el-button>
            <el-button size="small" @click="openRender(row)">render</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggle(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑 Prompt' : '新建 Prompt'" width="820px">
      <el-form label-width="130px" class="dialog-form">
        <div class="form-grid">
          <el-form-item label="promptKey"><el-input v-model="form.promptKey" /></el-form-item>
          <el-form-item label="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="scene"><el-input v-model="form.scene" /></el-form-item>
          <el-form-item label="version"><el-input v-model="form.version" /></el-form-item>
          <el-form-item label="enabled"><el-switch v-model="form.enabled" /></el-form-item>
          <el-form-item label="description" class="form-full"><el-input v-model="form.description" /></el-form-item>
          <el-form-item label="content" class="form-full"><el-input v-model="form.content" type="textarea" :rows="10" /></el-form-item>
        </div>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="renderDialog" title="Render Prompt" width="820px">
      <el-form label-width="130px">
        <el-form-item label="variables JSON"><el-input v-model="variablesJson" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="rendered"><pre>{{ renderedContent }}</pre></el-form-item>
      </el-form>
      <template #footer><el-button @click="renderDialog=false">关闭</el-button><el-button type="primary" @click="render">渲染</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createPromptTemplate, deletePromptTemplate, disablePromptTemplate, enablePromptTemplate, initDefaultPrompts, listPromptTemplates, renderPromptTemplate, updatePromptTemplate } from '../api/promptTemplate';

const rows = ref([]);
const query = reactive({ keyword: '', promptKey: '', scene: '', enabled: '' });
const dialogVisible = ref(false);
const renderDialog = ref(false);
const form = reactive({});
const renderTarget = ref(null);
const variablesJson = ref('{"question":"最近销售额是多少？","schema":"orders(amount)","knowledge":"销售额=sum(amount)"}');
const renderedContent = ref('');
const loadData = async () => { rows.value = await listPromptTemplates(query); };
const openForm = (row) => { Object.assign(form, { id: null, promptKey: '', name: '', scene: 'general', content: '', version: 'v1', enabled: true, description: '' }, row || {}); dialogVisible.value = true; };
const save = async () => { form.id ? await updatePromptTemplate(form.id, form) : await createPromptTemplate(form); ElMessage.success('保存成功'); dialogVisible.value = false; loadData(); };
const toggle = async (row) => { row.enabled ? await disablePromptTemplate(row.id) : await enablePromptTemplate(row.id); loadData(); };
const remove = async (id) => { await ElMessageBox.confirm('确认删除 Prompt？', '提示'); await deletePromptTemplate(id); loadData(); };
const initDefaults = async () => { await initDefaultPrompts(); ElMessage.success('初始化完成'); loadData(); };
const openRender = (row) => { renderTarget.value = row; renderedContent.value = ''; renderDialog.value = true; };
const render = async () => {
  let variables;
  try { variables = JSON.parse(variablesJson.value || '{}'); } catch { ElMessage.error('variables JSON 解析失败'); return; }
  const result = await renderPromptTemplate({ templateId: renderTarget.value.id, variables });
  renderedContent.value = result.renderedContent;
};
onMounted(loadData);
</script>
