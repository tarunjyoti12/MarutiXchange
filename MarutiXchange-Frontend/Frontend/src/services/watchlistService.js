import { watchlistApi } from './api';

const STORAGE_KEY = 'mx-watchlist';

// ── localStorage helpers ──────────────────────────────────────────────────────
function readLocal() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
  } catch {
    return [];
  }
}

function writeLocal(items) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
}

// ── Public API ────────────────────────────────────────────────────────────────

export async function getWatchlist(userId) {
  try {
    const res = await watchlistApi.get(
      '/api/v1/watchlist/users/' + userId,
      { params: { itemType: 'WATCHLIST', page: 0, size: 50 } }
    );
    const items = res.data?.content || res.data?.data || [];
    if (items.length > 0) {
      // Sync backend data into localStorage
      writeLocal(items);
      return { data: items };
    }
    // Backend returned empty — fall back to local
    return { data: readLocal() };
  } catch (err) {
    console.log('Using local watchlist:', err.message);
    return { data: readLocal() };
  }
}

export async function addToWatchlist(userId, carData) {
  // Save to localStorage immediately — persists across page refreshes
  const existing = readLocal();
  const alreadyExists = existing.find((w) => w.carListingId === carData.carListingId);
  if (!alreadyExists) {
    const newItem = {
      id:          carData.carListingId,
      carListingId: carData.carListingId,
      carName:     carData.carName,
      carPrice:    carData.carPrice,
      carImgUrl:   carData.carImgUrl,
      carBrand:    carData.carBrand,
      carYear:     carData.carYear,
      itemType:    'WATCHLIST',
    };
    writeLocal([...existing, newItem]);
  }

  // Also try backend
  try {
    const res = await watchlistApi.post(
      '/api/v1/watchlist/users/' + userId,
      {
        carListingId: Number(carData.carListingId),
        carName:      carData.carName,
        carPrice:     carData.carPrice,
        carImgUrl:    carData.carImgUrl,
        carBrand:     carData.carBrand,
        carYear:      carData.carYear,
        itemType:     'WATCHLIST',
      }
    );
    return res.data;
  } catch (err) {
    console.log('Saved to local watchlist only:', err.message);
    return { data: readLocal().find((w) => w.carListingId === carData.carListingId) };
  }
}

export async function removeFromWatchlist(userId, carListingId) {
  // Remove from localStorage immediately
  const updated = readLocal().filter((w) => w.carListingId !== carListingId);
  writeLocal(updated);

  // Also try backend
  try {
    const res = await watchlistApi.delete(
      '/api/v1/watchlist/users/' + userId + '/cars/' + carListingId,
      { params: { itemType: 'WATCHLIST' } }
    );
    return res.data;
  } catch (err) {
    console.log('Removed from local watchlist only:', err.message);
    return { success: true };
  }
}

export async function isWatched(userId, carListingId) {
  // Check localStorage first — instant, no network call
  const local = readLocal();
  if (local.find((w) => w.carListingId === carListingId)) return true;

  // Then check backend
  try {
    const res = await watchlistApi.get(
      '/api/v1/watchlist/users/' + userId + '/cars/' + carListingId + '/status'
    );
    return res.data?.data === true;
  } catch (err) {
    return false;
  }
}

// Used by DashboardPage to load watchlist tab
export function getLocalWatchlist() {
  return readLocal();
}