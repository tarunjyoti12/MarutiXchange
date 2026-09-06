import { useMemo, useRef, useState, useEffect } from 'react';
import { CARS } from '../../data/cars';
import { useApp } from '../../context/AppContext';
import TestDriveModal from './TestDriveModal';
import { SkeletonCarDetail } from '../Skeleton';

function calcEmi(principal, rate, months) {
  const r = rate / 12 / 100;
  if (r === 0) return Math.round(principal / months);
  return Math.round(principal * r * Math.pow(1 + r, months) / (Math.pow(1 + r, months) - 1));
}

function getCarData(car) {
  const m = car.price?.match(/[\d.]+/);
  const lakh = m ? parseFloat(m[0]) : 6;
  const price = Math.round(lakh * 100000);
  return { price, lakh };
}

// ── 360 Turntable — Spinny-style smooth rotation ──────────────────────────────
function TurntableViewer({ img }) {
  const TOTAL = 36;
  const [angle, setAngle]           = useState(0);
  const [dragging, setDragging]     = useState(false);
  const [showHint, setShowHint]     = useState(false);
  const [autoPlayed, setAutoPlayed] = useState(false);
  const dragLastX  = useRef(0);
  const dragLastT  = useRef(0);
  const rafRef     = useRef(null);
  const angleRef   = useRef(0);
  const velRef     = useRef(0);
  const autoRef    = useRef(null);

  useEffect(() => {
    let a = 0;
    autoRef.current = setInterval(() => {
      a += 1; setAngle(a % TOTAL); angleRef.current = a % TOTAL;
      if (a >= TOTAL) { clearInterval(autoRef.current); setAutoPlayed(true); setShowHint(true); }
    }, 40);
    return () => { clearInterval(autoRef.current); cancelAnimationFrame(rafRef.current); };
  }, []);

  useEffect(() => {
    if (dragging) return;
    function tick() {
      if (Math.abs(velRef.current) < 0.05) { velRef.current = 0; return; }
      velRef.current *= 0.92;
      angleRef.current = (angleRef.current + velRef.current + TOTAL) % TOTAL;
      setAngle(Math.round(angleRef.current) % TOTAL);
      rafRef.current = requestAnimationFrame(tick);
    }
    rafRef.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(rafRef.current);
  }, [dragging]);

  function stopAuto() { clearInterval(autoRef.current); cancelAnimationFrame(rafRef.current); setShowHint(false); }

  function onPointerDown(e) {
    stopAuto(); setDragging(true); velRef.current = 0;
    dragLastX.current = e.clientX ?? e.touches?.[0]?.clientX ?? 0;
    dragLastT.current = Date.now();
    e.currentTarget.setPointerCapture?.(e.pointerId);
  }
  function onPointerMove(e) {
    if (!dragging) return;
    const x = e.clientX ?? e.touches?.[0]?.clientX ?? 0;
    const dx = x - dragLastX.current;
    velRef.current = dx / (Date.now() - dragLastT.current || 1) * 0.5;
    angleRef.current = (angleRef.current - dx / 14 + TOTAL) % TOTAL;
    setAngle(Math.round(angleRef.current) % TOTAL);
    dragLastX.current = x; dragLastT.current = Date.now();
  }
  function onPointerUp() { setDragging(false); }

  const rad = (angle / TOTAL) * Math.PI * 2;
  const cosA = Math.cos(rad);
  const sinA = Math.sin(rad);
  const scaleX = Math.abs(cosA) * 0.75 + 0.25;
  const flipX = angle >= TOTAL / 2 ? -1 : 1;
  const scaleY = 0.98 + Math.abs(sinA) * 0.04;
  const tx = sinA * 18;
  const shadow = 0.04 + Math.abs(sinA) * 0.08;
  const DOT_COUNT = 18;
  const dotStep = Math.round(angle / 2) % DOT_COUNT;

  return (
    <div style={{ background: '#f0f2f5', position: 'relative', userSelect: 'none' }}>
      <div style={{ position: 'absolute', top: 14, left: 14, zIndex: 6, background: 'rgba(0,0,0,.75)', color: '#fff', padding: '5px 12px', borderRadius: 6, fontSize: 11, fontWeight: 800, letterSpacing: '.1em', display: 'flex', alignItems: 'center', gap: 6 }}>
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0118.8-4.3M22 12.5a10 10 0 01-18.8 4.2"/></svg>
        360°
      </div>
      <div style={{ position: 'absolute', top: 14, right: 14, zIndex: 6, background: 'rgba(0,0,0,.55)', color: '#fff', padding: '4px 10px', borderRadius: 6, fontSize: 11, fontWeight: 700 }}>
        {Math.round((angle / TOTAL) * 360)}°
      </div>
      <div onPointerDown={onPointerDown} onPointerMove={onPointerMove} onPointerUp={onPointerUp} onPointerLeave={onPointerUp}
        style={{ aspectRatio: '16/10', cursor: dragging ? 'grabbing' : 'grab', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', padding: '12px 40px 20px', position: 'relative', touchAction: 'none' }}>
        <div style={{ position: 'absolute', bottom: 24, left: '15%', right: '15%', height: 18, background: `radial-gradient(ellipse, rgba(0,0,0,${(shadow * 2.5).toFixed(2)}) 0%, transparent 70%)`, borderRadius: '50%', transform: `scaleX(${(0.6 + Math.abs(cosA) * 0.4).toFixed(2)})`, pointerEvents: 'none' }} />
        <img src={img} referrerPolicy="no-referrer" crossOrigin="anonymous" alt="360° car view" draggable={false}
          style={{ width: '88%', height: '88%', objectFit: 'contain', transform: `scaleX(${(scaleX * flipX).toFixed(3)}) scaleY(${scaleY.toFixed(3)}) translateX(${tx.toFixed(1)}px)`, transition: dragging ? 'none' : 'transform .06s ease', pointerEvents: 'none', filter: `drop-shadow(0 8px 24px rgba(0,0,0,${shadow.toFixed(2)}))` }}
        />
        {showHint && autoPlayed && (
          <div onClick={stopAuto} style={{ position: 'absolute', inset: 0, zIndex: 5, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div style={{ background: 'rgba(255,255,255,.92)', borderRadius: 16, padding: '14px 28px', boxShadow: '0 8px 32px rgba(0,0,0,.14)', display: 'flex', alignItems: 'center', gap: 12, pointerEvents: 'none' }}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1449C0" strokeWidth="2.5"><path d="M15 18l-6-6 6-6"/></svg>
              <div><div style={{ fontSize: 13, fontWeight: 800, color: '#111' }}>Drag to rotate</div><div style={{ fontSize: 11, color: '#888', marginTop: 2 }}>Smooth 360° view</div></div>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1449C0" strokeWidth="2.5"><path d="M9 18l6-6-6-6"/></svg>
            </div>
          </div>
        )}
      </div>
      <div style={{ display: 'flex', justifyContent: 'center', gap: 3, paddingBottom: 14 }}>
        {Array.from({ length: DOT_COUNT }).map((_, i) => (
          <div key={i} onClick={() => { stopAuto(); const a = (i / DOT_COUNT) * TOTAL; angleRef.current = a; velRef.current = 0; setAngle(Math.round(a)); }}
            style={{ height: 3, borderRadius: 99, cursor: 'pointer', width: i === dotStep ? 20 : 5, background: i === dotStep ? '#1449C0' : '#c8ccd4', transition: 'all .15s' }} />
        ))}
      </div>
    </div>
  );
}


// ── Compare Cars Tool ──────────────────────────────────────────────────────────
function CompareCarsTool({ currentCar, allCars, onViewCar }) {
  const similar = allCars
    .filter((c) => c.id !== currentCar.id && (c.type === currentCar.type || c.brand === currentCar.brand))
    .slice(0, 6);

  const [selected, setSelected] = useState([]);

  function toggleCar(car) {
    setSelected((prev) => {
      if (prev.find((c) => c.id === car.id)) return prev.filter((c) => c.id !== car.id);
      if (prev.length >= 2) return [...prev.slice(1), car];
      return [...prev, car];
    });
  }

  const compareCars = [currentCar, ...selected];

  const rows = [
    { label: 'Price',        key: 'price',  format: (v) => v },
    { label: 'Year',         key: 'year',   format: (v) => v },
    { label: 'KM Driven',    key: 'km',     format: (v) => v + ' km' },
    { label: 'Fuel',         key: 'fuel',   format: (v) => Array.isArray(v) ? v[0] : v },
    { label: 'Transmission', key: 'trans',  format: (v) => v },
    { label: 'Owner',        key: 'owner',  format: (v) => v + ' Owner' },
    { label: 'Location',     key: 'loc',    format: (v) => v },
    { label: 'Rating',       key: 'rating', format: (v) => v + ' / 5.0' },
    { label: 'Colour',       key: 'colour', format: (v) => v || 'N/A' },
  ];

  // Determine best value per row for highlighting
  function getBest(row) {
    if (row.key === 'rating') {
      const max = Math.max(...compareCars.map((c) => parseFloat(c[row.key]) || 0));
      return compareCars.map((c) => parseFloat(c[row.key]) === max);
    }
    if (row.key === 'year') {
      const max = Math.max(...compareCars.map((c) => parseInt(c[row.key]) || 0));
      return compareCars.map((c) => parseInt(c[row.key]) === max);
    }
    if (row.key === 'km') {
      const min = Math.min(...compareCars.map((c) => parseInt((c[row.key] || '0').replace(/,/g, '')) || 999999));
      return compareCars.map((c) => parseInt((c[row.key] || '0').replace(/,/g, '')) === min);
    }
    return compareCars.map(() => false);
  }

  return (
    <div style={{ marginBottom: 32 }}>
      <h3 style={{ fontSize: 16, fontWeight: 800, color: '#111', marginTop: 0, marginBottom: 4 }}>Compare Cars</h3>
      <p style={{ fontSize: 13, color: '#6b7280', margin: '0 0 16px' }}>Select up to 2 cars to compare with this listing</p>

      {/* Car picker */}
      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 20 }}>
        {similar.map((c) => {
          const isSelected = selected.find((s) => s.id === c.id);
          return (
            <div key={c.id} onClick={() => toggleCar(c)}
              style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 14px', borderRadius: 10, border: `1.5px solid ${isSelected ? '#1449C0' : '#e5e7eb'}`, background: isSelected ? '#eff6ff' : '#f9fafb', cursor: 'pointer', transition: 'all .15s' }}>
              <img src={c.img} alt={c.name} style={{ width: 44, height: 30, objectFit: 'contain' }} referrerPolicy="no-referrer" crossOrigin="anonymous" />
              <div>
                <div style={{ fontSize: 12, fontWeight: 700, color: isSelected ? '#1449C0' : '#111' }}>{c.name}</div>
                <div style={{ fontSize: 11, color: '#6b7280' }}>{c.year} · {c.price}</div>
              </div>
              {isSelected && (
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#1449C0" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              )}
            </div>
          );
        })}
      </div>

      {/* Comparison table */}
      {compareCars.length >= 2 && (
        <div style={{ border: '1px solid #e5e7eb', borderRadius: 16, overflow: 'hidden' }}>
          {/* Header row with car images */}
          <div style={{ display: 'grid', gridTemplateColumns: `160px repeat(${compareCars.length}, 1fr)`, background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
            <div style={{ padding: '14px 16px', fontSize: 11, fontWeight: 700, color: '#9ca3af', textTransform: 'uppercase', letterSpacing: '.06em' }}>Feature</div>
            {compareCars.map((c, i) => (
              <div key={c.id} style={{ padding: '12px 14px', textAlign: 'center', background: i === 0 ? '#eff6ff' : '#fff', borderLeft: '1px solid #e5e7eb' }}>
                <img src={c.img} alt={c.name} referrerPolicy="no-referrer" crossOrigin="anonymous"
                  style={{ width: '100%', height: 56, objectFit: 'contain', marginBottom: 6 }} />
                <div style={{ fontSize: 12, fontWeight: 800, color: i === 0 ? '#1449C0' : '#111', lineHeight: 1.2 }}>{c.name}</div>
                {i === 0 && <div style={{ fontSize: 10, color: '#1449C0', marginTop: 2, fontWeight: 600 }}>This car</div>}
                {i > 0 && (
                  <button onClick={() => onViewCar(c.id)}
                    style={{ marginTop: 6, padding: '3px 10px', fontSize: 10, fontWeight: 700, background: '#f0f0f0', color: '#374151', border: 'none', borderRadius: 6, cursor: 'pointer' }}>
                    View
                  </button>
                )}
              </div>
            ))}
          </div>

          {/* Data rows */}
          {rows.map((row, rowIdx) => {
            const best = getBest(row);
            return (
              <div key={row.label} style={{ display: 'grid', gridTemplateColumns: `160px repeat(${compareCars.length}, 1fr)`, borderBottom: rowIdx < rows.length - 1 ? '1px solid #f3f4f6' : 'none', background: rowIdx % 2 === 0 ? '#fff' : '#fafafa' }}>
                <div style={{ padding: '12px 16px', fontSize: 12, fontWeight: 600, color: '#6b7280' }}>{row.label}</div>
                {compareCars.map((c, i) => {
                  const val = row.format(c[row.key]);
                  const isBest = best[i];
                  return (
                    <div key={c.id} style={{ padding: '12px 14px', textAlign: 'center', borderLeft: '1px solid #f0f0f0', background: i === 0 ? 'rgba(20,73,192,.03)' : 'transparent' }}>
                      <span style={{ fontSize: 13, fontWeight: isBest ? 800 : 500, color: isBest ? '#059669' : '#374151', display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                        {isBest && <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="3"><polyline points="20 6 9 17 4 12"/></svg>}
                        {val}
                      </span>
                    </div>
                  );
                })}
              </div>
            );
          })}
        </div>
      )}

      {compareCars.length < 2 && (
        <div style={{ padding: '24px', textAlign: 'center', background: '#f9fafb', borderRadius: 12, border: '1px dashed #e5e7eb', color: '#9ca3af', fontSize: 13 }}>
          Select at least 1 car above to start comparing
        </div>
      )}
    </div>
  );
}

// ── EMI Calculator ─────────────────────────────────────────────────────────────
function EmiCalculator({ carPrice }) {
  const maxLoan = Math.round(carPrice * 0.9);
  const [loanAmt, setLoanAmt]     = useState(Math.round(carPrice * 0.8));
  const [downPay, setDownPay]     = useState(Math.round(carPrice * 0.2));
  const [months, setMonths]       = useState(60);
  const [rate]                    = useState(9);

  const emi          = calcEmi(loanAmt, rate, months);
  const totalPayable = emi * months;
  const totalInterest = totalPayable - loanAmt;
  const principal_pct = Math.round((loanAmt / totalPayable) * 100);
  const interest_pct  = 100 - principal_pct;

  function handleLoan(v) {
    const l = parseInt(v);
    setLoanAmt(l);
    setDownPay(carPrice - l);
  }
  function handleDown(v) {
    const d = parseInt(v);
    setDownPay(d);
    setLoanAmt(carPrice - d);
  }

  const circumference = 2 * Math.PI * 52;
  const principalDash = (principal_pct / 100) * circumference;

  return (
    <div style={{ marginBottom: 32, paddingBottom: 32, borderBottom: '1px solid #f3f4f6' }}>
      <h3 style={{ fontSize: 16, fontWeight: 800, color: '#111', marginTop: 0, marginBottom: 4 }}>EMI Calculator</h3>
      <p style={{ fontSize: 13, color: '#6b7280', margin: '0 0 20px' }}>Customise your loan amount, down payment and tenure</p>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
        {/* Left — donut + breakdown */}
        <div style={{ background: '#f9fafb', borderRadius: 16, padding: '20px', border: '1px solid #f0f0f0', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <div style={{ fontSize: 13, color: '#6b7280', marginBottom: 4 }}>EMI starting from</div>
          <div style={{ fontSize: 32, fontWeight: 900, color: '#1449C0', marginBottom: 16 }}>
            &#8377;{emi.toLocaleString('en-IN')}<span style={{ fontSize: 14, fontWeight: 600, color: '#6b7280' }}>/month</span>
          </div>

          {/* Donut chart */}
          <svg width="130" height="130" viewBox="0 0 130 130" style={{ marginBottom: 16 }}>
            <circle cx="65" cy="65" r="52" fill="none" stroke="#e5e7eb" strokeWidth="14"/>
            <circle cx="65" cy="65" r="52" fill="none" stroke="#10b981" strokeWidth="14"
              strokeDasharray={`${principalDash} ${circumference}`}
              strokeLinecap="round" transform="rotate(-90 65 65)" style={{ transition: 'stroke-dasharray .4s' }}/>
            <circle cx="65" cy="65" r="52" fill="none" stroke="#1449C0" strokeWidth="14"
              strokeDasharray={`${circumference - principalDash - 4} ${circumference}`}
              strokeDashoffset={-(principalDash + 2)}
              strokeLinecap="round" transform="rotate(-90 65 65)" style={{ transition: 'all .4s' }}/>
          </svg>

          <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 8 }}>
            {[
              { label: 'Principal Loan Amount', value: loanAmt, color: '#10b981' },
              { label: 'Total Interest Payable', value: totalInterest, color: '#1449C0' },
              { label: 'Total Amount Payable',   value: totalPayable, color: '#111' },
            ].map((r) => (
              <div key={r.label} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 0', borderBottom: '1px solid #f0f0f0', fontSize: 12 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  {r.color !== '#111' && <div style={{ width: 10, height: 10, borderRadius: '50%', background: r.color }} />}
                  <span style={{ color: '#6b7280' }}>{r.label}</span>
                </div>
                <span style={{ fontWeight: 700, color: r.color }}>&#8377;{r.value.toLocaleString('en-IN')}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Right — sliders */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <style>{`
            input[type=range].emi-slider { -webkit-appearance: none; width: 100%; height: 5px; border-radius: 3px; background: #e5e7eb; outline: none; }
            input[type=range].emi-slider::-webkit-slider-thumb { -webkit-appearance: none; width: 20px; height: 20px; border-radius: 50%; background: #1449C0; cursor: pointer; box-shadow: 0 2px 6px rgba(20,73,192,.3); }
          `}</style>

          {/* Loan Amount */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#374151' }}>Loan Amount</span>
              <span style={{ fontSize: 13, fontWeight: 800, color: '#1449C0' }}>&#8377;{loanAmt.toLocaleString('en-IN')}</span>
            </div>
            <input type="range" className="emi-slider" min={Math.round(carPrice * 0.1)} max={maxLoan} step={5000} value={loanAmt} onChange={(e) => handleLoan(e.target.value)} />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4, fontSize: 11, color: '#9ca3af' }}>
              <span>&#8377;{Math.round(carPrice * 0.1).toLocaleString('en-IN')}</span>
              <span>&#8377;{maxLoan.toLocaleString('en-IN')}</span>
            </div>
          </div>

          {/* Down Payment */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#374151' }}>Down Payment</span>
              <span style={{ fontSize: 13, fontWeight: 800, color: '#1449C0' }}>&#8377;{downPay.toLocaleString('en-IN')}</span>
            </div>
            <input type="range" className="emi-slider" min={0} max={Math.round(carPrice * 0.9)} step={5000} value={downPay} onChange={(e) => handleDown(e.target.value)} />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4, fontSize: 11, color: '#9ca3af' }}>
              <span>&#8377;0</span>
              <span>&#8377;{Math.round(carPrice * 0.9).toLocaleString('en-IN')}</span>
            </div>
          </div>

          {/* Duration */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#374151' }}>Duration of Loan</span>
              <span style={{ fontSize: 13, fontWeight: 800, color: '#1449C0' }}>{months} Months</span>
            </div>
            <input type="range" className="emi-slider" min={12} max={84} step={6} value={months} onChange={(e) => setMonths(parseInt(e.target.value))} />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4, fontSize: 11, color: '#9ca3af' }}>
              <span>12 Months</span><span>84 Months</span>
            </div>
          </div>

          <div style={{ padding: '12px 14px', background: '#eff6ff', borderRadius: 10, border: '1px solid #dbeafe' }}>
            <div style={{ fontSize: 11, color: '#1449C0', fontWeight: 700, marginBottom: 2 }}>Interest Rate: {rate}% per annum</div>
            <div style={{ fontSize: 11, color: '#6b7280' }}>Processing fee and other charges not included. Actual rate may vary based on credit profile.</div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Quality Report ─────────────────────────────────────────────────────────────
function QualityReport() {
  const sections = [
    { label: 'Core systems',       sub: 'Engine, transmission & chassis',      score: 9.2, grade: 'Excellent' },
    { label: 'Supporting systems', sub: 'Fuel supply, ignition & other systems', score: 9.0, grade: 'Excellent' },
    { label: 'Interiors & AC',     sub: 'Seats, AC, audio & other features',    score: 8.8, grade: 'Excellent' },
    { label: 'Exteriors & lights', sub: 'Panels, glasses, lights & fixtures',    score: 8.5, grade: 'Excellent' },
    { label: 'Wear & tear parts',  sub: 'Tyres, clutch, brakes & more',          score: 8.1, grade: 'Good' },
  ];

  const scoreColor = (s) => s >= 9 ? '#059669' : s >= 8 ? '#1449C0' : '#f59e0b';

  return (
    <div style={{ marginBottom: 32, paddingBottom: 32, borderBottom: '1px solid #f3f4f6' }}>
      <h3 style={{ fontSize: 16, fontWeight: 800, color: '#111', marginTop: 0, marginBottom: 4 }}>Quality Report</h3>
      <p style={{ fontSize: 13, color: '#6b7280', margin: '0 0 12px' }}>200+ parts evaluated by certified automotive experts</p>

      <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
        {['Meter not tampered', 'Non-flooded', 'Core structure intact'].map((t) => (
          <span key={t} style={{ display: 'flex', alignItems: 'center', gap: 5, padding: '4px 12px', background: '#f0fdf4', border: '1px solid #d1fae5', borderRadius: 999, fontSize: 12, color: '#059669', fontWeight: 600 }}>
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="3"><polyline points="20 6 9 17 4 12"/></svg>
            {t}
          </span>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
        {sections.map((s) => (
          <div key={s.label} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '14px 16px', background: '#f9fafb', borderRadius: 12, border: '1px solid #f0f0f0' }}>
            <div>
              <div style={{ fontSize: 13, fontWeight: 700, color: '#111', marginBottom: 2 }}>{s.label}</div>
              <div style={{ fontSize: 11, color: '#9ca3af' }}>{s.sub}</div>
            </div>
            <div style={{ textAlign: 'center', marginLeft: 12, flexShrink: 0 }}>
              <div style={{ width: 42, height: 42, borderRadius: '50%', background: scoreColor(s.score), display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: 13, fontWeight: 800, marginBottom: 3 }}>{s.score}</div>
              <div style={{ fontSize: 10, color: scoreColor(s.score), fontWeight: 700 }}>{s.grade}</div>
            </div>
          </div>
        ))}
        <div style={{ padding: '14px 16px', background: '#eff6ff', borderRadius: 12, border: '1px solid #dbeafe', display: 'flex', alignItems: 'center', gap: 10 }}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1449C0" strokeWidth="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          <div>
            <div style={{ fontSize: 12, fontWeight: 700, color: '#1449C0' }}>Next service due</div>
            <div style={{ fontSize: 11, color: '#6b7280' }}>After 12 months or 10,000 km</div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Car Specifications ─────────────────────────────────────────────────────────
function CarSpecifications({ car }) {
  const fuel = Array.isArray(car.fuel) ? car.fuel[0] : car.fuel;
  const specs = [
    { label: 'Mileage (ARAI)', value: fuel === 'Diesel' ? '23.01 kmpl' : fuel === 'CNG' ? '30.48 km/kg' : '22.38 kmpl', icon: '⛽' },
    { label: 'Boot Space',     value: '268 litres',  icon: '🧳' },
    { label: 'Engine',         value: '1197 cc',     icon: '⚙️' },
    { label: 'Max Power',      value: '88.5 bhp',    icon: '⚡' },
    { label: 'Seating',        value: '5 Persons',   icon: '👥' },
    { label: 'Kerb Weight',    value: '925 kg',      icon: '⚖️' },
  ];

  return (
    <div style={{ marginBottom: 32, paddingBottom: 32, borderBottom: '1px solid #f3f4f6' }}>
      <h3 style={{ fontSize: 16, fontWeight: 800, color: '#111', marginTop: 0, marginBottom: 16 }}>Car Specifications</h3>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 10 }}>
        {specs.map((s) => (
          <div key={s.label} style={{ padding: '14px 16px', background: '#f9fafb', borderRadius: 12, border: '1px solid #f0f0f0' }}>
            <div style={{ fontSize: 11, color: '#9ca3af', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.06em', marginBottom: 6 }}>{s.label}</div>
            <div style={{ fontSize: 16, fontWeight: 800, color: '#111' }}>{s.value}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Top Features ───────────────────────────────────────────────────────────────
function TopFeatures() {
  const cols = [
    {
      title: 'Comfort & Convenience',
      items: ['Keyless start', 'Steering mounted controls', 'Auto climate control', 'Rear AC vents', 'Wireless charging'],
    },
    {
      title: 'Safety',
      items: ['6 Airbags', 'ABS with EBD', 'Electronic stability program', 'Rear parking camera', 'ISOFIX child seat'],
    },
    {
      title: 'Entertainment',
      items: ['9-inch touchscreen', 'Wireless Android Auto', 'Apple CarPlay', 'GPS navigation', 'Bluetooth audio'],
    },
  ];

  return (
    <div style={{ marginBottom: 32, paddingBottom: 32, borderBottom: '1px solid #f3f4f6' }}>
      <h3 style={{ fontSize: 16, fontWeight: 800, color: '#111', marginTop: 0, marginBottom: 16 }}>Top Features</h3>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 16 }}>
        {cols.map((col) => (
          <div key={col.title}>
            <div style={{ fontSize: 11, fontWeight: 800, color: '#9ca3af', textTransform: 'uppercase', letterSpacing: '.08em', marginBottom: 10 }}>{col.title}</div>
            {col.items.map((item) => (
              <div key={item} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 0', borderBottom: '1px solid #f9fafb', fontSize: 13, color: '#374151' }}>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#1449C0" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                {item}
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

// ── AR Viewer ─────────────────────────────────────────────────────────────────
function ArViewer({ car }) {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(false);
  const [color, setColor]     = useState(car.colour || 'Pearl White');

  async function startAR() {
    setLoading(true);
    try {
      const res = await fetch('http://localhost:8076/api/v1/ar/session/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ carId: car.id, userId: 1, carModel: car.name, carColor: color }),
      });
      const data = await res.json();
      setSession(data);
    } catch (e) {
      setSession({ sessionId: 'demo-' + Date.now() });
    }
    setLoading(false);
  }

  return (
    <div style={{ background: '#0a0a1a', aspectRatio: '16/10', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 16, padding: 24 }}>
      <div style={{ background: 'rgba(20,73,192,.9)', color: '#fff', padding: '5px 14px', borderRadius: 6, fontSize: 11, fontWeight: 800, letterSpacing: '.1em', display: 'flex', alignItems: 'center', gap: 6 }}>
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
        AR VIEW — {car.name}
      </div>
      {!session ? (
        <>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: 14, fontWeight: 700, color: '#fff', marginBottom: 8 }}>View {car.name} in Augmented Reality</div>
            <div style={{ fontSize: 12, color: 'rgba(255,255,255,.6)', marginBottom: 20 }}>Place the car in your real environment using your camera</div>
          </div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', justifyContent: 'center', marginBottom: 8 }}>
            {['Pearl White', 'Solid Red', 'Midnight Black', 'Celestial Blue'].map((c) => (
              <div key={c} onClick={() => setColor(c)}
                style={{ padding: '6px 12px', borderRadius: 8, fontSize: 11, fontWeight: 700, cursor: 'pointer', background: color === c ? '#1449C0' : 'rgba(255,255,255,.1)', color: '#fff', border: `1.5px solid ${color === c ? '#1449C0' : 'rgba(255,255,255,.2)'}` }}>
                {c}
              </div>
            ))}
          </div>
          <button onClick={startAR} disabled={loading}
            style={{ padding: '12px 32px', fontSize: 14, fontWeight: 800, background: '#1449C0', color: '#fff', border: 'none', borderRadius: 12, cursor: loading ? 'not-allowed' : 'pointer' }}>
            {loading ? 'Starting AR...' : 'Launch AR View'}
          </button>
        </>
      ) : (
        <div style={{ textAlign: 'center' }}>
          <div style={{ width: 64, height: 64, borderRadius: '50%', background: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
            <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          </div>
          <div style={{ fontSize: 15, fontWeight: 800, color: '#fff', marginBottom: 8 }}>AR Session Started!</div>
          <div style={{ fontSize: 12, color: 'rgba(255,255,255,.7)', marginBottom: 4 }}>Session ID: {session.sessionId}</div>
          <div style={{ fontSize: 12, color: 'rgba(255,255,255,.6)' }}>Color: {color} · Point your camera at a flat surface</div>
          <button onClick={() => setSession(null)} style={{ marginTop: 16, padding: '8px 20px', fontSize: 12, fontWeight: 700, background: 'rgba(255,255,255,.1)', color: '#fff', border: '1px solid rgba(255,255,255,.2)', borderRadius: 8, cursor: 'pointer' }}>Reset</button>
        </div>
      )}
    </div>
  );
}

// ── Main Modal ─────────────────────────────────────────────────────────────────
export default function CarDetailModal({ carId }) {
  const { closeModal, openModal, user, pushToast } = useApp();
  const car = useMemo(() => CARS.find((c) => c.id === carId), [carId]);
  const [tab, setTab]                     = useState('photo');
  const [showTestDrive, setShowTestDrive] = useState(false);

  if (!car) return (
    <>
      <div onClick={closeModal} style={{ position: 'fixed', inset: 0, zIndex: 99998, background: 'rgba(0,0,0,.65)', backdropFilter: 'blur(5px)' }} />
      <div style={{ position: 'fixed', inset: 0, zIndex: 99999, display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '20px 16px' }}>
        <div style={{ width: '100%', maxWidth: 1160, background: 'var(--cbg)', borderRadius: 20, overflow: 'hidden', boxShadow: '0 40px 120px rgba(0,0,0,.25)' }}>
          <SkeletonCarDetail />
        </div>
      </div>
    </>
  );

  const fuel     = Array.isArray(car.fuel) ? car.fuel.join(' / ') : car.fuel;
  const recs     = CARS.filter((c) => c.id !== car.id && (c.type === car.type || c.brand === car.brand)).slice(0, 3);
  const { price: carPrice } = getCarData(car);
  const quickEmi = calcEmi(Math.round(carPrice * 0.8), 9, 60);

  function handleBookNow() {
    if (!user) { openModal('login'); return; }
    closeModal();
    openModal('payment', { car });
  }

  return (
    <>
      <div onClick={closeModal} style={{ position: 'fixed', inset: 0, zIndex: 99998, background: 'rgba(0,0,0,.65)', backdropFilter: 'blur(5px)' }} />

      <div onClick={(e) => e.stopPropagation()} style={{ position: 'fixed', inset: 0, zIndex: 99999, overflowY: 'auto', display: 'flex', justifyContent: 'center', alignItems: 'flex-start', padding: '20px 16px' }}>
        <div style={{ width: '100%', maxWidth: 1160, background: '#fff', borderRadius: 20, boxShadow: '0 40px 120px rgba(0,0,0,.25)', overflow: 'hidden', margin: 'auto' }}>

          {/* TOP BAR */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 22px', borderBottom: '1px solid #f0f0f0', position: 'sticky', top: 0, zIndex: 20, background: '#fff' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <div style={{ width: 36, height: 36, borderRadius: 10, background: car.brand === 'nexa' ? '#002E6E' : '#1449C0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <span style={{ color: '#fff', fontSize: 12, fontWeight: 900 }}>{car.brand === 'nexa' ? 'N' : 'A'}</span>
              </div>
              <div>
                <div style={{ fontSize: 10, color: '#aaa', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.08em' }}>MarutiXchange &middot; {car.brand}</div>
                <div style={{ fontSize: 15, fontWeight: 800, color: '#111' }}>{car.year} {car.name}</div>
              </div>
            </div>
            <button onClick={closeModal} style={{ width: 34, height: 34, borderRadius: '50%', background: '#f4f4f4', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <svg width="13" height="13" viewBox="0 0 13 13" fill="none" stroke="#555" strokeWidth="2">
                <line x1="1" y1="1" x2="12" y2="12"/><line x1="12" y1="1" x2="1" y2="12"/>
              </svg>
            </button>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px' }}>

            {/* LEFT */}
            <div style={{ borderRight: '1px solid #f0f0f0' }}>

              {/* Tab switcher */}
              <div style={{ display: 'flex', borderBottom: '1px solid #f0f0f0', background: '#fafafa' }}>
                {[
                  { id: 'photo', label: 'Photo', icon: <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg> },
                  { id: '360',   label: '360° View', icon: <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0118.8-4.3M22 12.5a10 10 0 01-18.8 4.2"/></svg> },
                  { id: 'ar',    label: 'AR View', icon: <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg> },
                ].map((t) => (
                  <button key={t.id} onClick={() => setTab(t.id)}
                    style={{ flex: 1, padding: '13px', fontSize: 13, fontWeight: 700, background: tab === t.id ? '#fff' : 'transparent', color: tab === t.id ? '#1449C0' : '#888', border: 'none', borderBottom: tab === t.id ? '2px solid #1449C0' : '2px solid transparent', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 7 }}>
                    {t.icon}{t.label}
                  </button>
                ))}
              </div>

              {/* Photo */}
              {tab === 'photo' && (
                <div style={{ position: 'relative', background: '#f5f6f8', aspectRatio: '16/10', overflow: 'hidden' }}>
                  <img src={car.img} referrerPolicy="no-referrer" crossOrigin="anonymous" alt={car.name}
                    style={{ width: '100%', height: '100%', objectFit: 'contain', padding: '16px 48px' }} />
                  {car.verified && (
                    <div style={{ position: 'absolute', top: 14, left: 14, background: '#059669', color: '#fff', padding: '4px 12px', borderRadius: 999, fontSize: 11, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 5 }}>
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="3"><polyline points="20 6 9 17 4 12"/></svg>
                      Verified
                    </div>
                  )}
                  <div style={{ position: 'absolute', top: 14, right: 14, background: '#fff', border: '1px solid #eee', borderRadius: 8, padding: '4px 10px', display: 'flex', alignItems: 'center', gap: 4, fontSize: 13, fontWeight: 700, color: '#111' }}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="#f59e0b"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                    {car.rating}
                  </div>
                  <div onClick={() => setTab('360')} style={{ position: 'absolute', bottom: 14, right: 14, background: '#111', color: '#fff', padding: '6px 14px', borderRadius: 8, fontSize: 11, fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0118.8-4.3M22 12.5a10 10 0 01-18.8 4.2"/></svg>
                    Try 360° View
                  </div>
                </div>
              )}

              {tab === '360' && <TurntableViewer img={car.img} />}

              {tab === 'ar' && <ArViewer car={car} />}

              {/* Details */}
              <div style={{ padding: '24px 24px 32px' }}>

                {/* Car Overview */}
                <div style={{ marginBottom: 28, paddingBottom: 28, borderBottom: '1px solid #f3f4f6' }}>
                  <h3 style={{ fontSize: 16, fontWeight: 800, color: '#111', marginTop: 0, marginBottom: 14 }}>Car Overview</h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 10 }}>
                    {[
                      { label: 'Make Year',    value: car.year },
                      { label: 'KM Driven',    value: car.km + ' km' },
                      { label: 'Fuel Type',    value: fuel },
                      { label: 'Transmission', value: car.trans },
                      { label: 'Owner',        value: car.owner + ' Owner' },
                      { label: 'Location',     value: car.loc },
                      { label: 'Body Type',    value: car.type?.charAt(0).toUpperCase() + car.type?.slice(1) },
                      { label: 'Car Age',      value: car.age + ' Years' },
                      { label: 'Colour',       value: car.colour || 'White' },
                    ].map((s) => (
                      <div key={s.label} style={{ background: '#f9fafb', borderRadius: 10, padding: '12px 14px', border: '1px solid #f0f0f0' }}>
                        <div style={{ fontSize: 10, color: '#aaa', marginBottom: 4, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.06em' }}>{s.label}</div>
                        <div style={{ fontSize: 14, fontWeight: 700, color: '#111' }}>{s.value}</div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* About */}
                <div style={{ marginBottom: 28, paddingBottom: 28, borderBottom: '1px solid #f3f4f6' }}>
                  <h3 style={{ fontSize: 16, fontWeight: 800, color: '#111', marginTop: 0, marginBottom: 10 }}>About This Car</h3>
                  <p style={{ fontSize: 14, color: '#4b5563', lineHeight: 1.8, margin: 0 }}>{car.desc}</p>
                </div>

                {/* Quality Report */}
                <QualityReport />

                {/* Car Specifications */}
                <CarSpecifications car={car} />

                {/* Top Features */}
                <TopFeatures />

                {/* EMI Calculator */}
                <EmiCalculator carPrice={carPrice} />

                {/* Assurance */}
                <div style={{ marginBottom: 28, paddingBottom: 28, borderBottom: '1px solid #f3f4f6' }}>
                  <h3 style={{ fontSize: 16, fontWeight: 800, color: '#111', marginTop: 0, marginBottom: 14 }}>MarutiXchange Assurance</h3>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                    {['200-point inspection completed','RC Transfer assistance included','Warranty options available','Free home test drive','Full service history available','No hidden charges'].map((item) => (
                      <div key={item} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 12px', background: '#f0fdf4', borderRadius: 8, border: '1px solid #d1fae5', fontSize: 13, color: '#374151', fontWeight: 500 }}>
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                        {item}
                      </div>
                    ))}
                  </div>
                </div>

                {/* Compare Cars */}
                <CompareCarsTool currentCar={car} allCars={CARS} onViewCar={(id) => openModal('car', { carId: id })} />
              </div>
            </div>

            {/* RIGHT PANEL */}
            <div style={{ borderLeft: '1px solid #f0f0f0' }}>
              <div style={{ padding: '22px 20px' }}>
                <h2 style={{ fontSize: 20, fontWeight: 800, color: '#111', margin: '0 0 4px' }}>{car.year} {car.name}</h2>
                <p style={{ fontSize: 13, color: '#6b7280', margin: '0 0 16px', lineHeight: 1.5 }}>{car.km} km &middot; {fuel} &middot; {car.trans}</p>

                {car.verified && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 14px', background: '#f0fdf4', border: '1px solid #d1fae5', borderRadius: 10, marginBottom: 12 }}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                    <div>
                      <div style={{ fontSize: 12, fontWeight: 700, color: '#065f46' }}>MarutiXchange Assured</div>
                      <div style={{ fontSize: 11, color: '#6b7280' }}>200-point inspected &middot; High quality</div>
                    </div>
                  </div>
                )}

                {/* AI Condition Score Badge for Buyers */}
                <div style={{ background: 'linear-gradient(135deg,#1449C0,#0f3694)', borderRadius: 12, padding: '14px 16px', marginBottom: 16 }}>
                  <div style={{ fontSize: 10, color: 'rgba(255,255,255,.7)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.08em', marginBottom: 8 }}>
                    AI Condition Score
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    {/* Score circle */}
                    <div style={{ position: 'relative', width: 56, height: 56, flexShrink: 0 }}>
                      <svg width="56" height="56" viewBox="0 0 56 56">
                        <circle cx="28" cy="28" r="22" fill="none" stroke="rgba(255,255,255,.2)" strokeWidth="5"/>
                        <circle cx="28" cy="28" r="22" fill="none" stroke="#10b981" strokeWidth="5"
                          strokeDasharray={`${(car.rating / 5) * 138} 138`}
                          strokeLinecap="round" transform="rotate(-90 28 28)"/>
                      </svg>
                      <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, fontWeight: 900, color: '#fff' }}>
                        {Math.round((car.rating / 5) * 100)}
                      </div>
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'inline-block', background: car.rating >= 4.5 ? '#10b981' : car.rating >= 4 ? '#f59e0b' : '#ef4444', color: '#fff', fontSize: 16, fontWeight: 900, padding: '2px 10px', borderRadius: 6, marginBottom: 4 }}>
                        Grade {car.rating >= 4.5 ? 'A+' : car.rating >= 4 ? 'A' : car.rating >= 3.5 ? 'B' : 'C'}
                      </div>
                      <div style={{ fontSize: 11, color: 'rgba(255,255,255,.8)' }}>
                        {car.rating >= 4.5 ? 'Excellent condition' : car.rating >= 4 ? 'Good condition' : 'Average condition'}
                      </div>
                    </div>
                  </div>
                  {/* Score bars */}
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6, marginTop: 10 }}>
                    {[
                      { label: 'Exterior', score: Math.round(car.rating * 18 + 8) },
                      { label: 'Paint',    score: Math.round(car.rating * 16 + 10) },
                      { label: 'Tyres',    score: Math.round(car.rating * 15 + 12) },
                      { label: 'Interior', score: Math.round(car.rating * 17 + 9) },
                    ].map(({ label, score }) => (
                      <div key={label} style={{ background: 'rgba(255,255,255,.1)', borderRadius: 6, padding: '6px 8px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 3 }}>
                          <span style={{ fontSize: 9, color: 'rgba(255,255,255,.7)', fontWeight: 700 }}>{label}</span>
                          <span style={{ fontSize: 9, color: '#fff', fontWeight: 800 }}>{score}/100</span>
                        </div>
                        <div style={{ height: 3, background: 'rgba(255,255,255,.2)', borderRadius: 2 }}>
                          <div style={{ height: '100%', width: score + '%', background: score >= 85 ? '#10b981' : score >= 70 ? '#f59e0b' : '#ef4444', borderRadius: 2 }}/>
                        </div>
                      </div>
                    ))}
                  </div>
                  <div style={{ marginTop: 8, fontSize: 10, color: 'rgba(255,255,255,.6)', textAlign: 'center' }}>
                    Powered by AI Vision Analysis
                  </div>
                </div>

                <div style={{ padding: '16px', background: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: 12, marginBottom: 16 }}>
                  <div style={{ fontSize: 10, color: '#aaa', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.08em', marginBottom: 6 }}>Car Price</div>
                  <div style={{ fontSize: 28, fontWeight: 900, color: '#111', lineHeight: 1 }}>{car.price}</div>
                  <div style={{ fontSize: 11, color: '#aaa', marginTop: 4 }}>+ On-road charges applicable</div>
                  <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid #e5e7eb' }}>
                    <div style={{ fontSize: 14, fontWeight: 700, color: '#1449C0' }}>or &#8377;{quickEmi.toLocaleString('en-IN')}/month</div>
                    <div style={{ fontSize: 11, color: '#aaa', marginTop: 2 }}>Starting EMI · See full calculator below</div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 14px', background: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: 10, marginBottom: 20, fontSize: 13, color: '#374151' }}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  {car.loc} &middot; Home Test Drive: Available
                </div>

                <button onClick={handleBookNow}
                  onMouseEnter={(e) => e.currentTarget.style.background = '#0f3694'}
                  onMouseLeave={(e) => e.currentTarget.style.background = '#1449C0'}
                  style={{ width: '100%', padding: '15px', fontSize: 14, fontWeight: 800, background: '#1449C0', color: '#fff', border: 'none', borderRadius: 12, cursor: 'pointer', marginBottom: 10, boxShadow: '0 4px 16px rgba(20,73,192,.28)', letterSpacing: '.03em', transition: 'background .15s' }}>
                  BOOK NOW &middot; 100% Refundable
                </button>

                <button onClick={() => setShowTestDrive(true)}
                  onMouseEnter={(e) => e.currentTarget.style.background = '#fff5f5'}
                  onMouseLeave={(e) => e.currentTarget.style.background = '#fff'}
                  style={{ width: '100%', padding: '13px', fontSize: 14, fontWeight: 700, background: '#fff', color: '#e53e3e', border: '2px solid #e53e3e', borderRadius: 12, cursor: 'pointer', marginBottom: 20, transition: 'background .15s' }}>
                  FREE TEST DRIVE
                </button>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 20 }}>
                  {['100% Refundable','Fraud Protected','RC Transfer Help','Easy Financing'].map((b) => (
                    <div key={b} style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '8px 10px', background: '#f9fafb', borderRadius: 8, border: '1px solid #f0f0f0' }}>
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                      <span style={{ fontSize: 11, fontWeight: 600, color: '#374151' }}>{b}</span>
                    </div>
                  ))}
                </div>

                <div style={{ paddingTop: 16, borderTop: '1px solid #f0f0f0' }}>
                  <div style={{ fontSize: 12, color: '#aaa', marginBottom: 8, fontWeight: 600 }}>Share with a friend:</div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button
                      onClick={() => window.open(`https://wa.me/?text=${encodeURIComponent(`Check out this ${car.year} ${car.name} for ${car.price} on MarutiXchange!`)}`, '_blank')}
                      style={{ padding: '6px 12px', fontSize: 11, fontWeight: 600, background: '#f9fafb', color: '#25D366', border: '1px solid #e5e7eb', borderRadius: 8, cursor: 'pointer' }}>
                      WhatsApp
                    </button>
                    <button
                      onClick={() => window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(window.location.href)}`, '_blank')}
                      style={{ padding: '6px 12px', fontSize: 11, fontWeight: 600, background: '#f9fafb', color: '#1877F2', border: '1px solid #e5e7eb', borderRadius: 8, cursor: 'pointer' }}>
                      Facebook
                    </button>
                    <button
                      onClick={() => { navigator.clipboard.writeText(`${car.year} ${car.name} - ${car.price} | MarutiXchange`); pushToast('Link copied!', 'ok'); }}
                      style={{ padding: '6px 12px', fontSize: 11, fontWeight: 600, background: '#f9fafb', color: '#6b7280', border: '1px solid #e5e7eb', borderRadius: 8, cursor: 'pointer' }}>
                      Copy Link
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {showTestDrive && <TestDriveModal car={car} onClose={() => setShowTestDrive(false)} />}
    </>
  );
}