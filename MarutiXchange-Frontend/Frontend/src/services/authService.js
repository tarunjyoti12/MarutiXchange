import { authApi } from './api';

// Register new user
export async function register(data) {
  const res = await authApi.post('/api/v1/users/register', data);
  return res.data;
}

// Login
export async function login(email, password) {
  const res = await authApi.post('/api/v1/users/login', { email, password });
  if (res.data?.data?.token) {
    localStorage.setItem('mx-token', res.data.data.token);
    localStorage.setItem('mx-user', JSON.stringify(res.data.data.user));
  }
  return res.data;
}

// Send OTP
export async function sendOtp(email) {
  const res = await authApi.post('/api/v1/users/otp/send', { email });
  return res.data;
}

// Verify OTP
export async function verifyOtp(email, otp) {
  const res = await authApi.post('/api/v1/users/otp/verify', { email, otp });
  return res.data;
}

// Logout
export function logout() {
  localStorage.removeItem('mx-token');
  localStorage.removeItem('mx-user');
}

// Get current user from localStorage
export function getCurrentUser() {
  const u = localStorage.getItem('mx-user');
  return u ? JSON.parse(u) : null;
}

// Check if logged in
export function isLoggedIn() {
  return !!localStorage.getItem('mx-token');
}

// Get user by ID
export async function getUserById(id) {
  const res = await authApi.get(`/api/v1/users/${id}`);
  return res.data;
}