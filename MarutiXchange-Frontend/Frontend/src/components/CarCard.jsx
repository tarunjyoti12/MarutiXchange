import { useEffect, useState } from 'react';
import { useApp } from '../context/AppContext';
import { addToWatchlist, removeFromWatchlist, isWatched } from '../services/watchlistService';

export default function CarCard({ car }) {
  const { user, openModal, pushToast } = useApp();
  const [watched,      setWatched]      = useState(false);
  const [watchLoading, setWatchLoading] = useState(false);

  useEffect(() => {
    if (!user || !car?.id) return;
    isWatched(user.id, car.id).then(setWatched);
  }, [user, car?.id]);

  async function handleWatch(e) {
    e.stopPropagation();
    if (!user) { openModal('login'); return; }
    if (watchLoading) return;

    setWatchLoading(true);
    const next = !watched;

    try {
      if (next) {
        await addToWatchlist(user.id, {
          carListingId: car.id,
          carName:      car.name,
          carPrice:     car.price,
          carImgUrl:    car.img,
          carBrand:     car.brand,
          carYear:      car.year,
        });
        setWatched(true);
        pushToast('Added to watchlist', 'ok');
      } else {
        await removeFromWatchlist(user.id, car.id);
        setWatched(false);
        pushToast('Removed from watchlist', 'info');
      }
    } catch (err) {
      console.log('Watchlist error:', err.message);
    } finally {
      setWatchLoading(false);
    }
  }

  const fuel = Array.isArray(car.fuel) ? car.fuel.join(' / ') : car.fuel;

  return (
    <article
      className="car-card"
      onClick={() => openModal('car', { carId: car.id })}
      style={{ cursor: 'pointer', position: 'relative' }}
    >
      {/* Image */}
      <div style={{ position: 'relative', aspectRatio: '16/10', background: 'var(--ibg)', overflow: 'hidden' }}>
        <img
          src={car.img}
          alt={car.name}
          referrerPolicy="no-referrer"
          crossOrigin="anonymous"
          style={{ width: '100%', height: '100%', objectFit: 'contain', padding: '8px 16px', transition: 'transform .3s' }}
        />

        {/* Badges */}
        <div style={{ position: 'absolute', top: 10, left: 10, display: 'flex', gap: 5 }}>
          {car.verified && (
            <span style={{ background: 'var(--ver)', color: '#fff', fontSize: 10, fontWeight: 700, padding: '3px 8px', borderRadius: 999 }}>
              Verified
            </span>
          )}
          <span style={{ background: car.brand === 'nexa' ? '#002E6E' : 'var(--ac)', color: '#fff', fontSize: 10, fontWeight: 700, padding: '3px 8px', borderRadius: 999, textTransform: 'capitalize' }}>
            {car.brand}
          </span>
        </div>

        {/* Watchlist button */}
        <button
          onClick={handleWatch}
          style={{
            position: 'absolute', top: 10, right: 10,
            width: 30, height: 30, borderRadius: '50%',
            background: watched ? 'var(--ac)' : 'rgba(0,0,0,.45)',
            border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center',
            cursor: 'pointer', transition: 'background .2s',
          }}
        >
          <svg width="14" height="14" viewBox="0 0 24 24"
            fill={watched ? '#fff' : 'none'}
            stroke="#fff" strokeWidth="2">
            <path d="M12.76 3.76a5.5 5.5 0 017.78 7.78l-7.78 7.78-7.78-7.78a5.5 5.5 0 017.78-7.78z"/>
          </svg>
        </button>
      </div>

      {/* Card body */}
      <div style={{ padding: '14px 16px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 6 }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: 'var(--tx1)', margin: 0, lineHeight: 1.2 }}>
            {car.name}
          </h3>
          <div style={{ fontSize: 15, fontWeight: 800, color: 'var(--ac)', flexShrink: 0, marginLeft: 8 }}>
            {car.price}
          </div>
        </div>

        <div style={{ fontSize: 12, color: 'var(--tx3)', marginBottom: 10 }}>
          {car.year} · {car.km} km · {fuel} · {car.trans}
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ fontSize: 12, color: 'var(--tx3)' }}>
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: 'middle', marginRight: 3 }}>
              <path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>
            </svg>
            {car.loc} · {car.owner} Owner
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 3, fontSize: 12, color: 'var(--tx2)', fontWeight: 600 }}>
            <svg width="11" height="11" viewBox="0 0 24 24" fill="#f59e0b" stroke="none">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
            {car.rating}
          </div>
        </div>
      </div>
    </article>
  );
}