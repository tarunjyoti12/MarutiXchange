# ✅ Payment Service Configuration Updated

## 🔧 Changes Made

### Port Configuration Updated
```properties
# OLD
server.port=8069

# NEW
server.port=8091
```

**Reason:** Rule Engine is already running on port 8055 and was previously using port 8069. Payment Service now uses port 8091 to avoid port conflicts.

---

## 📍 Service Ports Summary

| Service | Port | Status |
|---------|------|--------|
| **Rule Engine** | 8055 | ✅ Running (Already in use) |
| **Payment Service** | 8091 | ✅ Configured & Ready |
| **MySQL Database** | 3306 | ✅ Required |
| **Eureka Discovery** | 8761 | ✅ Optional |

---

## 🚀 How to Start Services

### Step 1: Start Rule Engine (Already Running ✅)
```
Status: ✅ Already running on http://localhost:8055
```

### Step 2: Start Payment Service (New Configuration)
```bash
cd D:\MarutiXchange\payment-service
mvn spring-boot:run
```

**Expected Output:**
```
Tomcat started on port(s): 8091 (http) with context path ''
Application started successfully
```

### Step 3: Verify Services are Connected

Check logs for:
```
[INFO] Rule Engine Request: {type=payment, ...}
[INFO] Rule Engine Response: {approved=true, ...}
```

---

## 🧪 Testing in Postman

### New API Endpoint
```
POST http://localhost:8091/api/v1/payments/initiate
```

### Quick Test
Copy this JSON and send to the endpoint above:

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
    "buyerId": 100,
    "sellerId": 200
  }
}
```

---

## 🔗 Integration Verification

### Rule Engine Integration is ACTIVE
```
Payment Service (Port 8091)
    ↓
    → Rule Engine (Port 8055)
    → Validates payment rules
    → Returns approval/rejection
    ↓
Payment is created (if approved)
Payment is rejected (if not approved)
```

### Logs to Check
```
[INFO] Rule Engine Request: {...}
[INFO] Rule Engine Response: {approved=true/false}
```

---

## ✨ Configuration Details

### Updated application.properties
```properties
server.port=8091                                           # NEW PORT
app.rule-engine.url=http://localhost:8055/rules/evaluate  # RULE ENGINE
app.rule-engine.enabled=true                              # INTEGRATION ACTIVE
app.rule-engine.retry-attempts=3                          # RETRY ON FAILURE
```

---

## 📊 Current Setup

```
Your Machine (Windows)
├── MySQL (3306) ...................... ✅ Must be running
├── Rule Engine (8055) ................ ✅ Already running
└── Payment Service (8091) ............ ✅ Ready to start
    └── Integrated with Rule Engine ... ✅ Automatic validation
        └── Postman Testing ........... ✅ Ready to test
```

---

## 🎯 Next Steps

1. **Wait for Payment Service to Start**
   - Watch the console logs
   - Look for: "Tomcat started on port(s): 8091"

2. **Test in Postman**
   - See: POSTMAN_TESTING_GUIDE.md
   - Send the sample JSON above
   - Check response

3. **Monitor Logs**
   - Watch for Rule Engine integration logs
   - Verify payment creation in database
   - Check MySQL for saved records

4. **Try Different Scenarios**
   - Valid payment (different buyer/seller)
   - Invalid payment (same buyer/seller)
   - High amount payment (may have warnings)

---

## 📝 File Updated

**File:** `src/main/resources/application.properties`

**Change:**
```
Line 2: server.port=8091  (changed from 8069)
```

**Reason:** Rule Engine running on 8055 & 8069 was in conflict

---

## ✅ Verification Checklist

- [x] Configuration file updated
- [x] Port changed from 8069 to 8091
- [x] Rule Engine integration active
- [x] Payment Service ready to start
- [x] Postman testing guide created

---

## 🚀 Ready to Test!

The Payment Service is now configured to:
1. ✅ Run on port 8091
2. ✅ Communicate with Rule Engine on port 8055
3. ✅ Validate payments automatically
4. ✅ Save valid payments to database

**Start the service and begin testing!** 🎉

---

**Configuration Date:** April 9, 2026  
**Status:** ✅ Ready for Testing  
**Rule Engine Integration:** ✅ Active & Configured
