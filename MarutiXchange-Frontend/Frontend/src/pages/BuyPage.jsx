import { useEffect, useMemo, useState } from 'react';
import { CARS } from '../data/cars';
import { REGIONS } from '../data/regions';
import { applyCarFilters, sortCars } from '../utils/filters';
import { useApp } from '../context/AppContext';
import { getAllCars } from '../services/carService';
import CarCard from '../components/CarCard';
import { SkeletonCard } from '../components/Skeleton';
import FilterPanel from '../components/buy/FilterPanel';

const DEFAULT_FILTERS = {
  brand: 'all', bodyType: 'all',
  fuels: [], owners: [], trans: [],
  priceMin: 0, priceMax: 40, kmMin: 0, kmMax: 100000,
  region: null, cities: [],
  query: '',
};

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

export default function BuyPage() {
  const { segment, navigate, searchQuery, setSearchQuery } = useApp();
  const [filters, setFilters] = useState(DEFAULT_FILTERS);
  const [sort,    setSort]    = useState('default');
  const [segTab,  setSegTab]  = useState(() => {
    const s = localStorage.getItem('mm-seg');
    return (s === 'arena' || s === 'nexa') ? s : 'all';
  });

  // Sync global segment toggle
  useEffect(() => {
    if (segment === 'arena' || segment === 'nexa') setSegTab(segment);
  }, [segment]);

  // ── Sync Nav searchQuery into local filters ──────────────────────────────
  useEffect(() => {
    setFilters((prev) => ({ ...prev, query: searchQuery }));
  }, [searchQuery]);

  // Clear global search when leaving the page (unmount)
  useEffect(() => {
    return () => setSearchQuery('');
  }, [setSearchQuery]);
  // ─────────────────────────────────────────────────────────────────────────

  const [cars,    setCars]    = useState(CARS);
  const [loading, setLoading] = useState(true);
  const [visibleCount, setVisibleCount] = useState(8);
  const PAGE_SIZE = 8;

  useEffect(() => {
    async function fetchCars() {
      setLoading(true);
      try {
        const res = await getAllCars();
        if (res?.data && Array.isArray(res.data) && res.data.length > 0) {
          setCars(res.data.map(mapBackendCar));
        } else {
          setCars(CARS);
        }
      } catch (err) {
        console.log('Using mock data — backend not connected:', err.message);
        setCars(CARS);
      } finally {
        setLoading(false);
      }
    }
    fetchCars();
  }, []);

  useEffect(() => { setVisibleCount(8); }, [filters, segTab, sort]);

  const results = useMemo(() => {
    let list = cars;
    if (segTab !== 'all') list = list.filter((c) => c.brand === segTab);
    list = applyCarFilters(list, filters);
    if (filters.region) {
      const cities = new Set(REGIONS[filters.region] || []);
      list = list.filter((c) => cities.has(c.loc));
    }
    return sortCars(list, sort);
  }, [cars, filters, segTab, sort]);

  return (
    <div style={{ maxWidth: 1340, margin: '0 auto', padding: '32px 24px 80px' }}>
      <div style={{ marginBottom: 24 }}>
        <div style={{ fontSize: 12, color: 'var(--ac)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.1em', marginBottom: 8 }}>
          Browse
        </div>
        <h1 style={{ fontSize: 'clamp(24px, 3vw, 38px)', fontWeight: 800, color: 'var(--tx1)', margin: 0 }}>
          Find your perfect Maruti
        </h1>

        {/* Show active search query banner */}
        {filters.query && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10 }}>
            <span style={{ fontSize: 13, color: 'var(--tx2)' }}>
              Showing results for <strong>"{filters.query}"</strong>
            </span>
            <button
              onClick={() => { setFilters((f) => ({ ...f, query: '' })); setSearchQuery(''); }}
              style={{ fontSize: 12, color: 'var(--ac)', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 700 }}
            >
              × Clear
            </button>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 18 }}>
        {['all', 'arena', 'nexa'].map((k) => (
          <button key={k} onClick={() => setSegTab(k)}
            style={{
              padding: '8px 18px', fontSize: 13, fontWeight: 700,
              border: `1.5px solid ${segTab === k ? 'var(--ac)' : 'var(--bd1)'}`,
              background: segTab === k ? 'var(--ac)' : 'var(--bg2)',
              color: segTab === k ? '#fff' : 'var(--tx2)',
              borderRadius: 10, cursor: 'pointer', textTransform: 'capitalize',
            }}>
            {k}
          </button>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '260px 1fr', gap: 24, alignItems: 'start' }}>
        <FilterPanel filters={filters} setFilters={setFilters} matchCount={results.length} />

        <div>
          {loading && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: 20 }}>
              {[1,2,3,4,5,6].map((i) => <SkeletonCard key={i} />)}
            </div>
          )}

          {!loading && results.length === 0 && (
            <div style={{ textAlign: 'center', padding: 80, color: 'var(--tx3)', border: '1px dashed var(--bd1)', borderRadius: 18 }}>
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--tx3)" strokeWidth="1.5" style={{ opacity: .4, display: 'block', margin: '0 auto 12px' }}>
                <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--tx1)', marginBottom: 8 }}>
                {filters.query ? `No cars found for "${filters.query}"` : 'No cars match your filters'}
              </div>
              <div style={{ fontSize: 13 }}>Try removing some filters.</div>
              <button
                onClick={() => { setFilters(DEFAULT_FILTERS); setSearchQuery(''); }}
                style={{ marginTop: 16, padding: '10px 20px', fontSize: 13, fontWeight: 700, background: 'var(--ac)', color: '#fff', border: 'none', borderRadius: 10, cursor: 'pointer' }}>
                Clear Filters
              </button>
            </div>
          )}

          {!loading && results.length > 0 && (
            <>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
                <div style={{ fontSize: 13, color: 'var(--tx3)', fontWeight: 600 }}>
                  Showing <span style={{ color: 'var(--tx1)', fontWeight: 800 }}>{Math.min(visibleCount, results.length)}</span> of <span style={{ color: 'var(--tx1)', fontWeight: 800 }}>{results.length}</span> cars
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(260px,1fr))', gap: 20 }}>
                {results.slice(0, visibleCount).map((c) => <CarCard key={c.id} car={c} />)}
              </div>

              {visibleCount < results.length && (
                <div style={{ textAlign: 'center', marginTop: 32 }}>
                  <button
                    onClick={() => setVisibleCount((v) => v + PAGE_SIZE)}
                    style={{
                      padding: '13px 36px', fontSize: 14, fontWeight: 700,
                      background: 'var(--bg2)', color: 'var(--tx1)',
                      border: '1.5px solid var(--bd1)', borderRadius: 12,
                      cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: 8,
                    }}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <polyline points="6 9 12 15 18 9"/>
                    </svg>
                    Load More ({results.length - visibleCount} remaining)
                  </button>
                </div>
              )}

              {visibleCount >= results.length && results.length > PAGE_SIZE && (
                <div style={{ textAlign: 'center', marginTop: 24, fontSize: 13, color: 'var(--tx3)', fontWeight: 600 }}>
                  All {results.length} cars shown
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}