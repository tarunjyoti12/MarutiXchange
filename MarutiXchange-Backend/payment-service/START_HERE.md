# 🎯 QUICK START - Payment Service on Port 8092

## ✅ Configuration Complete

```
Rule Engine ........... 8055 ✅
Payment Service ....... 8092 ✅  
MySQL Database ........ 3306 ✅
```

---

## 🚀 START SERVICE

```bash
cd D:\MarutiXchange\payment-service
mvn spring-boot:run
```

---

## 🧪 TEST IN POSTMAN

### URL
```
POST http://localhost:8092/api/v1/payments/initiate
```

### Body
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

### Response (Expected)
```
Status: 201 Created
Message: Payment initiated successfully
```

---

## ✨ Integration Works Like This

```
Postman sends request to 8092
  ↓
Payment Service receives it
  ↓
Calls Rule Engine on 8055
  ↓
Rule Engine validates payment
  ↓
Returns approved/rejected
  ↓
Payment Service creates or rejects
  ↓
Postman gets response
```

---

## 📋 Test Cases

| Case | buyerId | sellerId | Expected |
|------|---------|----------|----------|
| Valid | 100 | 200 | ✅ 201 Created |
| Invalid | 100 | 100 | ❌ 400 Error |
| High Amount | 100 | 200 | ✅ 201 (warning) |

---

## 🔍 Check Logs

Look for:
```
[INFO] Rule Engine Request: {...}
[INFO] Rule Engine Response: {approved=true}
[INFO] Payment initiated successfully: MM...
```

---

## ✅ Success Checklist

- [ ] Service started on 8092
- [ ] Rule Engine on 8055
- [ ] MySQL running
- [ ] Postman test sent
- [ ] Got 201 response
- [ ] Saw Rule Engine logs
- [ ] Database has records

---

**Ready!** 🚀
