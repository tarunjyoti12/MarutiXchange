# 📊 Visual API Testing Flow - Step by Step

## 🔄 Complete Payment Flow with Examples

```
┌─────────────────────────────────────────────────────────────┐
│           STEP 1: INITIATE PAYMENT (POST)                   │
├─────────────────────────────────────────────────────────────┤
│ URL: http://localhost:8092/api/v1/payments/initiate        │
│                                                             │
│ REQUEST BODY:                                              │
│ {                                                          │
│   "carListingId": 1,                                       │
│   "buyerId": 100,                                          │
│   "sellerId": 200,                                         │
│   "amount": 50000.0,                                       │
│   "paymentMethod": "UPI"                                   │
│ }                                                          │
│                                                             │
│ WHAT HAPPENS:                                              │
│ 1. Payment Service receives request                        │
│ 2. Calls Rule Engine (8055)                                │
│ 3. Rule Engine validates (buyerId ≠ sellerId, amount)     │
│ 4. Rule Engine approves                                    │
│ 5. Payment created in database                             │
│                                                             │
│ RESPONSE (201 Created):                                    │
│ {                                                          │
│   "status": 201,                                           │
│   "data": {                                                │
│     "transactionId": "MM1775716480294539FAA",             │
│     "status": "PENDING"                                    │
│   }                                                        │
│ }                                                          │
│                                                             │
│ ✅ NEXT: Save transactionId                                │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│         STEP 2: CONFIRM PAYMENT (PATCH)                     │
├─────────────────────────────────────────────────────────────┤
│ URL: /api/v1/payments/MM1775716480294539FAA/confirm        │
│                                                             │
│ REQUEST BODY: {}                                           │
│                                                             │
│ WHAT HAPPENS:                                              │
│ 1. Payment Service finds payment by transactionId          │
│ 2. Changes status: PENDING → SUCCESS                       │
│ 3. Saves to database                                       │
│                                                             │
│ RESPONSE (200 OK):                                         │
│ {                                                          │
│   "status": 200,                                           │
│   "data": {                                                │
│     "transactionId": "MM1775716480294539FAA",             │
│     "status": "SUCCESS"                                    │
│   }                                                        │
│ }                                                          │
│                                                             │
│ ✅ Status changed: PENDING → SUCCESS                       │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│       STEP 3: GET PAYMENT DETAILS (GET)                     │
├─────────────────────────────────────────────────────────────┤
│ URL: /api/v1/payments/MM1775716480294539FAA                │
│                                                             │
│ REQUEST BODY: (none)                                       │
│                                                             │
│ WHAT HAPPENS:                                              │
│ 1. Payment Service finds payment by transactionId          │
│ 2. Returns complete payment details                        │
│                                                             │
│ RESPONSE (200 OK):                                         │
│ {                                                          │
│   "status": 200,                                           │
│   "data": {                                                │
│     "transactionId": "MM1775716480294539FAA",             │
│     "buyerId": 100,                                        │
│     "sellerId": 200,                                       │
│     "amount": 50000.0,                                     │
│     "status": "SUCCESS",                                   │
│     "createdAt": "2026-04-09T12:04:40"                    │
│   }                                                        │
│ }                                                          │
│                                                             │
│ ✅ All payment details returned                            │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│        STEP 4: FAIL PAYMENT (PATCH - OPTIONAL)             │
├─────────────────────────────────────────────────────────────┤
│ URL: /api/v1/payments/MM.../fail?reason=USER_CANCELLED    │
│                                                             │
│ REQUEST BODY: {}                                           │
│                                                             │
│ WHAT HAPPENS:                                              │
│ 1. Payment Service finds payment by transactionId          │
│ 2. Changes status: SUCCESS → FAILED                        │
│ 3. Sets failure reason                                     │
│                                                             │
│ RESPONSE (200 OK):                                         │
│ {                                                          │
│   "status": 200,                                           │
│   "data": {                                                │
│     "transactionId": "MM1775716480294539FAA",             │
│     "status": "FAILED",                                    │
│     "failureReason": "USER_CANCELLED"                     │
│   }                                                        │
│ }                                                          │
│                                                             │
│ ✅ Status changed: SUCCESS → FAILED                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔗 Rule Engine Integration - What Happens Inside

```
┌──────────────────────────────────────────────────────────────────┐
│                 RULE ENGINE INTEGRATION FLOW                     │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  STEP 1: INITIATE PAYMENT                                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ POST /api/v1/payments/initiate                          │   │
│  │ {buyerId: 100, sellerId: 200, amount: 50000.0}        │   │
│  └────────────────────┬────────────────────────────────────┘   │
│                       │                                          │
│                       ▼                                          │
│  CALL RULE ENGINE (Internal)                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ POST http://localhost:8055/rules/evaluate              │   │
│  │                                                         │   │
│  │ REQUEST:                                                │   │
│  │ {                                                       │   │
│  │   "type": "payment",                                   │   │
│  │   "price": 50000.0,                                    │   │
│  │   "userId": 100,                                       │   │
│  │   "sellerId": 200,                                     │   │
│  │   "timestamp": 1775716314351                           │   │
│  │ }                                                       │   │
│  │                                                         │   │
│  │ RESPONSE:                                               │   │
│  │ {                                                       │   │
│  │   "approved": true,                                    │   │
│  │   "message": "Payment approved",                       │   │
│  │   "violations": null,                                  │   │
│  │   "warnings": null                                     │   │
│  │ }                                                       │   │
│  └────────────────────┬────────────────────────────────────┘   │
│                       │                                          │
│  IF APPROVED (true) ──┼─────────────────────────────┐          │
│  IF REJECTED (false) ─┼─────────────────────┐       │          │
│                       │                     │       │          │
│              ✅ CREATE PAYMENT    ❌ RETURN ERROR  │          │
│              Status: PENDING      Status: 400     │          │
│              → 201 Created        Bad Request     │          │
│                       │                           │          │
│                       └───────────┬─────────────────┘         │
│                                   │                           │
│                                   ▼                           │
│                          RETURN TO POSTMAN                    │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## ✅ Test Scenarios Matrix

```
┌─────────────────────────────────────────────────────────────┐
│              TEST SCENARIOS & EXPECTED RESULTS               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ SCENARIO 1: VALID PAYMENT                                  │
│ ├─ buyerId: 100                                            │
│ ├─ sellerId: 200                                           │
│ ├─ amount: 50000.0                                         │
│ ├─ Rule Engine: APPROVED ✅                                │
│ └─ Result: 201 Created ✅                                  │
│                                                             │
│ SCENARIO 2: SAME BUYER & SELLER                            │
│ ├─ buyerId: 100                                            │
│ ├─ sellerId: 100                                           │
│ ├─ amount: 50000.0                                         │
│ ├─ Rule Engine: REJECTED ❌                                │
│ └─ Result: 400 Bad Request ❌                              │
│    Error: "Buyer and seller cannot be the same"           │
│                                                             │
│ SCENARIO 3: AMOUNT TOO LOW                                 │
│ ├─ buyerId: 100                                            │
│ ├─ sellerId: 200                                           │
│ ├─ amount: 500.0                                           │
│ ├─ Validation: FAILED (before Rule Engine)                 │
│ └─ Result: 400 Bad Request ❌                              │
│    Error: "Amount must be at least 1000"                   │
│                                                             │
│ SCENARIO 4: HIGH AMOUNT                                    │
│ ├─ buyerId: 100                                            │
│ ├─ sellerId: 200                                           │
│ ├─ amount: 5000000.0                                       │
│ ├─ Rule Engine: APPROVED with WARNINGS ⚠️                 │
│ └─ Result: 201 Created ✅                                  │
│    Warnings: Amount is unusually high                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📱 API Endpoint Summary Card

```
┌─────────────────────────────────────────────────────┐
│ ENDPOINT 1: INITIATE PAYMENT                        │
├─────────────────────────────────────────────────────┤
│ METHOD: POST                                        │
│ URL: /api/v1/payments/initiate                     │
│ RULE ENGINE: ✅ Called                              │
│ STATUS: 201 Created (if valid)                      │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ ENDPOINT 2: CONFIRM PAYMENT                         │
├─────────────────────────────────────────────────────┤
│ METHOD: PATCH                                       │
│ URL: /api/v1/payments/{transactionId}/confirm      │
│ RULE ENGINE: ❌ Not called                          │
│ STATUS: 200 OK                                      │
│ Action: PENDING → SUCCESS                           │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ ENDPOINT 3: GET PAYMENT                             │
├─────────────────────────────────────────────────────┤
│ METHOD: GET                                         │
│ URL: /api/v1/payments/{transactionId}              │
│ RULE ENGINE: ❌ Not called                          │
│ STATUS: 200 OK                                      │
│ Action: Return payment details                      │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ ENDPOINT 4: FAIL PAYMENT                            │
├─────────────────────────────────────────────────────┤
│ METHOD: PATCH                                       │
│ URL: /api/v1/payments/{transactionId}/fail         │
│ RULE ENGINE: ❌ Not called                          │
│ STATUS: 200 OK                                      │
│ Action: SUCCESS → FAILED                            │
└─────────────────────────────────────────────────────┘
```

---

## 🎉 Quick Summary

```
AFTER INITIATE PAYMENT:

✅ STEP 2: CONFIRM (PENDING → SUCCESS)
✅ STEP 3: GET (View payment details)
✅ STEP 4: FAIL (SUCCESS → FAILED)

RULE ENGINE INTEGRATION:

✅ Called during STEP 1 (Initiate)
✅ Validates business rules
✅ Approves or rejects payment
✅ Check logs for integration details
```

---

**See detailed guides for complete Postman examples!** 📚
