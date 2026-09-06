# 🧪 Testing Payment Service with Rule Engine - Postman Guide

## ✅ Service Status

| Service | Port | Status |
|---------|------|--------|
| Rule Engine | 8055 | ✅ Running |
| Payment Service | 8091 | ✅ Starting |
| MySQL Database | 3306 | ✅ Required |

---

## 🚀 Payment Service URLs

### Base URL
```
http://localhost:8091
```

### API Endpoint
```
POST http://localhost:8091/api/v1/payments/initiate
```

### Swagger UI
```
http://localhost:8091/swagger-ui.html
```

---

## 📋 Test Case 1: Valid Payment (SHOULD PASS RULES ✅)

### Request

**URL:**
```
POST http://localhost:8091/api/v1/payments/initiate
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
  "amount": 50000,
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
    "amount": 50000,
    "status": "PENDING",
    "invoiceNumber": "INV-MX-1775716480294",
    "buyerId": 100,
    "sellerId": 200,
    "paymentMethod": "UPI"
  }
}
```

### What Happens Behind the Scenes
```
1. Payment request received at Payment Service (port 8091)
2. PaymentServiceImpl.initiatePayment() is called
3. RuleEngineClient.evaluate() is called
4. HTTP POST sent to Rule Engine (port 8055)
5. Rule Engine validates: different buyer & seller ✅
6. Rule Engine returns: approved = true
7. Payment is created in database
8. Response sent back to Postman
```

---

## 📋 Test Case 2: Invalid Payment - Same Buyer & Seller (SHOULD FAIL ❌)

### Request

**URL:**
```
POST http://localhost:8091/api/v1/payments/initiate
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
  "sellerId": 100,
  "amount": 50000,
  "paymentMethod": "UPI",
  "paymentType": "TOKEN",
  "upiId": "buyer@upi",
  "upiApp": "GooglePay",
  "isTokenPayment": true
}
```

### Expected Response (400 Bad Request)
```json
{
  "status": 400,
  "message": "Payment validation failed: Buyer and seller cannot be the same person",
  "error": "PaymentValidationException"
}
```

### What Happens
```
1. Payment request received
2. Rule Engine validation called
3. Rule Engine detects: buyerId == sellerId ❌
4. Rule Engine returns: approved = false
5. RuleEngineClient throws PaymentValidationException
6. Payment is NOT created
7. Error response sent to Postman
```

---

## 📋 Test Case 3: High Amount Payment (May Have Warnings ⚠️)

### Request

**URL:**
```
POST http://localhost:8091/api/v1/payments/initiate
```

**Body:**
```json
{
  "carListingId": 2,
  "auctionId": 2,
  "buyerId": 300,
  "sellerId": 400,
  "amount": 5000000,
  "paymentMethod": "BANK_TRANSFER",
  "paymentType": "FULL_PAYMENT",
  "bankName": "HDFC Bank",
  "isTokenPayment": false
}
```

### Expected Response (201 Created)
```json
{
  "status": 201,
  "message": "Payment initiated successfully",
  "data": {
    "transactionId": "MM...",
    "amount": 5000000,
    "status": "PENDING",
    "buyerId": 300,
    "sellerId": 400,
    "paymentMethod": "BANK_TRANSFER"
  }
}
```

### Check Logs for Warnings
```
[WARN] Payment warnings: [Amount is unusually high]
```

---

## 🔍 How to See Rule Engine Integration in Logs

### Look for these log patterns:

**When Rule Engine is called:**
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=100, sellerId=200, ...}
```

**When Rule Engine responds:**
```
[INFO] Rule Engine Response: {approved=true, message=Payment approved}
```

**When payment is rejected:**
```
[ERROR] Payment rejected by rule engine: [Buyer and seller cannot be the same person]
```

---

## 📊 Integration Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        POSTMAN REQUEST                           │
│    POST http://localhost:8091/api/v1/payments/initiate          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────┐
        │   Payment Service (Port 8091)    │
        │  ┌────────────────────────────┐  │
        │  │  PaymentController         │  │
        │  │  ↓                         │  │
        │  │  PaymentServiceImpl         │  │
        │  │  ↓                         │  │
        │  │  RuleEngineClient ◄────────┼──┼─────┐
        │  │  (HTTP Client)    │        │  │     │
        │  └────────────────────────────┘  │     │
        └──────────────────────────────────┘     │
                           │                      │
                           │ HTTP POST            │
                           │ (Rule Validation)    │
                           │                      │
                           ▼                      │
        ┌──────────────────────────────────────┐ │
        │   Rule Engine (Port 8055)            │ │
        │  ┌──────────────────────────────┐    │ │
        │  │  POST /rules/evaluate       │    │ │
        │  │  ↓                          │    │ │
        │  │  Rule Validation            │    │ │
        │  │  (Check buyer != seller)    │    │ │
        │  │  ↓                          │    │ │
        │  │  Return Response            │    │ │
        │  │  {approved: true/false}     │    │ │
        │  └──────────────────────────────┘    │ │
        └──────────────────────────────────────┘ │
                           ▲                      │
                           │ HTTP Response        │
                           └──────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────┐
        │    If approved: Create Payment   │
        │    If rejected: Throw Exception  │
        └──────────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────┐
        │      MySQL Database              │
        │      (Payment saved)             │
        └──────────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────┐
        │   Response Sent to Postman       │
        │   (201 Created or 400 Error)     │
        └──────────────────────────────────┘
```

---

## ✨ Key Configuration

### application.properties
```properties
server.port=8091                                           # Payment Service Port
app.rule-engine.url=http://localhost:8055/rules/evaluate  # Rule Engine URL
app.rule-engine.enabled=true                              # Rule validation enabled
app.rule-engine.retry-attempts=3                          # Retry on failure
```

---

## 🎯 Testing Checklist

Before testing:
- [ ] Rule Engine is running on port 8055
- [ ] MySQL database is running on port 3306
- [ ] Payment Service is running on port 8091
- [ ] Postman is installed and ready

### Test Sequence

1. **Test Case 1 (Valid Payment)**
   - [ ] Send valid payment request
   - [ ] Check response: 201 Created
   - [ ] Check logs: Rule Engine Request/Response
   - [ ] Verify: Payment in database

2. **Test Case 2 (Invalid Payment)**
   - [ ] Send invalid payment (same buyer/seller)
   - [ ] Check response: 400 Bad Request
   - [ ] Check logs: Payment rejected message
   - [ ] Verify: Payment NOT in database

3. **Test Case 3 (High Amount)**
   - [ ] Send high amount payment
   - [ ] Check response: 201 Created
   - [ ] Check logs: Warning message
   - [ ] Verify: Payment created with warning

---

## 🔧 Troubleshooting

### Issue: "Connection refused" when calling Payment Service

**Solution:**
```
Wait 30 seconds for Payment Service to start
OR
Check if port 8091 is available
OR
Check application logs for startup errors
```

### Issue: Rule Engine not responding

**Check:**
1. Rule Engine is running on port 8055
2. Network connectivity between services
3. Rule Engine logs for errors

### Issue: Payment created but Rule Engine didn't validate

**Check:**
1. Rule validation is enabled: `app.rule-engine.enabled=true`
2. Rule Engine URL is correct: `http://localhost:8055/rules/evaluate`
3. Logs show Rule Engine Request/Response

---

## 📝 Notes

- **Payment Service:** http://localhost:8091 (Port 8091)
- **Rule Engine:** http://localhost:8055 (Port 8055)
- **Integration:** Automatic - happens on every payment initiation
- **Retry Logic:** 3 attempts with exponential backoff
- **Configuration:** Externalized in application.properties

---

## ✅ Success Indicators

✅ Payment Service starts without errors
✅ Rule Engine URL is reachable
✅ Postman requests get responses
✅ Logs show Rule Engine integration
✅ Database has payment records
✅ Invalid payments are rejected

**Ready to test!** 🚀
