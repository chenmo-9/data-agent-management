<template>
  <section class="page">
    <h1 class="page-title">知识管理</h1>
    <el-card class="page-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="keyword" clearable />
        <el-select v-model="query.knowledgeType" placeholder="knowledgeType" clearable>
          <el-option label="business_rule" value="business_rule" /><el-option label="faq" value="faq" /><el-option label="metric" value="metric" /><el-option label="document" value="document" />
        </el-select>
        <el-button type="primary" @click="loadKnowledge">查询</el-button>
        <el-button type="success" @click="openKnowledge()">新建 text 知识</el-button>
      </div>
    </el-card>
    <el-card class="page-card">
      <el-table :data="knowledgeRows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="knowledgeType" label="类型" width="140" />
        <el-table-column prop="sourceType" label="来源" width="100" />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column label="操作" width="520" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openKnowledge(row)">编辑</el-button>
            <el-button size="small" @click="showChunks(row.id)">chunks</el-button>
            <el-button size="small" @click="rebuild(row.id)">rebuild</el-button>
            <el-button size="small" type="primary" @click="openEmbeddingDialog('knowledge', row.id)">重建向量</el-button>
            <el-button size="small" @click="clearEmbedding(row.id)">清向量</el-button>
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggleKnowledge(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="removeKnowledge(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card class="page-card">
      <h3>Agent 知识绑定</h3>
      <div class="toolbar">
        <el-select v-model="bindForm.agentId" placeholder="Agent" filterable clearable><el-option v-for="a in agents" :key="a.id" :label="a.name" :value="a.id" /></el-select>
        <el-select v-model="bindForm.knowledgeId" placeholder="Knowledge" filterable clearable><el-option v-for="k in knowledgeRows" :key="k.id" :label="k.title" :value="k.id" /></el-select>
        <el-button type="success" @click="bindKnowledge">绑定</el-button>
        <el-button @click="loadAgentKnowledge">查询绑定</el-button>
        <el-button type="primary" @click="openEmbeddingDialog('agent', bindForm.agentId)" :disabled="!bindForm.agentId">重建当前 Agent 向量</el-button>
      </div>
      <el-table :data="agentKnowledgeRows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="agentName" label="Agent" />
        <el-table-column prop="knowledgeTitle" label="Knowledge" />
        <el-table-column prop="enabled" label="启用" width="90" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggleBind(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="removeBind(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card class="page-card">
      <h3>上传 txt 文件</h3>
      <div class="toolbar">
        <el-input v-model="uploadTitle" placeholder="title" />
        <el-input v-model="uploadType" placeholder="knowledgeType" />
        <el-upload :auto-upload="false" :show-file-list="true" :limit="1" :on-change="onFileChange">
          <el-button>选择文件</el-button>
        </el-upload>
        <el-button type="primary" @click="uploadFile">上传</el-button>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="knowledgeForm.id ? '编辑知识' : '新建知识'" width="760px">
      <el-form label-width="130px" class="dialog-form">
        <el-form-item label="title"><el-input v-model="knowledgeForm.title" /></el-form-item>
        <el-form-item label="knowledgeType"><el-input v-model="knowledgeForm.knowledgeType" /></el-form-item>
        <el-form-item label="sourceType"><el-input v-model="knowledgeForm.sourceType" /></el-form-item>
        <el-form-item label="enabled"><el-switch v-model="knowledgeForm.enabled" /></el-form-item>
        <el-form-item label="content"><el-input v-model="knowledgeForm.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="saveKnowledge">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="chunkDialog" title="知识切片" width="760px">
      <el-table :data="chunks" border>
        <el-table-column prop="chunkIndex" label="#" width="80" />
        <el-table-column prop="content" label="content" />
        <el-table-column prop="embeddingStatus" label="embeddingStatus" width="140" />
        <el-table-column prop="embeddingDimension" label="dimension" width="110" />
        <el-table-column label="hasEmbedding" width="120">
          <template #default="{ row }">{{ row.hasEmbedding ? '是' : '否' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <el-dialog v-model="embeddingDialogVisible" title="重建知识向量" width="520px">
      <el-form label-width="130px" class="dialog-form">
        <el-form-item label="目标">
          <el-input :model-value="embeddingTargetLabel" disabled />
        </el-form-item>
        <el-form-item label="Embedding 模型">
          <el-select v-model="embeddingForm.modelConfigId" placeholder="选择 embedding 模型" filterable>
            <el-option v-for="model in embeddingModels" :key="model.id" :label="`${model.name} / ${model.provider} / ${model.modelName}`" :value="model.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="embeddingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEmbedding">开始重建</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listAgents } from '../api/agent';
import { listModelConfigs } from '../api/modelConfig';
import { bindAgentKnowledge, clearKnowledgeEmbedding, createBusinessKnowledge, deleteAgentKnowledge, deleteBusinessKnowledge, disableAgentKnowledge, disableBusinessKnowledge, enableAgentKnowledge, enableBusinessKnowledge, listAgentKnowledge, listBusinessKnowledge, listKnowledgeChunks, rebuildAgentKnowledgeEmbedding, rebuildKnowledgeChunks, rebuildKnowledgeEmbedding, updateBusinessKnowledge, uploadBusinessKnowledge } from '../api/knowledge';

const query = reactive({ keyword: '', knowledgeType: '', sourceType: '', enabled: '' });
const knowledgeRows = ref([]);
const agents = ref([]);
const agentKnowledgeRows = ref([]);
const chunks = ref([]);
const dialogVisible = ref(false);
const chunkDialog = ref(false);
const embeddingDialogVisible = ref(false);
const selectedFile = ref(null);
const uploadTitle = ref('');
const uploadType = ref('document');
const embeddingMode = ref('knowledge');
const embeddingTargetId = ref(null);
const embeddingModels = ref([]);
const bindForm = reactive({ agentId: null, knowledgeId: null });
const embeddingForm = reactive({ modelConfigId: null });
const knowledgeForm = reactive({});
const loadKnowledge = async () => { knowledgeRows.value = await listBusinessKnowledge(query); };
const loadOptions = async () => {
  const [agentList, modelList] = await Promise.all([listAgents({}), listModelConfigs({ modelType: 'embedding', enabled: true })]);
  agents.value = agentList || [];
  embeddingModels.value = (modelList || []).filter((item) => item.modelType === 'embedding' && item.enabled !== false);
};
const openKnowledge = (row) => { Object.assign(knowledgeForm, { id: null, title: '', knowledgeType: 'business_rule', sourceType: 'text', content: '', enabled: true }, row || {}); dialogVisible.value = true; };
const saveKnowledge = async () => { knowledgeForm.id ? await updateBusinessKnowledge(knowledgeForm.id, knowledgeForm) : await createBusinessKnowledge(knowledgeForm); ElMessage.success('保存成功'); dialogVisible.value = false; loadKnowledge(); };
const toggleKnowledge = async (row) => { row.enabled ? await disableBusinessKnowledge(row.id) : await enableBusinessKnowledge(row.id); loadKnowledge(); };
const removeKnowledge = async (id) => { await ElMessageBox.confirm('确认删除知识？', '提示'); await deleteBusinessKnowledge(id); loadKnowledge(); };
const showChunks = async (id) => { chunks.value = await listKnowledgeChunks(id); chunkDialog.value = true; };
const rebuild = async (id) => { chunks.value = await rebuildKnowledgeChunks(id); ElMessage.success('重建成功'); chunkDialog.value = true; };
const clearEmbedding = async (id) => { await ElMessageBox.confirm('确认清除该知识的向量数据？', '提示'); await clearKnowledgeEmbedding(id); ElMessage.success('清除成功'); };
const bindKnowledge = async () => { await bindAgentKnowledge(bindForm); ElMessage.success('绑定成功'); loadAgentKnowledge(); };
const loadAgentKnowledge = async () => { agentKnowledgeRows.value = await listAgentKnowledge({ agentId: bindForm.agentId }); };
const toggleBind = async (row) => { row.enabled ? await disableAgentKnowledge(row.id) : await enableAgentKnowledge(row.id); loadAgentKnowledge(); };
const removeBind = async (id) => { await deleteAgentKnowledge(id); ElMessage.success('删除成功'); loadAgentKnowledge(); };
const onFileChange = (file) => { selectedFile.value = file.raw; };
const uploadFile = async () => { const data = new FormData(); data.append('file', selectedFile.value); data.append('title', uploadTitle.value); data.append('knowledgeType', uploadType.value); await uploadBusinessKnowledge(data); ElMessage.success('上传成功'); loadKnowledge(); };
const openEmbeddingDialog = (mode, id) => {
  if (!id) {
    ElMessage.warning('请先选择目标');
    return;
  }
  embeddingMode.value = mode;
  embeddingTargetId.value = id;
  embeddingForm.modelConfigId = embeddingModels.value[0]?.id || null;
  embeddingDialogVisible.value = true;
};
const submitEmbedding = async () => {
  if (!embeddingForm.modelConfigId) {
    ElMessage.warning('请选择 embedding 模型');
    return;
  }
  const response = embeddingMode.value === 'agent'
    ? await rebuildAgentKnowledgeEmbedding(embeddingTargetId.value, embeddingForm.modelConfigId)
    : await rebuildKnowledgeEmbedding(embeddingTargetId.value, embeddingForm.modelConfigId);
  ElMessage.success(`完成：total=${response.totalCount} success=${response.successCount} failed=${response.failedCount}`);
  embeddingDialogVisible.value = false;
};
const embeddingTargetLabel = computed(() => (
  embeddingMode.value === 'agent'
    ? `Agent #${embeddingTargetId.value || '-'}`
    : `Knowledge #${embeddingTargetId.value || '-'}`
));
onMounted(() => { loadOptions(); loadKnowledge(); loadAgentKnowledge(); });
</script>
