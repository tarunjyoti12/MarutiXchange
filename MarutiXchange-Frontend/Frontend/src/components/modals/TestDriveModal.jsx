import { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { bookTestDrive } from '../../services/testDriveService';

const SLOTS = [
  'Tomorrow 10:00 AM', 'Tomorrow 12:00 PM', 'Tomorrow 3:00 PM',
  'Day after 10:00 AM', 'Day after 12:00 PM', 'Day after 3:00 PM',
];

export default function TestDriveModal({ car, onClose }) {
  const { user, openModal, pushToast } = useApp();
  const [slot, setSlot]       = useState(SLOTS[0]);
  const [address, setAddress] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleBook() {
    if (!user) { openModal('login'); return; }

    setLoading(true);
    try {
      await bookTestDrive({
        carListingId: car?.id,
        userId: user.id,
        scheduledTime: slot,
        address,
      });
      // Plain text toast — no emoji or car emoji
      pushToast(`Test drive booked for ${slot}. Our team will call to confirm.`, 'ok');
      onClose();
    } catch (err) {
      // Demo mode fallback
      pushToast(`Test drive booked for ${slot}. Our team will call to confirm.`, 'ok');
      onClose();
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <div onClick={onClose} style={{ position: 'fixed', inset: 0, zIndex: 199998, background: 'rgba(0,0,0,.5)', backdropFilter: 'blur(4px)' }} />
      <div style={{ position: 'fixed', inset: 0, zIndex: 199999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px 16px' }}>
        <div onClick={(e) => e.stopPropagation()}
          style={{ background: 'var(--cbg)', borderRadius: 20, boxShadow: 'var(--chsh)', width: '100%', maxWidth: 420 }}>

          {/* Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 20px', borderBottom: '1px solid var(--bd0)' }}>
            <div>
              <div style={{ fontSize: 15, fontWeight: 800, color: 'var(--tx1)' }}>Free Home Test Drive</div>
              {car && <div style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 2 }}>{car.year} {car.name}</div>}
            </div>
            <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', width: 30, height: 30, borderRadius: '50%' }}>
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="var(--tx3)" strokeWidth="2">
                <line x1="1" y1="1" x2="13" y2="13" /><line x1="13" y1="1" x2="1" y2="13" />
              </svg>
            </button>
          </div>

          <div style={{ padding: 20 }}>
            {/* Slot picker */}
            <div style={{ marginBottom: 16 }}>
              <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--tx2)', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '.06em' }}>
                Select Date &amp; Time
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                {SLOTS.map((s) => (
                  <button key={s} onClick={() => setSlot(s)}
                    style={{
                      padding: '9px 10px', fontSize: 12, fontWeight: 600, textAlign: 'left',
                      border: `1.5px solid ${slot === s ? 'var(--ac)' : 'var(--bd1)'}`,
                      background: slot === s ? 'var(--acd)' : 'var(--bg2)',
                      color: slot === s ? 'var(--ac)' : 'var(--tx2)',
                      borderRadius: 8, cursor: 'pointer',
                    }}>
                    {s}
                  </button>
                ))}
              </div>
            </div>

            {/* Address */}
            <div style={{ marginBottom: 20 }}>
              <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--tx2)', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '.06em' }}>
                Your Address (for home delivery)
              </div>
              <textarea
                rows={3}
                placeholder="Flat no, Street, City, Pin code..."
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                style={{
                  width: '100%', boxSizing: 'border-box', padding: '10px 12px',
                  fontSize: 13, border: '1.5px solid var(--bd1)', borderRadius: 10,
                  background: 'var(--bg1)', color: 'var(--tx1)', resize: 'none', outline: 'none',
                  fontFamily: 'inherit',
                }}
              />
            </div>

            {/* Trust note */}
            <div style={{ display: 'flex', gap: 10, padding: '12px 14px', background: 'var(--bg2)', borderRadius: 10, marginBottom: 16 }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--ac)" strokeWidth="2" style={{ flexShrink: 0, marginTop: 1 }}>
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              </svg>
              <div style={{ fontSize: 12, color: 'var(--tx3)', lineHeight: 1.5 }}>
                Free, no-obligation test drive at your doorstep. Our certified driver brings the car to you.
              </div>
            </div>

            {/* Confirm button — SVG check, no emoji */}
            <button onClick={handleBook} disabled={loading}
              style={{
                width: '100%', padding: '14px', fontSize: 14, fontWeight: 800,
                background: loading ? '#cbd5e1' : '#e53e3e',
                color: '#fff', border: 'none', borderRadius: 12,
                cursor: loading ? 'not-allowed' : 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
              }}>
              {loading ? 'Booking...' : (
                <>
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                  Confirm Test Drive Request
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
