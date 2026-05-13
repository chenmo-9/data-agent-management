<template>
  <section class="page">
    <h1 class="page-title">运行历史</h1>

    <el-card class="page-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索问题 / SQL / 回答" clearable />
        <el-select v-model="query.mode" placeholder="mode" clearable>
          <el-option label="nl2sql" value="nl2sql" />
          <el-option label="chat" value="chat" />
        </el-select>
        <el-select v-model="query.status" placeholder="status" clearable>
          <el-option label="running" value="running" />
          <el-option label="success" value="success" />
          <el-option label="failed" value="failed" />
        </el-select>
        <el-select v-model="query.success" placeholder="success" clearable>
          <el-option label="true" :value="true" />
          <el-option label="false" :value="false" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </el-card>

    <el-card class="page-card">
      <el-table :data="rows" border>
        <el-table-column prop="runId" label="Run ID" min-width="220" show-overflow-tooltip />
        <el-table-column prop="question" label="问题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="agentName" label="Agent" width="150" />
        <el-table-column prop="mode" label="mode" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时 ms" width="100" />
        <el-table-column prop="rowCount" label="行数" width="80" />
        <el-table-column prop="chartType" label="图表" width="110" />
        <el-table-column prop="confirmStatus" label="确认" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row.runId)">详情</el-button>
            <el-button size="small" type="danger" @click="remove(row.runId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="运行详情" size="72%">
      <el-empty v-if="!detail" description="暂无详情" />
      <template v-else>
        <el-descriptions v-if="isHistoryKnowledgeAnswerMode" :column="2" border>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detail.run?.status)">{{ detail.run?.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="意图">{{ historyIntent || '-' }}</el-descriptions-item>
          <el-descriptions-item label="回答" :span="2">{{ historyAnswer }}</el-descriptions-item>
        </el-descriptions>
        <el-collapse v-if="isHistoryKnowledgeAnswerMode" class="debug-collapse mt-12">
          <el-collapse-item title="运行调试信息" name="history-run-debug">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="Run ID">{{ detail.run?.runId }}</el-descriptions-item>
              <el-descriptions-item label="Session ID">{{ detail.run?.sessionId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="耗时">{{ detail.run?.durationMs || '-' }} ms</el-descriptions-item>
              <el-descriptions-item label="问题">{{ detail.run?.question }}</el-descriptions-item>
              <el-descriptions-item label="确认状态">{{ detail.confirmStatus || '-' }}</el-descriptions-item>
              <el-descriptions-item label="确认人">{{ detail.confirmedBy || '-' }}</el-descriptions-item>
              <el-descriptions-item label="取消原因" :span="2">{{ detail.cancelReason || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
        <el-descriptions v-else :column="2" border>
          <el-descriptions-item label="Run ID">{{ detail.run?.runId }}</el-descriptions-item>
          <el-descriptions-item label="Session ID">{{ detail.run?.sessionId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detail.run?.status)">{{ detail.run?.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">{{ detail.run?.durationMs || '-' }} ms</el-descriptions-item>
          <el-descriptions-item label="问题" :span="2">{{ detail.run?.question }}</el-descriptions-item>
          <el-descriptions-item label="回答" :span="2">{{ historyAnswer }}</el-descriptions-item>
          <el-descriptions-item label="确认状态">{{ detail.confirmStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="确认人">{{ detail.confirmedBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ detail.cancelReason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-card v-if="isHistoryKnowledgeAnswerMode" class="page-card result-card business-answer-card">
          <template #header>命中知识摘要</template>
          <el-alert title="本问题为业务规则问答，未执行 SQL。" type="info" :closable="false" show-icon class="mb-12" />
          <el-table v-if="historyKnowledgeChunks.length" :data="historyKnowledgeChunks" border>
            <el-table-column prop="title" label="知识标题" width="220" />
            <el-table-column label="内容摘要" min-width="320">
              <template #default="{ row }">{{ summarizeText(row.content) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无命中知识" />
        </el-card>

        <el-card v-if="detail.confirmStatus === 'pending'" class="page-card human-confirm-card">
          <template #header>待确认 SQL</template>
          <el-input v-model="confirmSql" class="confirm-sql-editor" type="textarea" :rows="8" />
          <div class="confirm-actions">
            <el-button type="primary" :loading="confirming" @click="confirmFromHistory">确认执行</el-button>
            <el-button type="danger" :loading="confirming" @click="cancelFromHistory">取消执行</el-button>
          </div>
        </el-card>

        <el-card v-if="isHistoryKnowledgeAnswerMode" class="page-card result-card">
          <template #header>调试详情</template>
          <el-collapse class="debug-collapse">
            <el-collapse-item title="节点事件" name="history-events">
              <el-table :data="detail.events || []" border>
                <el-table-column prop="nodeName" label="节点" width="150" />
                <el-table-column prop="eventType" label="事件" width="120" />
                <el-table-column prop="status" label="状态" width="100" />
                <el-table-column prop="message" label="消息" min-width="220" show-overflow-tooltip />
                <el-table-column label="data" min-width="260">
                  <template #default="{ row }"><pre class="json-pre">{{ prettyJson(row.dataJson) }}</pre></template>
                </el-table-column>
              </el-table>
            </el-collapse-item>
            <el-collapse-item title="Knowledge Recall JSON" name="history-knowledge-json">
              <pre class="json-pre">{{ prettyJson(historyKnowledgeRecallResult) }}</pre>
            </el-collapse-item>
            <el-collapse-item title="Schema / Relation Context" name="history-context">
              <h3>schemaContext</h3>
              <pre class="json-pre">{{ findEventData('schema_recall')?.schemaContext || '暂无' }}</pre>
              <h3>relationContext</h3>
              <pre class="json-pre">{{ findEventData('relation_recall')?.relationContext || '暂无' }}</pre>
            </el-collapse-item>
            <el-collapse-item title="chartSpec 原始 JSON" name="history-chart-json">
              <pre class="json-pre">{{ prettyJson(historyChartSpec) }}</pre>
            </el-collapse-item>
          </el-collapse>
        </el-card>

        <template v-if="!isHistoryKnowledgeAnswerMode">
        <el-divider content-position="left">SQL 与安全信息</el-divider>
        <el-collapse>
          <el-collapse-item title="Generated SQL" name="generated"><pre class="sql-block">{{ detail.generatedSql || '暂无' }}</pre></el-collapse-item>
          <el-collapse-item title="Validated SQL" name="validated"><pre class="sql-block">{{ detail.validatedSql || '暂无' }}</pre></el-collapse-item>
          <el-collapse-item title="Sanitized SQL" name="sanitized"><pre class="sql-block">{{ detail.sanitizedSql || '暂无' }}</pre></el-collapse-item>
          <el-collapse-item title="安全信息" name="security">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="自动 LIMIT">{{ formatBool(detail.sqlLimited) }}</el-descriptions-item>
              <el-descriptions-item label="LIMIT">{{ detail.sqlLimit || '-' }}</el-descriptions-item>
              <el-descriptions-item label="结果截断">{{ formatBool(detail.sqlResultTruncated) }}</el-descriptions-item>
              <el-descriptions-item label="查询超时">{{ detail.sqlQueryTimeoutSeconds ? `${detail.sqlQueryTimeoutSeconds}s` : '-' }}</el-descriptions-item>
              <el-descriptions-item label="安全说明">{{ detail.sqlSecurityMessage || '-' }}</el-descriptions-item>
              <el-descriptions-item label="校验错误">{{ detail.sqlValidationError || '-' }}</el-descriptions-item>
              <el-descriptions-item label="执行错误">{{ detail.sqlExecutionError || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>

        <el-divider content-position="left">召回与结果摘要</el-divider>
        <el-descriptions :column="4" border>
          <el-descriptions-item label="表">{{ detail.recalledTableCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="字段">{{ detail.recalledFieldCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="关系">{{ detail.recalledRelationCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="知识">{{ detail.recalledKnowledgeCount ?? '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">Relation Recall 命中关系</el-divider>
        <el-table :data="historyRelations" border>
          <el-table-column label="source" min-width="180">
            <template #default="{ row }">{{ row.sourceTableName }}.{{ row.sourceFieldName }}</template>
          </el-table-column>
          <el-table-column prop="joinType" label="joinType" width="120" />
          <el-table-column label="target" min-width="180">
            <template #default="{ row }">{{ row.targetTableName }}.{{ row.targetFieldName }}</template>
          </el-table-column>
          <el-table-column prop="relationType" label="relationType" width="120" />
          <el-table-column prop="score" label="score" width="100" />
          <el-table-column label="matchReasons" min-width="220">
            <template #default="{ row }">{{ (row.matchReasons || []).join(' ; ') || '-' }}</template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">SQL 结果预览</el-divider>
        <el-empty v-if="!historyResultRows.length" description="暂无结果预览" />
        <el-table v-else :data="historyResultRows" border>
          <el-table-column v-for="column in historyResultColumns" :key="column" :prop="column" :label="column" />
        </el-table>
        <pre class="json-pre mt-12">{{ prettyJson(detail.resultPreviewJson) }}</pre>

        <el-divider content-position="left">Python 分析</el-divider>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="分析引擎">{{ detail.pythonEngine || 'java_safe' }}</el-descriptions-item>
          <el-descriptions-item label="是否执行 Python">{{ formatBool(detail.pythonExecuted) }}</el-descriptions-item>
          <el-descriptions-item label="Python 成功">{{ formatBool(detail.pythonSuccess) }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ detail.pythonDurationMs ? `${detail.pythonDurationMs} ms` : '-' }}</el-descriptions-item>
          <el-descriptions-item label="fallback">{{ formatBool(detail.pythonFallbackUsed) }}</el-descriptions-item>
          <el-descriptions-item label="错误">{{ detail.pythonErrorMessage || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">核心结论</el-divider>
        <p class="core-summary">{{ historyCoreSummary }}</p>

        <el-divider content-position="left">报告</el-divider>
        <h3>{{ detail.reportTitle || '暂无标题' }}</h3>
        <ChartPreview :chart-spec="historyChartSpec" />
        <pre class="markdown-preview">{{ detail.reportMarkdown || '暂无报告' }}</pre>

        <el-divider content-position="left">节点事件</el-divider>
        <el-table :data="detail.events || []" border>
          <el-table-column prop="nodeName" label="节点" width="150" />
          <el-table-column prop="eventType" label="事件" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="消息" min-width="220" show-overflow-tooltip />
          <el-table-column prop="eventTime" label="时间" width="170" />
          <el-table-column label="data" min-width="260">
            <template #default="{ row }"><pre class="json-pre">{{ prettyJson(row.dataJson) }}</pre></template>
          </el-table-column>
        </el-table>
        </template>
      </template>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteGraphRun, getGraphRunDetail, listGraphRuns } from '../api/graphHistory';
import { cancelHumanRun, confirmHumanRun } from '../api/graphHuman';
import ChartPreview from '../components/ChartPreview.vue';
import { normalizeChartSpec, parseRows } from '../utils/chartSpec';

const route = useRoute();
const rows = ref([]);
const total = ref(0);
const detail = ref(null);
const detailVisible = ref(false);
const confirmSql = ref('');
const confirming = ref(false);
const query = reactive({ keyword: '', mode: '', status: '', success: '', page: 1, pageSize: 20 });
const historyResultRows = computed(() => parseRows(detail.value?.resultPreviewJson));
const historyResultColumns = computed(() => (historyResultRows.value.length ? Object.keys(historyResultRows.value[0]) : []));
const historyRelations = computed(() => {
  const eventData = findEventData('relation_recall') || findEventData('finish');
  return eventData?.relationRecallResult?.selectedRelations || eventData?.selectedRelations || [];
});
const historyChartSpec = computed(() => {
  const eventSpec = findEventData('finish')?.chartSpec || findEventData('report_generate')?.chartSpec;
  return normalizeChartSpec(eventSpec || { chartType: detail.value?.chartType }, historyResultRows.value,
    detail.value?.resultPreviewJson, null);
});
const historyIntent = computed(() => (
  detail.value?.intent
  || detail.value?.run?.intent
  || findEventData('finish')?.intent
  || findEventData('knowledge_load')?.intent
  || ''
));
const isHistoryKnowledgeAnswerMode = computed(() => {
  if (['business_rule', 'knowledge_answer'].includes(historyIntent.value)) {
    return true;
  }
  return !detail.value?.generatedSql && Number(detail.value?.recalledKnowledgeCount || 0) > 0
    && (!detail.value?.chartType || detail.value?.chartType === 'none');
});
const historyKnowledgeRecallResult = computed(() => (
  findEventData('knowledge_load')?.knowledgeRecallResult
  || findEventData('finish')?.knowledgeRecallResult
  || detail.value?.knowledgeRecallResult
  || null
));
const historyKnowledgeChunks = computed(() => (
  historyKnowledgeRecallResult.value?.selectedChunks || []
).slice(0, 5));
const historyCoreSummary = computed(() => detail.value?.reportSummary || detail.value?.summary || '暂无核心结论');
const historyAnswer = computed(() => detail.value?.reportSummary || detail.value?.summary || detail.value?.answer || '-');

const loadData = async () => {
  const payload = { ...query };
  if (payload.success === '') {
    delete payload.success;
  }
  const result = await listGraphRuns(payload);
  rows.value = result.records || [];
  total.value = result.total || 0;
};

const resetQuery = () => {
  Object.assign(query, { keyword: '', mode: '', status: '', success: '', page: 1, pageSize: 20 });
  loadData();
};

const openDetail = async (runId) => {
  detail.value = await getGraphRunDetail(runId);
  confirmSql.value = detail.value.confirmSql || detail.value.sanitizedSql || '';
  detailVisible.value = true;
};

const remove = async (runId) => {
  await ElMessageBox.confirm('确认删除该运行历史？', '提示');
  await deleteGraphRun(runId);
  ElMessage.success('删除成功');
  loadData();
};

const statusTag = (status) => {
  if (status === true || status === 'success') return 'success';
  if (status === false || status === 'failed' || status === 'error') return 'danger';
  return 'info';
};

const formatBool = (value) => (value === null || value === undefined ? '-' : value ? '是' : '否');

const summarizeText = (text) => {
  if (!text) return '-';
  return text.length > 120 ? `${text.slice(0, 120)}...` : text;
};

const confirmFromHistory = async () => {
  if (!detail.value?.run?.runId) return;
  confirming.value = true;
  try {
    await confirmHumanRun(detail.value.run.runId, { sql: confirmSql.value, confirmedBy: 'local_user' });
    ElMessage.success('确认执行完成');
    await openDetail(detail.value.run.runId);
    await loadData();
  } finally {
    confirming.value = false;
  }
};

const cancelFromHistory = async () => {
  if (!detail.value?.run?.runId) return;
  confirming.value = true;
  try {
    await cancelHumanRun(detail.value.run.runId, { reason: 'Canceled from graph history page', canceledBy: 'local_user' });
    ElMessage.success('已取消执行');
    await openDetail(detail.value.run.runId);
    await loadData();
  } finally {
    confirming.value = false;
  }
};

const prettyJson = (value) => {
  if (!value) return '暂无';
  try {
    return JSON.stringify(typeof value === 'string' ? JSON.parse(value) : value, null, 2);
  } catch (e) {
    return value;
  }
};

const parseEventData = (event) => {
  if (!event?.dataJson) return null;
  try {
    return typeof event.dataJson === 'string' ? JSON.parse(event.dataJson) : event.dataJson;
  } catch (e) {
    return null;
  }
};

const findEventData = (nodeName) => {
  const matched = [...(detail.value?.events || [])].reverse().find((event) => event.nodeName === nodeName);
  return parseEventData(matched);
};


onMounted(async () => {
  await loadData();
  if (route.query.runId) {
    openDetail(route.query.runId);
  }
});
</script>
