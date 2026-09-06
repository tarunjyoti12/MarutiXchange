// ─────────────────────────────────────────────────────────────────────────────
// CAR IMAGES — 4 angles per car
// IMPORTANT: These are the exact same URLs used in cars.js for the front image
// They are confirmed working. Side/rear/interior use alternate image numbers
// from the same CDN path which should also work once referrer policy is set.
// ─────────────────────────────────────────────────────────────────────────────
export const CAR_IMAGES = {

  // 1 — Maruti Brezza (ec/107543) — front image from cars.js confirmed working
  1: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/107543/brezza-exterior-right-front-three-quarter-13.jpeg?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/107543/brezza-exterior-right-side-view-3.jpeg?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/107543/brezza-exterior-rear-three-quarter-4.jpeg?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/107543/brezza-interior-dashboard-2.jpeg?isig=0&q=80',
  },

  // 2 — Grand Vitara (ec/123185)
  2: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/123185/grand-vitara-exterior-right-front-three-quarter-5.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/123185/grand-vitara-exterior-right-side-view-2.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/123185/grand-vitara-exterior-rear-three-quarter-3.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/123185/grand-vitara-interior-dashboard-2.png?isig=0&q=80',
  },

  // 3 — Maruti Swift (ec/159099)
  3: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/159099/swift-exterior-right-front-three-quarter-31.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/159099/swift-exterior-right-side-view-2.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/159099/swift-exterior-rear-three-quarter-3.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/159099/swift-interior-dashboard-2.png?isig=0&q=80',
  },

  // 4 — Maruti Baleno (ec/102663)
  4: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/102663/baleno-exterior-right-front-three-quarter-69.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/102663/baleno-exterior-right-side-view-2.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/102663/baleno-exterior-rear-three-quarter-5.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/102663/baleno-interior-dashboard-2.png?isig=0&q=80',
  },

  // 5 — Maruti Dzire (ec/170173)
  5: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/170173/dzire-exterior-right-front-three-quarter-27.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/170173/dzire-exterior-right-side-view-2.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/170173/dzire-exterior-rear-three-quarter-3.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/170173/dzire-interior-dashboard-2.png?isig=0&q=80',
  },

  // 6 — Maruti Fronx (ec/130591)
  6: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/130591/fronx-exterior-right-front-three-quarter-109.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/130591/fronx-exterior-right-side-view-2.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/130591/fronx-exterior-rear-three-quarter-2.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/130591/fronx-interior-dashboard-2.png?isig=0&q=80',
  },

  // 7 — Maruti WagonR (ec/112947)
  7: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/112947/wagon-r-2022-exterior-right-front-three-quarter.jpeg?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/112947/wagon-r-2022-exterior-right-side-view.jpeg?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/112947/wagon-r-2022-exterior-rear-three-quarter.jpeg?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/112947/wagon-r-2022-interior-dashboard.jpeg?isig=0&q=80',
  },

  // 8 — Maruti Jimny (ec/45299)
  8: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/45299/jimny-exterior-right-front-three-quarter-23.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/45299/jimny-exterior-right-side-view.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/45299/jimny-exterior-rear-three-quarter-2.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/45299/jimny-interior-dashboard.png?isig=0&q=80',
  },

  // 9 — Maruti Ertiga (ec/115777)
  9: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/115777/ertiga-exterior-right-front-three-quarter-10.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/115777/ertiga-exterior-right-side-view.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/115777/ertiga-exterior-rear-three-quarter.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/115777/ertiga-interior-dashboard.png?isig=0&q=80',
  },

  // 10 — Maruti XL6 (ec/115601)
  10: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/115601/xl6-exterior-right-front-three-quarter-13.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/115601/xl6-exterior-right-side-view.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/115601/xl6-exterior-rear-three-quarter.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/115601/xl6-interior-dashboard.png?isig=0&q=80',
  },

  // 11 — Alto K10 (ec/127563)
  11: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/127563/alto-k10-exterior-right-front-three-quarter-63.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/127563/alto-k10-exterior-right-side-view-2.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/127563/alto-k10-exterior-rear-three-quarter-2.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/127563/alto-k10-interior-dashboard.png?isig=0&q=80',
  },

  // 12 — Maruti S-Presso (ec/40087)
  12: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/40087/s-presso-exterior-right-front-three-quarter-4.jpeg?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/40087/s-presso-exterior-right-side-view.jpeg?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/40087/s-presso-exterior-rear-three-quarter.jpeg?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/40087/s-presso-interior-dashboard.jpeg?isig=0&q=80',
  },

  // 13 — Maruti Celerio (ec/101463)
  13: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/101463/celerio-exterior-right-front-three-quarter-4.png?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/101463/celerio-exterior-right-side-view-2.png?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/101463/celerio-exterior-rear-three-quarter-2.png?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/101463/celerio-interior-dashboard.png?isig=0&q=80',
  },

  // 14 — Maruti Ignis (ec/45573)
  14: {
    front:    'https://imgd.aeplcdn.com/664x374/n/cw/ec/45573/ignis-exterior-right-front-three-quarter-3.jpeg?isig=0&q=80',
    side:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/45573/ignis-exterior-right-side-view.jpeg?isig=0&q=80',
    rear:     'https://imgd.aeplcdn.com/664x374/n/cw/ec/45573/ignis-exterior-rear-three-quarter.jpeg?isig=0&q=80',
    interior: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/45573/ignis-interior-dashboard.jpeg?isig=0&q=80',
  },
};