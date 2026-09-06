import { useState } from 'react';
import { useApp } from '../../context/AppContext';

export default function ProfileModal() {
  const { closeModal, user, setUser, pushToast } = useApp();
  const [name,   setName]   = useState(user?.name  || '');
  const [phone,  setPhone]  = useState(user?.phone || user?.phoneNumber || '');
  const [email,  setEmail]  = useState(user?.email || '');
  const [saving, setSaving] = useState(false);
  const initials = name.trim().split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2) || 'U';

  async function handleSave() {
    if (!name.trim()) { pushToast('Name cannot be empty', 'err'); return; }
    setSaving(true);
    try {
      const updated = { ...user, name: name.trim(), phone: phone.trim(), email: email.trim() };
      setUser(updated);
      localStorage.setItem('mx-user', JSON.stringify(updated));
      pushToast('Profile updated!', 'ok');
      closeModal();
    } catch { pushToast('Failed to update profile', 'err'); }
    finally { setSaving(false); }
  }

  return (
    <>
      <div onClick={closeModal} style={{ position: 'fixed', inset: 0, zIndex: 99998, background: 'rgba(0,0,0,.6)', backdropFilter: 'blur(5px)' }} />
      <div style={{ position: 'fixed', inset: 0, zIndex: 99999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 20 }}>
        <div style={{ width: '100%', maxWidth: 440, background: 'var(--cbg)', borderRadius: 20, overflow: 'hidden', boxShadow: '0 32px 80px rgba(0,0,0,.3)' }}>
          <div style={{ background: 'linear-gradient(135deg, #1449C0, #0f3694)', padding: '24px 24px 20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h2 style={{ fontSize: 18, fontWeight: 800, color: '#fff', margin: 0 }}>Edit Profile</h2>
              <button onClick={closeModal} style={{ width: 30, height: 30, borderRadius: '50%', background: 'rgba(255,255,255,.2)', border: 'none', color: '#fff', fontSize: 18, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>x</button>
            </div>
            <div style={{ display: 'flex', justifyContent: 'center' }}>
              <div style={{ width: 68, height: 68, borderRadius: '50%', background: 'rgba(255,255,255,.25)', border: '3px solid rgba(255,255,255,.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 26, fontWeight: 900, color: '#fff' }}>{initials}</div>
            </div>
          </div>
          <div style={{ padding: 24 }}>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', fontSize: 11, fontWeight: 700, color: 'var(--tx3)', letterSpacing: '.08em', textTransform: 'uppercase', marginBottom: 7 }}>Full Name</label>
              <input type="text" value={name} placeholder="Your full name" onChange={e => setName(e.target.value)} style={{ width: '100%', padding: '11px 14px', fontSize: 14, border: '1.5px solid var(--bd1)', borderRadius: 10, background: 'var(--bg1)', color: 'var(--tx1)', outline: 'none', boxSizing: 'border-box' }} />
            </div>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', fontSize: 11, fontWeight: 700, color: 'var(--tx3)', letterSpacing: '.08em', textTransform: 'uppercase', marginBottom: 7 }}>Email Address</label>
              <input type="email" value={email} placeholder="you@example.com" onChange={e => setEmail(e.target.value)} style={{ width: '100%', padding: '11px 14px', fontSize: 14, border: '1.5px solid var(--bd1)', borderRadius: 10, background: 'var(--bg1)', color: 'var(--tx1)', outline: 'none', boxSizing: 'border-box' }} />
            </div>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', fontSize: 11, fontWeight: 700, color: 'var(--tx3)', letterSpacing: '.08em', textTransform: 'uppercase', marginBottom: 7 }}>Phone Number</label>
              <input type="tel" value={phone} placeholder="10-digit mobile" onChange={e => setPhone(e.target.value.replace(/\D/g,'').slice(0,10))} style={{ width: '100%', padding: '11px 14px', fontSize: 14, border: '1.5px solid var(--bd1)', borderRadius: 10, background: 'var(--bg1)', color: 'var(--tx1)', outline: 'none', boxSizing: 'border-box' }} />
            </div>
            <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
              <button onClick={closeModal} style={{ flex: 1, padding: 13, fontSize: 14, fontWeight: 700, background: 'var(--bg2)', color: 'var(--tx2)', border: '1.5px solid var(--bd1)', borderRadius: 12, cursor: 'pointer' }}>Cancel</button>
              <button onClick={handleSave} disabled={saving} style={{ flex: 2, padding: 13, fontSize: 14, fontWeight: 700, background: saving ? '#cbd5e1' : '#1449C0', color: '#fff', border: 'none', borderRadius: 12, cursor: saving ? 'not-allowed' : 'pointer' }}>{saving ? 'Saving...' : 'Save Changes'}</button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}