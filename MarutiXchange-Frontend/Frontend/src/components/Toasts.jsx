import { useApp } from '../context/AppContext';

const KIND_STYLE = {
  info: { bg: 'var(--bg1)', bd: 'var(--bd2)', c: 'var(--tx1)' },
  ok:   { bg: '#ECFDF5',   bd: '#10b981',    c: '#065F46' },
  err:  { bg: '#FEF2F2',   bd: '#ef4444',    c: '#991B1B' },
  warn: { bg: '#FFFBEB',   bd: '#f59e0b',    c: '#92400E' },
};

export default function Toasts() {
  const { toasts } = useApp();
  return (
    <div
      style={{
        position: 'fixed', right: 24, bottom: 24, zIndex: 9999999,
        display: 'flex', flexDirection: 'column', gap: 10, maxWidth: 320,
      }}
    >
      {toasts.map((t) => {
        const s = KIND_STYLE[t.kind] || KIND_STYLE.info;
        return (
          <div
            key={t.id}
            style={{
              background: s.bg, color: s.c,
              border: `1px solid ${s.bd}`, borderRadius: 12, padding: '12px 16px',
              fontSize: 13, fontWeight: 500, boxShadow: '0 10px 30px rgba(0,0,0,.12)',
              animation: 'slideInRight .25s cubic-bezier(.22,1,.36,1)',
            }}
          >
            {t.msg}
          </div>
        );
      })}
    </div>
  );
}