# 📝 ANSWER: WHAT TO TYPE IN POSTMAN BODY

## **Quick Answer:**

Copy and paste ONE of these into Postman body (select "raw" JSON):

---

## **🟢 SIMPLEST TEST - Start With This:**

```json
{
  "type": "car",
  "price": 700000,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false
}
```

**You should get back:**
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

## **POSTMAN SETUP (Only 4 Steps)**

### Step 1: Method & URL
```
POST  http://localhost:8069/rules/evaluate
```

### Step 2: Headers Tab
```
Content-Type: application/json
```

### Step 3: Body Tab
- Click **raw** radio button
- Select **JSON** from dropdown
- Paste the JSON above

### Step 4: Click Send ▶️

---

## **THAT'S IT!** ✅

You now know:
1. ✅ What URL to use
2. ✅ What headers to set
3. ✅ What body to send
4. ✅ How to test

**Go test it now in Postman!**

---

## **13 MORE EXAMPLES** (Optional)

See `SIMPLE_BODY_EXAMPLES.md` for:
- Valid car
- Invalid car
- Safe user
- Blacklisted user
- Valid order
- Invalid order
- Valid user
- Invalid user
- Buyer = Seller (blocked)
- Refund
- Escrow
- Bidding
- Pricing

---

## **NEED HELP?**

**Scenario:** Type = "car", Price = 700000
**Body:**
```json
{"type": "car", "price": 700000, "userId": 1, "sellerId": 2, "blacklisted": false}
```

**That's what to type!**
