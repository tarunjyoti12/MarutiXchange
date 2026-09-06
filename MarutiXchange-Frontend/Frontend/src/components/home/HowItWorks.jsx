import { useApp } from '../../context/AppContext';

const STEPS = [
  { n: '01', icon: 'M12 4v16m8-8H4', title: 'List Your Car', desc: 'Fill a quick form with car details, photos and your asking price. Takes under 5 minutes.', color: '#1449C0' },
  { n: '02', icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z', title: '200-Point Inspection', desc: 'Our certified Maruti experts inspect every car and assign a verified quality score.', color: '#059669' },
  { n: '03', icon: 'M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2z', title: 'Bid or Buy Now', desc: 'Buyers place live bids or click Buy Now. You get the best price the market offers.', color: '#7c3aed' },
  { n: '04', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2', title: 'RC Transfer Done', desc: 'We handle all paperwork and RTO formalities. Ownership transfers smoothly in 7 days.', color: '#D97706' },
];

export default function HowItWorks() {
  const { openModal, user } = useApp();
  return (
    <section style={{ padding: '72px 24px', background: 'var(--bg2)' }}>
      <div style={{ maxWidth: 1140, margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: 52 }}>
          <div style={{ display: 'inline-block', background: 'var(--acd)', color: 'var(--ac)', fontSize: 11, fontWeight: 800, letterSpacing: '.1em', textTransform: 'uppercase', padding: '4px 14px', borderRadius: 999, marginBottom: 14 }}>How It Works</div>
          <h2 style={{ fontSize: 32, fontWeight: 900, color: 'var(--tx1)', margin: '0 0 12px' }}>Sell or buy in 4 simple steps</h2>
          <p style={{ fontSize: 15, color: 'var(--tx3)', maxWidth: 520, margin: '0 auto' }}>MarutiXchange makes every transaction transparent, fast, and stress-free.</p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 20 }}>
          {STEPS.map((s, i) => (
            <div key={s.n} style={{ background: 'var(--cbg)', borderRadius: 18, padding: '28px 24px', border: '1px solid var(--bd1)', position: 'relative', overflow: 'hidden' }}>
              <div style={{ position: 'absolute', top: 16, right: 20, fontSize: 42, fontWeight: 900, color: 'var(--bg2)', lineHeight: 1 }}>{s.n}</div>
              <div style={{ width: 48, height: 48, borderRadius: 14, background: `${s.color}15`, display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: 18 }}>
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={s.color} strokeWidth="2" strokeLinecap="round"><path d={s.icon}/></svg>
              </div>
              <h3 style={{ fontSize: 16, fontWeight: 800, color: 'var(--tx1)', margin: '0 0 8px' }}>{s.title}</h3>
              <p style={{ fontSize: 13, color: 'var(--tx3)', margin: 0, lineHeight: 1.6 }}>{s.desc}</p>
              {i < STEPS.length - 1 && (
                <div style={{ display: 'none' }}></div>
              )}
            </div>
          ))}
        </div>

        
      </div>
    </section>
  );
}