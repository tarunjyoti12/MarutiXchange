import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { getCurrentUser, logout as authLogout } from '../services/authService';

const AppCtx = createContext(null);
const PAGES = ['home', 'buy', 'bidding', 'dashboard'];

export function AppProvider({ children }) {
  const [segment,     setSegmentState] = useState(() => localStorage.getItem('mm-seg') || 'arena');
  const [page,        setPage]         = useState('home');
  const [user,        setUser]         = useState(() => getCurrentUser());
  const [modal,       setModal]        = useState(null);
  const [toasts,      setToasts]       = useState([]);
  const [searchQuery, setSearchQuery]  = useState('');   // ← NEW: shared search state

  useEffect(() => {
    document.documentElement.setAttribute('data-segment', segment);
  }, [segment]);

  const setSegment = useCallback((s) => {
    setSegmentState(s);
    localStorage.setItem('mm-seg', s);
  }, []);

  const navigate = useCallback((p) => {
    if (!PAGES.includes(p)) return;
    setPage(p);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  const openModal  = useCallback((type, payload) => setModal({ type, payload }), []);
  const closeModal = useCallback(() => setModal(null), []);

  const pushToast = useCallback((msg, kind = 'info') => {
    const id = Date.now() + Math.random();
    setToasts((t) => [...t, { id, msg, kind }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 4000);
  }, []);

  const loginUser = useCallback((userData) => {
    localStorage.removeItem('mx-my-bids');
    localStorage.removeItem('mx-watchlist');
    setUser(userData);
  }, []);

  const logoutUser = useCallback(() => {
    authLogout();
    setUser(null);
    localStorage.removeItem('mx-my-bids');
    localStorage.removeItem('mx-watchlist');
    pushToast('Logged out successfully', 'info');
    navigate('home');
  }, [pushToast, navigate]);

  const value = useMemo(() => ({
    segment, setSegment,
    page, navigate,
    user, setUser, loginUser, logoutUser,
    modal, openModal, closeModal,
    toasts, pushToast,
    searchQuery, setSearchQuery,   // ← expose to all components
  }), [segment, setSegment, page, navigate, user, setUser, loginUser, logoutUser,
       modal, openModal, closeModal, toasts, pushToast, searchQuery, setSearchQuery]);

  return <AppCtx.Provider value={value}>{children}</AppCtx.Provider>;
}

export function useApp() {
  const ctx = useContext(AppCtx);
  if (!ctx) throw new Error('useApp must be used within AppProvider');
  return ctx;
}