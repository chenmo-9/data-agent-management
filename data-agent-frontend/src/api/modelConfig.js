import request from './request';

export const createModelConfig = (data) => request.post('/model-config/create', data);
export const listModelConfigs = (params) => request.get('/model-config/list', { params });
export const getModelConfig = (id) => request.get(`/model-config/${id}`);
export const updateModelConfig = (id, data) => request.put(`/model-config/${id}`, data);
export const deleteModelConfig = (id) => request.delete(`/model-config/${id}`);
export const enableModelConfig = (id) => request.put(`/model-config/${id}/enable`);
export const disableModelConfig = (id) => request.put(`/model-config/${id}/disable`);
