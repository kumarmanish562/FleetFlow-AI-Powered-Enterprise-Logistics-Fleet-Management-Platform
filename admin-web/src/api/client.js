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
  dashboard: async (params) => (await api.get('/analytics/dashboard', { params })).data,
  shipments: async (params) => (await api.get('/analytics/shipments/summary', { params })).data,
  fleet: async (params) => (await api.get('/analytics/fleet/summary', { params })).data,
  drivers: async () => (await api.get('/analytics/drivers/summary')).data,
  financial: async (params) => (await api.get('/analytics/financial/summary', { params })).data,
  delivery: async (params) => (await api.get('/analytics/delivery/summary', { params })).data,
  shipmentTrends: async (params) => (await api.get('/analytics/shipments/trends', { params })).data,
  shipmentDistribution: async (params) => (await api.get('/analytics/shipments/status-distribution', { params })).data,
  vehicleStatus: async () => (await api.get('/analytics/fleet/vehicle-status')).data,
  revenueTrends: async (params) => (await api.get('/analytics/financial/revenue-trends', { params })).data,
  maintenanceSummary: async (params) => (await api.get('/analytics/maintenance/summary', { params })).data,
  driverPerformance: async (params) => (await api.get('/analytics/drivers/performance', { params })).data,
  driverPerformanceChart: async (params) => (await api.get('/analytics/drivers/performance-chart', { params })).data,
};

export const resourceApi = {
  vehicles: (params) => api.get('/vehicles', { params }).then((r) => r.data),
  vehicle: (id) => api.get(`/vehicles/${id}`).then((r) => r.data),
  createVehicle: (body) => api.post('/vehicles', body).then((r) => r.data),
  updateVehicle: (id, body) => api.put(`/vehicles/${id}`, body).then((r) => r.data),
  updateVehicleStatus: (id, status) => api.patch(`/vehicles/${id}/status`, { status }).then((r) => r.data),
  deleteVehicle: (id) => api.delete(`/vehicles/${id}`),
  vehicleTypes: (params) => api.get('/vehicle-types', { params }).then((r) => r.data),
  drivers: (params) => api.get('/drivers', { params }).then((r) => r.data),
  driver: (id) => api.get(`/drivers/${id}`).then((r) => r.data),
  createDriver: (body) => api.post('/drivers', body).then((r) => r.data),
  updateDriver: (id, body) => api.put(`/drivers/${id}`, body).then((r) => r.data),
  updateDriverStatus: (id, employmentStatus) => api.patch(`/drivers/${id}/status`, { employmentStatus }).then((r) => r.data),
  updateDriverAvailability: (id, availability) => api.patch(`/drivers/${id}/availability`, { availability }).then((r) => r.data),
  assignDriverVehicle: (driverId, vehicleId) => api.post(`/drivers/${driverId}/assign-vehicle/${vehicleId}`).then((r) => r.data),
  unassignDriverVehicle: (driverId) => api.delete(`/drivers/${driverId}/vehicle`).then((r) => r.data),
  deactivateDriver: (id) => api.delete(`/drivers/${id}`),
  customers: (params) => api.get('/customers', { params }).then((r) => r.data),
  customer: (id) => api.get(`/customers/${id}`).then((r) => r.data),
  createCustomer: (body) => api.post('/customers', body).then((r) => r.data),
  updateCustomer: (id, body) => api.put(`/customers/${id}`, body).then((r) => r.data),
  updateCustomerStatus: (id, status) => api.patch(`/customers/${id}/status`, { status }).then((r) => r.data),
  shipments: (params) => api.get('/shipments', { params }).then((r) => r.data),
  createShipment: (body) => api.post('/shipments', body).then((r) => r.data),
  shipmentStatus: (id, body) => api.patch(`/shipments/${id}/status`, body).then((r) => r.data),
  assignShipment: (id, body) => api.patch(`/shipments/${id}/assign`, body).then((r) => r.data),
  confirmShipment: (id) => api.patch(`/shipments/${id}/confirm`).then((r) => r.data),
  cancelShipment: (id) => api.patch(`/shipments/${id}/cancel`).then((r) => r.data),
  shipmentTracking: (id, params) => api.get(`/shipments/${id}/tracking-events`, { params }).then((r) => r.data),
  routes: (params) => api.get('/routes', { params }).then((r) => r.data),
  route: (id) => api.get(`/routes/${id}`).then((r) => r.data),
  createRoute: (body) => api.post('/routes', body).then((r) => r.data),
  notifications: (params) => api.get('/notifications', { params }).then((r) => r.data),
  readNotification: (id) => api.patch(`/notifications/${id}/read`).then((r) => r.data),
  readAllNotifications: () => api.patch('/notifications/read-all').then((r) => r.data),
  maintenanceForVehicle: (vehicleId, params) => api.get(`/vehicles/${vehicleId}/maintenance-records`, { params }).then((r) => r.data),
  createMaintenance: (vehicleId, body) => api.post(`/vehicles/${vehicleId}/maintenance-records`, body).then((r) => r.data),
  upcomingMaintenance: (params) => api.get('/maintenance-records/upcoming', { params }).then((r) => r.data),
  overdueMaintenance: () => api.get('/maintenance-records/overdue').then((r) => r.data),
  completeMaintenance: (id, body) => api.patch(`/maintenance-records/${id}/complete`, body).then((r) => r.data),
  maintenanceStart: (id) => api.patch(`/maintenance-records/${id}/start`).then((r) => r.data),
  maintenanceCancel: (id) => api.patch(`/maintenance-records/${id}/cancel`).then((r) => r.data),
  latestTracking: (shipmentId) => api.get(`/shipments/${shipmentId}/tracking-events/latest`).then((r) => r.data),
  shipmentById: (shipmentId) => api.get(`/shipments/${shipmentId}`).then((r) => r.data),
  invoice: (id) => api.get(`/invoices/${id}`).then((r) => r.data),
  payment: (invoiceId, paymentId) => api.get(`/invoices/${invoiceId}/payments/${paymentId}`).then((r) => r.data),
  createInvoice: (body) => api.post('/invoices', body).then((r) => r.data),
  createPayment: (invoiceId, body) => api.post(`/invoices/${invoiceId}/payments`, body).then((r) => r.data),
  delivery: (id) => api.get(`/deliveries/${id}`).then((r) => r.data),
  startDelivery: (id) => api.patch(`/deliveries/${id}/start`).then((r) => r.data),
  completeDelivery: (id, body) => api.patch(`/deliveries/${id}/complete`, body).then((r) => r.data),
  failDelivery: (id, body) => api.patch(`/deliveries/${id}/fail`, body).then((r) => r.data),
  rescheduleDelivery: (id, body) => api.patch(`/deliveries/${id}/reschedule`, body).then((r) => r.data),
  notificationPreferences: () => api.get('/notification-preferences').then((r) => r.data),
  updateNotificationPreference: (payload) => api.put('/notification-preferences', payload).then((r) => r.data),
};
