import { useApp } from '../../context/AppContext';

export default function Footer() {
  const { navigate, openModal, user } = useApp();

  function handleLink(action) {
    switch (action) {
      case 'buy':       return navigate('buy');
      case 'sell':      return user ? openModal('sell') : openModal('login');
      case 'bidding':   return navigate('bidding');
      case 'dashboard': return user ? navigate('dashboard') : openModal('login');
      case 'login':     return openModal('login');
      default:          return null;
    }
  }

  const COLS = [
    {
      heading: 'Buy & Sell',
      links: [
        { label: 'Browse Used Cars',    action: 'buy' },
        { label: 'Sell Your Car',       action: 'sell' },
        { label: 'Live Bid Auctions',   action: 'bidding' },
        { label: 'My Dashboard',        action: 'dashboard' },
        { label: 'EMI Calculator',      action: 'buy' },
      ],
    },
    {
      heading: 'Services',
      links: [
        { label: 'RC Transfer',         action: 'dashboard' },
        { label: 'Car Inspection',      action: null },
        { label: 'Insurance',           action: null },
        { label: 'Warranty Plans',      action: null },
        { label: 'Home Test Drive',     action: null },
      ],
    },
    {
      heading: 'Support',
      links: [
        { label: 'Help Center',         action: null },
        { label: 'Contact Us',          action: 'login' },
        { label: 'Trust & Safety',      action: null },
        { label: 'Report a Listing',    action: null },
        { label: 'Grievance Redressal', action: null },
      ],
    },
    {
      heading: 'Company',
      links: [
        { label: 'About Us',            action: null },
        { label: 'Careers',             action: null },
        { label: 'Blog',                action: null },
        { label: 'Press & Media',       action: null },
        { label: 'Partner With Us',     action: null },
      ],
    },
  ];

  return (
    <footer style={{ background: 'var(--ftr-bg)', borderTop: '1px solid var(--ftr-bd)', marginTop: 80 }}>

      {/* Top CTA strip */}
      <div style={{ background: 'var(--ac)', padding: '20px 24px' }}>
        <div style={{ maxWidth: 1240, margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
          <div>
            <div style={{ fontSize: 16, fontWeight: 800, color: '#fff', marginBottom: 2 }}>Ready to sell your Maruti?</div>
            <div style={{ fontSize: 13, color: 'rgba(255,255,255,.8)' }}>Get the best price in 48 hours. Free inspection included.</div>
          </div>
          <div style={{ display: 'flex', gap: 10 }}>
            <button onClick={() => handleLink('sell')}
              style={{ padding: '10px 22px', fontSize: 13, fontWeight: 800, background: '#fff', color: 'var(--ac)', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
              Sell My Car →
            </button>
            <button onClick={() => handleLink('buy')}
              style={{ padding: '10px 22px', fontSize: 13, fontWeight: 700, background: 'rgba(255,255,255,.15)', color: '#fff', border: '1.5px solid rgba(255,255,255,.4)', borderRadius: 10, cursor: 'pointer' }}>
              Browse Cars
            </button>
          </div>
        </div>
      </div>

      {/* Main footer content */}
      <div style={{ maxWidth: 1240, margin: '0 auto', padding: '48px 24px 32px' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '280px repeat(4, 1fr)', gap: 40 }}>

          {/* Brand column */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 16 }}>
              <div className="seg-mark" style={{ width: 38, height: 38 }}>
                <svg width="20" height="20" viewBox="0 0 20 20" fill="white">
                  <path d="M10 2L3 7v10l7 1 7-1V7L10 2z" fillOpacity=".95" />
                  <path d="M10 2l7 5-7 3-7-3 7-5z" fillOpacity=".55" />
                </svg>
              </div>
              <div>
                <div style={{ fontSize: 18, fontWeight: 800, color: 'var(--tx1)', lineHeight: 1 }}>
                  Maruti<span style={{ color: 'var(--ac)' }}>Xchange</span>
                </div>
                <div style={{ fontSize: 10, color: 'var(--tx3)', fontWeight: 600, letterSpacing: '.08em', textTransform: 'uppercase' }}>Maruti Suzuki Exclusive</div>
              </div>
            </div>

            <p style={{ fontSize: 13, color: 'var(--tx3)', lineHeight: 1.7, marginBottom: 20 }}>
              India's only Maruti-exclusive certified resale marketplace. 30L+ users trust us for verified listings, transparent pricing, and smooth ownership transfers.
            </p>

            {/* Trust badges */}
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 20 }}>
              {['ISO 27001', 'SSL Secured', 'RBI Compliant'].map((b) => (
                <span key={b} style={{ fontSize: 10, fontWeight: 700, padding: '4px 10px', background: 'var(--bg2)', color: 'var(--tx2)', border: '1px solid var(--bd1)', borderRadius: 6, display: 'flex', alignItems: 'center', gap: 4 }}>
                  <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="var(--ver)" strokeWidth="3"><polyline points="20 6 9 17 4 12"/></svg>
                  {b}
                </span>
              ))}
            </div>

            {/* Social */}
            <div style={{ display: 'flex', gap: 8 }}>
              {[
                { label: 'Facebook', d: 'M18 2h-3a5 5 0 00-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 011-1h3z' },
                { label: 'Instagram', d: 'M16 11.37A4 4 0 1112.63 8 4 4 0 0116 11.37zm1.5-4.87h.01M6.5 19.5h11a3 3 0 003-3v-11a3 3 0 00-3-3h-11a3 3 0 00-3 3v11a3 3 0 003 3z' },
                { label: 'Twitter', d: 'M23 3a10.9 10.9 0 01-3.14 1.53 4.48 4.48 0 00-7.86 3v1A10.66 10.66 0 013 4s-4 9 5 13a11.64 11.64 0 01-7 2c9 5 20 0 20-11.5a4.5 4.5 0 00-.08-.83A7.72 7.72 0 0023 3z' },
                { label: 'YouTube', d: 'M22.54 6.42a2.78 2.78 0 00-1.95-1.96C18.88 4 12 4 12 4s-6.88 0-8.59.46a2.78 2.78 0 00-1.95 1.96A29 29 0 001 12a29 29 0 00.46 5.58A2.78 2.78 0 003.41 19.6C5.12 20 12 20 12 20s6.88 0 8.59-.46a2.78 2.78 0 001.95-1.95A29 29 0 0023 12a29 29 0 00-.46-5.58zM9.75 15.02V8.98L15.5 12l-5.75 3.02z' },
              ].map((s) => (
                <div key={s.label}
                  style={{ width: 34, height: 34, borderRadius: 8, background: 'var(--bg2)', border: '1px solid var(--bd1)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', transition: 'all .15s' }}
                  onMouseEnter={(e) => { e.currentTarget.style.background = 'var(--ac)'; e.currentTarget.style.borderColor = 'var(--ac)'; }}
                  onMouseLeave={(e) => { e.currentTarget.style.background = 'var(--bg2)'; e.currentTarget.style.borderColor = 'var(--bd1)'; }}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="var(--tx3)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d={s.d}/>
                  </svg>
                </div>
              ))}
            </div>
          </div>

          {/* Link columns */}
          {COLS.map((col) => (
            <div key={col.heading}>
              <div style={{ fontSize: 11, fontWeight: 800, textTransform: 'uppercase', letterSpacing: '.1em', color: 'var(--tx1)', marginBottom: 16, paddingBottom: 8, borderBottom: '2px solid var(--ac)', display: 'inline-block' }}>
                {col.heading}
              </div>
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: 11 }}>
                {col.links.map((l) => (
                  <li key={l.label}>
                    <a href="#"
                      onClick={(e) => { e.preventDefault(); if (l.action) handleLink(l.action); }}
                      style={{ fontSize: 13, color: l.action ? 'var(--tx2)' : 'var(--tx3)', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: 6, transition: 'color .15s', cursor: l.action ? 'pointer' : 'default' }}
                      onMouseEnter={(e) => { if (l.action) e.currentTarget.style.color = 'var(--ac)'; }}
                      onMouseLeave={(e) => { if (l.action) e.currentTarget.style.color = 'var(--tx2)'; }}
                    >
                      {l.action && (
                        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                          <polyline points="9 18 15 12 9 6"/>
                        </svg>
                      )}
                      {l.label}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Stats row */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: 16, margin: '40px 0 0', padding: '24px 0', borderTop: '1px solid var(--ftr-bd)', borderBottom: '1px solid var(--ftr-bd)' }}>
          {[
            { num: '30L+',     label: 'Registered Users',    color: '#1449C0',
              svg: <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#1449C0" strokeWidth="2" strokeLinecap="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg> },
            { num: '12,000+',  label: 'Verified Listings',   color: '#059669',
              svg: <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2" strokeLinecap="round"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg> },
            { num: '₹2,400Cr', label: 'Total Cars Sold',     color: '#7c3aed',
              svg: <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#7c3aed" strokeWidth="2" strokeLinecap="round"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/><path d="M9 9h.01M12 9h.01M15 9h.01M9 12h6"/></svg> },
            { num: '4.8 / 5',  label: 'Average App Rating',  color: '#f59e0b',
              svg: <svg width="22" height="22" viewBox="0 0 24 24" fill="#f59e0b" stroke="#f59e0b" strokeWidth="1"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg> },
          ].map((s) => (
            <div key={s.label} style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '4px 0' }}>
              <div style={{ width: 52, height: 52, borderRadius: 14, background: `${s.color}15`, border: `1.5px solid ${s.color}30`, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                {s.svg}
              </div>
              <div>
                <div style={{ fontSize: 22, fontWeight: 900, color: 'var(--tx1)', lineHeight: 1 }}>{s.num}</div>
                <div style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 4, fontWeight: 500 }}>{s.label}</div>
              </div>
            </div>
          ))}
        </div>

        {/* App download strip */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, marginTop: 24 }}>
          <div>
            <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--tx1)', marginBottom: 8 }}>Download the MarutiXchange App</div>
            <div style={{ display: 'flex', gap: 10 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 16px', background: '#111', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="white"><path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"/></svg>
                <div>
                  <div style={{ fontSize: 9, color: 'rgba(255,255,255,.7)', lineHeight: 1 }}>Download on the</div>
                  <div style={{ fontSize: 13, fontWeight: 700, color: '#fff', lineHeight: 1.3 }}>App Store</div>
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 16px', background: '#111', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="white"><path d="M3.18 23.76c.3.17.64.22.97.15l12.66-7.32-2.88-2.88-10.75 10.05zM.29 1.33C.11 1.64 0 2.01 0 2.44v19.12c0 .43.11.8.29 1.11l.06.06 10.71-10.71v-.25L.35 1.27l-.06.06zM20.1 10.01l-2.71-1.57-3.17 3.17 3.17 3.17 2.74-1.58c.78-.45.78-1.19-.03-1.19zM3.18.24L15.84 7.56l-2.88 2.88L2.21.39c.3-.22.67-.27.97-.15z"/></svg>
                <div>
                  <div style={{ fontSize: 9, color: 'rgba(255,255,255,.7)', lineHeight: 1 }}>Get it on</div>
                  <div style={{ fontSize: 13, fontWeight: 700, color: '#fff', lineHeight: 1.3 }}>Google Play</div>
                </div>
              </div>
            </div>
          </div>

          {/* Bottom legal links */}
          <div style={{ fontSize: 12, color: 'var(--tx3)', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 6 }}>
            <div style={{ display: 'flex', gap: 16 }}>
              {['Privacy Policy', 'Terms of Use', 'Cookie Policy', 'Sitemap'].map((l) => (
                <a key={l} href="#" onClick={(e) => e.preventDefault()} style={{ color: 'var(--tx3)', textDecoration: 'none' }}
                  onMouseEnter={(e) => e.currentTarget.style.color = 'var(--ac)'}
                  onMouseLeave={(e) => e.currentTarget.style.color = 'var(--tx3)'}>
                  {l}
                </a>
              ))}
            </div>
            <div>© {new Date().getFullYear()} MarutiXchange Pvt. Ltd. · CIN: U74999MH2024PTC000001</div>
          </div>
        </div>
      </div>
    </footer>
  );
}