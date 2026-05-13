import request from './request';

export const createDatasource = (data) => request.post('/datasource/create', data);
export const listDatasources = (params) => request.get('/datasource/list', { params });
export const getDatasource = (id) => request.get(`/datasource/${id}`);
export const updateDatasource = (id, data) => request.put(`/datasource/${id}`, data);
export const deleteDatasource = (id) => request.delete(`/datasource/${id}`);
export const enableDatasource = (id) => request.put(`/datasource/${id}/enable`);
export const disableDatasource = (id) => request.put(`/datasource/${id}/disable`);
export const testDatasource = (data) => request.post('/datasource/test', data);
export const testSavedDatasource = (id) => request.post(`/datasource/${id}/test`);
