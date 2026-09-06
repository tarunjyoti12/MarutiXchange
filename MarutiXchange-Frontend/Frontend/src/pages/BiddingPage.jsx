import { useEffect, useMemo, useRef, useState, useCallback } from 'react';
import { BIDS } from '../data/bids';
import { useApp } from '../context/AppContext';
import { getLiveAuctions } from '../services/bidService';
import BidCard from '../components/bidding/BidCard';
import { SkeletonBidCard } from '../components/Skeleton';
import AuctionCreateModal from '../components/modals/AuctionCreateModal';

// ── WebSocket — only connects when bidding-service is running ─────────────────
// Stops retrying after 3 failed attempts to avoid console spam
function useAuctionWebSocket(userId, onOutbid) {
  const wsRef       = useRef(null);
  const retriesRef  = useRef(0);
  const MAX_RETRIES = 3;

  const handleOutbid = useCallback(onOutbid, []);

  useEffect(() => {
    // Don't attempt if not logged in
    if (!userId) return;

    function connect() {
      // Stop retrying after max attempts
      if (retriesRef.current >= MAX_RETRIES) return;

      try {
        const ws = new WebSocket(`ws://localhost:8086/ws/bids?userId=${userId}`);
        wsRef.current = ws;

        ws.onopen = () => {
          // Connected — reset retry counter
          retriesRef.current = 0;
        };

        ws.onmessage = (event) => {
          try {
            const msg = JSON.parse(event.data);
            if (msg.type === 'OUTBID' && msg.bidderId === userId) {
              handleOutbid(msg);
            }
          } catch (e) {
            // ignore malformed messages
          }
        };

        ws.onclose = () => {
          retriesRef.current += 1;
          // Only reconnect if under retry limit
          if (retriesRef.current < MAX_RETRIES) {
            setTimeout(connect, 5000);
          }
          // Silently stop after MAX_RETRIES — service not running
        };

        ws.onerror = () => {
          ws.close();
        };

      } catch (e) {
        // WebSocket not available — silently ignore
      }
    }

    connect();
    return () => {
      retriesRef.current = MAX_RETRIES; // prevent reconnect on unmount
      wsRef.current?.close();
    };
  }, [userId]);
}

export default function BiddingPage() {
  const { segment, pushToast, user, openModal } = useApp();
  const [bids, setBids]                         = useState(BIDS);
  const [watchlist, setWatchlist]               = useState(new Set());
  // Sync global Arena/Nexa toggle to local segTab on mount
  const [segTab, setSegTab] = useState(() => {
    const s = localStorage.getItem('mm-seg');
    return (s === 'arena' || s === 'nexa') ? s : 'all';
  });

  // When global segment changes (Nav toggle), sync to Bidding page filter
  useEffect(() => {
    if (segment === 'arena' || segment === 'nexa') {
      setSegTab(segment);
    }
  }, [segment]);
  const [loading, setLoading]                   = useState(true);
  const [showCreateModal, setShowCreateModal]   = useState(false);

  // ── Merge localStorage bids into auction list ──────────────────────────────
  // Restores myBid state after navigation so bids persist across page changes
  function mergeLocalBids(auctionList) {
    const saved = JSON.parse(localStorage.getItem('mx-my-bids') || '[]');
    if (!saved.length) return auctionList;
    return auctionList.map((auction) => {
      const localBid = saved.find((b) => b.auctionId === auction.id);
      if (!localBid) return auction;
      const bidAmtL = localBid.bidAmount / 100000;
      return {
        ...auction,
        myBid:      bidAmtL,
        // Keep currentBid as highest of backend value or local bid
        currentBid: Math.max(auction.currentBid, bidAmtL),
      };
    });
  }

  // ── Fetch live auctions ────────────────────────────────────────────────────
  useEffect(() => {
    async function fetchAuctions() {
      setLoading(true);
      try {
        const res = await getLiveAuctions();
        if (res?.data && Array.isArray(res.data) && res.data.length >= 3) {
          const mapped = res.data.map((a) => ({
            id:           a.id,
            name:         a.carName,
            brand:        a.brand?.toLowerCase() || 'arena',
            year:         a.year,
            km:           a.mileage?.toLocaleString('en-IN') || '0',
            fuel:         a.fuelType || 'Petrol',
            img:          a.carImageUrl || 'https://imgd.aeplcdn.com/664x374/n/cw/ec/107543/brezza-exterior-right-front-three-quarter-13.jpeg?isig=0&q=80',
            startPrice:   (a.startingPrice || 0) / 100000,
            currentBid:   (a.currentHighestBid || a.startingPrice || 0) / 100000,
            buyNowPrice:  a.buyNowPrice ? a.buyNowPrice / 100000 : null,
            totalBidders: a.totalBids || 0,
            myBid:        null,
            status:       a.status?.toLowerCase() || 'live',
            endsAt:       new Date(a.endTime).getTime(),
            verified:     true,
            minIncrement: (a.minBidIncrement || 10000) / 100000,
          }));
          setBids(mergeLocalBids(mapped));
        } else {
          setBids(mergeLocalBids(BIDS));
        }
      } catch (err) {
        console.log('Using mock auction data:', err.message);
        setBids(mergeLocalBids(BIDS));
      } finally {
        setLoading(false);
      }
    }
    fetchAuctions();
  }, []);

  // ── Real-time outbid alerts — stops after 3 failed attempts ───────────────
  useAuctionWebSocket(user?.id, (msg) => {
    const car     = bids.find((b) => b.id === msg.auctionId);
    const carName = car?.name || 'your auction';
    pushToast(
      `You have been outbid on ${carName}. New highest: Rs.${(msg.bidAmount / 100000).toFixed(2)} L`,
      'warn'
    );
    setBids((prev) => prev.map((b) =>
      b.id === msg.auctionId
        ? { ...b, currentBid: msg.bidAmount / 100000, totalBidders: (b.totalBidders || 0) + 1 }
        : b
    ));
  });

  const filtered = useMemo(() => {
    return bids.filter((b) => segTab === 'all' || b.brand === segTab);
  }, [bids, segTab]);

  const liveCount    = bids.filter((b) => b.status === 'live').length;
  const totalBidders = bids.reduce((s, b) => s + b.totalBidders, 0);
  const myBidsCount  = bids.filter((b) => b.myBid != null).length;

  function toggleWatch(id) {
    setWatchlist((prev) => {
      const next = new Set(prev);
      if (next.has(id)) { next.delete(id); pushToast('Removed from watchlist', 'info'); }
      else              { next.add(id);    pushToast('Added to watchlist', 'ok'); }
      return next;
    });
  }

  function placeBid(id, amount) {
    setBids((prev) => {
      const updated = prev.map((b) => b.id === id
        ? { ...b, myBid: amount, currentBid: amount, totalBidders: b.totalBidders + (b.myBid ? 0 : 1) }
        : b,
      );
      const bid = prev.find((b) => b.id === id);
      if (bid) {
        const existing = JSON.parse(localStorage.getItem('mx-my-bids') || '[]');
        if (!existing.find((b) => b.auctionId === id)) {
          localStorage.setItem('mx-my-bids', JSON.stringify([
            ...existing,
            { id: Date.now(), carName: bid.name, bidAmount: amount * 100000, auctionId: id, placedAt: new Date().toISOString() },
          ]));
        }
      }
      return updated;
    });
  }

  return (
    <div style={{ maxWidth: 1340, margin: '0 auto', padding: '32px 24px 80px' }}>

      {/* Hero banner */}
      <section style={{ background: 'var(--seg-grd)', color: '#fff', borderRadius: 28, padding: '36px 32px', marginBottom: 32, boxShadow: 'var(--glow2)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 14 }}>
              <span style={{ width: 10, height: 10, background: '#ef4444', borderRadius: '50%', animation: 'pulse 1.2s infinite' }} />
              <span style={{ fontSize: 12, fontWeight: 800, textTransform: 'uppercase', letterSpacing: '.1em' }}>Live Auction Platform</span>
            </div>
            <h1 style={{ fontSize: 'clamp(26px, 3.4vw, 42px)', fontWeight: 800, margin: 0, lineHeight: 1.15 }}>
              Bid on verified {segment === 'nexa' ? 'Nexa' : 'Arena'} cars.
            </h1>
            <p style={{ fontSize: 14, opacity: .85, marginTop: 10, maxWidth: 620, marginBottom: 0 }}>
              Set your max, we'll auto-bid for you. Every car is RC-verified and inspection-checked.
            </p>
          </div>

          {user && (
            <button onClick={() => setShowCreateModal(true)}
              style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '12px 20px', background: 'rgba(255,255,255,.15)', color: '#fff', border: '1.5px solid rgba(255,255,255,.3)', borderRadius: 12, cursor: 'pointer', fontSize: 13, fontWeight: 700, backdropFilter: 'blur(4px)', flexShrink: 0 }}>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              Create Auction
            </button>
          )}
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(160px,1fr))', gap: 16, marginTop: 24 }}>
          <Stat num={liveCount}    label="Live Auctions" />
          <Stat num={totalBidders} label="Active Bidders" />
          <Stat num={myBidsCount}  label="My Bids" />
        </div>
      </section>

      {/* Segment filter */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        {['all', 'arena', 'nexa'].map((k) => (
          <button key={k} onClick={() => setSegTab(k)}
            style={{ padding: '8px 18px', fontSize: 13, fontWeight: 700, border: `1.5px solid ${segTab === k ? 'var(--ac)' : 'var(--bd1)'}`, background: segTab === k ? 'var(--ac)' : 'var(--bg2)', color: segTab === k ? '#fff' : 'var(--tx2)', borderRadius: 10, cursor: 'pointer', textTransform: 'capitalize' }}>
            {k} ({k === 'all' ? bids.length : bids.filter((b) => b.brand === k).length})
          </button>
        ))}
      </div>

      {/* Loading skeleton */}
      {loading && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(300px,1fr))', gap: 20 }}>
          {[1,2,3].map((i) => <SkeletonBidCard key={i} />)}
        </div>
      )}

      {/* Auction cards */}
      {!loading && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(300px,1fr))', gap: 20 }}>
          {filtered.map((b) => (
            <BidCard key={b.id} bid={b} watched={watchlist.has(b.id)} onToggleWatch={toggleWatch} onPlaceBid={placeBid} />
          ))}
        </div>
      )}

      {/* Empty state */}
      {!loading && filtered.length === 0 && (
        <div style={{ textAlign: 'center', padding: 80, color: 'var(--tx3)', border: '1px dashed var(--bd1)', borderRadius: 18 }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 12, opacity: .35 }}>
            <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="var(--tx2)" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M14.5 2.5l7 7-3 3-7-7z"/><path d="M4 20l6.5-6.5"/><path d="M2 22l4-4"/>
            </svg>
          </div>
          <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--tx1)', marginBottom: 8 }}>
            No {segTab} auctions running right now
          </div>
          <div style={{ fontSize: 13 }}>Check back soon for new listings.</div>
          {user && (
            <button onClick={() => setShowCreateModal(true)}
              style={{ marginTop: 16, padding: '10px 22px', fontSize: 13, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
              Create the first auction
            </button>
          )}
        </div>
      )}

      {showCreateModal && <AuctionCreateModal onClose={() => setShowCreateModal(false)} />}
    </div>
  );
}

function Stat({ num, label }) {
  return (
    <div>
      <div style={{ fontSize: 30, fontWeight: 800, lineHeight: 1 }}>{num}</div>
      <div style={{ fontSize: 12, opacity: .8, marginTop: 6, textTransform: 'uppercase', letterSpacing: '.08em' }}>{label}</div>
    </div>
  );
}