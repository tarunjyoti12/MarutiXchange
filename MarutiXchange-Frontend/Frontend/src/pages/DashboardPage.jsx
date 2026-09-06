import { useEffect, useState } from 'react';
import { CARS } from '../data/cars';
import { useApp } from '../context/AppContext';
import { getMyListings, getSellerAnalytics } from '../services/carService';
import { getMyBids } from '../services/bidService';
import { getWatchlist, getLocalWatchlist } from '../services/watchlistService';
import { getBuyerActivity, replyToBuyerActivity } from '../services/buyerActivityService';
import { SkeletonStatCard, SkeletonListingRow } from '../components/Skeleton';
import CarCard from '../components/CarCard';
import RcTransferTab from '../components/RcTransferTab';
import { REVIEWS } from '../data/reviews';

const TABS = [
  { id: 'listings',  label: 'My Listings' },
  { id: 'activity',  label: 'Buyer Activity' },
  { id: 'rc',        label: 'RC Transfer' },
  { id: 'reviews',   label: 'Reviews' },
  { id: 'mybids',    label: 'My Bids' },
  { id: 'saved', label: 'Saved Cars' },
];

// ── Inline SVG icons (no emoji) ────────────────────────────────────────────────
function IconLock() {
  return (
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none"
      stroke="var(--tx3)" strokeWidth="1.5" strokeLinecap="round">
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0110 0v4" />
    </svg>
  );
}

function IconHeart({ size = 36 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
      stroke="var(--tx3)" strokeWidth="1.5" strokeLinecap="round">
      <path d="M12.76 3.76a5.5 5.5 0 017.78 7.78l-7.78 7.78-7.78-7.78a5.5 5.5 0 017.78-7.78z" />
    </svg>
  );
}

function IconCheck({ size = 12 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
      stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
      <polyline points="20 6 9 17 4 12" />
    </svg>
  );
}

export default function DashboardPage() {
  const { user, navigate } = useApp();
  const [tab, setTab] = useState('listings');
  const [listings, setListings] = useState([]);
  const [myBids, setMyBids] = useState([]);
  const [watchlistCars, setWatchlistCars] = useState([]);
  const [analytics, setAnalytics] = useState({
    totalViews: '12,481', saves: '342',
    inquiries: '78', testDrives: '14',
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    async function fetchData() {
      setLoading(true);

      try {
        const listRes = await getMyListings(user.id);
        if (listRes?.data?.length > 0) {
          setListings(listRes.data.map((c) => ({
            id: c.id,
            name: `${c.brand} ${c.model}`,
            price: `₹${(c.askingPrice / 100000).toFixed(2)} L`,
            year: c.year,
            loc: c.city,
            img: c.images?.[0]?.imageUrl || CARS[0].img,
            verified: c.isCertified || false,
          })));
        }
      } catch (err) {
        console.log('Backend not available — showing empty listings:', err.message);
        setListings([]);
      }

      try {
        const localBids = JSON.parse(localStorage.getItem('mx-my-bids') || '[]');
        if (localBids.length > 0) {
          setMyBids(localBids);
        } else {
          const bidsRes = await getMyBids();
          if (bidsRes?.data?.length > 0) setMyBids(bidsRes.data);
        }
      } catch (err) {
        const localBids = JSON.parse(localStorage.getItem('mx-my-bids') || '[]');
        setMyBids(localBids);
      }

      try {
        const localItems = getLocalWatchlist();
        if (localItems.length > 0) {
          setWatchlistCars(localItems.map((w) => ({
            id: w.carListingId, name: w.carName, price: w.carPrice,
            img: w.carImgUrl, brand: w.carBrand || 'arena', year: w.carYear,
            fuel: ['Petrol'], trans: 'Manual', km: '0', loc: '',
            owner: '1st', rating: 4.5, verified: false, desc: '',
          })));
        } else {
          const watchRes = await getWatchlist(user.id);
          if (watchRes?.data?.length > 0) {
            setWatchlistCars(watchRes.data.map((w) => ({
              id: w.carListingId, name: w.carName, price: w.carPrice,
              img: w.carImgUrl, brand: w.carBrand || 'arena', year: w.carYear,
              fuel: ['Petrol'], trans: 'Manual', km: '0', loc: '',
              owner: '1st', rating: 4.5, verified: false, desc: '',
            })));
          }
        }
      } catch (err) {
        console.log('Watchlist error:', err.message);
      }

      try {
        const analyticsRes = await getSellerAnalytics(user.id);
        if (analyticsRes?.data && (
          analyticsRes.data.totalViews > 0 ||
          analyticsRes.data.totalSaves > 0
        )) {
          setAnalytics({
            totalViews: analyticsRes.data.totalViews?.toLocaleString('en-IN') || '12,481',
            saves:      analyticsRes.data.totalSaves?.toString()             || '342',
            inquiries:  analyticsRes.data.totalInquiries?.toString()         || '78',
            testDrives: analyticsRes.data.totalTestDrives?.toString()        || '14',
          });
        }
      } catch (err) {
        console.log('Using mock analytics:', err.message);
      }

      setLoading(false);
    }
    fetchData();
  }, [user]);

  if (!user) {
    return (
      <div style={{ maxWidth: 600, margin: '80px auto', textAlign: 'center', padding: 40 }}>
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'center' }}>
          <IconLock />
        </div>
        <h2 style={{ fontSize: 24, fontWeight: 800, color: 'var(--tx1)', marginBottom: 8 }}>
          Sign in to view Dashboard
        </h2>
        <p style={{ fontSize: 14, color: 'var(--tx3)', marginBottom: 24 }}>
          Please login to manage your listings, bids and watchlist.
        </p>
        <button onClick={() => navigate('home')}
          style={{ padding: '12px 24px', fontSize: 14, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 12, cursor: 'pointer' }}>
          Go to Home
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 1240, margin: '0 auto', padding: '32px 24px 80px' }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 'clamp(24px, 3vw, 38px)', fontWeight: 800, color: 'var(--tx1)', margin: 0 }}>
          Dashboard
        </h1>
        <p style={{ fontSize: 13, color: 'var(--tx3)', marginTop: 6 }}>
          Welcome back, {user.name}
        </p>
      </div>

      {/* Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(200px,1fr))', gap: 16, marginBottom: 32 }}>
        <StatCard label="Total Views"  value={analytics.totalViews} delta="+18%" />
        <StatCard label="Saves"        value={analytics.saves}      delta="+7%" />
        <StatCard label="Inquiries"    value={analytics.inquiries}  delta="+22%" />
        <StatCard label="Test Drives"  value={analytics.testDrives} delta="+5%" />
        <StatCard label="Saved Cars"   value={watchlistCars.length.toString()} delta="" />
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 6, borderBottom: '1px solid var(--bd1)', marginBottom: 24, overflowX: 'auto' }}>
        {TABS.map((t) => (
          <button key={t.id} onClick={() => setTab(t.id)}
            style={{
              padding: '10px 18px', fontSize: 13, fontWeight: 700,
              background: 'transparent', border: 'none', cursor: 'pointer',
              color: tab === t.id ? 'var(--ac)' : 'var(--tx3)',
              borderBottom: `2px solid ${tab === t.id ? 'var(--ac)' : 'transparent'}`,
              whiteSpace: 'nowrap',
            }}>
            {t.label}
            {t.id === 'saved' && watchlistCars.length > 0 && (
              <span style={{ marginLeft: 6, background: 'var(--ac)', color: '#fff', borderRadius: 999, fontSize: 10, padding: '1px 6px', fontWeight: 800 }}>
                {watchlistCars.length}
              </span>
            )}
            {t.id === 'mybids' && myBids.length > 0 && (
              <span style={{ marginLeft: 6, background: '#ef4444', color: '#fff', borderRadius: 999, fontSize: 10, padding: '1px 6px', fontWeight: 800 }}>
                {myBids.length}
              </span>
            )}
          </button>
        ))}
      </div>

      {tab === 'listings'  && <ListingsTable listings={listings} />}
      {tab === 'activity'  && <BuyerActivityTab user={user} />}
      {tab === 'rc'        && <RcTransferTab />}
      {tab === 'reviews'   && <ReviewsTab user={user} />}
      {tab === 'mybids'    && <MyBidsTab bids={myBids} />}
      {tab === 'saved'     && <WatchlistTab cars={watchlistCars} onBrowse={() => navigate('buy')} />}
    </div>
  );
}

const SPARKLINES = {
  'Total Views':  [42, 58, 51, 73, 89, 95, 112, 98, 124, 118, 139, 148],
  'Saves':        [12, 14, 11, 18, 22, 19, 28, 24, 31, 27, 34, 38],
  'Inquiries':    [3, 4, 3, 5, 6, 5, 8, 7, 9, 8, 10, 11],
  'Test Drives':  [1, 1, 2, 1, 2, 2, 3, 2, 3, 2, 3, 3],
  'Saved Cars':   [0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5],
};

function Sparkline({ data = [], color = '#1449C0' }) {
  if (!data.length) return null;
  const w = 80, h = 28;
  const min = Math.min(...data), max = Math.max(...data);
  const range = max - min || 1;
  const pts = data.map((v, i) => {
    const x = (i / (data.length - 1)) * w;
    const y = h - ((v - min) / range) * h * 0.85 - 2;
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  }).join(' ');
  return (
    <svg width={w} height={h} viewBox={`0 0 ${w} ${h}`} style={{ display: 'block' }}>
      <polyline points={pts} fill="none" stroke={color} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx={pts.split(' ').pop().split(',')[0]} cy={pts.split(' ').pop().split(',')[1]} r="2.5" fill={color} />
    </svg>
  );
}

function StatCard({ label, value, delta }) {
  const sparkData = SPARKLINES[label] || [];
  const isPositive = delta?.startsWith('+');
  return (
    <div style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 16, padding: 18, boxShadow: 'var(--csh)', position: 'relative', overflow: 'hidden' }}>
      <div style={{ fontSize: 11, color: 'var(--tx3)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.08em', marginBottom: 10 }}>
        {label}
      </div>
      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
        <div>
          <div style={{ fontSize: 28, fontWeight: 800, color: 'var(--tx1)', lineHeight: 1 }}>{value}</div>
          {delta && (
            <div style={{ fontSize: 12, fontWeight: 700, color: isPositive ? '#059669' : '#dc2626', marginTop: 6, display: 'flex', alignItems: 'center', gap: 3 }}>
              {isPositive
                ? <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2.5"><polyline points="18 15 12 9 6 15"/></svg>
                : <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2.5"><polyline points="6 9 12 15 18 9"/></svg>
              }
              {delta} this month
            </div>
          )}
        </div>
        <Sparkline data={sparkData} color={isPositive ? '#059669' : '#1449C0'} />
      </div>
    </div>
  );
}

function ListingsTable({ listings }) {
  if (listings.length === 0) {
    return (
      <div style={{ background: 'var(--cbg)', border: '1px dashed var(--bd1)', borderRadius: 18, padding: 60, textAlign: 'center', color: 'var(--tx3)' }}>
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--tx3)" strokeWidth="1.5" style={{ display: 'block', margin: '0 auto 14px', opacity: .4 }}>
          <rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/>
        </svg>
        <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--tx1)', marginBottom: 8 }}>No listings yet</div>
        <div style={{ fontSize: 13, marginBottom: 20 }}>Cars you list for sale will appear here.</div>
        <button
          onClick={() => document.querySelector('[data-sell]')?.click()}
          style={{ padding: '10px 22px', fontSize: 13, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
          List Your Car
        </button>
      </div>
    );
  }
  return (
    <div style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 18, overflow: 'hidden', boxShadow: 'var(--csh)' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
        <thead>
          <tr style={{ background: 'var(--bg2)' }}>
            {['Car', 'Price', 'Views', 'Saves', 'Status', 'Actions'].map((h) => (
              <th key={h} style={{ textAlign: 'left', padding: '12px 16px', fontSize: 11, textTransform: 'uppercase', letterSpacing: '.06em', color: 'var(--tx2)', fontWeight: 700 }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {listings.map((c) => (
            <tr key={c.id} style={{ borderTop: '1px solid var(--bd0)' }}>
              <td style={{ padding: '12px 16px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <img src={c.img} alt="" style={{ width: 54, height: 36, borderRadius: 6, objectFit: 'cover' }} />
                  <div>
                    <div style={{ fontWeight: 700, color: 'var(--tx1)' }}>{c.name}</div>
                    <div style={{ fontSize: 11, color: 'var(--tx3)' }}>{c.year} · {c.loc}</div>
                  </div>
                </div>
              </td>
              <td style={{ padding: '12px 16px', color: 'var(--ac)', fontWeight: 700 }}>{c.price}</td>
              <td style={{ padding: '12px 16px', color: 'var(--tx2)', fontWeight: 600 }}>
                {Math.floor(Math.random() * 800 + 200).toLocaleString('en-IN')}
              </td>
              <td style={{ padding: '12px 16px', color: 'var(--tx2)', fontWeight: 600 }}>
                {Math.floor(Math.random() * 60 + 10)}
              </td>
              <td style={{ padding: '12px 16px' }}>
                <span style={{ background: c.verified ? 'var(--chip-on)' : 'var(--bg2)', color: c.verified ? 'var(--chip-on-c)' : 'var(--tx3)', padding: '3px 10px', borderRadius: 999, fontSize: 11, fontWeight: 700 }}>
                  {c.verified ? 'Verified' : 'Pending'}
                </span>
              </td>
              <td style={{ padding: '12px 16px' }}>
                <button style={{ padding: '6px 12px', background: 'var(--acd)', color: 'var(--ac)', border: 'none', borderRadius: 8, fontSize: 12, fontWeight: 700, cursor: 'pointer' }}>
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function MyBidsTab({ bids }) {
  if (bids.length === 0) {
    return <Placeholder title="No bids yet" body="Place a bid on the Live Auctions page to see it here." />;
  }
  return (
    <div style={{ display: 'grid', gap: 14 }}>
      {bids.map((b, i) => (
        <div key={b.id || i} style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 14, padding: 14, display: 'flex', gap: 14, alignItems: 'center' }}>
          <div style={{ flex: 1 }}>
            <div style={{ fontWeight: 700, color: 'var(--tx1)' }}>{b.carName || b.name}</div>
            <div style={{ fontSize: 12, color: 'var(--tx3)', marginTop: 4 }}>
              Your bid: ₹{((b.bidAmount || b.myBid || 0) / 100000).toFixed(2)} L
            </div>
            {b.placedAt && (
              <div style={{ fontSize: 11, color: 'var(--tx3)', marginTop: 2 }}>
                Placed: {new Date(b.placedAt).toLocaleDateString('en-IN')}
              </div>
            )}
          </div>
          <span style={{ background: 'var(--chip-on)', color: 'var(--chip-on-c)', padding: '4px 12px', borderRadius: 999, fontSize: 11, fontWeight: 700 }}>
            Active
          </span>
        </div>
      ))}
    </div>
  );
}

function WatchlistTab({ cars, onBrowse }) {
  if (cars.length === 0) {
    return (
      <div style={{ background: 'var(--cbg)', border: '1px dashed var(--bd1)', borderRadius: 18, padding: 60, textAlign: 'center', color: 'var(--tx3)' }}>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 12 }}>
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none"
            stroke="var(--tx3)" strokeWidth="1.5" strokeLinecap="round">
            <path d="M12.76 3.76a5.5 5.5 0 017.78 7.78l-7.78 7.78-7.78-7.78a5.5 5.5 0 017.78-7.78z" />
          </svg>
        </div>
        <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--tx1)', marginBottom: 8 }}>No saved cars yet</div>
        <div style={{ fontSize: 13, marginBottom: 20 }}>
          Click the heart icon on any listing to save it here.
        </div>
        <button onClick={onBrowse}
          style={{ padding: '10px 22px', fontSize: 13, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
          Browse Cars
        </button>
      </div>
    );
  }
  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: 20 }}>
      {cars.map((c) => <CarCard key={c.id} car={c} />)}
    </div>
  );
}

function BuyerActivityTab() {
  const { user } = useApp();
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isDemoMode, setIsDemoMode] = useState(false);
  const [replyingId, setReplyingId] = useState(null);
  const [replyText, setReplyText] = useState('');
  const [replySuccess, setReplySuccess] = useState(null);

  const typeConfig = {
    inquiry:    { label: 'Inquiry',    bg: '#eff6ff', color: '#1449C0', border: '#dbeafe' },
    offer:      { label: 'Offer',      bg: '#f0fdf4', color: '#059669', border: '#d1fae5' },
    testdrive:  { label: 'Test Drive', bg: '#faf5ff', color: '#7c3aed', border: '#ede9fe' },
    INQUIRY:    { label: 'Inquiry',    bg: '#eff6ff', color: '#1449C0', border: '#dbeafe' },
    OFFER:      { label: 'Offer',      bg: '#f0fdf4', color: '#059669', border: '#d1fae5' },
    TEST_DRIVE: { label: 'Test Drive', bg: '#faf5ff', color: '#7c3aed', border: '#ede9fe' },
  };

  useEffect(() => {
    if (!user?.id) return;
    fetchActivities();
  }, [user]);

  async function fetchActivities() {
    setLoading(true);
    try {
      const res = await getBuyerActivity(user.id, 7);
      if (res?.data && Array.isArray(res.data) && res.data.length >= 0) {
        const mapped = res.data.map((item) => ({
          id:   item.id || item.activityId,
          type: (item.activityType || item.type || 'inquiry').toLowerCase().replace('_', ''),
          car:  item.carName || item.carTitle || 'Unknown Car',
          user: item.buyerName || item.userName || 'Unknown Buyer',
          time: item.timeAgo || formatTimeAgo(item.createdAt),
          msg:  item.message || item.content || '',
        }));
        setActivities(mapped);
        setIsDemoMode(false);
      } else {
        setActivities([]);
        setIsDemoMode(false);
      }
    } catch (err) {
      console.error('BuyerActivity API failed:', err.message);
      setIsDemoMode(false);
      setActivities([]);
    } finally {
      setLoading(false);
    }
  }

  function formatTimeAgo(dateStr) {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 60)  return `${mins} minutes ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24)   return `${hrs} hour${hrs > 1 ? 's' : ''} ago`;
    const days = Math.floor(hrs / 24);
    if (days === 1) return 'Yesterday';
    return `${days} days ago`;
  }

  async function handleReply(activityId) {
    if (!replyText.trim()) return;
    try {
      await replyToBuyerActivity(activityId, replyText.trim());
      setReplySuccess(activityId);
      setReplyingId(null);
      setReplyText('');
      setTimeout(() => setReplySuccess(null), 3000);
    } catch (err) {
      console.error('Reply failed:', err.message);
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {[1, 2, 3].map((i) => (
          <div key={i} style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 14, padding: '14px 16px', height: 80, opacity: 0.5 }} />
        ))}
      </div>
    );
  }

  if (activities.length === 0) {
    return (
      <div style={{ background: 'var(--cbg)', border: '1px dashed var(--bd1)', borderRadius: 18, padding: 60, textAlign: 'center', color: 'var(--tx3)' }}>
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--tx3)" strokeWidth="1.5" style={{ display: 'block', margin: '0 auto 14px', opacity: 0.4 }}>
          <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
        </svg>
        <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--tx1)', marginBottom: 8 }}>No buyer activity yet</div>
        <div style={{ fontSize: 13 }}>Inquiries, offers and test drive requests from buyers will appear here.</div>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
        <div style={{ fontSize: 13, color: 'var(--tx3)', fontWeight: 600 }}>
          {activities.length} interaction{activities.length !== 1 ? 's' : ''} in the last 7 days
        </div>
        {isDemoMode && (
          <span style={{ background: '#fef3c7', color: '#92400e', fontSize: 11, fontWeight: 700, padding: '3px 10px', borderRadius: 999 }}>
            Demo data — connect backend to see real inquiries
          </span>
        )}
      </div>

      {activities.map((a) => {
        const cfg = typeConfig[a.type] || typeConfig['inquiry'];
        const typeKey = a.type.replace('test_drive', 'testdrive');
        return (
          <div key={a.id} style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 14, padding: '14px 16px', display: 'flex', gap: 14, flexDirection: 'column' }}>
            <div style={{ display: 'flex', gap: 14 }}>
              <div style={{ width: 36, height: 36, borderRadius: '50%', background: cfg.bg, border: `1px solid ${cfg.border}`, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                {typeKey === 'inquiry'   && <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={cfg.color} strokeWidth="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>}
                {typeKey === 'offer'     && <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={cfg.color} strokeWidth="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>}
                {typeKey === 'testdrive' && <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={cfg.color} strokeWidth="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 4 }}>
                  <div>
                    <span style={{ background: cfg.bg, color: cfg.color, border: `1px solid ${cfg.border}`, fontSize: 10, fontWeight: 700, padding: '2px 8px', borderRadius: 999, marginRight: 8 }}>{cfg.label}</span>
                    <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--tx1)' }}>{a.user}</span>
                    <span style={{ fontSize: 12, color: 'var(--tx3)', marginLeft: 6 }}>on {a.car}</span>
                  </div>
                  <span style={{ fontSize: 11, color: 'var(--tx3)', flexShrink: 0, marginLeft: 8 }}>{a.time}</span>
                </div>
                <p style={{ fontSize: 13, color: 'var(--tx2)', margin: 0, lineHeight: 1.5 }}>"{a.msg}"</p>
              </div>
              {replySuccess === a.id ? (
                <span style={{ padding: '6px 14px', fontSize: 12, fontWeight: 700, background: '#d1fae5', color: '#059669', borderRadius: 8, flexShrink: 0, alignSelf: 'center' }}>Sent!</span>
              ) : (
                <button
                  onClick={() => setReplyingId(replyingId === a.id ? null : a.id)}
                  style={{ padding: '6px 14px', fontSize: 12, fontWeight: 700, background: 'var(--acd)', color: 'var(--ac)', border: 'none', borderRadius: 8, cursor: 'pointer', flexShrink: 0, alignSelf: 'center' }}>
                  Reply
                </button>
              )}
            </div>
            {replyingId === a.id && (
              <div style={{ display: 'flex', gap: 8, paddingLeft: 50 }}>
                <input
                  value={replyText}
                  onChange={(e) => setReplyText(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleReply(a.id)}
                  placeholder="Type your reply..."
                  style={{ flex: 1, padding: '8px 12px', fontSize: 13, border: '1px solid var(--bd1)', borderRadius: 8, background: 'var(--bg2)', color: 'var(--tx1)', outline: 'none' }}
                />
                <button
                  onClick={() => handleReply(a.id)}
                  style={{ padding: '8px 16px', fontSize: 12, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
                  Send
                </button>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}

function ReviewsTab({ user }) {
  const [filter, setFilter] = useState('all');
  const allReviews = REVIEWS.map((r, i) => ({ ...r, id: i + 1, car: ['Maruti Brezza', 'Grand Vitara', 'Maruti Swift', 'Maruti Baleno', 'Maruti Dzire', 'Maruti Fronx'][i % 6] }));
  const filtered = filter === 'all' ? allReviews : allReviews.filter((r) => r.rating === parseInt(filter));
  const avgRating = (allReviews.reduce((s, r) => s + r.rating, 0) / allReviews.length).toFixed(1);

  return (
    <div>
      <div style={{ display: 'grid', gridTemplateColumns: 'auto 1fr', gap: 24, background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 16, padding: 20, marginBottom: 20 }}>
        <div style={{ textAlign: 'center', padding: '0 24px 0 0', borderRight: '1px solid var(--bd1)' }}>
          <div style={{ fontSize: 48, fontWeight: 900, color: 'var(--tx1)', lineHeight: 1 }}>{avgRating}</div>
          <div style={{ display: 'flex', gap: 3, justifyContent: 'center', margin: '8px 0 4px' }}>
            {[1,2,3,4,5].map((s) => (
              <svg key={s} width="16" height="16" viewBox="0 0 24 24" fill={s <= Math.round(avgRating) ? '#f59e0b' : '#e5e7eb'}><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
            ))}
          </div>
          <div style={{ fontSize: 12, color: 'var(--tx3)' }}>{allReviews.length} reviews</div>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 6 }}>
          {[5,4,3,2,1].map((star) => {
            const count = allReviews.filter((r) => r.rating === star).length;
            const pct = Math.round((count / allReviews.length) * 100);
            return (
              <div key={star} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12 }}>
                <span style={{ color: 'var(--tx2)', width: 16, textAlign: 'right', fontWeight: 600 }}>{star}</span>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="#f59e0b"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                <div style={{ flex: 1, height: 8, background: 'var(--bg2)', borderRadius: 4, overflow: 'hidden' }}>
                  <div style={{ width: `${pct}%`, height: '100%', background: '#f59e0b', borderRadius: 4 }} />
                </div>
                <span style={{ color: 'var(--tx3)', width: 28 }}>{count}</span>
              </div>
            );
          })}
        </div>
      </div>
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        {['all', '5', '4', '3'].map((f) => (
          <button key={f} onClick={() => setFilter(f)}
            style={{ padding: '6px 14px', fontSize: 12, fontWeight: 700, border: `1.5px solid ${filter === f ? 'var(--ac)' : 'var(--bd1)'}`, background: filter === f ? 'var(--ac)' : 'var(--bg2)', color: filter === f ? '#fff' : 'var(--tx2)', borderRadius: 8, cursor: 'pointer' }}>
            {f === 'all' ? 'All Reviews' : `${f} Stars`}
          </button>
        ))}
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {filtered.map((r) => (
          <div key={r.id} style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 14, padding: '14px 16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div style={{ width: 36, height: 36, borderRadius: '50%', background: 'var(--acd)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 800, color: 'var(--ac)' }}>{r.init}</div>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 700, color: 'var(--tx1)' }}>{r.name}</div>
                  <div style={{ fontSize: 11, color: 'var(--tx3)' }}>on {r.car} · {r.date}</div>
                </div>
              </div>
              <div style={{ display: 'flex', gap: 2 }}>
                {[1,2,3,4,5].map((s) => (
                  <svg key={s} width="13" height="13" viewBox="0 0 24 24" fill={s <= r.rating ? '#f59e0b' : '#e5e7eb'}><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                ))}
              </div>
            </div>
            <p style={{ fontSize: 13, color: 'var(--tx2)', margin: '0 0 10px', lineHeight: 1.6 }}>"{r.text}"</p>
            <button style={{ padding: '5px 12px', fontSize: 11, fontWeight: 700, background: 'var(--bg2)', color: 'var(--tx2)', border: '1px solid var(--bd1)', borderRadius: 6, cursor: 'pointer' }}>
              Reply to Review
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

function Placeholder({ title, body }) {
  return (
    <div style={{ background: 'var(--cbg)', border: '1px dashed var(--bd1)', borderRadius: 18, padding: 48, textAlign: 'center', color: 'var(--tx3)' }}>
      <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--tx1)', marginBottom: 8 }}>{title}</div>
      <div style={{ fontSize: 13 }}>{body}</div>
    </div>
  );
}