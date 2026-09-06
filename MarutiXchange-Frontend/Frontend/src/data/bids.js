// Live auction data — realistic second-hand market starting prices
// All prices in Lakhs (L) — based on actual Cars24/CarDekho resale values

const now = Date.now();

export const BIDS = [
  {
    id: 'b1', name: 'Maruti Brezza ZXi+', brand: 'arena',
    year: 2023, km: '8,200', fuel: 'Petrol',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/107543/brezza-exterior-right-front-three-quarter-13.jpeg?isig=0&q=80',
    startPrice: 9.50, currentBid: 10.25, totalBidders: 14, myBid: null,
    status: 'live', endsAt: now + 2 * 3600000 + 14 * 60000,
    verified: true, minIncrement: 0.10,
  },
  {
    id: 'b2', name: 'Grand Vitara Alpha+', brand: 'nexa',
    year: 2022, km: '15,400', fuel: 'Hybrid',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/123185/grand-vitara-exterior-right-front-three-quarter-5.png?isig=0&q=80',
    startPrice: 13.50, currentBid: 15.25, totalBidders: 23, myBid: null,
    status: 'live', endsAt: now + 5 * 3600000 + 32 * 60000,
    verified: true, minIncrement: 0.25,
  },
  {
    id: 'b3', name: 'Maruti Jimny Zeta', brand: 'nexa',
    year: 2023, km: '4,800', fuel: 'Petrol',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/45299/jimny-exterior-right-front-three-quarter-23.png?isig=0&q=80',
    startPrice: 11.00, currentBid: 12.10, totalBidders: 9, myBid: null,
    status: 'live', endsAt: now + 47 * 60000,
    verified: true, minIncrement: 0.10,
  },
  {
    id: 'b4', name: 'Maruti Baleno Alpha', brand: 'nexa',
    year: 2022, km: '22,100', fuel: 'Petrol',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/102663/baleno-exterior-right-front-three-quarter-69.png?isig=0&q=80',
    startPrice: 5.50, currentBid: 6.25, totalBidders: 31, myBid: null,
    status: 'live', endsAt: now + 8 * 3600000 + 18 * 60000,
    verified: true, minIncrement: 0.05,
  },
  {
    id: 'b5', name: 'Maruti Swift ZXi', brand: 'arena',
    year: 2024, km: '3,100', fuel: 'Petrol',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/159099/swift-exterior-right-front-three-quarter-31.png?isig=0&q=80',
    startPrice: 6.75, currentBid: 7.50, totalBidders: 18, myBid: null,
    status: 'upcoming', endsAt: now + 24 * 3600000,
    verified: true, minIncrement: 0.10,
  },
  {
    id: 'b6', name: 'Fronx Sigma Turbo', brand: 'nexa',
    year: 2023, km: '11,700', fuel: 'Petrol',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/130591/fronx-exterior-right-front-three-quarter-109.png?isig=0&q=80',
    startPrice: 8.25, currentBid: 9.50, totalBidders: 27, myBid: null,
    status: 'live', endsAt: now + 3 * 3600000 + 5 * 60000,
    verified: true, minIncrement: 0.10,
  },
  {
    id: 'b7', name: 'Maruti Ertiga VXi', brand: 'arena',
    year: 2021, km: '38,500', fuel: 'CNG',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/115777/ertiga-exterior-right-front-three-quarter-10.png?isig=0&q=80',
    startPrice: 6.50, currentBid: 7.25, totalBidders: 19, myBid: null,
    status: 'live', endsAt: now + 6 * 3600000 + 45 * 60000,
    verified: true, minIncrement: 0.10,
  },
  {
    id: 'b8', name: 'Maruti WagonR LXi', brand: 'arena',
    year: 2022, km: '28,000', fuel: 'CNG',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/112947/wagon-r-2022-exterior-right-front-three-quarter.jpeg?isig=0&q=80',
    startPrice: 4.00, currentBid: 4.75, totalBidders: 12, myBid: null,
    status: 'live', endsAt: now + 4 * 3600000 + 20 * 60000,
    verified: false, minIncrement: 0.05,
  },
];
