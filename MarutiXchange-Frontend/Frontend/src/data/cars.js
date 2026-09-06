// Car inventory — Realistic Indian second-hand market prices (2025-26)
// Prices based on: model, year, km driven, owner count, fuel type, condition
// Source: CarDekho, Cars24, OLX Autos resale market data

export const CARS = [
  { id: 1, name: 'Maruti Brezza', type: 'suv', brand: 'arena', year: 2023,
    price: '₹9.75–11.50 L', fuel: ['Petrol', 'CNG'], km: '23,450', loc: 'Delhi',
    verified: true, rating: 4.7, owner: '1st', trans: 'Automatic', age: 2,
    colour: 'Pearl White',
    desc: "India's bestselling compact SUV — Boosterjet engine, panoramic sunroof, wireless CarPlay & connected car technology.",
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/107543/brezza-exterior-right-front-three-quarter-13.jpeg?isig=0&q=80' },

  { id: 2, name: 'Grand Vitara', type: 'suv', brand: 'nexa', year: 2022,
    price: '₹13.50–16.75 L', fuel: ['Petrol', 'Hybrid'], km: '18,200', loc: 'Mumbai',
    verified: true, rating: 4.9, owner: '1st', trans: 'Automatic', age: 3,
    colour: 'Grandeur Grey',
    desc: "Premium mid-size SUV with strong hybrid technology and available AllGrip AWD — Maruti's flagship product.",
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/123185/grand-vitara-exterior-right-front-three-quarter-5.png?isig=0&q=80' },

  { id: 3, name: 'Maruti Swift', type: 'hatchback', brand: 'arena', year: 2023,
    price: '₹5.75–7.25 L', fuel: ['Petrol', 'CNG'], km: '12,300', loc: 'Bangalore',
    verified: true, rating: 4.8, owner: '1st', trans: 'Manual', age: 2,
    colour: 'Solid Red',
    desc: "India's sporty hatchback icon — best-in-class handling, rev-happy Z-series engine, incredible fuel efficiency.",
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/159099/swift-exterior-right-front-three-quarter-31.png?isig=0&q=80' },

  { id: 4, name: 'Maruti Baleno', type: 'hatchback', brand: 'nexa', year: 2022,
    price: '₹5.50–7.00 L', fuel: ['Petrol'], km: '31,000', loc: 'Hyderabad',
    verified: true, rating: 4.6, owner: '2nd', trans: 'Automatic', age: 3,
    colour: 'Arctic White',
    desc: 'Premium hatchback with head-up display, 360-degree camera, and one of the most spacious cabins in its class.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/102663/baleno-exterior-right-front-three-quarter-69.png?isig=0&q=80' },

  { id: 5, name: 'Maruti Dzire', type: 'sedan', brand: 'arena', year: 2021,
    price: '₹4.75–6.25 L', fuel: ['Petrol', 'CNG'], km: '44,200', loc: 'Chennai',
    verified: false, rating: 4.4, owner: '2nd', trans: 'Automatic', age: 4,
    colour: 'Oxford Blue',
    desc: "India's bestselling compact sedan — excellent boot space, smooth ride quality, and low running costs.",
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/170173/dzire-exterior-right-front-three-quarter-27.png?isig=0&q=80' },

  { id: 6, name: 'Maruti Fronx', type: 'suv', brand: 'nexa', year: 2024,
    price: '₹8.25–10.75 L', fuel: ['Petrol', 'CNG'], km: '6,800', loc: 'Pune',
    verified: true, rating: 4.9, owner: '1st', trans: 'Automatic', age: 1,
    colour: 'Celestial Blue',
    desc: 'Bold sporty crossover — 1.0L Boosterjet turbo, head-up display, and stunning coupe-SUV styling.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/130591/fronx-exterior-right-front-three-quarter-109.png?isig=0&q=80' },

  { id: 7, name: 'Maruti WagonR', type: 'hatchback', brand: 'arena', year: 2022,
    price: '₹4.25–5.75 L', fuel: ['Petrol', 'CNG'], km: '37,000', loc: 'Jaipur',
    verified: true, rating: 4.3, owner: '1st', trans: 'Manual', age: 3,
    colour: 'Solid Fiery Red',
    desc: "Tall-boy hatchback with the widest cabin in its class — India's most practical everyday city car.",
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/112947/wagon-r-2022-exterior-right-front-three-quarter.jpeg?isig=0&q=80' },

  { id: 8, name: 'Maruti Jimny', type: 'suv', brand: 'nexa', year: 2023,
    price: '₹11.50–13.75 L', fuel: ['Petrol'], km: '9,500', loc: 'Ahmedabad',
    verified: true, rating: 4.8, owner: '1st', trans: 'Manual', age: 2,
    colour: 'Kinetic Yellow',
    desc: 'Legendary off-road 4WD with AllGrip Pro system — iconic boxy styling, go-anywhere capability.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/45299/jimny-exterior-right-front-three-quarter-23.png?isig=0&q=80' },

  { id: 9, name: 'Maruti Ertiga', type: 'mpv', brand: 'arena', year: 2022,
    price: '₹7.25–9.50 L', fuel: ['Petrol', 'CNG'], km: '41,000', loc: 'Kolkata',
    verified: true, rating: 4.6, owner: '2nd', trans: 'Automatic', age: 3,
    colour: 'Solid Auburn Red',
    desc: "India's most popular 7-seater MPV — comfortable, efficient, and perfect for large families.",
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/115777/ertiga-exterior-right-front-three-quarter-10.png?isig=0&q=80' },

  { id: 10, name: 'Maruti XL6', type: 'mpv', brand: 'nexa', year: 2023,
    price: '₹9.75–12.25 L', fuel: ['Petrol', 'CNG'], km: '19,600', loc: 'Lucknow',
    verified: true, rating: 4.7, owner: '1st', trans: 'Automatic', age: 2,
    colour: 'Nexa Blue',
    desc: 'Premium 6-seater MPV — captain seats, ambient lighting, and an elevated driving experience.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/115601/xl6-exterior-right-front-three-quarter-13.png?isig=0&q=80' },

  { id: 11, name: 'Alto K10', type: 'hatchback', brand: 'arena', year: 2023,
    price: '₹2.75–3.75 L', fuel: ['Petrol', 'CNG'], km: '8,900', loc: 'Surat',
    verified: false, rating: 4.2, owner: '1st', trans: 'Manual', age: 2,
    colour: 'Solid Fire Red',
    desc: 'Entry-level city hatchback — easy to park, economical to run, the perfect first car.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/127563/alto-k10-exterior-right-front-three-quarter-63.png?isig=0&q=80' },

  { id: 12, name: 'Maruti Ciaz', type: 'sedan', brand: 'nexa', year: 2021,
    price: '₹5.75–7.50 L', fuel: ['Petrol'], km: '52,000', loc: 'Indore',
    verified: true, rating: 4.3, owner: '3rd', trans: 'Automatic', age: 4,
    colour: 'Pearl Dignity Brown',
    desc: 'Premium mid-size sedan with one of the most spacious rear cabins in its segment.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/48542/ciaz-exterior-right-front-three-quarter-2.png?isig=0&q=80' },

  { id: 13, name: 'Maruti S-Presso', type: 'hatchback', brand: 'arena', year: 2023,
    price: '₹3.25–4.50 L', fuel: ['Petrol', 'CNG'], km: '14,500', loc: 'Nagpur',
    verified: true, rating: 4.3, owner: '1st', trans: 'Manual', age: 2,
    colour: 'Speedy Blue',
    desc: 'Mini-SUV styled hatchback with a tall stance, digital instrument cluster, and impressive fuel efficiency for city commutes.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/126463/s-presso-exterior-right-front-three-quarter-5.png?isig=0&q=80' },

  { id: 14, name: 'Maruti Celerio', type: 'hatchback', brand: 'arena', year: 2022,
    price: '₹3.75–5.25 L', fuel: ['Petrol', 'CNG'], km: '22,800', loc: 'Bhopal',
    verified: true, rating: 4.4, owner: '1st', trans: 'Automatic', age: 3,
    colour: 'Speedy Blue',
    desc: 'Feature-packed hatchback with the lightest AMT gearbox in its class — best-in-segment fuel economy of 26.68 km/l.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/53695/celerio-exterior-right-front-three-quarter-8.png?isig=0&q=80' },

  { id: 15, name: 'Maruti Eeco', type: 'van', brand: 'arena', year: 2022,
    price: '₹3.50–5.00 L', fuel: ['Petrol', 'CNG'], km: '48,000', loc: 'Vadodara',
    verified: true, rating: 4.2, owner: '2nd', trans: 'Manual', age: 3,
    colour: 'Silky Silver',
    desc: "India's most versatile van — seats up to 7 passengers or converts to a cargo carrier, unbeatable running costs with CNG.",
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/135523/eeco-exterior-right-front-three-quarter-3.png?isig=0&q=80' },

  { id: 16, name: 'Maruti Ignis', type: 'hatchback', brand: 'nexa', year: 2023,
    price: '₹4.75–6.25 L', fuel: ['Petrol'], km: '19,200', loc: 'Chandigarh',
    verified: true, rating: 4.5, owner: '1st', trans: 'Automatic', age: 2,
    colour: 'Lucent Orange',
    desc: 'Urban micro-SUV with a bold boxy stance, dual-tone roof, smartphone connectivity and one of the most distinctive designs in its class.',
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/142921/ignis-exterior-right-front-three-quarter-17.png?isig=0&q=80' },

  { id: 17, name: 'Maruti Invicto', type: 'mpv', brand: 'nexa', year: 2023,
    price: '₹21.50–25.75 L', fuel: ['Hybrid'], km: '11,400', loc: 'Bangalore',
    verified: true, rating: 4.9, owner: '1st', trans: 'Automatic', age: 2,
    colour: 'Midnight Black',
    desc: "Maruti's most premium MPV — strong hybrid tech, 7/8-seater luxury cabin, panoramic sunroof, and segment-leading features.",
    img: 'https://imgd.aeplcdn.com/664x374/n/cw/ec/147201/invicto-exterior-right-front-three-quarter-68.png?isig=0&q=80' },
];

export const BODY_TYPES = [
  { id: 'all',        label: 'All Types' },
  { id: 'hatchback',  label: 'Hatchback' },
  { id: 'sedan',      label: 'Sedan' },
  { id: 'suv',        label: 'SUV' },
  { id: 'mpv',        label: 'MPV' },
  { id: 'van',        label: 'Van' },
];

export const FUEL_OPTIONS = ['Petrol', 'CNG', 'Hybrid', 'Diesel'];
export const OWNER_OPTIONS = ['1st', '2nd', '3rd'];
export const TRANS_OPTIONS = ['Manual', 'Automatic'];