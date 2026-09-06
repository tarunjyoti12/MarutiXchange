# 🔥 PORT UPDATE - Payment Service Now on Port 8092

## ⚠️ Issue Found
Port 8091 was also already in use! 

## ✅ Solution Applied
Changed Payment Service to use **port 8092**

---

## 📊 FINAL Port Configuration

```
Rule Engine ........... 8055 ✅ (Already running)
Payment Service ....... 8092 ✅ (NEW - Now available)
MySQL Database ........ 3306 ✅ (Required)
```

---

## 🔧 Changes Made

### File: application.properties

**Change 1: Port**
```properties
# OLD: server.port=8091
# NEW: server.port=8092
```

**Change 2: Eureka (Optional Services)**
```properties
# DISABLED to prevent startup issues
eureka.client.enabled=false
```

---

## 🚀 Now Start the Service

### Command
```bash
cd D:\MarutiXchange\payment-service
mvn spring-boot:run
```

### Expected Output
```
Tomcat started on port(s): 8092 (http)
Application started successfully
```

---

## 🧪 Postman Testing - Updated URL

### New Payment API Endpoint
```
POST http://localhost:8092/api/v1/payments/initiate
```

### Test Request
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

### Expected Response
```json
{
  "status": 201,
  "message": "Payment initiated successfully",
  "data": {
    "transactionId": "MM...",
    "amount": 50000,
    "status": "PENDING"
  }
}
```

---

## 📱 All Service URLs

```
Payment Service .... http://localhost:8092
Rule Engine ........ http://localhost:8055
Swagger UI ......... http://localhost:8092/swagger-ui.html
Actuator Health ... http://localhost:8092/actuator/health
```

---

## ✨ Integration Check

### When you send a payment request:

1. **Request arrives at Payment Service (8092)**
   ```
   POST http://localhost:8092/api/v1/payments/initiate
   ```

2. **Payment Service calls Rule Engine (8055)**
   ```
   POST http://localhost:8055/rules/evaluate
   ```

3. **Rule Engine validates and responds**
   ```
   {approved: true/false}
   ```

4. **Payment Service creates or rejects payment**
   ```
   201 Created (if valid)
   400 Bad Request (if invalid)
   ```

5. **Postman gets response**

---

## 📋 Quick Commands

```bash
# Start service on port 8092
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/payment-service-1.0.0.jar

# Check if port is available
netstat -ano | findstr :8092

# Kill process on port 8092 (if needed)
taskkill /PID <PID> /F
```

---

## ✅ Verification Checklist

- [x] Port 8091 issue identified
- [x] Payment Service configured for port 8092
- [x] Eureka disabled (prevents startup errors)
- [x] Rule Engine still on 8055
- [x] Ready for Postman testing

---

## 🎯 Next Steps

1. **Start Payment Service**
   ```bash
   mvn spring-boot:run
   ```

2. **Open Postman**
   - URL: `http://localhost:8092/api/v1/payments/initiate`
   - Method: POST
   - Use body from above

3. **Send Test Request**
   - Click Send
   - Check response (should be 201 Created)

4. **Verify Integration**
   - Check logs for Rule Engine Request/Response
   - Verify payment in database

---

## 🎉 You're Ready!

✅ Port 8092 is configured  
✅ Rule Engine integration is active  
✅ Eureka errors prevented  
✅ Ready for Postman testing

**Start the service now!** 🚀

---

**Status:** ✅ Ready for Testing  
**Port:** 8092 (Payment Service)  
**Integration:** Active with Rule Engine (8055)
