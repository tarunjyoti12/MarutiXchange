import { useApp } from '../../context/AppContext';

export default function SellBanner() {
  const { openModal } = useApp();

  return (
    <section
      id="sellSection"
      style={{
        maxWidth: 1240, margin: '64px auto', padding: '40px 28px',
        background: 'var(--seg-grd)', color: '#fff', borderRadius: 28,
        display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'center',
        gap: 24, boxShadow: 'var(--glow2)',
      }}
    >
      <div>
        <div style={{ fontSize: 12, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.1em', opacity: .85, marginBottom: 8 }}>
          For Sellers
        </div>
        <h2 style={{ fontSize: 'clamp(22px, 2.8vw, 36px)', fontWeight: 800, margin: 0, lineHeight: 1.2 }}>
          Sell your Maruti in 48 hours.
        </h2>
        <p style={{ fontSize: 14, opacity: .85, marginTop: 10, maxWidth: 520 }}>
          List in 5 minutes. Verified buyers, real offers, zero tire-kickers. Take the best offer, we'll handle the RC transfer.
        </p>
      </div>
      <button
        onClick={() => openModal('sell')}
        style={{
          background: '#fff', color: 'var(--ac)', fontWeight: 700, fontSize: 14,
          padding: '14px 24px', border: 'none', borderRadius: 12, cursor: 'pointer',
          boxShadow: '0 6px 22px rgba(0,0,0,.18)',
        }}
      >
        Start Listing →
      </button>
    </section>
  );
}
