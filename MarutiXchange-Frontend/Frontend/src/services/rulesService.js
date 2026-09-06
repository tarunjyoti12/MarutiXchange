import axios from 'axios';

// Rules Engine Service — port 8069
const rulesApi = axios.create({
  baseURL: 'http://localhost:8069',
  headers: { 'Content-Type': 'application/json' },
  timeout: 5000,
});

// Core function — calls rules engine
async function evaluate(context) {
  try {
    const res = await rulesApi.post('/rules/evaluate', context);
    return res.data;
  } catch (err) {
    console.log('Rules engine error:', err.message);
    return { approved: true, message: 'Approved' };
  }
}

// 1. VALIDATE BID
export async function validateBid(bidAmount, currentHighestBid) {
  return evaluate({
    type: 'BID',
    bidAmount: bidAmount,
    currentHighestBid: currentHighestBid,
    ruleMatched: false,
  });
}

// 2. VALIDATE CAR LISTING — includes model and segment for Arena/Nexa validation
export async function validateCarListing(price, year, sellerVerified, duplicate, model, segment) {
  return evaluate({
    type: 'CAR_LISTING',
    price: price,
    year: year,
    sellerVerified: sellerVerified || false,
    duplicate: duplicate || false,
    model: model || null,
    segment: segment ? segment.toUpperCase() : null,
    ruleMatched: false,
  });
}

// 3. VALIDATE PAYMENT
export async function validatePayment(price, userId, sellerId) {
  return evaluate({
    type: 'PAYMENT',
    price: price,
    userId: userId,
    sellerId: sellerId,
    ruleMatched: false,
  });
}

// 4. VALIDATE NOTIFICATION
export async function validateNotification(hour, notificationCount, notificationType) {
  return evaluate({
    type: 'NOTIFICATION',
    hour: hour,
    notificationCount: notificationCount,
    notificationType: notificationType,
    ruleMatched: false,
  });
}

// 5. VALIDATE USER / FRAUD CHECK
export async function validateUser(userId, blacklisted) {
  return evaluate({
    type: 'fraud',
    userId: userId,
    blacklisted: blacklisted || false,
    ruleMatched: false,
  });
}

// 6. VALIDATE ORDER
export async function validateOrder(price) {
  return evaluate({
    type: 'order',
    price: price,
    ruleMatched: false,
  });
}

// 7. VALIDATE REFUND
export async function validateRefund(price) {
  return evaluate({
    type: 'refund',
    price: price,
    ruleMatched: false,
  });
}

// 8. CHECK ESCROW
export async function checkEscrow(price) {
  return evaluate({
    type: 'PAYMENT',
    price: price,
    ruleMatched: false,
  });
}