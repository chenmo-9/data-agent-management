import request from './request';

export const bindAgentDatasource = (data) => request.post('/agent-datasource/bind', data);
export const listAgentDatasource = (params) => request.get('/agent-datasource/list', { params });
export const listDatasourceByAgent = (agentId) => request.get(`/agent-datasource/agent/${agentId}`);
export const deleteAgentDatasource = (id) => request.delete(`/agent-datasource/${id}`);
export const enableAgentDatasource = (id) => request.put(`/agent-datasource/${id}/enable`);
export const disableAgentDatasource = (id) => request.put(`/agent-datasource/${id}/disable`);
