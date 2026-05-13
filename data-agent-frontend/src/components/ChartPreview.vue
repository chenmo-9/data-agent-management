<template>
  <div class="chart-preview">
    <el-descriptions v-if="chartSpec" class="chart-spec-meta" :column="2" border size="small">
      <el-descriptions-item label="chartType">{{ chartSpec.chartType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="title">{{ chartSpec.title || '-' }}</el-descriptions-item>
      <el-descriptions-item label="xField">{{ chartSpec.xField || '-' }}</el-descriptions-item>
      <el-descriptions-item label="yField">{{ chartSpec.yField || '-' }}</el-descriptions-item>
      <el-descriptions-item label="xData" :span="2">{{ formatArray(chartSpec.xData) }}</el-descriptions-item>
      <el-descriptions-item label="yData" :span="2">{{ formatArray(chartSpec.yData) }}</el-descriptions-item>
    </el-descriptions>
    <div v-if="!chartSpec || chartSpec.chartType === 'none'" class="chart-empty">
      暂无可视化图表
    </div>
    <div v-else-if="chartSpec.chartType === 'single_value'" class="single-value-card">
      <div class="single-value-label">{{ chartSpec.title || chartSpec.yField || '核心指标' }}</div>
      <div class="single-value-number">{{ singleValue }}</div>
      <div class="single-value-desc">{{ chartSpec.description }}</div>
    </div>
    <div v-else ref="chartRef" class="echarts-canvas"></div>
    <div v-if="chartError" class="chart-empty chart-error">
      图表渲染失败，已展示下方 xData / yData 明细。
    </div>
    <el-table v-if="fallbackRows.length" :data="fallbackRows" class="chart-fallback-table" border size="small">
      <el-table-column :label="chartSpec?.xField || 'xData'" prop="x" />
      <el-table-column :label="chartSpec?.yField || 'yData'" prop="y" />
    </el-table>
  </div>
</template>

<script setup>
import * as echarts from 'echarts';
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';

const props = defineProps({
  chartSpec: {
    type: Object,
    default: null,
  },
});

const chartRef = ref(null);
const chartError = ref(false);
let chartInstance = null;

const singleValue = computed(() => props.chartSpec?.yData?.[0] ?? '-');
const fallbackRows = computed(() => {
  const xData = props.chartSpec?.xData || [];
  const yData = props.chartSpec?.yData || [];
  if (!xData.length && !yData.length) {
    return [];
  }
  const length = Math.max(xData.length, yData.length);
  return Array.from({ length }, (_, index) => ({
    x: xData[index] ?? '',
    y: yData[index] ?? '',
  }));
});

const buildOption = () => {
  const spec = props.chartSpec || {};
  if (spec.chartType === 'pie') {
    return {
      title: { text: spec.title || '数据分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        name: spec.yField,
        type: 'pie',
        radius: '62%',
        data: (spec.xData || []).map((name, index) => ({
          name,
          value: spec.yData?.[index] ?? 0,
        })),
      }],
    };
  }
  return {
    title: { text: spec.title || '数据图表' },
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 24, top: 56, bottom: 36 },
    xAxis: { type: 'category', data: spec.xData || [] },
    yAxis: { type: 'value' },
    series: [{
      name: spec.yField,
      type: spec.chartType === 'line' ? 'line' : 'bar',
      data: spec.yData || [],
      smooth: spec.chartType === 'line',
    }],
  };
};

const renderChart = async () => {
  await nextTick();
  if (!props.chartSpec || ['none', 'single_value'].includes(props.chartSpec.chartType)) {
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }
    return;
  }
  if (!chartRef.value) {
    return;
  }
  try {
    chartError.value = false;
    chartInstance ||= echarts.init(chartRef.value);
    chartInstance.setOption(buildOption(), true);
    chartInstance.resize();
  } catch (error) {
    chartError.value = true;
  }
};

const formatArray = (value) => {
  if (!Array.isArray(value) || !value.length) {
    return '-';
  }
  return JSON.stringify(value);
};

watch(() => props.chartSpec, renderChart, { deep: true, immediate: true });

const handleResize = () => {
  chartInstance?.resize();
};

window.addEventListener('resize', handleResize);

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});
</script>
