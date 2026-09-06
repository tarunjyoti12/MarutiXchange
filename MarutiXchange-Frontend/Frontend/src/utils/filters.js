// Pure helpers for running the buy-page filter pipeline.

export function parsePriceRange(priceText) {
  // priceText examples: "₹9.74–14.90 L" → [9.74, 14.9]
  const m = priceText.match(/(\d+(?:\.\d+)?)\s*[–-]\s*(\d+(?:\.\d+)?)/);
  if (!m) return [0, 0];
  return [parseFloat(m[1]), parseFloat(m[2])];
}

export function parseKm(kmStr) {
  return Number(String(kmStr).replace(/[^\d]/g, '')) || 0;
}

export function applyCarFilters(cars, f) {
  return cars.filter((c) => {
    if (f.brand && f.brand !== 'all' && c.brand !== f.brand) return false;
    if (f.bodyType && f.bodyType !== 'all' && c.type !== f.bodyType) return false;
    if (f.fuels?.length && !c.fuel.some((x) => f.fuels.includes(x))) return false;
    if (f.owners?.length && !f.owners.includes(c.owner)) return false;
    if (f.trans?.length && !f.trans.includes(c.trans)) return false;

    const [lo, hi] = parsePriceRange(c.price);
    if (f.priceMax != null && lo > f.priceMax) return false;
    if (f.priceMin != null && hi < f.priceMin) return false;

    const km = parseKm(c.km);
    if (f.kmMax != null && km > f.kmMax) return false;
    if (f.kmMin != null && km < f.kmMin) return false;

    if (f.query) {
      const q = f.query.trim().toLowerCase();
      if (q && !`${c.name} ${c.loc} ${c.type}`.toLowerCase().includes(q)) return false;
    }
    return true;
  });
}

export function sortCars(cars, mode) {
  const out = [...cars];
  switch (mode) {
    case 'price-low':  return out.sort((a, b) => parsePriceRange(a.price)[0] - parsePriceRange(b.price)[0]);
    case 'price-high': return out.sort((a, b) => parsePriceRange(b.price)[1] - parsePriceRange(a.price)[1]);
    case 'km-low':     return out.sort((a, b) => parseKm(a.km) - parseKm(b.km));
    case 'year-new':   return out.sort((a, b) => b.year - a.year);
    case 'rating':     return out.sort((a, b) => b.rating - a.rating);
    default:           return out;
  }
}
