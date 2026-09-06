# ⚡ Quick Reference - API Testing After Payment Initiation

## 🔢 4 Steps to Complete Payment Testing

### STEP 1️⃣: INITIATE PAYMENT
```
POST http://localhost:8092/api/v1/payments/initiate

{
  "carListingId": 1,
  "buyerId": 100,
  "sellerId": 200,
  "amount": 50000.0,
  "paymentMethod": "UPI"
}

✅ Response: 201 Created
📌 SAVE: transactionId (you'll need it)
```

---

### STEP 2️⃣: CONFIRM PAYMENT
```
PATCH http://localhost:8092/api/v1/payments/{transactionId}/confirm

{}

✅ Response: 200 OK
status: PENDING → SUCCESS
```

---

### STEP 3️⃣: GET PAYMENT DETAILS
```
GET http://localhost:8092/api/v1/payments/{transactionId}

(no body)

✅ Response: 200 OK
Returns: Full payment details
```

---

### STEP 4️⃣: FAIL PAYMENT (Optional)
```
PATCH http://localhost:8092/api/v1/payments/{transactionId}/fail?reason=USER_CANCELLED

{}

✅ Response: 200 OK
status: SUCCESS → FAILED
```

---

## 🔗 Rule Engine Integration - 4 Test Cases

### TEST 1: Valid Payment ✅
```json
{
  "buyerId": 100,
  "sellerId": 200,
  "amount": 50000.0,
  "paymentMethod": "UPI"
}
```
**Expected:** ✅ 201 Created

---

### TEST 2: Same Buyer & Seller ❌
```json
{
  "buyerId": 100,
  "sellerId": 100,
  "amount": 50000.0,
  "paymentMethod": "UPI"
}
```
**Expected:** ❌ 400 Bad Request
**Error:** "Buyer and seller cannot be the same"

---

### TEST 3: Amount Too Low ❌
```json
{
  "buyerId": 100,
  "sellerId": 200,
  "amount": 500.0,
  "paymentMethod": "UPI"
}
```
**Expected:** ❌ 400 Bad Request
**Error:** "Amount must be at least 1000"

---

### TEST 4: High Amount ⚠️
```json
{
  "buyerId": 100,
  "sellerId": 200,
  "amount": 5000000.0,
  "paymentMethod": "BANK_TRANSFER"
}
```
**Expected:** ✅ 201 Created (with warnings)

---

## 📊 Rule Engine Logs to Check

### When Payment is APPROVED ✅
```
[INFO] Rule Engine Request: {type=payment, userId=100, sellerId=200, ...}
[INFO] Rule Engine Response: {approved=true, ...}
[INFO] Payment initiated successfully: MM...
```

### When Payment is REJECTED ❌
```
[INFO] Rule Engine Response: {approved=false, ...}
[ERROR] Payment rejected by rule engine: [Buyer and seller cannot be the same]
```

### When Rule Engine is DISABLED
```
[WARN] Rule engine is disabled. Skipping validation.
[INFO] Payment initiated successfully: MM...
```

---

## ✅ Testing Checklist

```
Initiate Payment
  ✅ Status 201
  ✅ Got transactionId
  
Confirm Payment
  ✅ Status 200
  ✅ Status changed to SUCCESS
  
Get Payment
  ✅ Status 200
  ✅ All details returned
  
Fail Payment
  ✅ Status 200
  ✅ Status changed to FAILED
```

---

## 📱 All Endpoints

| Step | Method | Endpoint | Purpose |
|------|--------|----------|---------|
| 1 | POST | `/api/v1/payments/initiate` | Create payment |
| 2 | PATCH | `/api/v1/payments/{id}/confirm` | Confirm payment |
| 3 | GET | `/api/v1/payments/{id}` | Get details |
| 4 | PATCH | `/api/v1/payments/{id}/fail` | Mark failed |

---

**See:** `COMPLETE_API_TESTING_GUIDE.md` for detailed examples
**See:** `TESTING_AFTER_INITIATE.md` for full workflow

🎉 **Ready to test!**
