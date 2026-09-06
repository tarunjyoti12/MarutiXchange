import { testDriveApi } from './api';

export async function bookTestDrive(data) {
  const res = await testDriveApi.post('/api/v1/test-drives', data);
  return res.data;
}

export async function getMyTestDrives(userId) {
  const res = await testDriveApi.get(`/api/v1/test-drives/user/${userId}`);
  return res.data;
}

export async function cancelTestDrive(testDriveId, reason) {
  const res = await testDriveApi.put(
    `/api/v1/test-drives/${testDriveId}/cancel`,
    { reason }
  );
  return res.data;
}