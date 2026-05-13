export const parseRows = (value) => {
  if (Array.isArray(value)) {
    return value;
  }
  if (!value || typeof value !== 'string') {
    return [];
  }
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    return [];
  }
};

export const normalizeChartSpec = (chartSpec, rows = [], resultPreviewJson, analysisResult) => {
  const previewRows = rows?.length ? rows : parseRows(resultPreviewJson);
  const spec = { ...(chartSpec || {}) };
  const chartType = spec.chartType || inferChartType(previewRows, analysisResult);
  spec.chartType = chartType || 'none';

  if (spec.chartType === 'none') {
    return spec;
  }

  if (spec.chartType === 'single_value') {
    return normalizeSingleValue(spec, previewRows, analysisResult);
  }

  if (['bar', 'line', 'pie'].includes(spec.chartType)) {
    return normalizeSeriesChart(spec, previewRows);
  }

  return spec;
};

const normalizeSingleValue = (spec, rows, analysisResult) => {
  const metric = findSingleValue(rows, analysisResult);
  if (metric) {
    spec.xField ||= metric.field;
    spec.yField ||= metric.field;
    spec.xData = hasData(spec.xData) ? spec.xData : [metric.field];
    spec.yData = hasData(spec.yData) ? spec.yData : [metric.value];
    spec.title ||= metric.field || '核心指标';
  }
  return spec;
};

const normalizeSeriesChart = (spec, rows) => {
  if (!rows?.length) {
    return spec;
  }
  const columns = Object.keys(rows[0] || {});
  if (!columns.length) {
    return spec;
  }
  const fields = inferFields(columns, rows);
  spec.xField ||= fields.xField;
  spec.yField ||= fields.yField;
  spec.xData = hasData(spec.xData) ? spec.xData : rows.map((row) => row[spec.xField]);
  spec.yData = hasData(spec.yData) ? spec.yData : rows.map((row) => toNumberIfPossible(row[spec.yField]));
  spec.title ||= spec.xField && spec.yField ? `${spec.yField} 按 ${spec.xField} 对比` : '数据图表';
  return spec;
};

const inferFields = (columns, rows) => {
  const firstRow = rows[0] || {};
  const nonNumericField = columns.find((column) => !isNumberLike(firstRow[column]));
  const numericField = columns.find((column) => isNumberLike(firstRow[column]));
  if (nonNumericField && numericField) {
    return { xField: nonNumericField, yField: numericField };
  }
  if (columns.length >= 2) {
    return { xField: columns[0], yField: columns[1] };
  }
  return { xField: columns[0], yField: columns[0] };
};

const findSingleValue = (rows, analysisResult) => {
  if (rows?.length === 1) {
    const columns = Object.keys(rows[0] || {});
    if (columns.length === 1) {
      return { field: columns[0], value: rows[0][columns[0]] };
    }
  }
  if (analysisResult?.singleValue !== undefined && analysisResult?.singleValue !== null) {
    return { field: 'singleValue', value: analysisResult.singleValue };
  }
  return null;
};

const inferChartType = (rows, analysisResult) => {
  if (rows?.length === 1 && Object.keys(rows[0] || {}).length === 1) {
    return 'single_value';
  }
  if (rows?.length > 1) {
    return 'bar';
  }
  if (analysisResult?.singleValue !== undefined && analysisResult?.singleValue !== null) {
    return 'single_value';
  }
  return 'none';
};

const hasData = (value) => Array.isArray(value) && value.length > 0;

const isNumberLike = (value) => value !== null && value !== undefined && value !== '' && !Number.isNaN(Number(value));

const toNumberIfPossible = (value) => (isNumberLike(value) ? Number(value) : value);
