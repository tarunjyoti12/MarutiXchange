import api from './api';

// Get RC transfer by order ID
export async function getRcTransferByOrder(orderId) {
  const res = await api.get(`/api/v1/rc-transfers/order/${orderId}`);
  return res.data;
}

// Get all RC transfers for a user
export async function getMyRcTransfers(userId) {
  const res = await api.get(`/api/v1/rc-transfers/user/${userId}`);
  return res.data;
}

// Initiate RC transfer
export async function initiateRcTransfer(data) {
  const res = await api.post('/api/v1/rc-transfers', data);
  return res.data;
}

// Upload RC document
export async function uploadDocument(transferId, documentType, file, requesterId) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('documentType', documentType);
  formData.append('requesterId', requesterId);
  const res = await api.post(
    `/api/v1/rc-transfers/${transferId}/documents`,
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  );
  return res.data;
}

// Cancel RC transfer
export async function cancelRcTransfer(transferId, reason, requesterId) {
  const res = await api.put(`/api/v1/rc-transfers/${transferId}/cancel`, null, {
    params: { reason, requesterId },
  });
  return res.data;
}
