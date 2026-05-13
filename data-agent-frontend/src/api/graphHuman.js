import request from './request';

export const getPendingHumanRun = (runId) => request.get(`/graph-human/${runId}`);

export const confirmHumanRun = (runId, data) => request.post(`/graph-human/${runId}/confirm`, data);

export const cancelHumanRun = (runId, data) => request.post(`/graph-human/${runId}/cancel`, data);
