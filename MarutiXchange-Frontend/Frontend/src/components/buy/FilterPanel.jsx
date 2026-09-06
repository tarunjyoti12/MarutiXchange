import { BODY_TYPES, FUEL_OPTIONS, OWNER_OPTIONS, TRANS_OPTIONS } from '../../data/cars';
import { REGIONS, REGION_LABELS } from '../../data/regions';

export default function FilterPanel({ filters, setFilters, matchCount }) {
  function toggleArray(key, value) {
    setFilters((prev) => {
      const arr = prev[key] || [];
      const next = arr.includes(value) ? arr.filter((x) => x !== value) : [...arr, value];
      return { ...prev, [key]: next };
    });
  }

  function reset() {
    setFilters({
      brand: 'all', bodyType: 'all',
      fuels: [], owners: [], trans: [],
      priceMin: 0, priceMax: 40, kmMin: 0, kmMax: 100000,
      region: null, cities: [],
      query: '',
    });
  }

  return (
    <aside
      style={{
        background: 'var(--cbg)', border: '1px solid var(--bd1)', borderRadius: 18,
        padding: 18, boxShadow: 'var(--csh)',
        position: 'sticky', top: 96,
        maxHeight: 'calc(100vh - 120px)', overflowY: 'auto',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
        <div style={{ fontSize: 13, fontWeight: 800, color: 'var(--tx1)', letterSpacing: '.02em' }}>
          Filters · {matchCount} results
        </div>
        <button
          onClick={reset}
          style={{ background: 'none', border: 'none', color: 'var(--ac)', fontSize: 12, fontWeight: 600, cursor: 'pointer' }}
        >
          Reset
        </button>
      </div>

      <Section title="Body Type">
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2,1fr)', gap: 6 }}>
          {BODY_TYPES.map((b) => (
            <button
              key={b.id}
              className={`buy-body-pill ${filters.bodyType === b.id ? 'on' : ''}`}
              onClick={() => setFilters({ ...filters, bodyType: b.id })}
              style={{
                padding: '7px 10px', borderRadius: 8, fontSize: 12, fontWeight: 600,
                border: '1.5px solid var(--bd1)', cursor: 'pointer',
                background: filters.bodyType === b.id ? 'var(--ac)' : 'var(--bg2)',
                color: filters.bodyType === b.id ? '#fff' : 'var(--tx2)',
                borderColor: filters.bodyType === b.id ? 'var(--ac)' : 'var(--bd1)',
              }}
            >
              {b.label}
            </button>
          ))}
        </div>
      </Section>

      <Section title={`Price · ₹${filters.priceMin}–${filters.priceMax} L`}>
        <input
          type="range" min="0" max="40" step="0.5" value={filters.priceMax}
          onChange={(e) => setFilters({ ...filters, priceMax: parseFloat(e.target.value) })}
          style={{ width: '100%' }}
        />
      </Section>

      <Section title={`KM Driven · up to ${filters.kmMax.toLocaleString()}`}>
        <input
          type="range" min="0" max="100000" step="5000" value={filters.kmMax}
          onChange={(e) => setFilters({ ...filters, kmMax: parseInt(e.target.value, 10) })}
          style={{ width: '100%' }}
        />
      </Section>

      <Section title="Fuel Type">
        <CheckboxGroup options={FUEL_OPTIONS} selected={filters.fuels} onToggle={(v) => toggleArray('fuels', v)} />
      </Section>

      <Section title="Owner">
        <CheckboxGroup options={OWNER_OPTIONS} selected={filters.owners} onToggle={(v) => toggleArray('owners', v)} />
      </Section>

      <Section title="Transmission">
        <CheckboxGroup options={TRANS_OPTIONS} selected={filters.trans} onToggle={(v) => toggleArray('trans', v)} />
      </Section>

      <Section title="Region">
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
          {Object.keys(REGIONS).map((r) => (
            <button
              key={r}
              onClick={() => setFilters({ ...filters, region: filters.region === r ? null : r })}
              style={{
                padding: '6px 11px', borderRadius: 999, fontSize: 11, fontWeight: 600,
                border: `1px solid ${filters.region === r ? 'var(--ac)' : 'var(--bd1)'}`,
                background: filters.region === r ? 'var(--chip-on)' : 'var(--bg2)',
                color: filters.region === r ? 'var(--chip-on-c)' : 'var(--tx2)',
                cursor: 'pointer',
              }}
            >
              {REGION_LABELS[r]}
            </button>
          ))}
        </div>
      </Section>
    </aside>
  );
}

function Section({ title, children }) {
  return (
    <div style={{ padding: '14px 0', borderBottom: '1px solid var(--bd0)' }}>
      <div style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.08em', color: 'var(--tx2)', marginBottom: 10 }}>
        {title}
      </div>
      {children}
    </div>
  );
}

function CheckboxGroup({ options, selected, onToggle }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      {options.map((o) => (
        <label key={o} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, color: 'var(--tx2)', cursor: 'pointer' }}>
          <input
            type="checkbox"
            checked={selected.includes(o)}
            onChange={() => onToggle(o)}
            style={{ accentColor: 'var(--ac)' }}
          />
          {o}
        </label>
      ))}
    </div>
  );
}
