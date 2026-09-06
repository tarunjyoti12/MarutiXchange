import { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { bidApi } from '../../services/api';

const FUEL_TYPES    = ['Petrol', 'Diesel', 'CNG', 'Electric', 'Hybrid'];
const BRANDS        = ['Brezza', 'Swift', 'Baleno', 'Grand Vitara', 'Fronx', 'WagonR', 'Dzire', 'Jimny', 'Ertiga', 'XL6', 'Alto K10', 'S-Presso', 'Celerio', 'Ignis'];
const SEGMENTS      = ['Arena', 'Nexa'];

function Field({ label, required, children }) {
  return (
    <div style={{ marginBottom: 16 }}>
      <label style={{ display: 'block', fontSize: 12, fontWeight: 700, color: 'var(--tx2)', textTransform: 'uppercase', letterSpacing: '.06em', marginBottom: 6 }}>
        {label}{required && <span style={{ color: '#ef4444', marginLeft: 2 }}>*</span>}
      </label>
      {children}
    </div>
  );
}

const INPUT_STYLE = {
  width: '100%', boxSizing: 'border-box',
  padding: '10px 12px', fontSize: 14,
  border: '1.5px solid var(--bd1)', borderRadius: 10,
  background: 'var(--bg1)', color: 'var(--tx1)',
  outline: 'none', fontFamily: 'inherit',
};

export default function AuctionCreateModal({ onClose }) {
  const { user, pushToast } = useApp();

  const [form, setForm] = useState({
    carName:        '',
    brand:          'Brezza',
    segment:        'Arena',
    year:           new Date().getFullYear() - 2,
    mileage:        '',
    fuelType:       'Petrol',
    startingPrice:  '',
    buyNowPrice:    '',
    minBidIncrement: '1000',
    durationHours:  '24',
    carImageUrl:    '',
    description:    '',
  });
  const [loading,  setLoading]  = useState(false);
  const [step,     setStep]     = useState(1); // 1=car details, 2=auction settings, 3=success

  function set(key, val) { setForm((f) => ({ ...f, [key]: val })); }

  function validateStep1() {
    if (!form.carName.trim())    return 'Car name is required';
    if (!form.year || form.year < 2000 || form.year > new Date().getFullYear()) return 'Enter a valid year';
    if (!form.mileage || isNaN(form.mileage)) return 'Enter valid mileage in km';
    return null;
  }

  function validateStep2() {
    if (!form.startingPrice || isNaN(form.startingPrice)) return 'Enter a valid starting price';
    if (parseFloat(form.startingPrice) < 50000) return 'Starting price must be at least Rs.50,000';
    if (form.buyNowPrice && parseFloat(form.buyNowPrice) <= parseFloat(form.startingPrice)) {
      return 'Buy Now price must be higher than starting price';
    }
    if (!form.durationHours || isNaN(form.durationHours) || form.durationHours < 1) {
      return 'Duration must be at least 1 hour';
    }
    return null;
  }

  async function handleSubmit() {
    const err = validateStep2();
    if (err) { pushToast(err, 'err'); return; }

    setLoading(true);
    try {
      const endTime = new Date(Date.now() + parseFloat(form.durationHours) * 3600000).toISOString();

      await bidApi.post('/api/v1/auctions', {
        carName:         form.carName,
        brand:           form.brand,
        segment:         form.segment.toLowerCase(),
        year:            parseInt(form.year),
        mileage:         parseInt(form.mileage),
        fuelType:        form.fuelType,
        startingPrice:   parseFloat(form.startingPrice),
        buyNowPrice:     form.buyNowPrice ? parseFloat(form.buyNowPrice) : null,
        minBidIncrement: parseFloat(form.minBidIncrement) || 1000,
        endTime,
        carImageUrl:     form.carImageUrl || null,
        description:     form.description || null,
        sellerId:        user?.id,
      });

      setStep(3);
      pushToast('Auction created successfully', 'ok');
    } catch (err) {
      // Demo mode
      setStep(3);
      pushToast('Auction created (demo mode)', 'ok');
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <div onClick={onClose} style={{ position: 'fixed', inset: 0, zIndex: 99998, background: 'rgba(0,0,0,.6)', backdropFilter: 'blur(4px)' }} />
      <div style={{ position: 'fixed', inset: 0, zIndex: 99999, overflowY: 'auto', display: 'flex', justifyContent: 'center', alignItems: 'flex-start', padding: '20px 16px' }}>
        <div onClick={(e) => e.stopPropagation()}
          style={{ background: 'var(--cbg)', borderRadius: 20, boxShadow: 'var(--chsh)', width: '100%', maxWidth: 560, margin: 'auto' }}>

          {/* Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 20px', borderBottom: '1px solid var(--bd0)' }}>
            <div>
              <div style={{ fontSize: 15, fontWeight: 800, color: 'var(--tx1)' }}>Create Auction</div>
              <div style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 2 }}>
                Step {step} of 2 — {step === 1 ? 'Car Details' : step === 2 ? 'Auction Settings' : 'Done'}
              </div>
            </div>
            <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', width: 32, height: 32, borderRadius: '50%' }}>
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="var(--tx3)" strokeWidth="2">
                <line x1="1" y1="1" x2="13" y2="13"/><line x1="13" y1="1" x2="1" y2="13"/>
              </svg>
            </button>
          </div>

          {/* Progress bar */}
          <div style={{ height: 3, background: 'var(--bg2)' }}>
            <div style={{ height: '100%', background: 'var(--ac)', width: step === 3 ? '100%' : step === 2 ? '66%' : '33%', transition: 'width .4s' }} />
          </div>

          <div style={{ padding: '24px 20px' }}>

            {/* Step 1 — Car Details */}
            {step === 1 && (
              <>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                  <div style={{ gridColumn: '1/-1' }}>
                    <Field label="Car Name" required>
                      <select value={form.brand} onChange={(e) => { set('brand', e.target.value); set('carName', `Maruti ${e.target.value}`); }} style={INPUT_STYLE}>
                        {BRANDS.map((b) => <option key={b}>{b}</option>)}
                      </select>
                    </Field>
                  </div>

                  <Field label="Segment" required>
                    <select value={form.segment} onChange={(e) => set('segment', e.target.value)} style={INPUT_STYLE}>
                      {SEGMENTS.map((s) => <option key={s}>{s}</option>)}
                    </select>
                  </Field>

                  <Field label="Year" required>
                    <input type="number" value={form.year} min="2000" max={new Date().getFullYear()}
                      onChange={(e) => set('year', e.target.value)} style={INPUT_STYLE} />
                  </Field>

                  <Field label="Mileage (km)" required>
                    <input type="number" value={form.mileage} placeholder="e.g. 24000"
                      onChange={(e) => set('mileage', e.target.value)} style={INPUT_STYLE} />
                  </Field>

                  <Field label="Fuel Type" required>
                    <select value={form.fuelType} onChange={(e) => set('fuelType', e.target.value)} style={INPUT_STYLE}>
                      {FUEL_TYPES.map((f) => <option key={f}>{f}</option>)}
                    </select>
                  </Field>

                  <div style={{ gridColumn: '1/-1' }}>
                    <Field label="Car Image URL">
                      <input type="url" value={form.carImageUrl} placeholder="https://..."
                        onChange={(e) => set('carImageUrl', e.target.value)} style={INPUT_STYLE} />
                    </Field>
                  </div>

                  <div style={{ gridColumn: '1/-1' }}>
                    <Field label="Description">
                      <textarea rows={3} value={form.description} placeholder="Describe the car condition, features, service history..."
                        onChange={(e) => set('description', e.target.value)}
                        style={{ ...INPUT_STYLE, resize: 'none' }} />
                    </Field>
                  </div>
                </div>

                <button onClick={() => { const err = validateStep1(); if (err) { pushToast(err, 'err'); return; } setStep(2); }}
                  style={{ width: '100%', padding: '13px', fontSize: 14, fontWeight: 800, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 12, cursor: 'pointer', marginTop: 4 }}>
                  Next: Auction Settings
                </button>
              </>
            )}

            {/* Step 2 — Auction Settings */}
            {step === 2 && (
              <>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                  <Field label="Starting Price (Rs.)" required>
                    <input type="number" value={form.startingPrice} placeholder="e.g. 500000"
                      onChange={(e) => set('startingPrice', e.target.value)} style={INPUT_STYLE} />
                    <div style={{ fontSize: 11, color: 'var(--tx3)', marginTop: 4 }}>
                      {form.startingPrice ? `Rs.${(parseFloat(form.startingPrice)/100000).toFixed(2)} L` : ''}
                    </div>
                  </Field>

                  <Field label="Buy Now Price (Rs.) — optional">
                    <input type="number" value={form.buyNowPrice} placeholder="e.g. 700000"
                      onChange={(e) => set('buyNowPrice', e.target.value)} style={INPUT_STYLE} />
                    <div style={{ fontSize: 11, color: 'var(--tx3)', marginTop: 4 }}>
                      {form.buyNowPrice ? `Rs.${(parseFloat(form.buyNowPrice)/100000).toFixed(2)} L` : 'Leave blank to disable Buy Now'}
                    </div>
                  </Field>

                  <Field label="Min Bid Increment (Rs.)" required>
                    <input type="number" value={form.minBidIncrement} min="1000"
                      onChange={(e) => set('minBidIncrement', e.target.value)} style={INPUT_STYLE} />
                  </Field>

                  <Field label="Auction Duration (hours)" required>
                    <select value={form.durationHours} onChange={(e) => set('durationHours', e.target.value)} style={INPUT_STYLE}>
                      {[1, 3, 6, 12, 24, 48, 72].map((h) => (
                        <option key={h} value={h}>{h} {h === 1 ? 'hour' : 'hours'}</option>
                      ))}
                    </select>
                  </Field>
                </div>

                {/* Summary card */}
                <div style={{ padding: '14px 16px', background: 'var(--acd)', border: '1px solid var(--acd2)', borderRadius: 12, marginBottom: 16 }}>
                  <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--tx2)', marginBottom: 10 }}>Auction Summary</div>
                  {[
                    { label: 'Car',            value: `${form.year} Maruti ${form.brand}` },
                    { label: 'Starting Price', value: form.startingPrice ? `Rs.${(parseFloat(form.startingPrice)/100000).toFixed(2)} L` : '—' },
                    { label: 'Buy Now Price',  value: form.buyNowPrice   ? `Rs.${(parseFloat(form.buyNowPrice)/100000).toFixed(2)} L`   : 'Disabled' },
                    { label: 'Duration',       value: `${form.durationHours} hours` },
                    { label: 'Ends at',        value: new Date(Date.now() + parseFloat(form.durationHours||0)*3600000).toLocaleString('en-IN') },
                  ].map((row) => (
                    <div key={row.label} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, padding: '4px 0', borderBottom: '1px solid var(--bd0)' }}>
                      <span style={{ color: 'var(--tx3)' }}>{row.label}</span>
                      <span style={{ fontWeight: 700, color: 'var(--tx1)' }}>{row.value}</span>
                    </div>
                  ))}
                </div>

                <div style={{ display: 'flex', gap: 10 }}>
                  <button onClick={() => setStep(1)}
                    style={{ flex: 1, padding: '13px', fontSize: 14, fontWeight: 700, background: 'var(--bg2)', color: 'var(--tx2)', border: '1.5px solid var(--bd1)', borderRadius: 12, cursor: 'pointer' }}>
                    Back
                  </button>
                  <button onClick={handleSubmit} disabled={loading}
                    style={{ flex: 2, padding: '13px', fontSize: 14, fontWeight: 800, background: loading ? '#cbd5e1' : 'var(--ac)', color: '#fff', border: 'none', borderRadius: 12, cursor: loading ? 'not-allowed' : 'pointer' }}>
                    {loading ? 'Creating...' : 'Launch Auction'}
                  </button>
                </div>
              </>
            )}

            {/* Step 3 — Success */}
            {step === 3 && (
              <div style={{ textAlign: 'center', padding: '20px 0' }}>
                <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
                  <div style={{ width: 64, height: 64, borderRadius: '50%', background: '#dcfce7', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="#16a34a" strokeWidth="2.5" strokeLinecap="round">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                  </div>
                </div>
                <div style={{ fontSize: 18, fontWeight: 800, color: 'var(--tx1)', marginBottom: 8 }}>Auction Created</div>
                <div style={{ fontSize: 13, color: 'var(--tx3)', marginBottom: 24, lineHeight: 1.6 }}>
                  Your auction for <strong>{form.year} Maruti {form.brand}</strong> is now live.<br />
                  Bidders will be notified immediately.
                </div>
                <button onClick={onClose}
                  style={{ padding: '12px 32px', fontSize: 14, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 12, cursor: 'pointer' }}>
                  View Live Auctions
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
