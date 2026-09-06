import axios from 'axios';

// Direct to bidding-service with JWT auth
const bidApi = axios.create({
  baseURL: 'http://localhost:8086',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

// Attach JWT token automatically
bidApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('mx-token');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }
  return config;
});

// Get all live auctions
export async function getLiveAuctions() {
  const res = await bidApi.get('/api/v1/auctions/live');
  return res.data;
}

// Get all auctions
export async function getAllAuctions() {
  const res = await bidApi.get('/api/v1/auctions');
  return res.data;
}

// Place a bid — goes through rules engine on backend
export async function placeBid(auctionId, bidAmount, bidderName) {
  const res = await bidApi.post('/api/v1/bids', {
    auctionId,
    bidAmount,
    bidderName,
  });
  return res.data;
}

// Get my bids
export async function getMyBids() {
  const res = await bidApi.get('/api/v1/bids/my');
  return res.data;
}

// Get bids for auction
export async function getBidsByAuction(auctionId) {
  const res = await bidApi.get('/api/v1/bids/auction/' + auctionId);
  return res.data;
}

// Buy now
export async function buyNow(auctionId, buyerName) {
  const res = await bidApi.post('/api/v1/bids/buy-now', {
    auctionId,
    buyerName,
  });
  return res.data;
}
