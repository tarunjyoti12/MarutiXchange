# 📚 Complete Payment Service Testing - All Steps & Integration

## 🎯 Full Payment API Testing Guide

After successfully initiating a payment, you need to test 4 additional API endpoints and verify the Rule Engine integration.

---

## 📋 Payment Lifecycle Overview

```
START
  ↓
STEP 1: INITIATE PAYMENT (POST /initiate)
  ├─ Calls Rule Engine (8055)
  ├─ Rule Engine validates
  └─ Creates payment if valid
        ↓
STEP 2: CONFIRM PAYMENT (PATCH /confirm)
  └─ Changes status: PENDING → SUCCESS
        ↓
STEP 3: GET PAYMENT (GET /details)
  └─ Retrieves saved payment
        ↓
STEP 4: FAIL PAYMENT (PATCH /fail)
  └─ Changes status: SUCCESS → FAILED
        ↓
END
```

---

## 🚀 STEP 1: INITIATE PAYMENT

**URL:**
```
POST http://localhost:8092/api/v1/payments/initiate
```

**Headers:**
```
Content-Type: application/json
```

**Body:**
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

**Expected Response (201 Created):**
```json
{
  "status": 201,
  "message": "Payment initiated successfully",
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "carListingId": 1,
    "auctionId": 1,
    "buyerId": 100,
    "sellerId": 200,
    "amount": 50000.0,
    "paymentMethod": "UPI",
    "invoiceNumber": "INV-MX-1775716480294",
    "status": "PENDING",
    "createdAt": "2026-04-09T12:04:40"
  }
}
```

**Important:** 
- ✅ Save the `transactionId` value
- You'll need it for Steps 2, 3, and 4

**Rule Engine Integration Check:**
Look in logs for:
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=100, sellerId=200, ...}
[INFO] Rule Engine Response: {approved=true, message=Payment approved}
```

---

## 🚀 STEP 2: CONFIRM PAYMENT

**URL (Replace MM... with your transactionId):**
```
PATCH http://localhost:8092/api/v1/payments/MM1775716480294539FAA/confirm
```

**Body:**
```json
{}
```

**Expected Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment confirmed successfully",
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "buyerId": 100,
    "sellerId": 200,
    "amount": 50000.0,
    "status": "SUCCESS",
    "updatedAt": "2026-04-09T12:05:10"
  }
}
```

**What Changed:**
- Status: `PENDING` → `SUCCESS`

---

## 🚀 STEP 3: GET PAYMENT DETAILS

**URL (Replace MM... with your transactionId):**
```
GET http://localhost:8092/api/v1/payments/MM1775716480294539FAA
```

**Body:**
```
(No body needed)
```

**Expected Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment retrieved successfully",
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "carListingId": 1,
    "auctionId": 1,
    "buyerId": 100,
    "sellerId": 200,
    "amount": 50000.0,
    "paymentMethod": "UPI",
    "invoiceNumber": "INV-MX-1775716480294",
    "status": "SUCCESS",
    "createdAt": "2026-04-09T12:04:40",
    "updatedAt": "2026-04-09T12:05:10"
  }
}
```

**Verify:**
- All your payment data is saved correctly
- Status shows latest value (SUCCESS)

---

## 🚀 STEP 4: FAIL PAYMENT (Optional)

**URL (Replace MM... with your transactionId):**
```
PATCH http://localhost:8092/api/v1/payments/MM1775716480294539FAA/fail?reason=USER_CANCELLED
```

**Possible Reasons:**
```
USER_CANCELLED
PAYMENT_DECLINED
NETWORK_ERROR
INVALID_DETAILS
INSUFFICIENT_FUNDS
OTHER
```

**Body:**
```json
{}
```

**Expected Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment failed successfully",
  "data": {
    "transactionId": "MM1775716480294539FAA",
    "status": "FAILED",
    "failureReason": "USER_CANCELLED",
    "updatedAt": "2026-04-09T12:06:00"
  }
}
```

**What Changed:**
- Status: `SUCCESS` → `FAILED`
- failureReason: `USER_CANCELLED`

---

## 🔗 RULE ENGINE INTEGRATION TESTING

### Test Case 1: Valid Payment (Should Pass) ✅

**Request:**
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

**Expected Result:**
- Status: ✅ 201 Created
- Payment: ✅ Created in database
- Rule Engine: ✅ Approved (approved=true)

**Logs Should Show:**
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=100, sellerId=200, blacklisted=false, timestamp=..., ...}
[INFO] Rule Engine Response: {approved=true, message=Payment approved, violations=null, warnings=null}
[INFO] Payment initiated successfully: MM...
```

---

### Test Case 2: Invalid Payment - Same Buyer & Seller (Should Fail) ❌

**Request:**
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

**Expected Result:**
- Status: ❌ 400 Bad Request
- Payment: ❌ NOT created in database
- Rule Engine: ❌ Rejected (approved=false)
- Error Message: "Buyer and seller cannot be the same person"

**Logs Should Show:**
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=100, sellerId=100, ...}
[INFO] Rule Engine Response: {approved=false, violations={...}, message=Payment validation failed}
[ERROR] Payment rejected by rule engine: [Buyer and seller cannot be the same person]
```

---

### Test Case 3: Invalid Payment - Amount Too Low (Should Fail) ❌

**Request:**
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

**Expected Result:**
- Status: ❌ 400 Bad Request
- Error Message: "Amount must be at least 1000"
- This is a validation error (before Rule Engine call)

**Response:**
```json
{
  "error": "Bad Request",
  "message": "Amount must be at least 1000",
  "status": 400
}
```

---

### Test Case 4: High Amount with Warnings (Should Pass with Warnings) ⚠️

**Request:**
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

**Expected Result:**
- Status: ✅ 201 Created
- Payment: ✅ Created in database
- Warnings: ⚠️ Logged but payment still created

**Logs Should Show:**
```
[INFO] Rule Engine Request: {type=payment, price=5000000.0, ...}
[INFO] Rule Engine Response: {..., approved=true, warnings={high_amount: "Amount is unusually high"}}
[WARN] Payment warnings: [Amount is unusually high]
[INFO] Payment initiated successfully: MM...
```

---

## 📊 Complete Testing Workflow

### Workflow A: Full Payment Lifecycle ✅

```
Step 1: POST /initiate (valid payment)
  ↓ 201 Created
  Get: transactionId

Step 2: PATCH /confirm (confirm payment)
  ↓ 200 OK
  Status: PENDING → SUCCESS

Step 3: GET /details (check payment)
  ↓ 200 OK
  Status: SUCCESS

Step 4: PATCH /fail (mark as failed)
  ↓ 200 OK
  Status: SUCCESS → FAILED

Final: GET /details (verify)
  ↓ 200 OK
  Status: FAILED
```

### Workflow B: Rejected Payment ❌

```
Step 1: POST /initiate (invalid payment - same buyer/seller)
  ↓ 400 Bad Request
  Error: "Buyer and seller cannot be the same"

Step 2: Payment NOT created
  ✅ Cannot call /confirm (payment doesn't exist)
```

---

## 🎯 Postman Collection Setup

Create a Postman collection named "Payment Service Testing" with these requests:

1. **Initiate Payment - Valid**
   - POST http://localhost:8092/api/v1/payments/initiate
   - Valid JSON body

2. **Initiate Payment - Same User**
   - POST http://localhost:8092/api/v1/payments/initiate
   - buyerId = sellerId (should fail)

3. **Initiate Payment - Low Amount**
   - POST http://localhost:8092/api/v1/payments/initiate
   - amount = 500.0 (should fail)

4. **Confirm Payment**
   - PATCH http://localhost:8092/api/v1/payments/{{transactionId}}/confirm
   - Body: {}

5. **Get Payment Details**
   - GET http://localhost:8092/api/v1/payments/{{transactionId}}
   - No body

6. **Fail Payment**
   - PATCH http://localhost:8092/api/v1/payments/{{transactionId}}/fail?reason=USER_CANCELLED
   - Body: {}

**Pro Tip:** Use Postman variables
- Create variable: `transactionId`
- Extract transactionId from response
- Use `{{transactionId}}` in subsequent requests

---

## ✅ Complete Testing Checklist

### Payment Lifecycle
- [ ] Step 1: Initiate → 201 Created
- [ ] Step 2: Confirm → 200 OK, status=SUCCESS
- [ ] Step 3: Get Details → 200 OK
- [ ] Step 4: Fail → 200 OK, status=FAILED

### Rule Engine Integration
- [ ] Valid payment → 201 Created
- [ ] Same buyer/seller → 400 Error
- [ ] Low amount → 400 Error
- [ ] High amount → 201 Created with warnings

### Logs Verification
- [ ] Rule Engine Request logged
- [ ] Rule Engine Response logged
- [ ] approved=true for valid payments
- [ ] approved=false for invalid payments
- [ ] Warnings shown for high amounts

### Database
- [ ] Payment created in MySQL
- [ ] Status updates correctly
- [ ] All fields saved
- [ ] transactionId is unique

---

## 📱 All Endpoints Reference

| Method | Endpoint | Status | Purpose |
|--------|----------|--------|---------|
| POST | `/api/v1/payments/initiate` | 201 | Create payment (calls Rule Engine) |
| PATCH | `/api/v1/payments/{id}/confirm` | 200 | Mark as successful |
| PATCH | `/api/v1/payments/{id}/fail` | 200 | Mark as failed |
| GET | `/api/v1/payments/{id}` | 200 | Get payment details |
| GET | `/swagger-ui.html` | 200 | API Documentation |
| GET | `/actuator/health` | 200 | Health check |

---

## 🔍 Logs to Monitor

**Always check application logs for:**

1. **Rule Engine Request:**
   ```
   [INFO] Rule Engine Request: {type=payment, ...}
   ```

2. **Rule Engine Response:**
   ```
   [INFO] Rule Engine Response: {approved=true/false, ...}
   ```

3. **Payment Creation:**
   ```
   [INFO] Payment initiated successfully: MM...
   ```

4. **Rejection:**
   ```
   [ERROR] Payment rejected by rule engine: [...]
   ```

5. **Warnings:**
   ```
   [WARN] Payment warnings: [...]
   ```

---

## 📚 Additional Resources

**See these files for more details:**
- `COMPLETE_API_TESTING_GUIDE.md` - Detailed testing guide
- `TESTING_AFTER_INITIATE.md` - Step-by-step workflow
- `QUICK_TEST_REFERENCE.md` - Quick lookup card
- `ERROR_FOUND_AND_FIXED.md` - Troubleshooting

---

## 🎉 Summary

After initiating a payment, you can:

1. **Confirm Payment** → Change status to SUCCESS
2. **Get Payment Details** → Retrieve saved payment
3. **Fail Payment** → Change status to FAILED
4. **Test Rule Engine Integration** → Verify validation works

The Rule Engine is called automatically during Step 1 (Initiate Payment) to validate:
- Different buyer & seller ✅
- Amount >= 1000 ✅
- Other business rules ✅

---

**Ready to test?** Follow the 4 steps above with your Postman requests! 🚀
