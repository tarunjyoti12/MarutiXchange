import { useEffect, useRef, useState } from 'react';
import { useApp } from '../../context/AppContext';

// Animates a number counter from 0 → target. Used for hero stats.
function useCountUp(target, duration = 1400) {
  const [value, setValue] = useState(0);
  useEffect(() => {
    let start;
    let raf;
    const step = (ts) => {
      if (!start) start = ts;
      const p = Math.min(1, (ts - start) / duration);
      const eased = 1 - Math.pow(1 - p, 3);
      setValue(target * eased);
      if (p < 1) raf = requestAnimationFrame(step);
    };
    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
  }, [target, duration]);
  return value;
}

function Stat({ value, suffix, label, decimal = false }) {
  const v = useCountUp(value);
  return (
    <div className="stat-it">
      <div className="sv" style={{ fontSize: 32, fontWeight: 800, color: 'var(--tx1)', lineHeight: 1 }}>
        {decimal ? v.toFixed(1) : Math.round(v)}
        <span style={{ fontSize: 20, color: 'var(--ac)', marginLeft: 2 }}>{suffix}</span>
      </div>
      <div className="sl" style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 6 }}>{label}</div>
    </div>
  );
}

// Lightweight particle canvas (replaces the vanilla implementation).
function ParticleCanvas() {
  const ref = useRef(null);

  useEffect(() => {
    const canvas = ref.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let pts = [];
    let raf;

    function resize() {
      canvas.width = canvas.clientWidth;
      canvas.height = canvas.clientHeight;
      const count = Math.min(70, Math.floor((canvas.width * canvas.height) / 22000));
      pts = Array.from({ length: count }, () => ({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        vx: (Math.random() - 0.5) * 0.3,
        vy: (Math.random() - 0.5) * 0.3,
        r: Math.random() * 1.6 + 0.4,
      }));
    }

    function draw() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      const accent = getComputedStyle(document.documentElement).getPropertyValue('--ac').trim() || '#1449C0';
      ctx.fillStyle = accent;
      pts.forEach((p) => {
        p.x += p.vx; p.y += p.vy;
        if (p.x < 0 || p.x > canvas.width)  p.vx *= -1;
        if (p.y < 0 || p.y > canvas.height) p.vy *= -1;
        ctx.globalAlpha = 0.35;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fill();
      });
      raf = requestAnimationFrame(draw);
    }

    resize();
    draw();
    window.addEventListener('resize', resize);
    return () => {
      cancelAnimationFrame(raf);
      window.removeEventListener('resize', resize);
    };
  }, []);

  return (
    <canvas
      ref={ref}
      style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', pointerEvents: 'none' }}
    />
  );
}

export default function Hero() {
  const { navigate } = useApp();

  function scrollToSell() {
    document.getElementById('sellSection')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  return (
    <section className="hero" style={{ padding: 0, position: 'relative', overflow: 'hidden' }}>
      <div className="hero-bg" />
      <div className="hero-mesh" />
      <div className="hero-grid" />
      <div className="orb orb1" />
      <div className="orb orb2" />
      <div className="orb orb3" />
      <ParticleCanvas />
      <div className="hero-inner">
        <div className="hero-pill">
          <div className="ldot" />
          India's Only Maruti-Exclusive Resale Platform
        </div>
        <h1 className="hero-h1">
          Find Your<br />
          <span className="word-acc">Perfect Maruti.</span><br />
          <span className="word-ghost">Buy. Sell. Trust.</span>
        </h1>
        <p className="hero-sub">
          India's largest C2C marketplace exclusively for Maruti Suzuki. 12,000+ verified listings across Nexa &amp; Arena segments.
        </p>
        <div className="hero-btns">
          <button className="btn-hero p" onClick={() => navigate('buy')}>
            <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M5 17H3v-5l2-5h14l2 5v5h-2M5 17h14M5 17a2 2 0 1 0 4 0m6 0a2 2 0 1 0 4 0" />
            </svg>
            Browse Cars
          </button>
          <button className="btn-hero g" onClick={scrollToSell}>
            <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z" />
              <line x1="7" y1="7" x2="7.01" y2="7" />
            </svg>
            Sell Your Car
          </button>
        </div>
        <div className="hero-stats">
          <Stat value={12}   suffix="K+" label="Cars Listed" />
          <Stat value={98}   suffix="%"  label="Sellers Verified" />
          <Stat value={4.8}  suffix="★"  label="Platform Rating" decimal />
          <Stat value={50}   suffix="K+" label="Happy Buyers" />
        </div>
      </div>
    </section>
  );
}
