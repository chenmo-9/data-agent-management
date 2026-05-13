<template>
  <section class="page run-page">
    <h1 class="page-title">运行中心</h1>

    <el-alert
      title="运行前请确认已创建 Agent、chat 模型配置、数据源绑定、orders 语义模型和业务知识。"
      type="info"
      show-icon
      :closable="false"
    />

    <el-card class="page-card run-form-card">
      <template #header>数据问答</template>
      <el-form label-width="110px">
        <div class="form-grid">
          <el-form-item label="Agent">
            <el-select v-model="form.agentId" placeholder="选择 Agent" filterable>
              <el-option v-for="agent in agents" :key="agent.id" :label="agent.name" :value="agent.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型配置">
            <el-select v-model="form.modelConfigId" placeholder="选择 chat 模型" filterable>
              <el-option
                v-for="model in chatModels"
                :key="model.id"
                :label="`${model.name} / ${model.provider} / ${model.modelName}`"
                :value="model.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Embedding 模型">
            <el-select v-model="form.embeddingModelConfigId" placeholder="可选，优先用于知识召回" filterable clearable>
              <el-option
                v-for="model in embeddingModels"
                :key="model.id"
                :label="`${model.name} / ${model.provider} / ${model.modelName}`"
                :value="model.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="模式">
            <el-segmented v-model="form.mode" :options="modeOptions" />
          </el-form-item>
          <el-form-item label="Session ID">
            <el-input v-model="form.sessionId" placeholder="留空自动生成" />
          </el-form-item>
          <el-form-item label="Knowledge TopK">
            <el-input-number v-model="form.knowledgeTopK" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="人工确认">
            <el-switch v-model="form.confirmBeforeExecute" active-text="执行前确认" />
          </el-form-item>
          <el-form-item label="问题" class="form-full">
            <el-input
              v-model="form.question"
              type="textarea"
              :rows="4"
              placeholder="例如：最近销售额是多少？"
            />
          </el-form-item>
        </div>
        <div class="toolbar">
          <el-button type="primary" :loading="running" @click="runNormal">普通运行</el-button>
          <el-button type="success" :loading="streaming" @click="runStream">SSE 流式运行</el-button>
          <el-button @click="fillExample">填充示例问题</el-button>
          <el-button @click="goHistory">查看运行历史</el-button>
          <el-button :disabled="!result.runId" @click="goCurrentHistory">查看本次历史</el-button>
          <el-button @click="clearResult">清空结果</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card v-if="result.confirmRequired && result.confirmStatus === 'pending'" class="page-card result-card human-confirm-card">
      <template #header>SQL 待人工确认</template>
      <el-alert title="确认或修改 SQL 后仍会重新经过安全校验，取消则不会执行 SQL。" type="warning" :closable="false" show-icon />
      <el-input v-model="confirmSql" class="confirm-sql-editor" type="textarea" :rows="8" />
      <p class="muted-text">{{ result.sqlSecurityMessage || '确认后将继续执行 SQL、分析和报告节点。' }}</p>
      <div class="confirm-actions">
        <el-button type="primary" :loading="confirming" @click="confirmRun">确认执行</el-button>
        <el-button type="danger" :loading="confirming" @click="cancelRun">取消执行</el-button>
        <el-button @click="goCurrentHistory">查看本次历史</el-button>
      </div>
    </el-card>

    <el-card class="page-card result-card">
      <template #header>运行状态</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="当前状态">
          <el-tag :type="statusType">{{ statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="!isKnowledgeAnswerMode" label="Session ID">{{ result.sessionId || form.sessionId || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="!isKnowledgeAnswerMode" label="Run ID">{{ result.runId || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="!isKnowledgeAnswerMode" label="历史保存">{{ formatBool(result.historySaved) }}</el-descriptions-item>
        <el-descriptions-item v-if="!isKnowledgeAnswerMode" label="耗时">{{ result.durationMs ? `${result.durationMs} ms` : '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="!isKnowledgeAnswerMode" label="事件数">{{ result.eventCount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="意图">{{ result.intent || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="!isKnowledgeAnswerMode" label="数据源 ID">{{ result.datasourceId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="回答" :span="2">{{ answerText }}</el-descriptions-item>
        <el-descriptions-item v-if="errorMessage" label="错误" :span="2">{{ errorMessage }}</el-descriptions-item>
      </el-descriptions>
      <el-collapse v-if="isKnowledgeAnswerMode" class="debug-collapse mt-12">
        <el-collapse-item title="运行调试信息" name="run-debug">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Run ID">{{ result.runId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Session ID">{{ result.sessionId || form.sessionId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="历史保存">{{ formatBool(result.historySaved) }}</el-descriptions-item>
            <el-descriptions-item label="耗时">{{ result.durationMs ? `${result.durationMs} ms` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="事件数">{{ result.eventCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="数据源 ID">{{ result.datasourceId || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-collapse-item>
      </el-collapse>
      <div v-if="chatModeTips.length" class="chat-mode-tips">
        <el-alert
          v-for="tip in chatModeTips"
          :key="tip"
          :title="tip"
          type="warning"
          show-icon
          :closable="false"
        />
      </div>
    </el-card>

    <el-card v-if="isKnowledgeAnswerMode" class="page-card result-card business-answer-card">
      <template #header>命中知识摘要</template>
      <el-alert title="本问题为业务规则问答，未执行 SQL。" type="info" :closable="false" show-icon class="mb-12" />
      <el-table v-if="compactKnowledgeChunks.length" :data="compactKnowledgeChunks" border>
        <el-table-column prop="title" label="知识标题" width="220" />
        <el-table-column label="内容摘要" min-width="320">
          <template #default="{ row }">{{ summarizeText(row.content) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无命中知识" />
    </el-card>

    <el-card v-if="isKnowledgeAnswerMode" class="page-card result-card">
      <template #header>调试详情</template>
      <el-collapse class="debug-collapse">
        <el-collapse-item title="节点事件" name="events">
          <el-table :data="events" border class="event-table">
            <el-table-column prop="nodeName" label="节点" width="160" />
            <el-table-column prop="eventType" label="事件" width="130" />
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column prop="message" label="消息" min-width="220" />
            <el-table-column type="expand">
              <template #default="{ row }">
                <pre class="json-pre">{{ formatJson(row.data) }}</pre>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
        <el-collapse-item title="Knowledge Recall JSON" name="knowledge-json">
          <pre class="json-pre">{{ formatJson(result.knowledgeRecallResult) }}</pre>
        </el-collapse-item>
        <el-collapse-item title="knowledgeContext" name="knowledge-context">
          <pre class="json-pre">{{ result.knowledgeContext || '暂无' }}</pre>
        </el-collapse-item>
        <el-collapse-item title="Schema / Relation Context" name="context">
          <h3>schemaContext</h3>
          <pre class="json-pre">{{ result.schemaContext || '暂无' }}</pre>
          <h3>relationContext</h3>
          <pre class="json-pre">{{ result.relationContext || '暂无' }}</pre>
        </el-collapse-item>
        <el-collapse-item title="chartSpec 原始 JSON" name="chart-json">
          <pre class="json-pre">{{ formatJson(result.chartSpec) }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card v-if="!isKnowledgeAnswerMode" class="page-card result-card">
      <template #header>节点事件流</template>
      <el-table :data="events" border class="event-table">
        <el-table-column prop="nodeName" label="节点" width="160" />
        <el-table-column prop="eventType" label="事件" width="130" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="eventTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="消息" min-width="220" />
        <el-table-column prop="timestamp" label="时间" width="190" />
        <el-table-column type="expand">
          <template #default="{ row }">
            <pre class="json-pre">{{ formatJson(row.data) }}</pre>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="!isKnowledgeAnswerMode" class="page-card result-card">
      <template #header>Schema 召回结果</template>
      <el-collapse>
        <el-collapse-item title="召回概览" name="recall-summary">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="召回表数">{{ result.recalledTableCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="召回字段数">{{ result.recalledFieldCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="是否 fallback">{{ formatBool(result.schemaRecallFallbackUsed) }}</el-descriptions-item>
            <el-descriptions-item label="说明">{{ result.schemaRecallMessage || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-collapse-item>
        <el-collapse-item title="selectedTables" name="recall-tables">
          <el-table :data="recalledTables" border>
            <el-table-column type="expand">
              <template #default="{ row }">
                <el-table :data="row.fields || []" border>
                  <el-table-column prop="fieldName" label="fieldName" />
                  <el-table-column prop="businessName" label="businessName" />
                  <el-table-column prop="dataType" label="dataType" width="120" />
                  <el-table-column prop="score" label="score" width="100" />
                  <el-table-column label="matchReasons" min-width="220">
                    <template #default="{ row: fieldRow }">{{ (fieldRow.matchReasons || []).join(' ; ') || '-' }}</template>
                  </el-table-column>
                </el-table>
              </template>
            </el-table-column>
            <el-table-column prop="tableName" label="tableName" />
            <el-table-column prop="businessName" label="businessName" />
            <el-table-column prop="score" label="score" width="100" />
            <el-table-column label="matchReasons" min-width="220">
              <template #default="{ row }">{{ (row.matchReasons || []).join(' ; ') || '-' }}</template>
            </el-table-column>
            <el-table-column label="fieldCount" width="110">
              <template #default="{ row }">{{ (row.fields || []).length }}</template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
        <el-collapse-item title="schemaContext" name="recall-context">
          <pre class="json-pre">{{ result.schemaContext || '暂无' }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card v-if="!isKnowledgeAnswerMode" class="page-card result-card">
      <template #header>知识召回结果</template>
      <el-collapse>
        <el-collapse-item title="召回概览" name="knowledge-summary">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="召回 chunks">{{ result.recalledKnowledgeCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="是否 fallback">{{ formatBool(result.knowledgeRecallFallbackUsed) }}</el-descriptions-item>
            <el-descriptions-item label="说明" :span="2">{{ result.knowledgeRecallMessage || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-collapse-item>
        <el-collapse-item title="selectedChunks" name="knowledge-chunks">
          <el-table :data="recalledKnowledgeChunks" border>
            <el-table-column prop="title" label="title" min-width="180" />
            <el-table-column prop="score" label="score" width="100" />
            <el-table-column prop="knowledgeType" label="knowledgeType" width="130" />
            <el-table-column prop="sourceType" label="sourceType" width="120" />
            <el-table-column prop="matchReason" label="matchReason" min-width="220" />
            <el-table-column label="content" min-width="280">
              <template #default="{ row }">{{ summarizeText(row.content) }}</template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
        <el-collapse-item title="knowledgeContext" name="knowledge-context">
          <pre class="json-pre">{{ result.knowledgeContext || '暂无' }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card v-if="!isKnowledgeAnswerMode" class="page-card result-card">
      <template #header>表关系召回结果</template>
      <el-collapse>
        <el-collapse-item title="召回概览" name="relation-summary">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="召回关系数">{{ result.recalledRelationCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="是否 fallback">{{ formatBool(result.relationRecallFallbackUsed) }}</el-descriptions-item>
            <el-descriptions-item label="说明" :span="2">{{ result.relationRecallMessage || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-collapse-item>
        <el-collapse-item title="selectedRelations" name="relation-list">
          <el-table :data="recalledRelations" border>
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
        </el-collapse-item>
        <el-collapse-item title="relationRecallResult JSON" name="relation-json">
          <pre class="json-pre">{{ formatJson(result.relationRecallResult || relationEventData) }}</pre>
        </el-collapse-item>
        <el-collapse-item title="relationContext" name="relation-context">
          <pre class="json-pre">{{ result.relationContext || '暂无' }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <div v-if="!isKnowledgeAnswerMode" class="run-result-grid">
      <el-card class="page-card result-card">
        <template #header>核心结论</template>
        <p class="core-summary">{{ coreSummary }}</p>
      </el-card>

      <el-card class="page-card result-card">
        <template #header>可视化图表</template>
        <ChartPreview :chart-spec="normalizedChartSpec" />
      </el-card>
    </div>

    <div v-if="!isKnowledgeAnswerMode" class="run-result-grid">
      <el-card class="page-card result-card">
        <template #header>SQL</template>
        <el-collapse>
          <el-collapse-item title="原始模型输出" name="raw">
            <pre class="sql-block">{{ result.rawLlmSqlOutput || '暂无' }}</pre>
          </el-collapse-item>
          <el-collapse-item title="提取后的 SQL" name="extract">
            <pre class="sql-block">{{ result.extractedSql || '暂无' }}</pre>
          </el-collapse-item>
          <el-collapse-item title="修复后的 SQL" name="repair">
            <pre class="sql-block">{{ result.repairedSql || '暂无' }}</pre>
          </el-collapse-item>
          <el-collapse-item title="校验/执行错误" name="error">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="是否尝试修复">{{ formatBool(result.sqlRepairAttempted) }}</el-descriptions-item>
              <el-descriptions-item label="修复是否成功">{{ formatBool(result.sqlRepairSuccess) }}</el-descriptions-item>
              <el-descriptions-item label="修复说明">{{ result.sqlRepairMessage || '-' }}</el-descriptions-item>
              <el-descriptions-item label="安全说明">{{ result.sqlSecurityMessage || '-' }}</el-descriptions-item>
              <el-descriptions-item label="自动 LIMIT">{{ formatBool(result.sqlLimited) }}</el-descriptions-item>
              <el-descriptions-item label="LIMIT 值">{{ result.sqlLimit || '-' }}</el-descriptions-item>
              <el-descriptions-item label="结果截断">{{ formatBool(result.sqlResultTruncated) }}</el-descriptions-item>
              <el-descriptions-item label="查询超时">{{ result.sqlQueryTimeoutSeconds ? `${result.sqlQueryTimeoutSeconds}s` : '-' }}</el-descriptions-item>
              <el-descriptions-item label="校验错误">{{ result.sqlValidationError || '-' }}</el-descriptions-item>
              <el-descriptions-item label="执行错误">{{ result.sqlExecutionError || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
        <h3>Generated SQL</h3>
        <pre class="sql-block">{{ result.generatedSql || '暂无' }}</pre>
        <h3>Validated SQL</h3>
        <pre class="sql-block">{{ result.validatedSql || '暂无' }}</pre>
        <h3>Sanitized SQL</h3>
        <pre class="sql-block">{{ result.sanitizedSql || '暂无' }}</pre>
      </el-card>

      <el-card class="page-card result-card">
        <template #header>分析结果</template>
        <p>{{ result.analysisSummary || '暂无分析摘要' }}</p>
        <pre class="json-pre">{{ formatJson(result.analysisResult) }}</pre>
      </el-card>
    </div>

    <el-card class="page-card result-card python-panel">
      <template #header>Python 分析</template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="分析引擎">{{ result.pythonEngine || 'java_safe' }}</el-descriptions-item>
        <el-descriptions-item label="是否执行 Python">{{ formatBool(result.pythonExecuted) }}</el-descriptions-item>
        <el-descriptions-item label="Python 成功">{{ formatBool(result.pythonSuccess) }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ result.pythonDurationMs ? `${result.pythonDurationMs} ms` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="fallback">{{ formatBool(result.pythonFallbackUsed) }}</el-descriptions-item>
        <el-descriptions-item label="错误">{{ result.pythonErrorMessage || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-collapse class="mt-12">
        <el-collapse-item title="Python 代码" name="code">
          <pre class="code-block">{{ result.pythonCode || '未执行 Python 沙箱代码' }}</pre>
        </el-collapse-item>
        <el-collapse-item title="stdout" name="stdout">
          <pre class="code-block">{{ result.pythonStdout || '暂无 stdout' }}</pre>
        </el-collapse-item>
        <el-collapse-item title="stderr" name="stderr">
          <pre class="stderr-block">{{ result.pythonStderr || '暂无 stderr' }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card v-if="!isKnowledgeAnswerMode" class="page-card result-card">
      <template #header>SQL 查询结果</template>
      <el-alert v-if="result.sqlResultTruncated" type="warning" :closable="false" show-icon class="mb-12"
        :title="`结果已按安全限制截断，仅展示前 ${result.sqlLimit || result.rowCount || 100} 行。`" />
      <el-empty v-if="!resultRows.length" description="暂无结果" />
      <el-table v-else :data="resultRows" border>
        <el-table-column v-for="column in resultColumns" :key="column" :prop="column" :label="column" />
      </el-table>
      <p class="muted-text">rowCount: {{ result.rowCount ?? resultRows.length }}</p>
    </el-card>

    <el-card v-if="!isKnowledgeAnswerMode" class="page-card result-card">
      <template #header>{{ result.reportTitle || 'Markdown 报告' }}</template>
      <el-tabs>
        <el-tab-pane label="Markdown 预览">
          <div class="markdown-rendered" v-html="renderedMarkdown"></div>
        </el-tab-pane>
        <el-tab-pane label="Markdown 原文">
          <pre class="markdown-preview">{{ result.reportMarkdown || '暂无报告' }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import MarkdownIt from 'markdown-it';
import { listAgents } from '../api/agent';
import { listModelConfigs } from '../api/modelConfig';
import { buildStreamUrl, runGraph } from '../api/graph';
import { cancelHumanRun, confirmHumanRun } from '../api/graphHuman';
import ChartPreview from '../components/ChartPreview.vue';
import { normalizeChartSpec, parseRows } from '../utils/chartSpec';

const modeOptions = ['nl2sql', 'chat'];
const markdown = new MarkdownIt({ html: false, linkify: true, breaks: true });
const router = useRouter();

const agents = ref([]);
const models = ref([]);
const events = ref([]);
const running = ref(false);
const streaming = ref(false);
const status = ref('idle');
const errorMessage = ref('');
const eventSource = ref(null);
const confirming = ref(false);
const confirmSql = ref('');

const form = reactive({
  agentId: null,
  modelConfigId: null,
  embeddingModelConfigId: null,
  knowledgeTopK: 5,
  mode: 'nl2sql',
  sessionId: '',
  question: '',
  confirmBeforeExecute: false,
});

const result = reactive({
  runId: '',
  durationMs: null,
  historySaved: null,
  eventCount: null,
  confirmRequired: null,
  confirmStatus: '',
  confirmSql: '',
  confirmedSql: '',
  paused: null,
  sessionId: '',
  answer: '',
  intent: '',
  datasourceId: null,
  schemaRecallResult: null,
  schemaRecallFallbackUsed: null,
  schemaRecallMessage: '',
  recalledTableCount: null,
  recalledFieldCount: null,
  schemaContext: '',
  relationRecallResult: null,
  relationContext: '',
  recalledRelationCount: null,
  relationRecallMessage: '',
  relationRecallFallbackUsed: null,
  knowledgeRecallResult: null,
  knowledgeContext: '',
  knowledgeRecallFallbackUsed: null,
  knowledgeRecallMessage: '',
  recalledKnowledgeCount: null,
  rawLlmSqlOutput: '',
  extractedSql: '',
  generatedSql: '',
  repairedSql: '',
  validatedSql: '',
  sanitizedSql: '',
  sqlLimited: null,
  sqlLimit: null,
  sqlResultTruncated: null,
  sqlQueryTimeoutSeconds: null,
  sqlSecurityMessage: '',
  sqlRepairAttempted: null,
  sqlRepairSuccess: null,
  sqlRepairMessage: '',
  sqlValidationError: '',
  sqlExecutionError: '',
  sqlResult: [],
  resultPreviewJson: '',
  rowCount: null,
  analysisSummary: '',
  analysisResult: null,
  pythonEngine: '',
  pythonExecuted: null,
  pythonSuccess: null,
  pythonCode: '',
  pythonStdout: '',
  pythonStderr: '',
  pythonExitCode: null,
  pythonDurationMs: null,
  pythonErrorMessage: '',
  pythonFallbackUsed: null,
  reportMarkdown: '',
  reportTitle: '',
  reportSummary: '',
  summary: '',
  reportResult: null,
  chartSpec: null,
  success: null,
  message: '',
});

const chatModels = computed(() => models.value.filter((item) => item.modelType === 'chat' && item.enabled !== false));
const embeddingModels = computed(() => models.value.filter((item) => item.modelType === 'embedding' && item.enabled !== false));
const selectedChatModel = computed(() => models.value.find((item) => item.id === form.modelConfigId));
const isMockChatMode = computed(
  () => form.mode === 'chat' && String(selectedChatModel.value?.provider || '').toLowerCase() === 'mock'
);
const businessRuleQuestionKeywords = ['退款', '规则', '口径', '是否计入', '是否排除'];
const isBusinessRuleQuestionInChat = computed(
  () => form.mode === 'chat' && businessRuleQuestionKeywords.some((keyword) => form.question.includes(keyword))
);
const chatModeTips = computed(() => {
  const tips = [];
  if (isMockChatMode.value) {
    tips.push('当前为 Mock Chat 普通对话模式，不会执行 NL2SQL / Knowledge Recall / RAG 验证。');
  }
  if (isBusinessRuleQuestionInChat.value) {
    tips.push('该问题建议切换到 nl2sql 模式验证业务知识召回。');
  }
  return tips;
});
const recalledTables = computed(() => result.schemaRecallResult?.selectedTables || []);
const relationEventData = computed(() => findEventData('relation_recall') || findEventData('finish'));
const recalledRelations = computed(() => {
  const direct = result.relationRecallResult?.selectedRelations || [];
  if (direct.length) {
    return direct;
  }
  return relationEventData.value?.relationRecallResult?.selectedRelations || relationEventData.value?.selectedRelations || [];
});
const recalledKnowledgeChunks = computed(() => result.knowledgeRecallResult?.selectedChunks || []);
const isKnowledgeAnswerMode = computed(() => ['business_rule', 'knowledge_answer'].includes(result.intent));
const compactKnowledgeChunks = computed(() => recalledKnowledgeChunks.value.slice(0, 5));
const resultRows = computed(() => {
  if (Array.isArray(result.sqlResult) && result.sqlResult.length) {
    return result.sqlResult;
  }
  return parseRows(result.resultPreviewJson);
});
const resultColumns = computed(() => (resultRows.value.length ? Object.keys(resultRows.value[0]) : []));
const normalizedChartSpec = computed(() =>
  normalizeChartSpec(result.chartSpec, resultRows.value, result.resultPreviewJson, result.analysisResult)
);
const coreSummary = computed(() =>
  result.reportSummary || result.reportResult?.summary || result.summary || '暂无核心结论'
);
const answerText = computed(() =>
  result.reportSummary || result.reportResult?.summary || result.summary || result.answer || '-'
);
const renderedMarkdown = computed(() => markdown.render(result.reportMarkdown || '暂无报告'));
const statusText = computed(() => {
  const map = {
    idle: '未开始',
    running: '运行中',
    success: '成功',
    failed: '失败',
  };
  return map[status.value] || status.value;
});
const statusType = computed(() => {
  if (status.value === 'success') {
    return 'success';
  }
  if (status.value === 'failed') {
    return 'danger';
  }
  if (status.value === 'running') {
    return 'primary';
  }
  return 'info';
});

const loadOptions = async () => {
  const [agentList, modelList] = await Promise.all([listAgents({}), listModelConfigs({})]);
  agents.value = agentList || [];
  models.value = modelList || [];
};

const validateForm = () => {
  if (!form.agentId) {
    ElMessage.warning('请选择 Agent');
    return false;
  }
  if (!form.modelConfigId) {
    ElMessage.warning('请选择模型配置');
    return false;
  }
  if (!form.question.trim()) {
    ElMessage.warning('请输入问题');
    return false;
  }
  return true;
};

const buildPayload = () => ({
  agentId: form.agentId,
  modelConfigId: form.modelConfigId,
  question: form.question.trim(),
  sessionId: form.sessionId || undefined,
  mode: form.mode || 'nl2sql',
  embeddingModelConfigId: form.embeddingModelConfigId || undefined,
  knowledgeTopK: form.knowledgeTopK || 5,
  confirmBeforeExecute: form.confirmBeforeExecute || false,
});

const resetResultFields = () => {
  events.value = [];
  errorMessage.value = '';
  Object.assign(result, {
    sessionId: '',
    runId: '',
    durationMs: null,
    historySaved: null,
    eventCount: null,
    confirmRequired: null,
    confirmStatus: '',
    confirmSql: '',
    confirmedSql: '',
    paused: null,
    answer: '',
    intent: '',
    datasourceId: null,
    schemaRecallResult: null,
    schemaRecallFallbackUsed: null,
    schemaRecallMessage: '',
    recalledTableCount: null,
    recalledFieldCount: null,
    schemaContext: '',
    relationRecallResult: null,
    relationContext: '',
    recalledRelationCount: null,
    relationRecallMessage: '',
    relationRecallFallbackUsed: null,
    knowledgeRecallResult: null,
    knowledgeContext: '',
    knowledgeRecallFallbackUsed: null,
    knowledgeRecallMessage: '',
    recalledKnowledgeCount: null,
    rawLlmSqlOutput: '',
    extractedSql: '',
    generatedSql: '',
    repairedSql: '',
    validatedSql: '',
    sanitizedSql: '',
    sqlLimited: null,
    sqlLimit: null,
    sqlResultTruncated: null,
    sqlQueryTimeoutSeconds: null,
    sqlSecurityMessage: '',
    sqlRepairAttempted: null,
    sqlRepairSuccess: null,
    sqlRepairMessage: '',
    sqlValidationError: '',
    sqlExecutionError: '',
    sqlResult: [],
    resultPreviewJson: '',
    rowCount: null,
    analysisSummary: '',
    analysisResult: null,
    pythonEngine: '',
    pythonExecuted: null,
    pythonSuccess: null,
    pythonCode: '',
    pythonStdout: '',
    pythonStderr: '',
    pythonExitCode: null,
    pythonDurationMs: null,
    pythonErrorMessage: '',
    pythonFallbackUsed: null,
    reportMarkdown: '',
    reportTitle: '',
    reportSummary: '',
    summary: '',
    reportResult: null,
    chartSpec: null,
    success: null,
    message: '',
  });
};

const applyGraphResult = (data = {}) => {
  result.runId = data.runId || result.runId;
  result.durationMs = data.durationMs ?? result.durationMs;
  result.historySaved = data.historySaved ?? result.historySaved;
  result.eventCount = data.eventCount ?? result.eventCount;
  result.confirmRequired = data.confirmRequired ?? result.confirmRequired;
  result.confirmStatus = data.confirmStatus || result.confirmStatus;
  result.confirmSql = data.confirmSql || result.confirmSql;
  result.confirmedSql = data.confirmedSql || result.confirmedSql;
  result.paused = data.paused ?? result.paused;
  if (data.confirmSql) {
    confirmSql.value = data.confirmSql;
  }
  result.sessionId = data.sessionId || result.sessionId;
  result.answer = data.answer || result.answer;
  result.intent = data.intent || result.intent;
  result.datasourceId = data.datasourceId ?? result.datasourceId;
  result.schemaRecallResult = data.schemaRecallResult || result.schemaRecallResult;
  result.schemaRecallFallbackUsed = data.schemaRecallFallbackUsed ?? result.schemaRecallFallbackUsed;
  result.schemaRecallMessage = data.schemaRecallMessage || result.schemaRecallMessage;
  result.recalledTableCount = data.recalledTableCount ?? result.recalledTableCount;
  result.recalledFieldCount = data.recalledFieldCount ?? result.recalledFieldCount;
  result.schemaContext = data.schemaContext || data.schemaRecallResult?.schemaContext || result.schemaContext;
  result.relationRecallResult = data.relationRecallResult || result.relationRecallResult;
  result.relationContext = data.relationContext || result.relationContext;
  result.recalledRelationCount = data.recalledRelationCount ?? result.recalledRelationCount;
  result.relationRecallMessage = data.relationRecallMessage || result.relationRecallMessage;
  result.relationRecallFallbackUsed = data.relationRecallFallbackUsed ?? result.relationRecallFallbackUsed;
  result.knowledgeRecallResult = data.knowledgeRecallResult || result.knowledgeRecallResult;
  result.knowledgeContext = data.knowledgeContext || data.knowledgeRecallResult?.knowledgeContext || result.knowledgeContext;
  result.knowledgeRecallFallbackUsed = data.knowledgeRecallFallbackUsed ?? result.knowledgeRecallFallbackUsed;
  result.knowledgeRecallMessage = data.knowledgeRecallMessage || result.knowledgeRecallMessage;
  result.recalledKnowledgeCount = data.recalledKnowledgeCount ?? result.recalledKnowledgeCount;
  result.rawLlmSqlOutput = data.rawLlmSqlOutput || result.rawLlmSqlOutput;
  result.extractedSql = data.extractedSql || result.extractedSql;
  result.generatedSql = data.generatedSql || result.generatedSql;
  result.repairedSql = data.repairedSql || result.repairedSql;
  result.validatedSql = data.validatedSql || result.validatedSql;
  result.sanitizedSql = data.sanitizedSql || result.sanitizedSql;
  result.sqlLimited = data.sqlLimited ?? result.sqlLimited;
  result.sqlLimit = data.sqlLimit ?? result.sqlLimit;
  result.sqlResultTruncated = data.sqlResultTruncated ?? result.sqlResultTruncated;
  result.sqlQueryTimeoutSeconds = data.sqlQueryTimeoutSeconds ?? result.sqlQueryTimeoutSeconds;
  result.sqlSecurityMessage = data.sqlSecurityMessage || result.sqlSecurityMessage;
  result.sqlRepairAttempted = data.sqlRepairAttempted ?? result.sqlRepairAttempted;
  result.sqlRepairSuccess = data.sqlRepairSuccess ?? result.sqlRepairSuccess;
  result.sqlRepairMessage = data.sqlRepairMessage || result.sqlRepairMessage;
  result.sqlValidationError = data.sqlValidationError || result.sqlValidationError;
  result.sqlExecutionError = data.sqlExecutionError || result.sqlExecutionError;
  result.sqlResult = Array.isArray(data.sqlResult) ? data.sqlResult : result.sqlResult;
  result.resultPreviewJson = data.resultPreviewJson || result.resultPreviewJson;
  result.rowCount = data.rowCount ?? result.rowCount;
  result.analysisSummary = data.analysisSummary || result.analysisSummary;
  result.analysisResult = data.analysisResult || data.metrics || result.analysisResult;
  result.pythonEngine = data.pythonEngine || result.pythonEngine;
  result.pythonExecuted = data.pythonExecuted ?? result.pythonExecuted;
  result.pythonSuccess = data.pythonSuccess ?? result.pythonSuccess;
  result.pythonCode = data.pythonCode || result.pythonCode;
  result.pythonStdout = data.pythonStdout || result.pythonStdout;
  result.pythonStderr = data.pythonStderr || result.pythonStderr;
  result.pythonExitCode = data.pythonExitCode ?? result.pythonExitCode;
  result.pythonDurationMs = data.pythonDurationMs ?? result.pythonDurationMs;
  result.pythonErrorMessage = data.pythonErrorMessage || result.pythonErrorMessage;
  result.pythonFallbackUsed = data.pythonFallbackUsed ?? result.pythonFallbackUsed;
  result.reportMarkdown = data.reportMarkdown || result.reportMarkdown;
  result.reportTitle = data.reportTitle || result.reportTitle;
  result.reportSummary = data.reportSummary || data.reportResult?.summary || result.reportSummary;
  result.summary = data.summary || result.summary;
  result.reportResult = data.reportResult || result.reportResult;
  result.chartSpec = data.chartSpec || data.reportResult?.chartSpec || result.chartSpec;
  result.success = data.success ?? result.success;
  result.message = data.message || result.message;
  if (data.events) {
    events.value = data.events;
  }
};

const confirmRun = async () => {
  if (!result.runId) return;
  confirming.value = true;
  try {
    const response = await confirmHumanRun(result.runId, { sql: confirmSql.value, confirmedBy: 'local_user' });
    applyGraphResult(response.graphRunVO || {});
    if (response.graphRunVO?.events) {
      events.value = response.graphRunVO.events;
    }
    status.value = response.status === 'success' ? 'success' : 'failed';
    result.confirmRequired = false;
    result.confirmStatus = response.confirmStatus || 'confirmed';
    result.confirmSql = '';
    confirmSql.value = '';
    ElMessage.success(response.message || '确认执行完成');
  } catch (error) {
    errorMessage.value = error.message;
    if (String(error.message || '').includes('not pending')) {
      result.confirmRequired = false;
      result.confirmStatus = '';
      result.confirmSql = '';
      confirmSql.value = '';
    }
  } finally {
    confirming.value = false;
  }
};

const cancelRun = async () => {
  if (!result.runId) return;
  confirming.value = true;
  try {
    const response = await cancelHumanRun(result.runId, { reason: 'User canceled SQL execution', canceledBy: 'local_user' });
    result.confirmStatus = response.confirmStatus || 'canceled';
    result.confirmRequired = false;
    result.confirmSql = '';
    confirmSql.value = '';
    status.value = 'failed';
    result.message = response.message;
    ElMessage.success('已取消执行');
  } finally {
    confirming.value = false;
  }
};

const goHistory = () => {
  router.push('/graph-history');
};

const goCurrentHistory = () => {
  if (result.runId) {
    router.push({ path: '/graph-history', query: { runId: result.runId } });
  }
};

const runNormal = async () => {
  if (!validateForm()) {
    return;
  }
  closeStream();
  resetResultFields();
  running.value = true;
  status.value = 'running';
  try {
    const data = await runGraph(buildPayload());
    applyGraphResult(data);
    status.value = data.success === false ? 'failed' : 'success';
    if (data.success === false) {
      errorMessage.value = data.message || '运行失败';
    }
  } catch (error) {
    status.value = 'failed';
    errorMessage.value = error.message || '运行失败';
  } finally {
    running.value = false;
  }
};

const runStream = () => {
  if (!validateForm()) {
    return;
  }
  closeStream();
  resetResultFields();
  status.value = 'running';
  streaming.value = true;
  const source = new EventSource(buildStreamUrl(buildPayload()));
  eventSource.value = source;
  ['node_start', 'node_end', 'message', 'error', 'finish'].forEach((eventName) => {
    source.addEventListener(eventName, handleStreamEvent);
  });
  source.onerror = () => {
    if (status.value === 'running') {
      status.value = 'failed';
      errorMessage.value = errorMessage.value || 'SSE 连接异常';
    }
    streaming.value = false;
    closeStream();
  };
};

const handleStreamEvent = (event) => {
  if (!event.data) {
    return;
  }
  const graphEvent = JSON.parse(event.data);
  events.value.push(graphEvent);
  result.sessionId = graphEvent.sessionId || result.sessionId;
  if (graphEvent.data) {
    applyGraphResult(graphEvent.data);
  }
  if (graphEvent.eventType === 'error' || graphEvent.status === 'failed') {
    status.value = 'failed';
    errorMessage.value = graphEvent.message || '运行失败';
  }
  if (graphEvent.eventType === 'finish') {
    status.value = status.value === 'failed' ? 'failed' : 'success';
    streaming.value = false;
    closeStream();
  }
};

const closeStream = () => {
  if (eventSource.value) {
    eventSource.value.close();
    eventSource.value = null;
  }
};

const clearResult = () => {
  closeStream();
  running.value = false;
  streaming.value = false;
  status.value = 'idle';
  resetResultFields();
};

const fillExample = () => {
  form.mode = 'nl2sql';
  form.question = '最近销售额是多少？';
  form.knowledgeTopK = 5;
  if (!form.sessionId) {
    form.sessionId = `session-${Date.now()}`;
  }
};

const eventTagType = (eventStatus) => {
  if (eventStatus === 'success') {
    return 'success';
  }
  if (eventStatus === 'failed' || eventStatus === 'error') {
    return 'danger';
  }
  return 'primary';
};

const formatJson = (value) => {
  if (!value) {
    return '{}';
  }
  return JSON.stringify(value, null, 2);
};

const findEventData = (nodeName) => {
  const matched = [...events.value].reverse().find((event) => event.nodeName === nodeName && event.data);
  return matched?.data || null;
};

const formatBool = (value) => {
  if (value === null || value === undefined) {
    return '-';
  }
  return value ? '是' : '否';
};

const summarizeText = (text) => {
  if (!text) {
    return '-';
  }
  return text.length > 120 ? `${text.slice(0, 120)}...` : text;
};

onMounted(loadOptions);
onBeforeUnmount(closeStream);
</script>
