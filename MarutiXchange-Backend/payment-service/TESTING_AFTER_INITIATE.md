# 🎯 Testing Summary - Steps After Payment Initiation

## 📋 Payment Lifecycle (5 Steps)

```
Step 1: INITIATE PAYMENT
        ↓
Step 2: CONFIRM PAYMENT
        ↓
Step 3: GET PAYMENT DETAILS
        ↓
Step 4: FAIL PAYMENT (optional)
        ↓
Step 5: VERIFY IN DATABASE
```

---

## 🚀 Step-by-Step Testing

### STEP 1: INITIATE PAYMENT ✅ (You're here)

**Postman:**
```
POST http://localhost:8092/api/v1/payments/initiate
```

**Body:**
```json
{
  "carListingId": 1,
  "buyerId": 100,
  "sellerId": 200,
  "amount": 50000.0,
  "paymentMethod": "UPI"
}
```

**Response (201):**
```json
{
  "status": 201,
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "status": "PENDING"
  }
}
```

**✅ Check:**
- [ ] Status: 201 Created
- [ ] transactionId: MM...
- [ ] status: PENDING
- **Save this transactionId** - you'll need it for next steps!

---

### STEP 2: CONFIRM PAYMENT

**Postman:**
```
PATCH http://localhost:8092/api/v1/payments/MM1775716480294539FAA/confirm
```

**Body:**
```json
{}
```

**Response (200):**
```json
{
  "status": 200,
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "status": "SUCCESS"
  }
}
```

**✅ Check:**
- [ ] Status: 200 OK
- [ ] status changed to: SUCCESS

---

### STEP 3: GET PAYMENT DETAILS

**Postman:**
```
GET http://localhost:8092/api/v1/payments/MM1775716480294539FAA
```

**Body:**
```
(Leave empty)
```

**Response (200):**
```json
{
  "status": 200,
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "status": "SUCCESS",
    "amount": 50000.0,
    "buyerId": 100,
    "sellerId": 200
  }
}
```

**✅ Check:**
- [ ] Status: 200 OK
- [ ] All your payment data is there
- [ ] status: SUCCESS

---

### STEP 4: FAIL PAYMENT (Optional)

**Postman:**
```
PATCH http://localhost:8092/api/v1/payments/MM1775716480294539FAA/fail?reason=USER_CANCELLED
```

**Body:**
```json
{}
```

**Response (200):**
```json
{
  "status": 200,
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "status": "FAILED",
    "failureReason": "USER_CANCELLED"
  }
}
```

**✅ Check:**
- [ ] Status: 200 OK
- [ ] status: FAILED
- [ ] failureReason: USER_CANCELLED

---

## 🔗 Rule Engine Integration Testing

### How to Verify Rule Engine is Working

#### Test 1: Valid Payment (Should Pass) ✅

**Send this:**
```json
{
  "carListingId": 1,
  "buyerId": 100,
  "sellerId": 200,
  "amount": 50000.0,
  "paymentMethod": "UPI"
}
```

**Expected:**
- ✅ 201 Created
- ✅ Payment in database

**Check Logs for:**
```
[INFO] Rule Engine Request: {type=payment, userId=100, sellerId=200, ...}
[INFO] Rule Engine Response: {approved=true, ...}
```

---

#### Test 2: Invalid Payment - Same User (Should Fail) ❌

**Send this:**
```json
{
  "carListingId": 1,
  "buyerId": 100,
  "sellerId": 100,
  "amount": 50000.0,
  "paymentMethod": "UPI"
}
```

**Expected:**
- ❌ 400 Bad Request
- ❌ Payment NOT in database
- ❌ Error message: "Buyer and seller cannot be the same"

**Check Logs for:**
```
[INFO] Rule Engine Response: {approved=false, ...}
[ERROR] Payment rejected by rule engine
```

---

#### Test 3: Invalid Payment - Low Amount (Should Fail) ❌

**Send this:**
```json
{
  "carListingId": 1,
  "buyerId": 100,
  "sellerId": 200,
  "amount": 500.0,
  "paymentMethod": "UPI"
}
```

**Expected:**
- ❌ 400 Bad Request
- ❌ Error message: "Amount must be at least 1000"

---

#### Test 4: High Amount (May Have Warnings) ⚠️

**Send this:**
```json
{
  "carListingId": 1,
  "buyerId": 100,
  "sellerId": 200,
  "amount": 5000000.0,
  "paymentMethod": "BANK_TRANSFER",
  "bankName": "HDFC Bank"
}
```

**Expected:**
- ✅ 201 Created
- ⚠️ Warnings in logs

**Check Logs for:**
```
[WARN] Payment warnings: [Amount is unusually high]
```

---

## 📊 Full Flow Diagram

```
INITIATE PAYMENT (Step 1)
├─ Valid Data ✅
│  ├─ Call Rule Engine (8055)
│  ├─ Rule Engine approves
│  └─ Create Payment in DB → 201 Created
│
└─ Invalid Data ❌
   ├─ Call Rule Engine (8055)
   ├─ Rule Engine rejects
   └─ Return 400 Error

CONFIRM PAYMENT (Step 2)
├─ Payment exists ✅
│  └─ Change status PENDING → SUCCESS
│
└─ Payment not found ❌
   └─ Return 404 Error

GET PAYMENT (Step 3)
├─ Payment exists ✅
│  └─ Return payment details
│
└─ Payment not found ❌
   └─ Return 404 Error

FAIL PAYMENT (Step 4)
├─ Payment exists ✅
│  └─ Change status → FAILED
│
└─ Payment not found ❌
   └─ Return 404 Error
```

---

## 📝 Copy-Paste Ready Requests

### Request 1: Valid Payment
```
POST http://localhost:8092/api/v1/payments/initiate

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

### Request 2: Confirm Payment
```
PATCH http://localhost:8092/api/v1/payments/{COPY_TRANSACTION_ID_HERE}/confirm

{}
```

### Request 3: Get Payment
```
GET http://localhost:8092/api/v1/payments/{COPY_TRANSACTION_ID_HERE}
```

### Request 4: Fail Payment
```
PATCH http://localhost:8092/api/v1/payments/{COPY_TRANSACTION_ID_HERE}/fail?reason=USER_CANCELLED

{}
```

---

## ✅ Testing Checklist

**Payment Lifecycle:**
- [ ] Step 1: Initiate → 201 Created ✅
- [ ] Get transactionId from response
- [ ] Step 2: Confirm → 200 OK, status=SUCCESS ✅
- [ ] Step 3: Get Details → 200 OK, status=SUCCESS ✅
- [ ] Step 4: Fail → 200 OK, status=FAILED ✅

**Rule Engine Integration:**
- [ ] Valid payment created (Rule approved)
- [ ] Invalid payment rejected (Rule rejected)
- [ ] Logs show Rule Engine Request
- [ ] Logs show Rule Engine Response

**Database:**
- [ ] Payment record created
- [ ] Status updated correctly
- [ ] All fields saved

---

## 🎉 Summary

### 4 API Endpoints You Need to Test

1. **POST /initiate** - Create payment (calls Rule Engine)
2. **PATCH /confirm** - Mark as successful
3. **GET /details** - Retrieve payment
4. **PATCH /fail** - Mark as failed

### How Rule Engine Integration Works

```
Payment Initiation
    ↓
Call Rule Engine API (localhost:8055)
    ↓
If Valid → Create Payment ✅
If Invalid → Return Error ❌
    ↓
Return Response to Postman
```

---

**Next:** Follow the 4 steps above with your transactionId!
