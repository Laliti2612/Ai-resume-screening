import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('jwt_token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const authAPI = {
  login: (data) => api.post('/api/auth/login', data),
  me:    ()     => api.get('/api/auth/me'),
};

export const resumeAPI = {
  upload:  (formData) => api.post('/api/resumes/upload', formData),
  getById: (id)       => api.get(`/api/resumes/${id}`),
  delete:  (id)       => api.delete(`/api/resumes/${id}`),
};

export const candidateAPI = {
  list:         (params)     => api.get('/api/candidates', { params }),
  getById:      (id)         => api.get(`/api/candidates/${id}`),
  updateStatus: (id, status) => api.patch(`/api/candidates/${id}/status`, { status }),
};

export const jobAPI = {
  list:   ()         => api.get('/api/jobs'),
  create: (data)     => api.post('/api/jobs', data),
  update: (id, data) => api.put(`/api/jobs/${id}`, data),
  delete: (id)       => api.delete(`/api/jobs/${id}`),
};

export const dashboardAPI = {
  stats:       () => api.get('/api/dashboard/stats'),
  shortlisted: () => api.get('/api/dashboard/shortlisted'),
};

export default api;