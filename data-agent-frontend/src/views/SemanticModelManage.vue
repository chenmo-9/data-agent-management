<template>
  <section class="page">
    <h1 class="page-title">语义模型</h1>
    <el-card class="page-card">
      <div class="toolbar">
        <el-select v-model="selectedDatasourceId" placeholder="选择数据源" filterable clearable @change="handleDatasourceChange">
          <el-option v-for="item in datasources" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-input v-model="tableQuery.keyword" placeholder="表 keyword" clearable />
        <el-button type="primary" @click="loadTables">查询表</el-button>
        <el-button type="success" @click="openTable()">新建表语义</el-button>
      </div>
    </el-card>

    <el-card class="page-card">
      <template #header>表语义</template>
      <el-table :data="tables" border highlight-current-row @current-change="selectTable">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tableName" label="tableName" />
        <el-table-column prop="businessName" label="businessName" />
        <el-table-column prop="synonyms" label="synonyms" min-width="220" show-overflow-tooltip />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click.stop="openTable(row)">编辑</el-button>
            <el-button size="small" @click.stop="viewModel(row)">完整模型</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click.stop="toggleTable(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click.stop="removeTable(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="page-card">
      <template #header>字段语义</template>
      <div class="toolbar">
        <span>当前表：{{ currentTable?.tableName || '请选择表' }}</span>
        <el-button type="success" :disabled="!currentTable" @click="openField()">新建字段</el-button>
      </div>
      <el-table :data="fields" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fieldName" label="fieldName" />
        <el-table-column prop="businessName" label="businessName" />
        <el-table-column prop="dataType" label="dataType" width="120" />
        <el-table-column prop="synonyms" label="synonyms" min-width="220" show-overflow-tooltip />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openField(row)">编辑</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggleField(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="removeField(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="page-card">
      <template #header>表关系</template>
      <div class="toolbar">
        <span>当前数据源关系：{{ datasourceName }}</span>
        <el-button type="success" :disabled="!selectedDatasourceId" @click="openRelation()">新建关系</el-button>
      </div>
      <el-table :data="relations" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="关系" min-width="320">
          <template #default="{ row }">{{ row.sourceTableName }}.{{ row.sourceFieldName }} {{ row.joinType }} {{ row.targetTableName }}.{{ row.targetFieldName }}</template>
        </el-table-column>
        <el-table-column prop="relationType" label="relationType" width="120" />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column prop="description" label="description" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openRelation(row)">编辑</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggleRelation(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="removeRelation(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="tableDialog" :title="tableForm.id ? '编辑表语义' : '新建表语义'" width="680px">
      <el-form label-width="130px" class="dialog-form">
        <el-form-item label="datasourceId"><el-input-number v-model="tableForm.datasourceId" :min="1" /></el-form-item>
        <el-form-item label="tableName"><el-input v-model="tableForm.tableName" /></el-form-item>
        <el-form-item label="businessName"><el-input v-model="tableForm.businessName" /></el-form-item>
        <el-form-item label="description"><el-input v-model="tableForm.description" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="synonyms"><el-input v-model="tableForm.synonyms" /></el-form-item>
        <el-form-item label="enabled"><el-switch v-model="tableForm.enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="tableDialog=false">取消</el-button><el-button type="primary" @click="saveTable">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="fieldDialog" :title="fieldForm.id ? '编辑字段语义' : '新建字段语义'" width="720px">
      <el-form label-width="130px" class="dialog-form">
        <div class="form-grid">
          <el-form-item label="tableId"><el-input-number v-model="fieldForm.tableId" :min="1" /></el-form-item>
          <el-form-item label="fieldName"><el-input v-model="fieldForm.fieldName" /></el-form-item>
          <el-form-item label="businessName"><el-input v-model="fieldForm.businessName" /></el-form-item>
          <el-form-item label="dataType"><el-input v-model="fieldForm.dataType" /></el-form-item>
          <el-form-item label="synonyms"><el-input v-model="fieldForm.synonyms" /></el-form-item>
          <el-form-item label="exampleValue"><el-input v-model="fieldForm.exampleValue" /></el-form-item>
          <el-form-item label="primaryKey"><el-switch v-model="fieldForm.primaryKey" /></el-form-item>
          <el-form-item label="nullable"><el-switch v-model="fieldForm.nullable" /></el-form-item>
          <el-form-item label="enabled"><el-switch v-model="fieldForm.enabled" /></el-form-item>
          <el-form-item label="description" class="form-full"><el-input v-model="fieldForm.description" type="textarea" rows="3" /></el-form-item>
        </div>
      </el-form>
      <template #footer><el-button @click="fieldDialog=false">取消</el-button><el-button type="primary" @click="saveField">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="relationDialog" :title="relationForm.id ? '编辑表关系' : '新建表关系'" width="780px">
      <el-form label-width="130px" class="dialog-form">
        <div class="form-grid">
          <el-form-item label="datasourceId"><el-input-number v-model="relationForm.datasourceId" :min="1" /></el-form-item>
          <el-form-item label="sourceTableId">
            <el-select v-model="relationForm.sourceTableId" filterable @change="loadSourceFields">
              <el-option v-for="item in tables" :key="item.id" :label="item.tableName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="sourceFieldId">
            <el-select v-model="relationForm.sourceFieldId" filterable>
              <el-option v-for="item in sourceFieldOptions" :key="item.id" :label="`${item.fieldName} / ${item.businessName}`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="targetTableId">
            <el-select v-model="relationForm.targetTableId" filterable @change="loadTargetFields">
              <el-option v-for="item in tables" :key="item.id" :label="item.tableName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="targetFieldId">
            <el-select v-model="relationForm.targetFieldId" filterable>
              <el-option v-for="item in targetFieldOptions" :key="item.id" :label="`${item.fieldName} / ${item.businessName}`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="relationType">
            <el-select v-model="relationForm.relationType">
              <el-option label="logical" value="logical" />
              <el-option label="foreign_key" value="foreign_key" />
              <el-option label="business" value="business" />
            </el-select>
          </el-form-item>
          <el-form-item label="joinType">
            <el-select v-model="relationForm.joinType">
              <el-option label="INNER JOIN" value="INNER JOIN" />
              <el-option label="LEFT JOIN" value="LEFT JOIN" />
            </el-select>
          </el-form-item>
          <el-form-item label="enabled"><el-switch v-model="relationForm.enabled" /></el-form-item>
          <el-form-item label="description" class="form-full"><el-input v-model="relationForm.description" type="textarea" rows="3" /></el-form-item>
        </div>
      </el-form>
      <template #footer><el-button @click="relationDialog=false">取消</el-button><el-button type="primary" @click="saveRelation">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="modelDialog" title="完整语义模型" width="760px"><pre>{{ modelJson }}</pre></el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listDatasources } from '../api/datasource';
import {
  createSemanticField,
  createSemanticRelation,
  createSemanticTable,
  deleteSemanticField,
  deleteSemanticRelation,
  deleteSemanticTable,
  disableSemanticField,
  disableSemanticRelation,
  disableSemanticTable,
  enableSemanticField,
  enableSemanticRelation,
  enableSemanticTable,
  getSemanticModelByTableId,
  listRelationsByDatasourceId,
  listSemanticFields,
  listSemanticTables,
  updateSemanticField,
  updateSemanticRelation,
  updateSemanticTable,
} from '../api/semanticModel';

const datasources = ref([]);
const selectedDatasourceId = ref(null);
const tableQuery = reactive({ keyword: '', enabled: '' });
const tables = ref([]);
const fields = ref([]);
const relations = ref([]);
const currentTable = ref(null);
const tableDialog = ref(false);
const fieldDialog = ref(false);
const relationDialog = ref(false);
const modelDialog = ref(false);
const modelJson = ref('');
const sourceFieldOptions = ref([]);
const targetFieldOptions = ref([]);
const tableForm = reactive({});
const fieldForm = reactive({});
const relationForm = reactive({});

const datasourceName = computed(() => datasources.value.find((item) => item.id === selectedDatasourceId.value)?.name || '请选择数据源');

const loadOptions = async () => {
  datasources.value = await listDatasources({});
};

const loadTables = async () => {
  if (!selectedDatasourceId.value) {
    tables.value = [];
    fields.value = [];
    relations.value = [];
    currentTable.value = null;
    return;
  }
  tables.value = await listSemanticTables({ datasourceId: selectedDatasourceId.value, keyword: tableQuery.keyword });
  relations.value = await listRelationsByDatasourceId(selectedDatasourceId.value);
  fields.value = [];
  currentTable.value = null;
};

const handleDatasourceChange = async () => {
  await loadTables();
};

const selectTable = async (row) => {
  currentTable.value = row;
  fields.value = row ? await listSemanticFields({ tableId: row.id }) : [];
};

const openTable = (row) => {
  Object.assign(tableForm, { id: null, datasourceId: selectedDatasourceId.value, tableName: '', businessName: '', description: '', synonyms: '', enabled: true }, row || {});
  tableDialog.value = true;
};

const saveTable = async () => {
  tableForm.id ? await updateSemanticTable(tableForm.id, tableForm) : await createSemanticTable(tableForm);
  ElMessage.success('保存成功');
  tableDialog.value = false;
  await loadTables();
};

const toggleTable = async (row) => {
  row.enabled ? await disableSemanticTable(row.id) : await enableSemanticTable(row.id);
  await loadTables();
};

const removeTable = async (id) => {
  await ElMessageBox.confirm('确认删除表语义？', '提示');
  await deleteSemanticTable(id);
  await loadTables();
};

const openField = (row) => {
  Object.assign(fieldForm, { id: null, tableId: currentTable.value?.id, fieldName: '', businessName: '', dataType: '', description: '', synonyms: '', exampleValue: '', primaryKey: false, nullable: true, enabled: true }, row || {});
  fieldDialog.value = true;
};

const saveField = async () => {
  fieldForm.id ? await updateSemanticField(fieldForm.id, fieldForm) : await createSemanticField(fieldForm);
  ElMessage.success('保存成功');
  fieldDialog.value = false;
  await selectTable(currentTable.value);
};

const toggleField = async (row) => {
  row.enabled ? await disableSemanticField(row.id) : await enableSemanticField(row.id);
  await selectTable(currentTable.value);
};

const removeField = async (id) => {
  await ElMessageBox.confirm('确认删除字段语义？', '提示');
  await deleteSemanticField(id);
  await selectTable(currentTable.value);
};

const viewModel = async (row) => {
  modelJson.value = JSON.stringify(await getSemanticModelByTableId(row.id), null, 2);
  modelDialog.value = true;
};

const loadSourceFields = async (tableId) => {
  sourceFieldOptions.value = tableId ? await listSemanticFields({ tableId }) : [];
  if (!sourceFieldOptions.value.find((item) => item.id === relationForm.sourceFieldId)) {
    relationForm.sourceFieldId = null;
  }
};

const loadTargetFields = async (tableId) => {
  targetFieldOptions.value = tableId ? await listSemanticFields({ tableId }) : [];
  if (!targetFieldOptions.value.find((item) => item.id === relationForm.targetFieldId)) {
    relationForm.targetFieldId = null;
  }
};

const openRelation = async (row) => {
  Object.assign(relationForm, {
    id: null,
    datasourceId: selectedDatasourceId.value,
    sourceTableId: null,
    sourceFieldId: null,
    targetTableId: null,
    targetFieldId: null,
    relationType: 'logical',
    joinType: 'INNER JOIN',
    description: '',
    enabled: true,
  }, row || {});
  relationDialog.value = true;
  await loadSourceFields(relationForm.sourceTableId);
  await loadTargetFields(relationForm.targetTableId);
};

const saveRelation = async () => {
  relationForm.id ? await updateSemanticRelation(relationForm.id, relationForm) : await createSemanticRelation(relationForm);
  ElMessage.success('保存成功');
  relationDialog.value = false;
  relations.value = await listRelationsByDatasourceId(selectedDatasourceId.value);
};

const toggleRelation = async (row) => {
  row.enabled ? await disableSemanticRelation(row.id) : await enableSemanticRelation(row.id);
  relations.value = await listRelationsByDatasourceId(selectedDatasourceId.value);
};

const removeRelation = async (id) => {
  await ElMessageBox.confirm('确认删除表关系？', '提示');
  await deleteSemanticRelation(id);
  relations.value = await listRelationsByDatasourceId(selectedDatasourceId.value);
};

onMounted(async () => {
  await loadOptions();
  await loadTables();
});
</script>
