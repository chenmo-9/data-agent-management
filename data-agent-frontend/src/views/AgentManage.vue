<template>
  <section class="page">
    <h1 class="page-title">Agent 管理</h1>
    <el-card class="page-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="keyword" clearable />
        <el-select v-model="query.status" placeholder="status" clearable>
          <el-option label="draft" value="draft" />
          <el-option label="published" value="published" />
          <el-option label="offline" value="offline" />
        </el-select>
        <el-input v-model="query.category" placeholder="category" clearable />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openCreate">新建</el-button>
      </div>
    </el-card>
    <el-card class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="success" @click="doPublish(row.id)">发布</el-button>
            <el-button size="small" type="warning" @click="doOffline(row.id)">下线</el-button>
            <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑 Agent' : '新建 Agent'" width="760px">
      <el-form label-width="130px" class="dialog-form">
        <div class="form-grid">
          <el-form-item label="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="status"><el-input v-model="form.status" /></el-form-item>
          <el-form-item label="avatar"><el-input v-model="form.avatar" /></el-form-item>
          <el-form-item label="category"><el-input v-model="form.category" /></el-form-item>
          <el-form-item label="tags"><el-input v-model="form.tags" /></el-form-item>
          <el-form-item label="adminId"><el-input-number v-model="form.adminId" :min="0" /></el-form-item>
          <el-form-item label="description" class="form-full"><el-input v-model="form.description" type="textarea" /></el-form-item>
          <el-form-item label="prompt" class="form-full"><el-input v-model="form.prompt" type="textarea" :rows="4" /></el-form-item>
          <el-form-item label="presetQuestions" class="form-full"><el-input v-model="form.presetQuestions" type="textarea" :rows="3" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createAgent, deleteAgent, listAgents, offlineAgent, publishAgent, updateAgent } from '../api/agent';

const rows = ref([]);
const query = reactive({ keyword: '', status: '', category: '' });
const dialogVisible = ref(false);
const editingId = ref(null);
const emptyForm = () => ({ name: '', description: '', avatar: '', category: '', tags: '', prompt: '', presetQuestions: '[]', adminId: 1, status: 'draft' });
const form = reactive(emptyForm());

const loadData = async () => { rows.value = await listAgents(query); };
const resetForm = (data = emptyForm()) => Object.assign(form, emptyForm(), data);
const openCreate = () => { editingId.value = null; resetForm(); dialogVisible.value = true; };
const openEdit = (row) => { editingId.value = row.id; resetForm(row); dialogVisible.value = true; };
const save = async () => {
  if (editingId.value) await updateAgent(editingId.value, form);
  else await createAgent(form);
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  loadData();
};
const doDelete = async (id) => {
  await ElMessageBox.confirm('确认删除该 Agent？', '提示');
  await deleteAgent(id);
  ElMessage.success('删除成功');
  loadData();
};
const doPublish = async (id) => { await publishAgent(id); ElMessage.success('发布成功'); loadData(); };
const doOffline = async (id) => { await offlineAgent(id); ElMessage.success('下线成功'); loadData(); };
onMounted(loadData);
</script>
