import { REVIEWS } from '../../data/reviews';

export default function Testimonials() {
  return (
    <section style={{ maxWidth: 1240, margin: '0 auto', padding: '48px 24px' }}>
      <div style={{ marginBottom: 24 }}>
        <div style={{ fontSize: 12, color: 'var(--ac)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.1em', marginBottom: 8 }}>
          What our users say
        </div>
        <h2 style={{ fontSize: 'clamp(22px, 2.8vw, 36px)', fontWeight: 800, color: 'var(--tx1)', margin: 0 }}>
          Real buyers. Real sellers. Real outcomes.
        </h2>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(280px,1fr))', gap: 18 }}>
        {REVIEWS.map((r) => (
          <article
            key={r.name}
            style={{
              background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 18,
              padding: 20, boxShadow: 'var(--csh)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <div style={{
                width: 42, height: 42, borderRadius: '50%',
                background: 'var(--acd)', color: 'var(--ac)', fontWeight: 700,
                display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14,
              }}>
                {r.init}
              </div>
              <div>
                <div style={{ fontWeight: 700, color: 'var(--tx1)' }}>{r.name}</div>
                <div style={{ fontSize: 12, color: 'var(--tx3)' }}>
                  {'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)} · {r.date}
                </div>
              </div>
            </div>
            <p style={{ fontSize: 13, color: 'var(--tx2)', lineHeight: 1.55, margin: 0 }}>{r.text}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
