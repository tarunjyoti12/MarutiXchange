const REASONS = [
  { icon: 'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z', label: '100% Maruti Only', desc: 'Every listing is a genuine Maruti Suzuki vehicle — no other brands, no compromises.' },
  { icon: 'M15 12a3 3 0 11-6 0 3 3 0 016 0z M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z', label: 'Full Transparency', desc: 'Service history, accident records, and inspection reports — all visible before you bid.' },
  { icon: 'M13 10V3L4 14h7v7l9-11h-7z', label: 'Live Auction Bidding', desc: 'Real-time bids, live leaderboard, and outbid alerts via push notifications.' },
  { icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z', label: 'Best Price Guaranteed', desc: 'Auction model ensures you always get the highest offer the market will pay.' },
  { icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4', label: 'Hassle-Free RC Transfer', desc: 'We manage all RTO paperwork and deliver ownership transfer in 7 working days.' },
  { icon: 'M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z', label: '24x7 Support', desc: 'Dedicated relationship managers available round the clock for buyers and sellers.' },
];

export default function WhyMaruti() {
  return (
    <section style={{ padding: '72px 24px' }}>
      <div style={{ maxWidth: 1140, margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: 52 }}>
          <div style={{ display: 'inline-block', background: 'var(--acd)', color: 'var(--ac)', fontSize: 11, fontWeight: 800, letterSpacing: '.1em', textTransform: 'uppercase', padding: '4px 14px', borderRadius: 999, marginBottom: 14 }}>Why MarutiXchange</div>
          <h2 style={{ fontSize: 32, fontWeight: 900, color: 'var(--tx1)', margin: '0 0 12px' }}>India's most trusted Maruti marketplace</h2>
          <p style={{ fontSize: 15, color: 'var(--tx3)', maxWidth: 520, margin: '0 auto' }}>Built exclusively for Maruti Suzuki — the brand 30 lakh Indian families trust every year.</p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 16 }}>
          {REASONS.map((r) => (
            <div key={r.label} style={{ display: 'flex', gap: 16, padding: '20px', background: 'var(--cbg)', borderRadius: 14, border: '1px solid var(--bd1)' }}>
              <div style={{ width: 44, height: 44, borderRadius: 12, background: 'var(--acd)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--ac)" strokeWidth="2" strokeLinecap="round"><path d={r.icon}/></svg>
              </div>
              <div>
                <div style={{ fontSize: 14, fontWeight: 800, color: 'var(--tx1)', marginBottom: 5 }}>{r.label}</div>
                <div style={{ fontSize: 12, color: 'var(--tx3)', lineHeight: 1.6 }}>{r.desc}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}