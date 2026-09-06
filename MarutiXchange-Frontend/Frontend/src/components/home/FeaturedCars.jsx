import { useEffect, useMemo, useState } from 'react';
import { CARS, BODY_TYPES } from '../../data/cars';
import { useApp } from '../../context/AppContext';
import { getAllCars } from '../../services/carService';
import CarCard from '../CarCard';

// Same mapper as BuyPage — keeps images[] intact for CarDetailModal gallery
function mapBackendCar(c) {
  return {
    id:       c.id,
    name:     `${c.brand} ${c.model}`,
    type:     c.bodyType?.toLowerCase() || 'hatchback',
    brand:    c.segment?.toLowerCase() || 'arena',
    year:     c.year,
    price:    `₹${(c.askingPrice / 100000).toFixed(2)} L`,
    fuel:     [c.fuelType],
    km:       c.mileage?.toLocaleString('en-IN') || '0',
    loc:      c.city,
    verified: c.isCertified || false,
    rating:   c.rating || 4.5,
    owner:    `${c.ownerNumber || 1}st`,
    trans:    c.transmission || 'Manual',
    desc:     c.description || '',
    age:      c.age || new Date().getFullYear() - c.year,
    colours:  c.colours || [],
    images:   Array.isArray(c.images) ? c.images : [],
    img:      c.images?.[0]?.imageUrl ||
              'https://imgd.aeplcdn.com/664x374/n/cw/ec/107543/brezza-exterior-right-front-three-quarter-13.jpeg?isig=0&q=80',
  };
}

export default function FeaturedCars() {
  const { segment } = useApp();
  const [bodyType, setBodyType] = useState('all');
  const [allCars,  setAllCars]  = useState(CARS);
  const [loading,  setLoading]  = useState(true);

  useEffect(() => {
    async function fetchCars() {
      setLoading(true);
      try {
        const res = await getAllCars({ page: 0, size: 20 });
        if (res?.data && Array.isArray(res.data) && res.data.length > 0) {
          setAllCars(res.data.map(mapBackendCar));
        } else {
          setAllCars(CARS);
        }
      } catch (err) {
        console.log('Using mock data:', err.message);
        setAllCars(CARS);
      } finally {
        setLoading(false);
      }
    }
    fetchCars();
  }, []);

  const cars = useMemo(() => {
    return allCars
      .filter((c) => c.brand === segment)
      .filter((c) => bodyType === 'all' || c.type === bodyType)
      .slice(0, 8);
  }, [allCars, segment, bodyType]);

  return (
    <section style={{ padding: '72px 24px 36px', maxWidth: 1240, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'end', flexWrap: 'wrap', gap: 16, marginBottom: 24 }}>
        <div>
          <div style={{ fontSize: 12, color: 'var(--ac)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.1em', marginBottom: 8 }}>
            Most Searched {segment === 'nexa' ? 'Nexa' : 'Arena'} Cars
          </div>
          <h2 style={{ fontSize: 'clamp(24px, 3.2vw, 40px)', fontWeight: 800, color: 'var(--tx1)', margin: 0 }}>
            Trending this week
          </h2>
        </div>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {BODY_TYPES.map((b) => (
            <button key={b.id} onClick={() => setBodyType(b.id)}
              className="buy-body-pill"
              style={{
                padding: '7px 14px', fontSize: 12, fontWeight: 600,
                background: bodyType === b.id ? 'var(--ac)' : 'var(--bg2)',
                color: bodyType === b.id ? '#fff' : 'var(--tx2)',
                border: `1.5px solid ${bodyType === b.id ? 'var(--ac)' : 'var(--bd1)'}`,
                borderRadius: 10, cursor: 'pointer', transition: 'all .15s',
              }}>
              {b.label}
            </button>
          ))}
        </div>
      </div>

      {loading && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: 20 }}>
          {[1,2,3,4].map((i) => (
            <div key={i} style={{ background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 20, overflow: 'hidden' }}>
              <div style={{ aspectRatio: '16/10', background: 'var(--bg2)' }} />
              <div style={{ padding: 16 }}>
                <div style={{ height: 18, background: 'var(--bg2)', borderRadius: 6, marginBottom: 8, width: '70%' }} />
                <div style={{ height: 16, background: 'var(--bg2)', borderRadius: 6, width: '40%' }} />
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: 20 }}>
          {cars.map((c) => <CarCard key={c.id} car={c} />)}
        </div>
      )}

      {!loading && cars.length === 0 && (
        <div style={{ textAlign: 'center', padding: 60, color: 'var(--tx3)' }}>
          No {segment} cars match this body type.
        </div>
      )}
    </section>
  );
}
