import request from './request';

export const runGraph = (data) => request.post('/graph/run', {
  mode: 'nl2sql',
  ...data,
});

export const buildStreamUrl = (params = {}) => {
  const query = new URLSearchParams();
  query.set('agentId', params.agentId);
  query.set('modelConfigId', params.modelConfigId);
  query.set('question', params.question || '');
  query.set('mode', params.mode || 'nl2sql');
  if (params.sessionId) {
    query.set('sessionId', params.sessionId);
  }
  if (params.embeddingModelConfigId) {
    query.set('embeddingModelConfigId', params.embeddingModelConfigId);
  }
  if (params.knowledgeTopK) {
    query.set('knowledgeTopK', params.knowledgeTopK);
  }
  if (params.confirmBeforeExecute) {
    query.set('confirmBeforeExecute', 'true');
  }
  return `/api/graph/stream-get?${query.toString()}`;
};
