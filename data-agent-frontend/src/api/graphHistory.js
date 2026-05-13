import request from './request';

export const listGraphRuns = (params) => request.get('/graph-history/list', { params });

export const getGraphRunDetail = (runId) => request.get(`/graph-history/${runId}`);

export const listGraphRunEvents = (runId) => request.get(`/graph-history/${runId}/events`);

export const deleteGraphRun = (runId) => request.delete(`/graph-history/${runId}`);
