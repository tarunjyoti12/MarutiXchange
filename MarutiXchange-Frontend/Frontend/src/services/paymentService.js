import { paymentApi } from './api';

// ── Existing payment methods ──────────────────────────────────────────────────
export async function initiatePayment(data) {
  const res = await paymentApi.post('/api/v1/payments/initiate', data);
  return res.data;
}

export async function confirmPayment(transactionId) {
  const res = await paymentApi.patch(`/api/v1/payments/${transactionId}/confirm`);
  return res.data;
}

export async function getPayment(transactionId) {
  const res = await paymentApi.get(`/api/v1/payments/${transactionId}`);
  return res.data;
}

export async function calculateEmi(data) {
  const res = await paymentApi.post('/api/v1/payments/emi/calculate', data);
  return res.data;
}

// ── Razorpay methods ──────────────────────────────────────────────────────────

// Step 1: Create Razorpay order from backend
export async function createRazorpayOrder(data) {
  const res = await paymentApi.post('/api/v1/razorpay/create-order', data);
  return res.data;
}

// Step 2: Verify payment signature after successful payment
export async function verifyRazorpayPayment(data) {
  const res = await paymentApi.post('/api/v1/razorpay/verify-payment', data);
  return res.data;
}

// Step 3: Notify backend of payment failure
export async function razorpayPaymentFailed(data) {
  const res = await paymentApi.post('/api/v1/razorpay/payment-failed', data);
  return res.data;
}