import request from './request';

export const createBusinessKnowledge = (data) => request.post('/knowledge/business/create', data);
export const listBusinessKnowledge = (params) => request.get('/knowledge/business/list', { params });
export const getBusinessKnowledge = (id) => request.get(`/knowledge/business/${id}`);
export const updateBusinessKnowledge = (id, data) => request.put(`/knowledge/business/${id}`, data);
export const deleteBusinessKnowledge = (id) => request.delete(`/knowledge/business/${id}`);
export const enableBusinessKnowledge = (id) => request.put(`/knowledge/business/${id}/enable`);
export const disableBusinessKnowledge = (id) => request.put(`/knowledge/business/${id}/disable`);
export const uploadBusinessKnowledge = (data) => request.post('/knowledge/business/upload', data, {
  headers: { 'Content-Type': 'multipart/form-data' },
});

export const bindAgentKnowledge = (data) => request.post('/knowledge/agent/bind', data);
export const listAgentKnowledge = (params) => request.get('/knowledge/agent/list', { params });
export const listKnowledgeByAgentId = (agentId) => request.get(`/knowledge/agent/${agentId}`);
export const deleteAgentKnowledge = (id) => request.delete(`/knowledge/agent/${id}`);
export const enableAgentKnowledge = (id) => request.put(`/knowledge/agent/${id}/enable`);
export const disableAgentKnowledge = (id) => request.put(`/knowledge/agent/${id}/disable`);
export const listKnowledgeChunks = (id) => request.get(`/knowledge/business/${id}/chunks`);
export const rebuildKnowledgeChunks = (id) => request.post(`/knowledge/business/${id}/chunks/rebuild`);
export const rebuildKnowledgeEmbedding = (knowledgeId, modelConfigId) => request.post(`/knowledge/business/${knowledgeId}/embedding/rebuild`, null, {
  params: { modelConfigId },
});
export const rebuildAgentKnowledgeEmbedding = (agentId, modelConfigId) => request.post(`/knowledge/agent/${agentId}/embedding/rebuild`, null, {
  params: { modelConfigId },
});
export const clearKnowledgeEmbedding = (knowledgeId) => request.delete(`/knowledge/business/${knowledgeId}/embedding`);
