import { useEffect, useState } from 'react';
import { useApp } from '../context/AppContext';
import { rcApi } from '../services/api';

const STATUS_COLORS = {
  INITIATED:           '#f59e0b',
  DOCUMENTS_PENDING:   '#f59e0b',
  DOCUMENTS_SUBMITTED: '#3b82f6',
  RTO_SUBMITTED:       '#8b5cf6',
  RTO_PROCESSING:      '#8b5cf6',
  COMPLETED:           '#10b981',
  REJECTED:            '#ef4444',
  CANCELLED:           '#64748b',
};

const STATUS_LABELS = {
  INITIATED:           'Initiated',
  DOCUMENTS_PENDING:   'Documents Pending',
  DOCUMENTS_SUBMITTED: 'Documents Submitted',
  RTO_SUBMITTED:       'Submitted to RTO',
  RTO_PROCESSING:      'RTO Processing',
  COMPLETED:           'Completed',
  REJECTED:            'Rejected',
  CANCELLED:           'Cancelled',
};

const STEPS = [
  'Initiated',
  'Documents Submitted',
  'Submitted to RTO',
  'RTO Processing',
  'Completed',
];

function getProgress(status) {
  const map = {
    INITIATED: 10, DOCUMENTS_PENDING: 20,
    DOCUMENTS_SUBMITTED: 40, RTO_SUBMITTED: 60,
    RTO_PROCESSING: 80, COMPLETED: 100,
    REJECTED: 100, CANCELLED: 0,
  };
  return map[status] || 0;
}

function getStepIndex(status) {
  const map = {
    INITIATED: 0, DOCUMENTS_PENDING: 0,
    DOCUMENTS_SUBMITTED: 1, RTO_SUBMITTED: 2,
    RTO_PROCESSING: 3, COMPLETED: 4,
  };
  return map[status] ?? -1;
}

export default function RcTransferTab() {
  const { user, pushToast } = useApp();
  const [transfers, setTransfers] = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [showForm,  setShowForm]  = useState(false);
  const [form,      setForm]      = useState({ registrationNumber: '', orderId: '', rtoOffice: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!user) return;
    async function fetchTransfers() {
      setLoading(true);
      try {
        const res = await rcApi.get('/api/v1/rc-transfers/user/' + user.id);
        const items = res.data?.data?.content || res.data?.data || [];
        setTransfers(items);
      } catch (err) {
        console.log('RC Transfer fetch error:', err.message);
        setTransfers([]);
      } finally {
        setLoading(false);
      }
    }
    fetchTransfers();
  }, [user]);

  async function handleInitiate() {
    if (!form.registrationNumber.trim()) { pushToast('Registration number is required', 'err'); return; }
    setSubmitting(true);
    try {
      await rcApi.post('/api/v1/rc-transfers', {
        buyerId:            user.id,
        orderId:            form.orderId || null,
        registrationNumber: form.registrationNumber,
        rtoOffice:          form.rtoOffice || null,
      });
      pushToast('RC Transfer initiated successfully', 'ok');
      setShowForm(false);
      setForm({ registrationNumber: '', orderId: '', rtoOffice: '' });
      // Refresh list
      const res = await rcApi.get('/api/v1/rc-transfers/user/' + user.id);
      setTransfers(res.data?.data?.content || res.data?.data || []);
    } catch (err) {
      // Demo mode
      const demo = {
        id: Date.now(),
        registrationNumber: form.registrationNumber,
        rtoOffice: form.rtoOffice,
        status: 'INITIATED',
        expectedCompletionDate: new Date(Date.now() + 30 * 86400000).toISOString(),
      };
      setTransfers((prev) => [demo, ...prev]);
      pushToast('RC Transfer initiated (demo mode)', 'ok');
      setShowForm(false);
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 60, color: 'var(--tx3)', fontSize: 13 }}>Loading RC transfers...</div>;
  }

  return (
    <div>
      {/* Header row */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <div style={{ fontSize: 16, fontWeight: 800, color: 'var(--tx1)' }}>RC Transfer</div>
          <div style={{ fontSize: 13, color: 'var(--tx3)', marginTop: 2 }}>Track your vehicle ownership transfer status</div>
        </div>
        <button onClick={() => setShowForm((v) => !v)}
          style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '9px 16px', fontSize: 13, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Initiate RC Transfer
        </button>
      </div>

      {/* Initiate form */}
      {showForm && (
        <div style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 16, padding: 20, marginBottom: 20, boxShadow: 'var(--csh)' }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--tx1)', marginBottom: 16 }}>New RC Transfer Request</div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            {[
              { key: 'registrationNumber', label: 'Vehicle Registration No.', placeholder: 'DL 01 AB 1234', required: true },
              { key: 'orderId',            label: 'Order ID (optional)',       placeholder: 'From your purchase' },
              { key: 'rtoOffice',          label: 'RTO Office',               placeholder: 'e.g. Saket RTO, Delhi' },
            ].map((f) => (
              <div key={f.key} style={{ gridColumn: f.key === 'registrationNumber' ? '1/-1' : 'auto' }}>
                <label style={{ display: 'block', fontSize: 11, fontWeight: 700, color: 'var(--tx2)', textTransform: 'uppercase', letterSpacing: '.06em', marginBottom: 6 }}>
                  {f.label}{f.required && <span style={{ color: '#ef4444', marginLeft: 2 }}>*</span>}
                </label>
                <input
                  type="text"
                  value={form[f.key]}
                  placeholder={f.placeholder}
                  onChange={(e) => setForm((prev) => ({ ...prev, [f.key]: e.target.value }))}
                  style={{ width: '100%', boxSizing: 'border-box', padding: '10px 12px', fontSize: 13, border: '1.5px solid var(--bd1)', borderRadius: 10, background: 'var(--bg1)', color: 'var(--tx1)', outline: 'none', fontFamily: 'inherit' }}
                />
              </div>
            ))}
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
            <button onClick={() => setShowForm(false)}
              style={{ flex: 1, padding: '10px', fontSize: 13, fontWeight: 700, background: 'var(--bg2)', color: 'var(--tx2)', border: '1.5px solid var(--bd1)', borderRadius: 10, cursor: 'pointer' }}>
              Cancel
            </button>
            <button onClick={handleInitiate} disabled={submitting}
              style={{ flex: 2, padding: '10px', fontSize: 13, fontWeight: 700, background: submitting ? '#cbd5e1' : 'var(--ac)', color: '#fff', border: 'none', borderRadius: 10, cursor: submitting ? 'not-allowed' : 'pointer' }}>
              {submitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </div>
        </div>
      )}

      {/* Empty state */}
      {transfers.length === 0 && !showForm && (
        <div style={{ background: 'var(--cbg)', border: '1px dashed var(--bd1)', borderRadius: 18, padding: 48, textAlign: 'center' }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 12 }}>
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--tx3)" strokeWidth="1.5" style={{ opacity: .4 }}>
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>
            </svg>
          </div>
          <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--tx1)', marginBottom: 8 }}>No RC Transfers</div>
          <div style={{ fontSize: 13, color: 'var(--tx3)' }}>RC transfers appear here after completing a purchase.</div>
        </div>
      )}

      {/* Transfer cards */}
      <div style={{ display: 'grid', gap: 16 }}>
        {transfers.map((t) => (
          <div key={t.id} style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 16, padding: 20, boxShadow: 'var(--csh)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: 16 }}>
              <div>
                <div style={{ fontWeight: 800, fontSize: 16, color: 'var(--tx1)' }}>RC Transfer #{t.id}</div>
                <div style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 4 }}>
                  Reg: {t.registrationNumber} {t.rtoOffice ? `· ${t.rtoOffice}` : ''}
                </div>
              </div>
              <span style={{ padding: '4px 12px', borderRadius: 999, fontSize: 11, fontWeight: 700, background: (STATUS_COLORS[t.status] || '#64748b') + '20', color: STATUS_COLORS[t.status] || '#64748b', border: '1px solid ' + (STATUS_COLORS[t.status] || '#64748b') + '40' }}>
                {STATUS_LABELS[t.status] || t.status}
              </span>
            </div>

            {/* Progress bar */}
            <div style={{ marginBottom: 14 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: 'var(--tx3)', marginBottom: 6 }}>
                <span>Progress</span>
                <span>{getProgress(t.status)}%</span>
              </div>
              <div style={{ height: 6, background: 'var(--bg2)', borderRadius: 999, overflow: 'hidden' }}>
                <div style={{ height: '100%', width: getProgress(t.status) + '%', background: STATUS_COLORS[t.status] || '#64748b', borderRadius: 999, transition: 'width .5s' }} />
              </div>
            </div>

            {/* Step tracker */}
            <div style={{ display: 'flex', gap: 4, marginBottom: 12 }}>
              {STEPS.map((step, i) => {
                const current = getStepIndex(t.status);
                const done    = i <= current;
                return (
                  <div key={step} style={{ flex: 1, textAlign: 'center' }}>
                    <div style={{ width: 20, height: 20, borderRadius: '50%', background: done ? STATUS_COLORS[t.status] || '#64748b' : 'var(--bg2)', margin: '0 auto 4px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      {done ? (
                        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="3"><polyline points="20 6 9 17 4 12"/></svg>
                      ) : (
                        <div style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--bd1)' }} />
                      )}
                    </div>
                    <div style={{ fontSize: 9, color: done ? 'var(--tx2)' : 'var(--tx3)', fontWeight: done ? 700 : 400, lineHeight: 1.2 }}>{step}</div>
                  </div>
                );
              })}
            </div>

            {t.expectedCompletionDate && (
              <div style={{ fontSize: 12, color: 'var(--tx3)' }}>
                Expected completion: <strong style={{ color: 'var(--tx1)' }}>
                  {new Date(t.expectedCompletionDate).toLocaleDateString('en-IN')}
                </strong>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
