import axios from 'axios';

// Relative base — the Vite dev server proxies /api to the Spring Boot backend.
const api = axios.create({ baseURL: '/api' });

export const listProjects = () => api.get('/projects').then((r) => r.data);
export const getProject = (id) => api.get(`/projects/${id}`).then((r) => r.data);
export const createProject = (body) => api.post('/projects', body).then((r) => r.data);
export const deleteProject = (id) => api.delete(`/projects/${id}`).then((r) => r.data);

export const analyzeProject = (id) => api.post(`/projects/${id}/analyze`).then((r) => r.data);
export const getRuns = (projectId) => api.get(`/projects/${projectId}/runs`).then((r) => r.data);
export const getRun = (runId) => api.get(`/runs/${runId}`).then((r) => r.data);
export const getRunFiles = (runId) => api.get(`/runs/${runId}/files`).then((r) => r.data);

export const compareProjects = (ids) =>
  api.get('/compare', { params: { ids: ids.join(',') } }).then((r) => r.data);

export default api;
