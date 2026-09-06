import { useEffect, useRef, useState } from 'react';
import { useApp } from '../../context/AppContext';
import { login, register } from '../../services/authService';

export default function LoginModal() {
  const { closeModal, loginUser, pushToast } = useApp();
  const [mode,  setMode]  = useState('login'); // 'login' | 'register'
  const [loading, setLoading] = useState(false);

  // Login form
  const [email,    setEmail]    = useState('');
  const [password, setPassword] = useState('');
  const [showForgot, setShowForgot] = useState(false);
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotSent, setForgotSent] = useState(false);

  // Register form
  const [regName,  setRegName]  = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPhone, setRegPhone] = useState('');
  const [regPass,  setRegPass]  = useState('');

  async function handleLogin() {
    if (!email || !password) {
      pushToast('Please enter email and password', 'err');
      return;
    }
    setLoading(true);
    try {
      const res = await login(email, password);
      const userData = res?.data?.user || res?.user;
      loginUser(userData);
      pushToast(`Welcome back, ${userData?.name || 'User'}!`, 'ok');
      closeModal();
    } catch (err) {
      pushToast(err?.response?.data?.message || 'Invalid credentials', 'err');
    } finally {
      setLoading(false);
    }
  }

  async function handleForgot() {
    if (!forgotEmail.trim()) { pushToast('Enter your email address', 'err'); return; }
    // In production, calls POST /api/users/forgot-password
    setForgotSent(true);
    pushToast('Reset link sent to ' + forgotEmail, 'ok');
  }

  async function handleRegister() {
    if (!regName || !regEmail || !regPass || !regPhone) {
      pushToast('Please fill all fields', 'err');
      return;
    }
    setLoading(true);
    try {
      await register({
        name: regName,
        email: regEmail,
        password: regPass,
        phoneNumber: regPhone,
        role: 'BUYER',
      });
      pushToast('Account created! Please login.', 'ok');
      setMode('login');
      setEmail(regEmail);
    } catch (err) {
      pushToast(err?.response?.data?.message || 'Registration failed', 'err');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      onClick={closeModal}
      style={{
        position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
        zIndex: 999999,
        background: 'rgba(5,10,25,.65)', backdropFilter: 'blur(6px)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        padding: 20, boxSizing: 'border-box',
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          display: 'flex', width: '100%', maxWidth: 860,
          borderRadius: 24, overflow: 'hidden',
          boxShadow: '0 32px 80px rgba(0,0,0,.4)',
          maxHeight: '90vh',
        }}
      >
        {/* LEFT PANEL */}
        <div style={{
          width: 300, flexShrink: 0,
          background: 'linear-gradient(145deg, #1449C0 0%, #0f3694 100%)',
          padding: '48px 36px',
          display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 48 }}>
              <div style={{ width: 36, height: 36, borderRadius: 10, background: 'rgba(255,255,255,.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <svg width="20" height="20" viewBox="0 0 20 20" fill="white">
                  <path d="M10 2L3 7v10l7 1 7-1V7L10 2z" fillOpacity=".95" />
                </svg>
              </div>
              <span style={{ fontSize: 18, fontWeight: 800, color: '#fff' }}>MarutiXchange</span>
            </div>
            <h2 style={{ fontSize: 24, fontWeight: 900, color: '#fff', lineHeight: 1.2, margin: '0 0 28px' }}>
              India's Most Trusted<br />Maruti Marketplace
            </h2>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 12 }}>
              {['12,000+ Verified Listings', 'Live Auction Bidding', 'RC Transfer Assistance', '200-Point Inspection'].map((item) => (
                <li key={item} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#fff', flexShrink: 0 }} />
                  <span style={{ fontSize: 13, color: 'rgba(255,255,255,.9)', fontWeight: 500 }}>{item}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* RIGHT PANEL */}
        <div style={{
          flex: 1, background: '#fff',
          padding: '40px 44px',
          display: 'flex', flexDirection: 'column',
          overflowY: 'auto', position: 'relative',
        }}>
          <button onClick={closeModal} style={{ position: 'absolute', top: 16, right: 16, width: 32, height: 32, borderRadius: '50%', background: '#f0f0f0', border: 'none', fontSize: 16, cursor: 'pointer', color: '#666' }}>×</button>

          {/* Tabs */}
          <div style={{ display: 'flex', gap: 0, marginBottom: 28, background: '#f5f7ff', borderRadius: 12, padding: 4 }}>
            {['login', 'register'].map((m) => (
              <button key={m} onClick={() => setMode(m)} style={{
                flex: 1, padding: '10px', fontSize: 14, fontWeight: 700,
                background: mode === m ? '#1449C0' : 'transparent',
                color: mode === m ? '#fff' : '#888',
                border: 'none', borderRadius: 10, cursor: 'pointer',
                textTransform: 'capitalize', transition: 'all .2s',
              }}>
                {m === 'login' ? 'Sign In' : 'Register'}
              </button>
            ))}
          </div>

          {/* LOGIN FORM */}
          {mode === 'login' && (
            <>
              <h2 style={{ fontSize: 24, fontWeight: 800, color: '#1a1a2e', margin: '0 0 20px' }}>Sign in to your account</h2>
              <Field label="Email Address">
                <input type="email" placeholder="you@example.com" value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
                  style={inputStyle} />
              </Field>
              <Field label="Password">
                <input type="password" placeholder="Your password" value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
                  style={inputStyle} />
              </Field>

              {/* Forgot Password */}
              <div style={{ textAlign: 'right', marginTop: -8, marginBottom: 12 }}>
                <button onClick={() => setShowForgot(!showForgot)} style={{ background: 'none', border: 'none', color: 'var(--ac)', fontSize: 12, cursor: 'pointer', fontWeight: 600 }}>
                  Forgot Password?
                </button>
              </div>

              {showForgot && (
                <div style={{ background: 'var(--bg2)', border: '1px solid var(--bd1)', borderRadius: 12, padding: 16, marginBottom: 16 }}>
                  {!forgotSent ? (
                    <>
                      <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--tx1)', marginBottom: 10 }}>Reset your password</div>
                      <input type="email" placeholder="Enter your registered email" value={forgotEmail}
                        onChange={(e) => setForgotEmail(e.target.value)}
                        style={{ ...inputStyle, marginBottom: 10 }} />
                      <div style={{ display: 'flex', gap: 8 }}>
                        <button onClick={() => setShowForgot(false)} style={{ flex: 1, padding: '10px', fontSize: 12, fontWeight: 700, background: 'none', border: '1px solid var(--bd1)', borderRadius: 8, cursor: 'pointer', color: 'var(--tx2)' }}>Cancel</button>
                        <button onClick={handleForgot} style={{ flex: 2, padding: '10px', fontSize: 12, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }}>Send Reset Link</button>
                      </div>
                    </>
                  ) : (
                    <div style={{ textAlign: 'center', padding: '8px 0' }}>
                      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2" style={{ display: 'block', margin: '0 auto 8px' }}><polyline points="20 6 9 17 4 12"/></svg>
                      <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--tx1)' }}>Reset link sent!</div>
                      <div style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 4 }}>Check your email inbox</div>
                    </div>
                  )}
                </div>
              )}

              <button onClick={handleLogin} disabled={loading} style={primaryBtn}>
                {loading ? 'Signing in...' : 'Sign In →'}
              </button>
              <p style={{ fontSize: 12, color: '#aaa', textAlign: 'center', marginTop: 16 }}>
                Don't have an account?{' '}
                <button onClick={() => setMode('register')} style={{ background: 'none', border: 'none', color: '#1449C0', cursor: 'pointer', fontWeight: 700 }}>
                  Register here
                </button>
              </p>
            </>
          )}

          {/* REGISTER FORM */}
          {mode === 'register' && (
            <>
              <h2 style={{ fontSize: 24, fontWeight: 800, color: '#1a1a2e', margin: '0 0 20px' }}>Create your account</h2>
              <Field label="Full Name">
                <input type="text" placeholder="Your full name" value={regName}
                  onChange={(e) => setRegName(e.target.value)} style={inputStyle} />
              </Field>
              <Field label="Email Address">
                <input type="email" placeholder="you@example.com" value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)} style={inputStyle} />
              </Field>
              <Field label="Phone Number">
                <input type="tel" placeholder="10-digit mobile number" value={regPhone}
                  onChange={(e) => setRegPhone(e.target.value.replace(/\D/g, '').slice(0, 10))} style={inputStyle} />
              </Field>
              <Field label="Password">
                <input type="password" placeholder="Minimum 8 characters" value={regPass}
                  onChange={(e) => setRegPass(e.target.value)} style={inputStyle} />
              </Field>
              <button onClick={handleRegister} disabled={loading} style={primaryBtn}>
                {loading ? 'Creating account...' : 'Create Account →'}
              </button>
              <p style={{ fontSize: 12, color: '#aaa', textAlign: 'center', marginTop: 16 }}>
                Already have an account?{' '}
                <button onClick={() => setMode('login')} style={{ background: 'none', border: 'none', color: '#1449C0', cursor: 'pointer', fontWeight: 700 }}>
                  Sign in
                </button>
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

function Field({ label, children }) {
  return (
    <div style={{ marginBottom: 16 }}>
      <label style={{ display: 'block', fontSize: 11, fontWeight: 700, color: '#888', letterSpacing: '.08em', textTransform: 'uppercase', marginBottom: 8 }}>
        {label}
      </label>
      {children}
    </div>
  );
}

const inputStyle = {
  width: '100%', padding: '13px 16px', fontSize: 14,
  border: '1.5px solid #e0e4f0', borderRadius: 12,
  background: '#f5f7ff', color: '#1a1a2e', outline: 'none',
  boxSizing: 'border-box',
};

const primaryBtn = {
  width: '100%', padding: '14px', fontSize: 15, fontWeight: 700,
  background: '#1449C0', color: '#fff',
  border: 'none', borderRadius: 14, cursor: 'pointer',
  marginTop: 4,
};