import { useEffect, useState } from 'react';
import { getBidsByAuction } from '../../services/bidService';
import { useApp } from '../../context/AppContext';

// ── Name masking ──────────────────────────────────────────────────────────────
function maskName(name, isMe) {
  if (!name) return 'Bidder';
  if (isMe) return name + ' (You)';
  const first = name.trim().split(' ')[0];
  if (first.length <= 2) return first + '***';
  return first[0] + '*'.repeat(Math.max(first.length - 2, 2)) + first[first.length - 1];
}

// ── Rank badge ────────────────────────────────────────────────────────────────
function RankBadge({ rank }) {
  if (rank === 1) return (
    <div style={{ width: 26, height: 26, borderRadius: '50%', background: 'linear-gradient(135deg,#F59E0B,#D97706)', boxShadow: '0 2px 8px rgba(217,119,6,.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
      <svg width="12" height="12" viewBox="0 0 24 24" fill="#fff"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
    </div>
  );
  if (rank === 2) return (
    <div style={{ width: 26, height: 26, borderRadius: '50%', background: 'linear-gradient(135deg,#9CA3AF,#6B7280)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
      <span style={{ fontSize: 10, fontWeight: 800, color: '#fff' }}>2</span>
    </div>
  );
  if (rank === 3) return (
    <div style={{ width: 26, height: 26, borderRadius: '50%', background: 'linear-gradient(135deg,#CD7F32,#A0522D)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
      <span style={{ fontSize: 10, fontWeight: 800, color: '#fff' }}>3</span>
    </div>
  );
  return (
    <div style={{ width: 26, height: 26, borderRadius: '50%', background: 'var(--bg2)', border: '1px solid var(--bd1)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
      <span style={{ fontSize: 10, fontWeight: 700, color: 'var(--tx3)' }}>{rank}</span>
    </div>
  );
}

// ── Mock data generator — safe, no external dependencies ──────────────────────
// 8 different name pools — one per card, picked by string/number hash
const ALL_NAMES = [
  ['Rahul S.', 'Priya M.', 'Amit K.', 'Sneha R.', 'Vikram P.'],
  ['Ananya T.', 'Rohan D.', 'Kavya N.', 'Arjun V.', 'Meera L.'],
  ['Deepak S.', 'Nisha P.', 'Suresh K.', 'Pooja R.', 'Kiran M.'],
  ['Divya A.', 'Manish T.', 'Sunita B.', 'Ravi C.', 'Lakshmi S.'],
  ['Sanjay K.', 'Aarti V.', 'Nikhil B.', 'Rekha P.', 'Gaurav M.'],
  ['Harish T.', 'Swati D.', 'Pankaj R.', 'Neha S.', 'Arun K.'],
  ['Vishal N.', 'Preeti A.', 'Rajesh M.', 'Anjali T.', 'Sunil P.'],
  ['Kartik S.', 'Pallavi R.', 'Vivek K.', 'Shreya M.', 'Dinesh V.'],
];

// Hash function that works for both string and number IDs
function hashId(id) {
  if (typeof id === 'number') return Math.abs(id);
  if (typeof id === 'string') {
    let hash = 0;
    for (let i = 0; i < id.length; i++) {
      hash = ((hash << 5) - hash) + id.charCodeAt(i);
      hash |= 0;
    }
    return Math.abs(hash);
  }
  return 0;
}

function getMockBids(auctionId, currentBid, myBid, userId, userName) {
  // Safe base price
  const base = (typeof currentBid === 'number' && currentBid > 0) ? currentBid : 10;

  // Use hash so string IDs like 'b1','b2' give different name pools
  const poolIndex = hashId(auctionId) % ALL_NAMES.length;
  const names = ALL_NAMES[poolIndex] || ALL_NAMES[0];

  // Safe offsets
  const offsets = [0.10, 0.25, 0.45, 0.70, 1.05];

  const result = [];

  // Add user's own bid if placed
  if (myBid != null && typeof myBid === 'number') {
    result.push({
      bidderId:    userId || 0,
      bidderName:  userName || 'You',
      bidAmount:   Math.round(myBid * 100000),
      isWinningBid: true,
      status:      'ACTIVE',
    });
  }

  // Add 5 mock bidders — guaranteed safe
  for (let i = 0; i < 5; i++) {
    const name   = (names && names[i]) ? names[i] : `Bidder ${i + 1}`;
    const offset = offsets[i] || (i * 0.15 + 0.10);
    const ceiling = (myBid != null && typeof myBid === 'number') ? myBid : base;
    const amt    = Math.round(Math.max((ceiling - offset - 0.05) * 100000, 50000) / 1000) * 1000;

    result.push({
      bidderId:    4000 + i,
      bidderName:  name,
      bidAmount:   amt,
      isWinningBid: false,
      status:      myBid != null ? 'OUTBID' : 'ACTIVE',
    });
  }

  // Sort descending
  return result.sort((a, b) => b.bidAmount - a.bidAmount);
}

// ── Main component ────────────────────────────────────────────────────────────
export default function BidLeaderboard({ auctionId, isLive, myBid, totalBidders, currentBid }) {
  const { user, openModal } = useApp();
  const [bids,     setBids]     = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [countdown, setCountdown] = useState(10);

  function loadBids() {
    // Always set mock data immediately
    const mock = getMockBids(auctionId, currentBid, myBid, user?.id, user?.name);
    setBids(mock);

    // Then try backend
    setLoading(true);
    getBidsByAuction(auctionId)
      .then(res => {
        const list = Array.isArray(res?.data) ? res.data : [];
        if (list.length > 0) setBids(list);
        // If empty, keep mock data already set
      })
      .catch(() => {
        // Keep mock data already set
      })
      .finally(() => {
        setLoading(false);
        setCountdown(10);
      });
  }

  // Load on mount
  useEffect(() => {
    loadBids();
  }, [auctionId]);

  // Rebuild mock when myBid changes
  useEffect(() => {
    setBids(getMockBids(auctionId, currentBid, myBid, user?.id, user?.name));
  }, [myBid, currentBid]);

  // Auto refresh when live
  useEffect(() => {
    if (!isLive) return;
    const iv1 = setInterval(loadBids, 10000);
    const iv2 = setInterval(() => setCountdown(c => c > 0 ? c - 1 : 10), 1000);
    return () => { clearInterval(iv1); clearInterval(iv2); };
  }, [isLive, auctionId]);

  const visible = bids.slice(0, expanded ? 10 : 5);

  // Show login wall if user not signed in
  if (!user) {
    return (
      <div style={{ position: 'relative', minHeight: 160 }}>
        {/* Blurred preview */}
        <div style={{ filter: 'blur(4px)', pointerEvents: 'none', opacity: 0.4 }}>
          {[1,2,3].map(i => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '7px 10px', marginBottom: 3, background: 'var(--bg2)', borderRadius: 8 }}>
              <div style={{ width: 26, height: 26, borderRadius: '50%', background: 'var(--bd1)' }}/>
              <div style={{ width: 26, height: 26, borderRadius: '50%', background: 'var(--bd1)' }}/>
              <div style={{ flex: 1, height: 12, background: 'var(--bd1)', borderRadius: 4 }}/>
              <div style={{ width: 60, height: 12, background: 'var(--bd1)', borderRadius: 4 }}/>
            </div>
          ))}
        </div>
        {/* Lock overlay */}
        <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 10 }}>
          <div style={{ width: 40, height: 40, borderRadius: '50%', background: 'var(--acd)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--ac)" strokeWidth="2.5">
              <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/>
            </svg>
          </div>
          <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--tx1)', textAlign: 'center' }}>Sign in to see the leaderboard</div>
          <button
            onClick={() => openModal('login')}
            style={{ padding: '8px 20px', fontSize: 12, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
            Sign In
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
        <span style={{ fontSize: 11, fontWeight: 700, color: 'var(--tx2)', textTransform: 'uppercase', letterSpacing: '.06em' }}>
          Top Bidders
        </span>
        {isLive && (
          <button onClick={loadBids} disabled={loading}
            style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 10, color: 'var(--tx3)', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }}>
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
              style={{ animation: loading ? 'spin .8s linear infinite' : 'none' }}>
              <path d="M23 4v6h-6"/><path d="M1 20v-6h6"/>
              <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
            </svg>
            {countdown}s
          </button>
        )}
      </div>

      {/* Bid rows */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
        {visible.map((b, i) => {
          const rank  = i + 1;
          const isMe  = user && b.bidderId === user.id;
          const amtL  = ((b.bidAmount || 0) / 100000).toFixed(2);
          const isTop = rank === 1;

          return (
            <div key={`${b.bidderId}-${i}`}
              style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '7px 10px', borderRadius: 8, background: isMe ? 'var(--acd)' : isTop ? 'rgba(245,158,11,.05)' : 'transparent', border: isMe ? '1px solid var(--acd2)' : isTop ? '1px solid rgba(245,158,11,.15)' : '1px solid transparent' }}>

              <RankBadge rank={rank} />

              {/* Avatar */}
              <div style={{ width: 26, height: 26, borderRadius: '50%', background: isMe ? 'var(--ac)' : isTop ? 'rgba(245,158,11,.15)' : 'var(--bg2)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 10, fontWeight: 800, color: isMe ? '#fff' : isTop ? '#D97706' : 'var(--tx3)', flexShrink: 0 }}>
                {(b.bidderName || 'B')[0].toUpperCase()}
              </div>

              {/* Name + status */}
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 12, fontWeight: isMe ? 700 : 600, color: isMe ? 'var(--ac)' : 'var(--tx1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {maskName(b.bidderName, isMe)}
                </div>
                <div style={{ fontSize: 9, marginTop: 1, fontWeight: 600, color: isTop ? '#D97706' : b.status === 'OUTBID' ? '#ef4444' : '#6b7280' }}>
                  {isTop ? '★ Highest Bidder' : b.status === 'OUTBID' ? 'Outbid' : 'Active'}
                </div>
              </div>

              {/* Amount */}
              <div style={{ textAlign: 'right', flexShrink: 0 }}>
                <div style={{ fontSize: 12, fontWeight: 800, color: isTop ? '#D97706' : isMe ? 'var(--ac)' : 'var(--tx2)' }}>
                  ₹{amtL} L
                </div>
                {rank > 1 && <div style={{ fontSize: 9, color: 'var(--tx3)' }}>#{rank}</div>}
              </div>
            </div>
          );
        })}
      </div>

      {/* Show more */}
      {bids.length > 5 && (
        <button onClick={() => setExpanded(v => !v)}
          style={{ marginTop: 8, width: '100%', padding: '6px', fontSize: 11, fontWeight: 600, background: 'none', border: '1px solid var(--bd1)', borderRadius: 7, color: 'var(--tx3)', cursor: 'pointer' }}>
          {expanded ? 'Show less ▲' : `+${bids.length - 5} more bidders ▼`}
        </button>
      )}
    </div>
  );
}