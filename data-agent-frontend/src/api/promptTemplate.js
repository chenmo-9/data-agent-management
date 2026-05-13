import request from './request';

export const initDefaultPrompts = () => request.post('/prompt-template/init-defaults');
export const createPromptTemplate = (data) => request.post('/prompt-template/create', data);
export const listPromptTemplates = (params) => request.get('/prompt-template/list', { params });
export const getPromptTemplate = (id) => request.get(`/prompt-template/${id}`);
export const updatePromptTemplate = (id, data) => request.put(`/prompt-template/${id}`, data);
export const deletePromptTemplate = (id) => request.delete(`/prompt-template/${id}`);
export const enablePromptTemplate = (id) => request.put(`/prompt-template/${id}/enable`);
export const disablePromptTemplate = (id) => request.put(`/prompt-template/${id}/disable`);
export const renderPromptTemplate = (data) => request.post('/prompt-template/render', data);
