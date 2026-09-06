# Rule Engine Integration - Quick Reference Guide

## 📁 Key Files & Locations

### Client Implementation
```
src/main/java/com/marutixchange/payment_service/client/RuleEngineClient.java
```
**Key Methods:**
- `evaluate(Object request)` - Main entry point for rule validation
- `callRuleEngineWithRetry()` - Handles retry logic with exponential backoff
- `buildRuleRequest()` - Constructs Rule Engine request from payment data
- `parseRuleEngineResponse()` - Parses Rule Engine response and extracts violations

### Data Transfer Objects
```
src/main/java/com/marutixchange/payment_service/dto/RuleEngineRequest.java
src/main/java/com/marutixchange/payment_service/dto/RuleEngineResponse.java
```

### Rule Result Objects
```
src/main/java/com/marutixchange/payment_service/rules/RuleResult.java
src/main/java/com/marutixchange/payment_service/rules/RuleViolation.java
```

### Service Integration
```
src/main/java/com/marutixchange/payment_service/service/PaymentServiceImpl.java
```
**Integration Point:** Line ~60 in `initiatePayment()` method
```java
// 🔥 STEP 1: CALL RULE ENGINE
RuleResult ruleResult = ruleEngineClient.evaluate(request);

// 🔥 STEP 2: CHECK VALIDITY
if (!ruleResult.isValid()) {
    log.error("Payment rejected by rule engine: {}", ruleResult.getErrorMessages());
    throw new PaymentValidationException(
        String.join(", ", ruleResult.getErrorMessages())
    );
}
```

### Configuration Files
```
src/main/resources/application.properties          - Production config
src/main/resources/application-test.properties     - Test config
```

### Tests
```
src/test/java/com/marutixchange/payment_service/client/RuleEngineClientTest.java
```
**Test Cases:**
1. testEvaluatePaymentApproved
2. testEvaluatePaymentWithViolations
3. testEvaluatePaymentWithRetry
4. testEvaluatePaymentWithRuleEngineDisabled
5. testEvaluatePaymentWithNullResponse
6. testEvaluatePaymentWithWarnings

### Documentation
```
RULE_ENGINE_INTEGRATION.md                    - Detailed guide
RULE_ENGINE_CONFIG_EXAMPLES.md                - Configuration examples
RULE_ENGINE_INTEGRATION_SUMMARY.md            - Executive summary
RULE_ENGINE_INTEGRATION_CHECKLIST.md          - Completion checklist
```

## 🔧 Configuration Properties

```properties
# Rule Engine Service Configuration
app.rule-engine.url=http://localhost:8055/rules/evaluate
app.rule-engine.enabled=true
app.rule-engine.retry-attempts=3
app.rule-engine.timeout-ms=5000
```

### Property Details

| Property | Default | Description |
|----------|---------|-------------|
| `app.rule-engine.url` | N/A | Rule Engine microservice endpoint |
| `app.rule-engine.enabled` | true | Enable/disable rule validation |
| `app.rule-engine.retry-attempts` | 3 | Number of retry attempts on failure |
| `app.rule-engine.timeout-ms` | 5000 | HTTP request timeout in milliseconds |

## 🎯 Integration Points

### 1. Payment Initiation
**File:** `PaymentServiceImpl.java`
**Method:** `initiatePayment(PaymentRequest request)`
**Action:** Validates payment before creation

```java
RuleResult ruleResult = ruleEngineClient.evaluate(request);
if (!ruleResult.isValid()) {
    throw new PaymentValidationException(...);
}
```

### 2. Rule Engine Request
**Sent to:** `POST http://localhost:8055/rules/evaluate`
**Format:** JSON with fields: type, price, userId, sellerId, blacklisted, timestamp, carListingId, auctionId, paymentMethod

### 3. Rule Engine Response
**Expected:** JSON with fields: approved, message, violations, warnings, suggestions

## 🔌 How to Connect

### Step 1: Start Rule Engine
```bash
# Rule Engine should be running at http://localhost:8055
docker run -p 8055:8055 rule-engine:latest
```

### Step 2: Configure Payment Service
```properties
# In application.properties
app.rule-engine.url=http://localhost:8055/rules/evaluate
app.rule-engine.enabled=true
```

### Step 3: Start Payment Service
```bash
mvn spring-boot:run
# or
java -jar target/payment-service-1.0.0.jar
```

### Step 4: Test Integration
```bash
# Run unit tests
mvn test -Dtest=RuleEngineClientTest

# Run integration tests
mvn test -Dtest=PaymentServiceImplTest

# Run all tests
mvn test
```

## 🔄 Request/Response Examples

### Sample Rule Engine Request
```json
{
  "type": "payment",
  "price": 50000.0,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false,
  "timestamp": 1775716314351,
  "carListingId": 1,
  "auctionId": 1,
  "paymentMethod": "UPI"
}
```

### Sample Approved Response
```json
{
  "approved": true,
  "message": "Payment approved",
  "violations": null,
  "warnings": null,
  "suggestions": null
}
```

### Sample Rejected Response
```json
{
  "approved": false,
  "message": "Payment validation failed",
  "violations": {
    "amount_check": {
      "code": "MAX_AMOUNT_EXCEEDED",
      "message": "Amount exceeds maximum limit",
      "severity": "CRITICAL"
    }
  },
  "warnings": null,
  "suggestions": null
}
```

## 📊 Logging Output

### When Payment is Approved
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=1, ...}
[INFO] Rule Engine Response: {approved=true, message=Payment approved}
[INFO] Payment initiated successfully: MM1775716480294539FAA
```

### When Payment is Rejected
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=1, ...}
[INFO] Rule Engine Response: {approved=false, message=Payment validation failed, ...}
[ERROR] Payment rejected by rule engine: [Max amount exceeded]
```

### When Rule Engine is Unavailable
```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=1, ...}
[WARN] Rule Engine call failed (Attempt 1/3). Retrying...
[WARN] Rule Engine call failed (Attempt 2/3). Retrying...
[ERROR] Rule Engine unavailable after 3 attempts: Connection refused
```

## 🛠️ Debugging

### Enable Debug Logging
Add to `application.properties`:
```properties
logging.level.com.marutixchange.payment_service.client=DEBUG
logging.level.org.springframework.web.client=DEBUG
```

### Check Configuration
```bash
# View current configuration
mvn spring-boot:run --debug

# Check if properties are loaded correctly
grep "app.rule-engine" logs/payment-service.log
```

### Test Rule Engine Connection
```bash
# Using curl
curl -X POST http://localhost:8055/rules/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "type": "payment",
    "price": 50000.0,
    "userId": 1,
    "sellerId": 2,
    "blacklisted": false
  }'
```

## 📋 Verification Checklist

Before deploying to production:

- [ ] Rule Engine microservice is running
- [ ] Rule Engine is accessible at configured URL
- [ ] Database migrations are complete
- [ ] All tests pass: `mvn test`
- [ ] Configuration is set correctly
- [ ] Logs show successful rule validation
- [ ] Sample payment passes rule validation
- [ ] Sample payment fails when expected

## 🚀 Deployment

### Development
```bash
mvn clean package
java -jar target/payment-service-1.0.0.jar \
  --app.rule-engine.url=http://localhost:8055/rules/evaluate
```

### Staging
```bash
mvn clean package
java -jar target/payment-service-1.0.0.jar \
  --app.rule-engine.url=http://staging-rule-engine:8055/rules/evaluate
```

### Production
```bash
mvn clean package -Denv=prod
java -jar target/payment-service-1.0.0.jar \
  --app.rule-engine.url=http://prod-rule-engine:8055/rules/evaluate \
  --app.rule-engine.enabled=true \
  --app.rule-engine.retry-attempts=5
```

## 🎓 Learning Resources

### Project Structure
- **client/**: HTTP client for external services
- **config/**: Spring configuration beans
- **controller/**: REST API endpoints
- **dto/**: Data transfer objects
- **entity/**: Database entities
- **repository/**: Database access
- **rules/**: Rule validation models
- **service/**: Business logic
- **security/**: Authentication & authorization

### Code Samples

#### Using RuleEngineClient
```java
@Autowired
private RuleEngineClient ruleEngineClient;

public void validatePayment(PaymentRequest request) {
    RuleResult result = ruleEngineClient.evaluate(request);
    
    if (result.isValid()) {
        // Payment is valid
        processPayment(request);
    } else {
        // Payment is invalid
        throw new PaymentValidationException(
            String.join(", ", result.getErrorMessages())
        );
    }
}
```

#### Checking Warnings
```java
RuleResult result = ruleEngineClient.evaluate(request);

if (result.hasWarnings()) {
    log.warn("Payment warnings: {}", result.getWarnings());
    // Still process payment, just log warning
}
```

## 📞 Support

For issues or questions:
1. Check **RULE_ENGINE_INTEGRATION.md** for detailed documentation
2. Review test cases in **RuleEngineClientTest.java** for usage examples
3. Check logs for error messages
4. Verify Rule Engine is accessible and responding

---

**Version:** 1.0.0  
**Last Updated:** April 9, 2026  
**Status:** ✅ Production Ready
