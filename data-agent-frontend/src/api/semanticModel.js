import request from './request';

export const createSemanticTable = (data) => request.post('/semantic-model/table/create', data);
export const listSemanticTables = (params) => request.get('/semantic-model/table/list', { params });
export const getSemanticTable = (id) => request.get(`/semantic-model/table/${id}`);
export const updateSemanticTable = (id, data) => request.put(`/semantic-model/table/${id}`, data);
export const deleteSemanticTable = (id) => request.delete(`/semantic-model/table/${id}`);
export const enableSemanticTable = (id) => request.put(`/semantic-model/table/${id}/enable`);
export const disableSemanticTable = (id) => request.put(`/semantic-model/table/${id}/disable`);

export const createSemanticField = (data) => request.post('/semantic-model/field/create', data);
export const listSemanticFields = (params) => request.get('/semantic-model/field/list', { params });
export const getSemanticField = (id) => request.get(`/semantic-model/field/${id}`);
export const updateSemanticField = (id, data) => request.put(`/semantic-model/field/${id}`, data);
export const deleteSemanticField = (id) => request.delete(`/semantic-model/field/${id}`);
export const enableSemanticField = (id) => request.put(`/semantic-model/field/${id}/enable`);
export const disableSemanticField = (id) => request.put(`/semantic-model/field/${id}/disable`);
export const getSemanticModelByTableId = (id) => request.get(`/semantic-model/table/${id}/model`);
export const listSemanticModelsByDatasourceId = (datasourceId) => request.get(`/semantic-model/datasource/${datasourceId}/models`);
export const createSemanticRelation = (data) => request.post('/semantic-model/relation/create', data);
export const listSemanticRelations = (params) => request.get('/semantic-model/relation/list', { params });
export const getSemanticRelation = (id) => request.get(`/semantic-model/relation/${id}`);
export const updateSemanticRelation = (id, data) => request.put(`/semantic-model/relation/${id}`, data);
export const deleteSemanticRelation = (id) => request.delete(`/semantic-model/relation/${id}`);
export const enableSemanticRelation = (id) => request.put(`/semantic-model/relation/${id}/enable`);
export const disableSemanticRelation = (id) => request.put(`/semantic-model/relation/${id}/disable`);
export const listRelationsByDatasourceId = (datasourceId) => request.get(`/semantic-model/datasource/${datasourceId}/relations`);
export const listRelationsByTableId = (tableId) => request.get(`/semantic-model/table/${tableId}/relations`);
