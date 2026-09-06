import axios from 'axios';

// =============================================
// All API calls go through the API Gateway
// Gateway runs on port 8064 locally
// In production nginx proxies /api/ to gateway
// =============================================

const GATEWAY_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8064';

// Single axios instance — everything goes through gateway
export const authApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

export const carApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

export const bidApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

export const watchlistApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

export const testDriveApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

export const rcApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

export const orderApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

export const paymentApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

// Add JWT token to all requests automatically
function addAuthInterceptor(apiInstance) {
  apiInstance.interceptors.request.use((config) => {
    const token = localStorage.getItem('mx-token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
  return apiInstance;
}

addAuthInterceptor(authApi);
addAuthInterceptor(carApi);
addAuthInterceptor(bidApi);
addAuthInterceptor(watchlistApi);
addAuthInterceptor(testDriveApi);
addAuthInterceptor(rcApi);
addAuthInterceptor(orderApi);
addAuthInterceptor(paymentApi);

export default authApi;
