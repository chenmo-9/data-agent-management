<template>
  <section class="page">
    <h1 class="page-title">数据源管理</h1>
    <el-card class="page-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="keyword" clearable />
        <el-select v-model="query.dbType" placeholder="dbType" clearable>
          <el-option label="h2" value="h2" /><el-option label="mysql" value="mysql" /><el-option label="postgresql" value="postgresql" />
        </el-select>
        <el-select v-model="query.enabled" placeholder="enabled" clearable><el-option label="true" :value="true" /><el-option label="false" :value="false" /></el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openCreate">新建</el-button>
      </div>
    </el-card>
    <el-card class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="dbType" label="类型" width="100" />
        <el-table-column prop="url" label="URL" min-width="260" show-overflow-tooltip />
        <el-table-column prop="hasPassword" label="hasPassword" width="120" />
        <el-table-column prop="maskedPassword" label="password" width="140" />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="doTest(row.id)">测试</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggle(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑数据源' : '新建数据源'" width="760px">
      <el-form label-width="130px" class="dialog-form">
        <div class="form-grid">
          <el-form-item label="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="dbType"><el-select v-model="form.dbType"><el-option label="h2" value="h2" /><el-option label="mysql" value="mysql" /><el-option label="postgresql" value="postgresql" /></el-select></el-form-item>
          <el-form-item label="url" class="form-full"><el-input v-model="form.url" /></el-form-item>
          <el-form-item label="username"><el-input v-model="form.username" /></el-form-item>
          <el-form-item label="password">
            <el-input v-model="form.password" type="password" show-password :placeholder="editingId ? '留空表示不修改' : '请输入密码'" />
          </el-form-item>
          <el-form-item label="databaseName"><el-input v-model="form.databaseName" /></el-form-item>
          <el-form-item label="host"><el-input v-model="form.host" /></el-form-item>
          <el-form-item label="port"><el-input-number v-model="form.port" :min="0" /></el-form-item>
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
import { createDatasource, deleteDatasource, disableDatasource, enableDatasource, listDatasources, testSavedDatasource, updateDatasource } from '../api/datasource';

const rows = ref([]);
const query = reactive({ keyword: '', dbType: '', enabled: '' });
const dialogVisible = ref(false);
const editingId = ref(null);
const emptyForm = () => ({ name: '', dbType: 'mysql', url: 'jdbc:mysql://localhost:3306/test', username: 'root', password: '', databaseName: 'test', host: 'localhost', port: 3306, enabled: true, description: '' });
const form = reactive(emptyForm());
const loadData = async () => { rows.value = await listDatasources(query); };
const resetForm = (data = emptyForm()) => Object.assign(form, emptyForm(), data, { password: '' });
const openCreate = () => { editingId.value = null; resetForm(); dialogVisible.value = true; };
const openEdit = (row) => { editingId.value = row.id; resetForm(row); dialogVisible.value = true; };
const buildPayload = () => {
  const payload = { ...form };
  delete payload.maskedPassword;
  delete payload.hasPassword;
  if (editingId.value && !payload.password) {
    delete payload.password;
  }
  return payload;
};
const save = async () => {
  const payload = buildPayload();
  editingId.value ? await updateDatasource(editingId.value, payload) : await createDatasource(payload);
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  loadData();
};
const toggle = async (row) => { row.enabled ? await disableDatasource(row.id) : await enableDatasource(row.id); ElMessage.success('操作成功'); loadData(); };
const doDelete = async (id) => { await ElMessageBox.confirm('确认删除该数据源？', '提示'); await deleteDatasource(id); ElMessage.success('删除成功'); loadData(); };
const doTest = async (id) => { const result = await testSavedDatasource(id); ElMessage[result.success ? 'success' : 'error'](result.message); };
onMounted(loadData);
</script>
