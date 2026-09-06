const BADGES = [
  { title: 'Verified Sellers',     body: 'RC + Aadhaar matched. Fraud blocked before it reaches you.' },
  { title: '100-point Inspection', body: 'Every featured listing is physically inspected.' },
  { title: 'Smooth RC Transfer',   body: 'We handle the paperwork end-to-end in every state.' },
  { title: 'Price Transparency',   body: 'Live market data, not dealer markup guesswork.' },
];

export default function TrustStrip() {
  return (
    <section style={{ padding: '48px 24px', maxWidth: 1240, margin: '0 auto' }}>
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit,minmax(220px,1fr))',
        gap: 18,
      }}>
        {BADGES.map((b) => (
          <div
            key={b.title}
            style={{
              background: 'var(--cbg)',
              border: '1px solid var(--bd1)',
              borderRadius: 18,
              padding: '20px 22px',
              boxShadow: 'var(--csh)',
            }}
          >
            <div style={{
              width: 36, height: 36, borderRadius: 10,
              background: 'var(--acd)', color: 'var(--ac)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              marginBottom: 12,
            }}>
              <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path d="M9 12l2 2 4-4" /><path d="M12 2l8 4v6a10 10 0 01-8 10A10 10 0 014 12V6l8-4z" />
              </svg>
            </div>
            <div style={{ fontSize: 15, fontWeight: 700, color: 'var(--tx1)', marginBottom: 4 }}>
              {b.title}
            </div>
            <div style={{ fontSize: 13, color: 'var(--tx3)', lineHeight: 1.5 }}>
              {b.body}
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
