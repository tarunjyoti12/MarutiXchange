import { useState } from 'react';
import { useCountdown } from '../../hooks/useCountdown';
import { useApp } from '../../context/AppContext';
import { placeBid, buyNow } from '../../services/bidService';
import BidLeaderboard from './BidLeaderboard';

const MIN_INCREMENT = 1000;

function Spinner({ size = 14 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
      style={{ animation: 'spin .8s linear infinite' }}>
      <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
    </svg>
  );
}

export default function BidCard({ bid, onPlaceBid, watched, onToggleWatch }) {
  const remaining = useCountdown(bid.endsAt);
  const { user, openModal, pushToast } = useApp();
  const [showBoard, setShowBoard] = useState(false);
  const [amount,    setAmount]    = useState('');
  const [loading,   setLoading]   = useState(false);
  const [bnLoad,    setBnLoad]    = useState(false);
  const [err,       setErr]       = useState('');

  const isLive    = bid.status === 'live' && remaining !== 'Ended';
  const hasBuyNow = bid.buyNowPrice != null;
  const isWinning = bid.myBid != null && bid.myBid >= bid.currentBid;
  const isOutbid  = bid.myBid != null && bid.myBid  < bid.currentBid;
  const minNext   = parseFloat((bid.currentBid + bid.minIncrement).toFixed(2));

  function saveBid(amt) {
    const prev = JSON.parse(localStorage.getItem('mx-my-bids') || '[]');
    if (!prev.find(b => b.auctionId === bid.id)) {
      localStorage.setItem('mx-my-bids', JSON.stringify([...prev,
        { id: Date.now(), carName: bid.name, bidAmount: amt, auctionId: bid.id, placedAt: new Date().toISOString() }
      ]));
    }
  }

  async function handleBid() {
    if (!user) { openModal('login'); return; }
    const n = parseFloat(amount || minNext);
    if (isNaN(n) || n <= bid.currentBid) { setErr(`Must be above ₹${bid.currentBid.toFixed(2)} L`); return; }
    if ((n - bid.currentBid) * 100000 < MIN_INCREMENT) { setErr(`Min increment ₹1,000`); return; }
    setErr(''); setLoading(true);
    try {
      await placeBid(bid.id, n * 100000, user.name || 'Bidder');
      onPlaceBid(bid.id, n);
      pushToast(`₹${n.toFixed(2)} L bid placed!`, 'ok');
      saveBid(n * 100000);
      setAmount('');
    } catch {
      onPlaceBid(bid.id, n);
      pushToast(`₹${n.toFixed(2)} L bid placed!`, 'ok');
      saveBid(n * 100000);
      setAmount('');
    } finally { setLoading(false); }
  }

  async function handleBuyNow() {
    if (!user) { openModal('login'); return; }
    setBnLoad(true);
    try { await buyNow(bid.id, user.name || 'Buyer'); } catch {}
    finally {
      setBnLoad(false);
      openModal('payment', { car: { id: bid.id, name: bid.name, price: `₹${bid.buyNowPrice?.toFixed(2)} L`, img: bid.img, year: bid.year, km: bid.km, loc: '' } });
      pushToast(`Buy Now at ₹${bid.buyNowPrice?.toFixed(2)} L!`, 'ok');
    }
  }

  return (
    <>
      <style>{`
        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes pulse-dot { 0%,100%{opacity:1}50%{opacity:.3} }
        @keyframes win-glow { 0%,100%{box-shadow:0 0 0 0 rgba(5,150,105,.3)}50%{box-shadow:0 0 0 6px rgba(5,150,105,0)} }
        .bid-card:hover { box-shadow: 0 8px 32px rgba(0,0,0,.12) !important; transform: translateY(-2px); }
        .bid-input:focus { border-color: var(--ac) !important; outline: none; box-shadow: 0 0 0 3px var(--acd); }
        .bid-btn:hover:not(:disabled) { background: #0f3694 !important; }
      `}</style>

      <article className="bid-card" style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 16, overflow: 'hidden', boxShadow: 'var(--csh)', display: 'flex', flexDirection: 'column', transition: 'all .25s' }}>

        {/* ── Image ── */}
        <div style={{ position: 'relative', aspectRatio: '16/9', overflow: 'hidden', background: '#f0f2f5' }}>
          <img src={bid.img} alt={bid.name}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />

          {/* Top badges */}
          <div style={{ position: 'absolute', top: 10, left: 10, display: 'flex', gap: 6 }}>
            <span style={{ background: isLive ? '#ef4444' : '#475569', color: '#fff', padding: '3px 9px', borderRadius: 999, fontSize: 10, fontWeight: 800, display: 'flex', alignItems: 'center', gap: 4 }}>
              {isLive && <span style={{ width: 5, height: 5, background: '#fff', borderRadius: '50%', animation: 'pulse-dot 1s infinite' }}/>}
              {isLive ? 'LIVE' : 'ENDED'}
            </span>
            {bid.verified && (
              <span style={{ background: '#059669', color: '#fff', padding: '3px 9px', borderRadius: 999, fontSize: 10, fontWeight: 700 }}>
                ✓ VERIFIED
              </span>
            )}
          </div>

          {/* Watchlist */}
          <button onClick={() => onToggleWatch(bid.id)}
            style={{ position: 'absolute', top: 10, right: 10, width: 32, height: 32, borderRadius: '50%', background: watched ? 'var(--ac)' : 'rgba(255,255,255,.85)', border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', backdropFilter: 'blur(4px)', boxShadow: '0 2px 8px rgba(0,0,0,.15)', transition: 'all .2s' }}>
            <svg width="14" height="14" fill={watched ? '#fff' : 'none'} stroke={watched ? '#fff' : '#666'} strokeWidth="2" viewBox="0 0 24 24">
              <path d="M12.76 3.76a5.5 5.5 0 017.78 7.78l-7.78 7.78-7.78-7.78a5.5 5.5 0 017.78-7.78z"/>
            </svg>
          </button>

          {/* Timer */}
          <div style={{ position: 'absolute', bottom: 10, right: 10, background: 'rgba(0,0,0,.7)', color: '#fff', padding: '4px 10px', borderRadius: 8, fontSize: 12, fontWeight: 700, backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', gap: 5 }}>
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke={isLive ? '#ef4444' : '#aaa'} strokeWidth="2.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            {remaining}
          </div>
        </div>

        {/* ── Car Info ── */}
        <div style={{ padding: '14px 14px 0' }}>
          <h3 style={{ fontSize: 15, fontWeight: 800, color: 'var(--tx1)', margin: '0 0 2px', lineHeight: 1.2 }}>{bid.name}</h3>
          <p style={{ fontSize: 12, color: 'var(--tx3)', margin: 0 }}>{bid.year} · {bid.km} km · {bid.fuel}</p>
        </div>

        {/* ── Bid Stats ── */}
        <div style={{ margin: '12px 14px 0', background: 'var(--bg2)', borderRadius: 10, padding: '10px 12px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
            <div>
              <div style={{ fontSize: 9, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.1em', color: 'var(--tx3)', marginBottom: 2 }}>Current Highest Bid</div>
              <div style={{ fontSize: 20, fontWeight: 900, color: 'var(--ac)', lineHeight: 1 }}>₹{bid.currentBid.toFixed(2)} L</div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: 9, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.1em', color: 'var(--tx3)', marginBottom: 2 }}>Bidders</div>
              <div style={{ fontSize: 20, fontWeight: 900, color: 'var(--tx1)', lineHeight: 1 }}>{bid.totalBidders}</div>
            </div>
          </div>
          <div style={{ marginTop: 6, paddingTop: 6, borderTop: '1px solid var(--bd1)', fontSize: 10, color: 'var(--tx3)', display: 'flex', gap: 12 }}>
            <span>Min bid: ₹{minNext.toFixed(2)} L</span>
            <span>Min increment: ₹1,000</span>
          </div>
        </div>

        {/* ── Status banners ── */}
        {isWinning && (
          <div style={{ margin: '10px 14px 0', padding: '9px 12px', background: 'linear-gradient(135deg,#059669,#047857)', borderRadius: 10, display: 'flex', alignItems: 'center', gap: 8, animation: 'win-glow 2s infinite' }}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="#fff"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
            <div>
              <div style={{ fontSize: 12, fontWeight: 800, color: '#fff' }}>You are currently winning!</div>
              <div style={{ fontSize: 10, color: 'rgba(255,255,255,.8)' }}>Your bid: ₹{bid.myBid?.toFixed(2)} L</div>
            </div>
          </div>
        )}
        {isOutbid && (
          <div style={{ margin: '10px 14px 0', padding: '9px 12px', background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 10, display: 'flex', alignItems: 'center', gap: 8 }}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ef4444" strokeWidth="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <div>
              <div style={{ fontSize: 12, fontWeight: 800, color: '#dc2626' }}>You've been outbid!</div>
              <div style={{ fontSize: 10, color: '#ef4444' }}>Bid higher to stay in — current: ₹{bid.currentBid.toFixed(2)} L</div>
            </div>
          </div>
        )}

        {/* ── Bid Input + Button ── */}
        <div style={{ padding: '10px 14px 0' }}>
          {isLive ? (
            <>
              <div style={{ display: 'flex', gap: 8 }}>
                <div style={{ flex: 1, position: 'relative' }}>
                  <span style={{ position: 'absolute', left: 11, top: '50%', transform: 'translateY(-50%)', fontSize: 13, fontWeight: 700, color: 'var(--tx3)', pointerEvents: 'none' }}>₹</span>
                  <input
                    type="number" step="0.05"
                    value={amount}
                    onChange={e => { setAmount(e.target.value); setErr(''); }}
                    onKeyDown={e => e.key === 'Enter' && handleBid()}
                    placeholder={minNext.toFixed(2)}
                    className="bid-input"
                    style={{ width: '100%', padding: '10px 10px 10px 24px', fontSize: 14, fontWeight: 700, border: `1.5px solid ${err ? '#fca5a5' : 'var(--bd1)'}`, borderRadius: 10, background: 'var(--bg1)', color: 'var(--tx1)', boxSizing: 'border-box', transition: 'all .2s' }}
                  />
                </div>
                <button onClick={handleBid} disabled={loading} className="bid-btn"
                  style={{ padding: '10px 20px', fontSize: 14, fontWeight: 800, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 10, cursor: loading ? 'wait' : 'pointer', display: 'flex', alignItems: 'center', gap: 6, transition: 'background .2s', whiteSpace: 'nowrap', minWidth: 80 }}>
                  {loading ? <Spinner/> : <>
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
                    Bid
                  </>}
                </button>
              </div>
              {err && <div style={{ marginTop: 5, fontSize: 11, color: '#ef4444', fontWeight: 600 }}>{err}</div>}
            </>
          ) : (
            <div style={{ padding: '10px', background: 'var(--bg2)', borderRadius: 10, textAlign: 'center', fontSize: 12, color: 'var(--tx3)', fontWeight: 600 }}>
              Auction has ended
            </div>
          )}
        </div>

        {/* ── Buy Now ── */}
        {hasBuyNow && isLive && (
          <div style={{ margin: '8px 14px 0', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 12px', background: 'rgba(124,58,237,.06)', border: '1px solid rgba(124,58,237,.2)', borderRadius: 10 }}>
            <div>
              <div style={{ fontSize: 9, fontWeight: 700, textTransform: 'uppercase', color: '#7c3aed', marginBottom: 1 }}>Buy Now</div>
              <div style={{ fontSize: 15, fontWeight: 900, color: '#7c3aed' }}>₹{bid.buyNowPrice?.toFixed(2)} L</div>
            </div>
            <button onClick={handleBuyNow} disabled={bnLoad}
              style={{ padding: '8px 16px', fontSize: 12, fontWeight: 800, background: bnLoad ? '#cbd5e1' : '#7c3aed', color: '#fff', border: 'none', borderRadius: 8, cursor: bnLoad ? 'not-allowed' : 'pointer', display: 'flex', alignItems: 'center', gap: 5 }}>
              {bnLoad ? <Spinner size={12}/> : 'Buy Now →'}
            </button>
          </div>
        )}

        {/* ── Pay Now (when won) ── */}
        {!isLive && isWinning && (
          <div style={{ margin: '8px 14px 0' }}>
            <button
              onClick={() => openModal('payment', { car: { id: bid.id, name: bid.name, price: `₹${bid.myBid?.toFixed(2)} L`, img: bid.img, year: bid.year, km: bid.km, loc: '' } })}
              style={{ width: '100%', padding: '12px', fontSize: 13, fontWeight: 800, background: 'linear-gradient(135deg,#059669,#047857)', color: '#fff', border: 'none', borderRadius: 10, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, boxShadow: '0 4px 14px rgba(5,150,105,.35)' }}>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.5"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
              Pay Now · ₹{bid.myBid?.toFixed(2)} L
            </button>
          </div>
        )}

        {/* ── Leaderboard Toggle ── */}
        <div style={{ margin: '10px 14px 14px' }}>
          <button onClick={() => setShowBoard(v => !v)}
            style={{ width: '100%', padding: '8px 12px', background: 'var(--bg2)', border: '1px solid var(--bd1)', borderRadius: 10, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'space-between', transition: 'all .15s' }}
            onMouseEnter={e => e.currentTarget.style.background = 'var(--acd)'}
            onMouseLeave={e => e.currentTarget.style.background = 'var(--bg2)'}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="var(--ac)" strokeWidth="2">
                <rect x="2" y="10" width="6" height="12" rx="1"/><rect x="9" y="6" width="6" height="16" rx="1"/><rect x="16" y="13" width="6" height="9" rx="1"/>
              </svg>
              <span style={{ fontSize: 11, fontWeight: 700, color: 'var(--tx2)' }}>View Leaderboard</span>
              <span style={{ fontSize: 10, padding: '1px 6px', background: 'var(--acd)', color: 'var(--ac)', borderRadius: 999, fontWeight: 700 }}>{bid.totalBidders}</span>
            </div>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--tx3)" strokeWidth="2.5">
              {showBoard ? <polyline points="18 15 12 9 6 15"/> : <polyline points="6 9 12 15 18 9"/>}
            </svg>
          </button>

          {showBoard && (
            <div style={{ marginTop: 8, border: '1px solid var(--bd1)', borderRadius: 10, padding: '12px', background: 'var(--cbg)' }}>
              <BidLeaderboard
                auctionId={bid.id}
                isLive={isLive}
                myBid={bid.myBid}
                totalBidders={bid.totalBidders}
                currentBid={bid.currentBid}
              />
            </div>
          )}
        </div>

      </article>
    </>
  );
}