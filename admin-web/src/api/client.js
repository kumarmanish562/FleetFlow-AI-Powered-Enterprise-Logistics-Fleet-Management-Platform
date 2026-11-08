import axios from 'axios';

const STORAGE_KEY = 'fleetflow.session.v1';
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export function readSession() {
  try { return JSON.parse(sessionStorage.getItem(STORAGE_KEY) || 'null'); }
  catch { return null; }
}

export function writeSession(session) {
  if (session) sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  else sessionStorage.removeItem(STORAGE_KEY);
}

export const api = axios.create({ baseURL: API_BASE_URL, timeout: 20_000 });
let refreshPromise;

api.interceptors.request.use((config) => {
  const session = readSession();
  if (session?.accessToken) config.headers.Authorization = `Bearer ${session.accessToken}`;
  return config;
});

api.interceptors.response.use((response) => response, async (error) => {
  const request = error.config;
  if (error.response?.status !== 401 || !request || request._retried || request.url?.includes('/auth/')) {
    return Promise.reject(error);
  }
  const current = readSession();
  if (!current?.refreshToken) {
    writeSession(null);
    window.dispatchEvent(new Event('fleetflow:session-expired'));
    return Promise.reject(error);
  }
  request._retried = true;
  try {
    refreshPromise ||= axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken: current.refreshToken }, { timeout: 20_000 })
      .then(({ data }) => {
        const next = { ...data, user: current.user };
        writeSession(next);
        return next;
      }).finally(() => { refreshPromise = undefined; });
    const next = await refreshPromise;
    request.headers.Authorization = `${next.tokenType || 'Bearer'} ${next.accessToken}`;
    return api(request);
  } catch (refreshError) {
    writeSession(null);
    window.dispatchEvent(new Event('fleetflow:session-expired'));
    return Promise.reject(refreshError);
  }
});

export function apiMessage(error) {
  return error?.response?.data?.message || error?.response?.data?.detail || error?.message || 'Request failed. Please try again.';
}

export const authApi = {
  login: async (credentials) => (await api.post('/auth/login', credentials)).data,
  me: async () => (await api.get('/auth/me')).data,
  logout: async (refreshToken) => api.post('/auth/logout', { refreshToken }),
};

export const analyticsApi = {
  dashboard: async () => (await api.get('/analytics/dashboard')).data,
  shipments: async () => (await api.get('/analytics/shipments/summary')).data,
  fleet: async () => (await api.get('/analytics/fleet/summary')).data,
  drivers: async () => (await api.get('/analytics/drivers/summary')).data,
  financial: async () => (await api.get('/analytics/financial/summary')).data,
  delivery: async () => (await api.get('/analytics/delivery/summary')).data,
};

export const resourceApi = {
  vehicles: (params) => api.get('/vehicles', { params }).then((r) => r.data),
  drivers: (params) => api.get('/drivers', { params }).then((r) => r.data),
  customers: (params) => api.get('/customers', { params }).then((r) => r.data),
  shipments: (params) => api.get('/shipments', { params }).then((r) => r.data),
  routes: (params) => api.get('/routes', { params }).then((r) => r.data),
  notifications: (params) => api.get('/notifications', { params }).then((r) => r.data),
  readNotification: (id) => api.patch(`/notifications/${id}/read`).then((r) => r.data),
  readAllNotifications: () => api.patch('/notifications/read-all').then((r) => r.data),
  maintenanceForVehicle: (vehicleId, params) => api.get(`/vehicles/${vehicleId}/maintenance-records`, { params }).then((r) => r.data),
  maintenanceStart: (id) => api.patch(`/maintenance-records/${id}/start`).then((r) => r.data),
  maintenanceCancel: (id) => api.patch(`/maintenance-records/${id}/cancel`).then((r) => r.data),
  latestTracking: (shipmentId) => api.get(`/shipments/${shipmentId}/tracking-events/latest`).then((r) => r.data),
  shipmentById: (shipmentId) => api.get(`/shipments/${shipmentId}`).then((r) => r.data),
  notificationPreferences: () => api.get('/notification-preferences').then((r) => r.data),
  updateNotificationPreference: (payload) => api.put('/notification-preferences', payload).then((r) => r.data),
};
