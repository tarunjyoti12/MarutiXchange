# 📝 POSTMAN - WHAT TO TYPE IN BODY

## **Port:** `8069`

---

## **🎯 MAIN ENDPOINT**

```
Method: POST
URL: http://localhost:8069/rules/evaluate
```

---

## **📋 COPY & PASTE - REQUEST BODIES**

### **EXAMPLE 1: Valid Car Price ✅**

Go to Postman:
1. Select **Body** tab
2. Select **raw** option
3. Select **JSON** from dropdown
4. Copy and paste this:

```json
{
  "type": "car",
  "price": 700000,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false
}
```

**Expected Response:**
```json
{
  "type": "car",
  "price": 700000,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false,
  "risky": false,
  "approved": true,
  "message": "Valid Car Listing"
}
```

---

### **EXAMPLE 2: Invalid Car Price (Zero) ❌**

```json
{
  "type": "car",
  "price": 0,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false
}
```

**Expected Response:**
```json
{
  "type": "car",
  "price": 0,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false,
  "risky": false,
  "approved": false,
  "message": "Invalid car price"
}
```

---

### **EXAMPLE 3: Fraud Detection - Safe User ✅**

```json
{
  "type": "fraud",
  "price": 50000,
  "userId": 5,
  "sellerId": 6,
  "blacklisted": false
}
```

**Expected Response:**
```json
{
  "type": "fraud",
  "price": 50000,
  "userId": 5,
  "sellerId": 6,
  "blacklisted": false,
  "risky": false,
  "approved": true,
  "message": "Safe User"
}
```

---

### **EXAMPLE 4: Fraud Detection - Blacklisted User ❌**

```json
{
  "type": "fraud",
  "price": 50000,
  "userId": 5,
  "sellerId": 6,
  "blacklisted": true
}
```

**Expected Response:**
```json
{
  "type": "fraud",
  "price": 50000,
  "userId": 5,
  "sellerId": 6,
  "blacklisted": true,
  "risky": false,
  "approved": false,
  "message": "User is blacklisted"
}
```

---

### **EXAMPLE 5: Valid Order ✅**

```json
{
  "type": "order",
  "price": 15000,
  "userId": 10,
  "sellerId": 11
}
```

**Expected Response:**
```json
{
  "type": "order",
  "price": 15000,
  "userId": 10,
  "sellerId": 11,
  "blacklisted": false,
  "risky": false,
  "approved": true,
  "message": "Valid Order"
}
```

---

### **EXAMPLE 6: Invalid Order (Negative Price) ❌**

```json
{
  "type": "order",
  "price": -500,
  "userId": 10,
  "sellerId": 11
}
```

**Expected Response:**
```json
{
  "type": "order",
  "price": -500,
  "userId": 10,
  "sellerId": 11,
  "blacklisted": false,
  "risky": false,
  "approved": false,
  "message": "Invalid order amount"
}
```

---

### **EXAMPLE 7: Valid User ✅**

```json
{
  "type": "user",
  "price": 0,
  "userId": 123,
  "sellerId": 50
}
```

**Expected Response:**
```json
{
  "type": "user",
  "price": 0,
  "userId": 123,
  "sellerId": 50,
  "blacklisted": false,
  "risky": false,
  "approved": true,
  "message": "Valid User"
}
```

---

### **EXAMPLE 8: Invalid User (No User ID) ❌**

```json
{
  "type": "user",
  "price": 0,
  "userId": null,
  "sellerId": 50
}
```

**Expected Response:**
```json
{
  "type": "user",
  "price": 0,
  "userId": null,
  "sellerId": 50,
  "blacklisted": false,
  "risky": false,
  "approved": false,
  "message": "Invalid User"
}
```

---

### **EXAMPLE 9: Buyer = Seller (BLOCKED) ❌ - CRITICAL RULE**

```json
{
  "type": "car",
  "price": 500000,
  "userId": 99,
  "sellerId": 99,
  "blacklisted": false
}
```

**Expected Response:**
```json
{
  "type": "car",
  "price": 500000,
  "userId": 99,
  "sellerId": 99,
  "blacklisted": false,
  "risky": false,
  "approved": false,
  "message": "Buyer and seller cannot be same"
}
```

---

### **EXAMPLE 10: Refund Check**

```json
{
  "type": "refund",
  "price": 25000,
  "userId": 12,
  "sellerId": 13
}
```

---

### **EXAMPLE 11: Escrow Rules**

```json
{
  "type": "escrow",
  "price": 300000,
  "userId": 30,
  "sellerId": 31
}
```

---

### **EXAMPLE 12: Bidding Rules**

```json
{
  "type": "bidding",
  "price": 100000,
  "userId": 20,
  "sellerId": 21
}
```

---

### **EXAMPLE 13: Pricing Rules**

```json
{
  "type": "pricing",
  "price": 50000,
  "userId": 40,
  "sellerId": 41
}
```

---

## **🔑 FIELD DEFINITIONS**

| Field | Type | Required | Example | Description |
|-------|------|----------|---------|-------------|
| `type` | String | ✅ YES | "car" | Rule type: car, fraud, order, user, refund, escrow, bidding, pricing |
| `price` | Number | ❌ NO | 700000 | Price/Amount (can be 0 or negative) |
| `userId` | Number | ❌ NO | 1 | Buyer/User ID (can be null) |
| `sellerId` | Number | ❌ NO | 2 | Seller ID (can be null) |
| `blacklisted` | Boolean | ❌ NO | false | Is user blacklisted? |
| `risky` | Boolean | ❌ NO | false | Is transaction risky? |
| `approved` | Boolean | ❌ NO | false | Approval status (OUTPUT only) |
| `message` | String | ❌ NO | "" | Result message (OUTPUT only) |

---

## **⚡ QUICK START IN POSTMAN**

### Step 1: Open Postman
Click `New` → Select `HTTP`

### Step 2: Set Method & URL
- **Method:** `POST` (dropdown on left)
- **URL:** `http://localhost:8069/rules/evaluate`

### Step 3: Set Headers
Click **Headers** tab → Add:
- **Key:** `Content-Type`
- **Value:** `application/json`

### Step 4: Add Body
1. Click **Body** tab
2. Select **raw** radio button
3. Select **JSON** from dropdown (right side)
4. Paste one of the examples above
5. Click **Send**

### Step 5: View Response
Response appears in lower panel with:
- Status code (200 = success)
- JSON response
- Response time

---

## **✅ RESPONSE STRUCTURE**

Every response contains:
```json
{
  "type": "...",           // Your input type
  "price": 0,              // Your input price
  "userId": 1,             // Your input userId
  "sellerId": 2,           // Your input sellerId
  "blacklisted": false,    // Your input blacklisted
  "risky": false,          // Your input risky
  "approved": true,        // RESULT: true/false
  "message": "..."         // RESULT: Reason
}
```

---

## **🎯 KEY POINTS**

1. **Only `type` is required** - everything else is optional
2. **`approved` shows the result** - true = passed, false = failed
3. **`message` explains why** - read this to understand the result
4. **Status 200 = success** - means API processed your request
5. **All 8 types work** - car, fraud, order, user, refund, escrow, bidding, pricing

---

## **🚀 TEST NOW**

1. Copy Example 1 (Valid Car)
2. Open Postman
3. POST to `http://localhost:8069/rules/evaluate`
4. Paste body
5. Click Send
6. You should see `"approved": true`

**That's it! You're testing the microservice! 🎉**
