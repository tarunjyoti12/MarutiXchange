# 🎯 SUPER SIMPLE - POSTMAN BODY EXAMPLES

## **Just Copy & Paste These!**

---

## **#1 - VALID CAR (Approved ✅)**

```json
{
  "type": "car",
  "price": 700000,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false
}
```

**Response:** `"approved": true` ✅

---

## **#2 - INVALID CAR (Rejected ❌)**

```json
{
  "type": "car",
  "price": 0,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false
}
```

**Response:** `"approved": false` ❌

---

## **#3 - SAFE USER (Approved ✅)**

```json
{
  "type": "fraud",
  "price": 50000,
  "userId": 5,
  "sellerId": 6,
  "blacklisted": false
}
```

**Response:** `"approved": true` ✅

---

## **#4 - BLACKLISTED USER (Rejected ❌)**

```json
{
  "type": "fraud",
  "price": 50000,
  "userId": 5,
  "sellerId": 6,
  "blacklisted": true
}
```

**Response:** `"approved": false` ❌

---

## **#5 - VALID ORDER (Approved ✅)**

```json
{
  "type": "order",
  "price": 15000,
  "userId": 10,
  "sellerId": 11
}
```

**Response:** `"approved": true` ✅

---

## **#6 - INVALID ORDER (Rejected ❌)**

```json
{
  "type": "order",
  "price": -500,
  "userId": 10,
  "sellerId": 11
}
```

**Response:** `"approved": false` ❌

---

## **#7 - VALID USER (Approved ✅)**

```json
{
  "type": "user",
  "price": 0,
  "userId": 123,
  "sellerId": 50
}
```

**Response:** `"approved": true` ✅

---

## **#8 - INVALID USER (Rejected ❌)**

```json
{
  "type": "user",
  "price": 0,
  "userId": null,
  "sellerId": 50
}
```

**Response:** `"approved": false` ❌

---

## **#9 - BUYER = SELLER (Rejected ❌) - CRITICAL**

```json
{
  "type": "car",
  "price": 500000,
  "userId": 99,
  "sellerId": 99,
  "blacklisted": false
}
```

**Response:** `"approved": false`, Message: "Buyer and seller cannot be same" ❌

---

## **#10 - REFUND**

```json
{
  "type": "refund",
  "price": 25000,
  "userId": 12,
  "sellerId": 13
}
```

---

## **#11 - ESCROW**

```json
{
  "type": "escrow",
  "price": 300000,
  "userId": 30,
  "sellerId": 31
}
```

---

## **#12 - BIDDING**

```json
{
  "type": "bidding",
  "price": 100000,
  "userId": 20,
  "sellerId": 21
}
```

---

## **#13 - PRICING**

```json
{
  "type": "pricing",
  "price": 50000,
  "userId": 40,
  "sellerId": 41
}
```

---

## **IN POSTMAN - STEPS**

### 1️⃣ Create New Request
- Click **+** button
- Select **POST**

### 2️⃣ Enter URL
```
http://localhost:8069/rules/evaluate
```

### 3️⃣ Set Headers
| Key | Value |
|-----|-------|
| Content-Type | application/json |

### 4️⃣ Go to Body Tab
- Select **raw**
- Select **JSON** from dropdown

### 5️⃣ Paste One Example Above

### 6️⃣ Click Send

### 7️⃣ View Response
Look for: `"approved": true` or `"approved": false`

---

## **THAT'S IT! 🎉**

Now you can test all 13 examples!
