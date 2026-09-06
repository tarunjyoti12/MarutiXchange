# 🧪 Complete API Testing Guide - Payment Service + Rule Engine Integration

## 📊 Payment API Flow - All Endpoints

```
1. Initiate Payment
   ↓
2. Confirm Payment
   ↓
3. Get Payment Details
   ↓
4. Refund Payment (optional)
```

---

## 🚀 Step 1: INITIATE PAYMENT (Create Payment)

### Endpoint
```
POST http://localhost:8092/api/v1/payments/initiate
```

### Request Body
```json
{
  "carListingId": 1,
  "auctionId": 1,
  "buyerId": 100,
  "sellerId": 200,
  "amount": 50000.0,
  "paymentMethod": "UPI",
  "paymentType": "TOKEN",
  "upiId": "buyer@upi",
  "upiApp": "GooglePay",
  "isTokenPayment": true
}
```

### Expected Response (201 Created)
```json
{
  "status": 201,
  "message": "Payment initiated successfully",
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "idempotencyKey": null,
    "carListingId": 1,
    "auctionId": 1,
    "buyerId": 100,
    "sellerId": 200,
    "amount": 50000.0,
    "tokenAmount": 10000.0,
    "remainingAmount": 0.0,
    "paymentMethod": "UPI",
    "paymentType": "TOKEN",
    "upiId": "buyer@upi",
    "upiApp": "GooglePay",
    "bankName": null,
    "invoiceNumber": "INV-MX-1775716480294",
    "isTokenPayment": true,
    "status": "PENDING",
    "failureReason": null,
    "createdAt": "2026-04-09T12:04:40"
  }
}
```

### ✅ What to Check
- [ ] Status code is **201 Created**
- [ ] `transactionId` is generated (e.g., MM...)
- [ ] `status` is **PENDING**
- [ ] `invoiceNumber` is generated
- [ ] All your data is saved correctly

### 🔗 Rule Engine Integration Verification
**Check logs for:**
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=100, sellerId=200, ...}
[INFO] Rule Engine Response: {approved=true, message=Payment approved}
```

---

## 🚀 Step 2: CONFIRM PAYMENT (Mark as Successful)

**Use the `transactionId` from Step 1**

### Endpoint
```
PATCH http://localhost:8092/api/v1/payments/{transactionId}/confirm
```

### Example (Replace MM... with your transactionId)
```
PATCH http://localhost:8092/api/v1/payments/MM1775716480294539FAA/confirm
```

### Request Body
```json
{}
```

### Expected Response (200 OK)
```json
{
  "status": 200,
  "message": "Payment confirmed successfully",
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "status": "SUCCESS",
    "amount": 50000.0,
    "buyerId": 100,
    "sellerId": 200,
    "paymentMethod": "UPI"
  }
}
```

### ✅ What to Check
- [ ] Status code is **200 OK**
- [ ] Payment `status` changed from **PENDING** to **SUCCESS**
- [ ] No error message
- [ ] Same `transactionId`

---

## 🚀 Step 3: GET PAYMENT DETAILS (Retrieve Payment)

### Endpoint
```
GET http://localhost:8092/api/v1/payments/{transactionId}
```

### Example
```
GET http://localhost:8092/api/v1/payments/MM1775716480294539FAA
```

### Request Body
```
(No body needed)
```

### Expected Response (200 OK)
```json
{
  "status": 200,
  "message": "Payment retrieved successfully",
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "status": "SUCCESS",
    "amount": 50000.0,
    "buyerId": 100,
    "sellerId": 200,
    "paymentMethod": "UPI",
    "invoiceNumber": "INV-MX-1775716480294",
    "createdAt": "2026-04-09T12:04:40",
    "updatedAt": "2026-04-09T12:05:10"
  }
}
```

### ✅ What to Check
- [ ] Status code is **200 OK**
- [ ] Payment details match what you created
- [ ] `status` is **SUCCESS** (from Step 2)

---

## 🚀 Step 4: FAIL PAYMENT (Optional - Mark as Failed)

### Endpoint
```
PATCH http://localhost:8092/api/v1/payments/{transactionId}/fail?reason=USER_CANCELLED
```

### Example
```
PATCH http://localhost:8092/api/v1/payments/MM1775716480294539FAA/fail?reason=USER_CANCELLED
```

### Request Body
```json
{}
```

### Expected Response (200 OK)
```json
{
  "status": 200,
  "message": "Payment failed successfully",
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "status": "FAILED",
    "failureReason": "USER_CANCELLED",
    "amount": 50000.0
  }
}
```

### ✅ What to Check
- [ ] Status code is **200 OK**
- [ ] Payment `status` changed to **FAILED**
- [ ] `failureReason` shows the reason you provided

---

## 🔗 TESTING RULE ENGINE INTEGRATION

### Test Case 1: Valid Payment (Should Pass) ✅

**Body:**
```json
{
  "carListingId": 1,
  "auctionId": 1,
  "buyerId": 100,
  "sellerId": 200,
  "amount": 50000.0,
  "paymentMethod": "UPI"
}
```

**Expected:**
- ✅ 201 Created
- ✅ Payment created in database
- ✅ Logs show: `"approved": true`

**Logs to check:**
```
[INFO] Rule Engine Request: {...}
[INFO] Rule Engine Response: {approved=true, ...}
[INFO] Payment initiated successfully: MM...
```

---

### Test Case 2: Invalid Payment - Same Buyer & Seller (Should Fail) ❌

**Body:**
```json
{
  "carListingId": 1,
  "auctionId": 1,
  "buyerId": 100,
  "sellerId": 100,
  "amount": 50000.0,
  "paymentMethod": "UPI"
}
```

**Expected:**
- ❌ 400 Bad Request
- ❌ Payment NOT created
- ❌ Logs show: `"approved": false`

**Logs to check:**
```
[INFO] Rule Engine Response: {approved=false, ...}
[ERROR] Payment rejected by rule engine: [Buyer and seller cannot be the same person]
```

---

### Test Case 3: Invalid Payment - Amount Too Low (Should Fail) ❌

**Body:**
```json
{
  "carListingId": 1,
  "auctionId": 1,
  "buyerId": 100,
  "sellerId": 200,
  "amount": 500.0,
  "paymentMethod": "UPI"
}
```

**Expected:**
- ❌ 400 Bad Request (validation error)
- ❌ Payment NOT created
- ✅ Error message: "Amount must be at least 1000"

**Response:**
```json
{
  "error": "Bad Request",
  "message": "Amount must be at least 1000"
}
```

---

### Test Case 4: High Amount Payment (May Have Warnings) ⚠️

**Body:**
```json
{
  "carListingId": 2,
  "auctionId": 2,
  "buyerId": 300,
  "sellerId": 400,
  "amount": 5000000.0,
  "paymentMethod": "BANK_TRANSFER",
  "paymentType": "FULL_PAYMENT",
  "bankName": "HDFC Bank"
}
```

**Expected:**
- ✅ 201 Created (payment still created)
- ✅ Warnings in logs

**Logs to check:**
```
[INFO] Rule Engine Response: {..., "warnings": {"amount": "Unusually high"}}
[WARN] Payment warnings: [Amount is unusually high]
[INFO] Payment initiated successfully: MM...
```

---

## 📊 Complete Testing Workflow

### Workflow 1: Happy Path ✅

```
1. POST /initiate (Valid payment)
   ↓ Status: 201 Created
   Get: transactionId
   
2. PATCH /confirm (Confirm payment)
   ↓ Status: 200 OK
   Status: SUCCESS
   
3. GET /details (Check payment)
   ↓ Status: 200 OK
   Status: SUCCESS
```

### Workflow 2: Failed Payment ❌

```
1. POST /initiate (Invalid payment - same buyer/seller)
   ↓ Status: 400 Bad Request
   Error message shown
   
2. Payment NOT created in database
```

### Workflow 3: With Failure Reason ❌

```
1. POST /initiate (Valid payment)
   ↓ Status: 201 Created
   Get: transactionId
   
2. PATCH /fail (Mark as failed)
   ↓ Status: 200 OK
   Status: FAILED
   failureReason: provided reason
   
3. GET /details (Check payment)
   ↓ Status: 200 OK
   Status: FAILED
```

---

## 🎯 Postman Collection Setup

### Create Collection: "Payment Service Testing"

**Requests:**

1. **Initiate Payment (Valid)**
   - POST http://localhost:8092/api/v1/payments/initiate
   - Body: Valid payment JSON

2. **Initiate Payment (Invalid - Same User)**
   - POST http://localhost:8092/api/v1/payments/initiate
   - Body: Same buyerId & sellerId

3. **Initiate Payment (Amount Too Low)**
   - POST http://localhost:8092/api/v1/payments/initiate
   - Body: amount = 500.0

4. **Confirm Payment**
   - PATCH http://localhost:8092/api/v1/payments/{{transactionId}}/confirm
   - Body: {}

5. **Get Payment Details**
   - GET http://localhost:8092/api/v1/payments/{{transactionId}}
   - Body: (empty)

6. **Fail Payment**
   - PATCH http://localhost:8092/api/v1/payments/{{transactionId}}/fail?reason=USER_CANCELLED
   - Body: {}

---

## 🔍 How to View Rule Engine Integration in Action

### Check Payment Service Logs

**Look for these patterns:**

**1. When initiating payment with valid data:**
```
[INFO] Initiating payment for buyer: 100, amount: 50000.0
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=100, sellerId=200, blacklisted=false, ...}
[INFO] Rule Engine Response: {approved=true, message=Payment approved}
[INFO] Payment initiated successfully: MM...
```

**2. When initiating payment with invalid data:**
```
[INFO] Initiating payment for buyer: 100, amount: 50000.0
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=100, sellerId=100, ...}
[INFO] Rule Engine Response: {approved=false, violations: {...}}
[ERROR] Payment rejected by rule engine: [Buyer and seller cannot be the same person]
```

**3. When Rule Engine is disabled:**
```
[WARN] Rule engine is disabled. Skipping validation.
[INFO] Payment initiated successfully: MM...
```

---

## ✅ Complete Testing Checklist

### Test Cases
- [ ] **Valid Payment** - Should create payment (201)
- [ ] **Same Buyer/Seller** - Should reject (400)
- [ ] **Low Amount** - Should reject (400)
- [ ] **High Amount** - Should create with warnings (201)
- [ ] **Confirm Payment** - Should change status to SUCCESS (200)
- [ ] **Get Payment** - Should return payment details (200)
- [ ] **Fail Payment** - Should change status to FAILED (200)

### Rule Engine Integration
- [ ] **Rule Engine Request logged** - See Rule Engine Request in logs
- [ ] **Rule Engine Response logged** - See Rule Engine Response in logs
- [ ] **Valid payments approved** - approved=true
- [ ] **Invalid payments rejected** - approved=false
- [ ] **Warnings captured** - Warnings logged

### Database
- [ ] **Payment created in database** - Check MySQL for payment records
- [ ] **Status updated correctly** - PENDING → SUCCESS or FAILED
- [ ] **All fields saved** - transactionId, amount, buyerId, sellerId, etc.

---

## 📱 Database Query (Optional)

Check payments in MySQL:

```sql
-- View all payments
SELECT * FROM payments ORDER BY created_at DESC LIMIT 10;

-- View specific payment
SELECT * FROM payments WHERE transaction_id = 'MM...';

-- View payment statistics
SELECT 
  status, 
  COUNT(*) as count,
  SUM(amount) as total_amount
FROM payments
GROUP BY status;

-- View by buyer
SELECT * FROM payments WHERE buyer_id = 100;

-- View by seller
SELECT * FROM payments WHERE seller_id = 200;
```

---

## 🎉 Summary

### After Initiating Payment, You Can:

1. **Confirm Payment** → Changes status to SUCCESS
2. **Get Payment Details** → Retrieve saved payment information
3. **Fail Payment** → Mark payment as failed with reason
4. **Test Different Scenarios** → Valid, invalid, warnings

### Rule Engine Integration Testing:

1. **Valid payments** → Rule Engine approves → Payment created ✅
2. **Invalid payments** → Rule Engine rejects → Error returned ❌
3. **Check logs** → Verify Rule Engine Request/Response
4. **Database** → Confirm payments are saved

---

## 📚 All Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/payments/initiate` | Create payment (calls Rule Engine) |
| PATCH | `/api/v1/payments/{id}/confirm` | Mark payment as successful |
| PATCH | `/api/v1/payments/{id}/fail` | Mark payment as failed |
| GET | `/api/v1/payments/{id}` | Get payment details |
| GET | `/swagger-ui.html` | API Documentation |
| GET | `/actuator/health` | Health check |

---

**Ready to test!** 🚀

Start with Step 1 (Initiate Payment), then follow Steps 2-4 in sequence.
