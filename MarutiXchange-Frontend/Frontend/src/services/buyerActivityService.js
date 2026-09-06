import axios from 'axios';

// Buyer Activity microservice — port 8073
export const buyerActivityApi = axios.create({
  baseURL: import.meta.env.VITE_BUYER_ACTIVITY_API_URL || 'http://localhost:8073',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

// Attach JWT token to every request
buyerActivityApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('mx-token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Fetch buyer activity for a seller
 * GET /api/v1/buyer-activity/seller/{sellerId}?days=7
 */
export async function getBuyerActivity(sellerId, days = 7) {
  const response = await buyerActivityApi.get(
    `/api/v1/buyer-activity/seller/${sellerId}`,
    { params: { days } }
  );
  return response;
}

/**
 * Reply to a buyer activity item
 * POST /api/v1/buyer-activity/{activityId}/reply
 */
export async function replyToBuyerActivity(activityId, message) {
  const response = await buyerActivityApi.post(
    `/api/v1/buyer-activity/${activityId}/reply`,
    { message }
  );
  return response;
}