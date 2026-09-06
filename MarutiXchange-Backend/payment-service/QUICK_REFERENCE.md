# 🎯 Quick Reference Card - Rule Engine Integration Testing

## 🔴 CURRENT SERVICE PORTS

```
Rule Engine ........... 8055 ✅ RUNNING (already in use)
Payment Service ....... 8091 ✅ READY (newly configured)
MySQL Database ........ 3306 ✅ REQUIRED
```

---

## 📋 POSTMAN TEST - Copy & Paste Ready

### Test 1: Valid Payment ✅

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

**Expected:** ✅ 201 Created - Payment Approved by Rule Engine

---

### Test 2: Invalid Payment ❌

**Same request, but change:**
```json
"buyerId": 100,
"sellerId": 100    ← SAME as buyerId
```

**Expected:** ❌ 400 Bad Request - Payment Rejected by Rule Engine

---

### Test 3: High Amount ⚠️

**Same request, but change:**
```json
"amount": 5000000   ← VERY HIGH
```

**Expected:** ✅ 201 Created - Payment Approved with Warnings in Logs

---

## 🔍 WHAT'S HAPPENING BEHIND THE SCENES

```
Postman sends request to Payment Service (8091)
        ↓
Payment Service receives request
        ↓
PaymentServiceImpl calls RuleEngineClient
        ↓
RuleEngineClient makes HTTP POST to Rule Engine (8055)
        ↓
Rule Engine validates:
  ✅ Is buyerId != sellerId?
  ✅ Is amount within limits?
  ✅ Other business rules...
        ↓
Rule Engine responds: approved=true or false
        ↓
IF approved: Payment saved to MySQL database ✅
IF rejected: Exception thrown to Postman ❌
        ↓
Response sent back to Postman
```

---

## 📊 LOGS TO LOOK FOR

### When Rule Engine is Called (Success)
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=100, sellerId=200, ...}
[INFO] Rule Engine Response: {approved=true, message=Payment approved}
[INFO] Payment initiated successfully: MM...
```

### When Rule Engine Rejects (Failure)
```
[INFO] Rule Engine Request: {...}
[INFO] Rule Engine Response: {approved=false}
[ERROR] Payment rejected by rule engine: [Buyer and seller cannot be the same person]
```

### When Rule Engine Has Warnings
```
[INFO] Rule Engine Response: {..., warnings: {amount: "Unusually high"}}
[WARN] Payment warnings: [Unusually high amount]
[INFO] Payment initiated successfully: MM...
```

---

## 🚀 START PAYMENT SERVICE

```bash
cd D:\MarutiXchange\payment-service
mvn spring-boot:run
```

**Wait for:**
```
Tomcat started on port(s): 8091 (http)
```

---

## ✨ INTEGRATION SUMMARY

| Component | Status |
|-----------|--------|
| Code | ✅ Integrated |
| Tests | ✅ All Passing (10/10) |
| Configuration | ✅ Updated (Port 8091) |
| Rule Engine | ✅ Running (Port 8055) |
| Documentation | ✅ Complete |
| Ready to Test | ✅ YES |

---

## 🎯 SUCCESS INDICATORS

When testing, you should see:

✅ Postman response: 201 Created (for valid payments)
✅ Logs show Rule Engine Request/Response
✅ Database has payment records
✅ Invalid payments get 400 error
✅ Warnings are logged but payments still created

---

## 📞 TROUBLESHOOTING

**Q: Port 8091 not opening?**
- A: Wait 30 seconds for Payment Service to fully start
- Check: Logs for startup errors
- Kill: Any process using port 8091

**Q: Rule Engine not responding?**
- A: Verify it's running on port 8055
- Check: Network connectivity
- Restart: Rule Engine service

**Q: No logs showing Rule Engine integration?**
- A: Check: `app.rule-engine.enabled=true` in properties
- Verify: Rule Engine URL is correct
- Restart: Payment Service

---

## 📍 ENDPOINTS

### Payment Service
```
Base URL: http://localhost:8091
Swagger: http://localhost:8091/swagger-ui.html
API: POST http://localhost:8091/api/v1/payments/initiate
```

### Rule Engine
```
Base URL: http://localhost:8055
Validate: POST http://localhost:8055/rules/evaluate
```

---

## 💾 DATABASE QUERY

Check saved payments in MySQL:

```sql
SELECT * FROM payments ORDER BY created_at DESC LIMIT 10;
```

---

## ⚡ QUICK COMMANDS

```bash
# Start Payment Service
mvn spring-boot:run

# Build (if needed)
mvn clean package

# Run JAR file
java -jar target/payment-service-1.0.0.jar

# Check if port is in use
netstat -ano | findstr :8091

# Kill process on port 8091 (if needed)
taskkill /PID <PID> /F
```

---

## 📋 CONFIGURATION

**File:** `application.properties`

**Key Settings:**
```properties
server.port=8091                                    ← Payment Service Port
app.rule-engine.url=http://localhost:8055/rules    ← Rule Engine
app.rule-engine.enabled=true                       ← Integration Active
app.rule-engine.retry-attempts=3                   ← Retry on Failure
```

---

## 🎉 YOU'RE ALL SET!

1. ✅ Payment Service configured for port 8091
2. ✅ Rule Engine integration is active
3. ✅ Ready to test in Postman
4. ✅ Documentation provided

**Start testing now!** 🚀

---

**Last Updated:** April 9, 2026  
**Status:** ✅ Ready for Testing
