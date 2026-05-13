import axios from 'axios';
import { ElMessage } from 'element-plus';

const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
});

service.interceptors.response.use(
  (response) => {
    const result = response.data;
    if (result && Object.prototype.hasOwnProperty.call(result, 'success')) {
      if (!result.success) {
        const message = result.message || 'Request failed';
        ElMessage.error(message);
        return Promise.reject(new Error(message));
      }
      return result.data;
    }
    return result;
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || 'Network error');
    return Promise.reject(error);
  },
);

export default service;
