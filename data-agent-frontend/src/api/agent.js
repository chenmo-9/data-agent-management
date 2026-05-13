import request from './request';

export const createAgent = (data) => request.post('/agent/create', data);
export const listAgents = (params) => request.get('/agent/list', { params });
export const getAgent = (id) => request.get(`/agent/${id}`);
export const updateAgent = (id, data) => request.put(`/agent/${id}`, data);
export const deleteAgent = (id) => request.delete(`/agent/${id}`);
export const publishAgent = (id) => request.put(`/agent/${id}/publish`);
export const offlineAgent = (id) => request.put(`/agent/${id}/offline`);
