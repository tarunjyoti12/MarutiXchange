import { carApi } from './api';

export async function getAllCars(filters = {}) {
  const res = await carApi.get('/api/v1/listings', { params: filters });
  return res.data;
}

export async function getCarById(id) {
  const res = await carApi.get(`/api/v1/listings/${id}`);
  return res.data;
}

export async function searchCars(params = {}) {
  const res = await carApi.get('/api/v1/search', { params });
  return res.data;
}

export async function createListing(listingData) {
  const res = await carApi.post('/api/v1/listings', listingData);
  return res.data;
}

export async function getMyListings(sellerId) {
  const res = await carApi.get(`/api/v1/listings/seller/${sellerId}`);
  return res.data;
}

export async function getSellerAnalytics(sellerId) {
  const res = await carApi.get(`/api/v1/analytics/seller/${sellerId}`);
  return res.data;
}