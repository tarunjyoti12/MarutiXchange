# Rule Engine Integration Guide

## Overview

The Payment Service is integrated with the Rule Engine microservice to validate payments before processing. This document describes the integration architecture, configuration, and usage.

## Architecture

```
Payment Service                  Rule Engine Service
    |                                  |
    |-- RuleEngineClient              |
    |     (HTTP Client)                |
    |                                  |
    +----POST /rules/evaluate------>  |
         (RuleEngineRequest)           |
                                       |
                               <--RuleEngineResponse--+
```

## Configuration

### Application Properties

Add the following properties to `application.properties`:

```properties
# Rule Engine Integration
app.rule-engine.url=http://localhost:8055/rules/evaluate
app.rule-engine.timeout-ms=5000
app.rule-engine.retry-attempts=3
app.rule-engine.enabled=true
```

**Property Descriptions:**
- `app.rule-engine.url`: Base URL of the Rule Engine microservice
- `app.rule-engine.timeout-ms`: HTTP request timeout in milliseconds
- `app.rule-engine.retry-attempts`: Number of retry attempts for failed requests
- `app.rule-engine.enabled`: Enable/disable rule engine validation (defaults to true)

## Request Format

When a payment is initiated, the Payment Service sends the following request to the Rule Engine:

```json
{
  "type": "payment",
  "price": 50000.0,
  "userId": 1,
  "sellerId": 2,
  "blacklisted": false,
  "timestamp": 1712671200000,
  "carListingId": "CAR123",
  "auctionId": "AUCTION456",
  "paymentMethod": "UPI"
}
```

**Field Descriptions:**
- `type`: Always "payment" for payment validation
- `price`: Payment amount
- `userId`: Buyer ID
- `sellerId`: Seller ID
- `blacklisted`: Flag indicating if user is blacklisted
- `timestamp`: Current system timestamp
- `carListingId`: (Optional) Associated car listing ID
- `auctionId`: (Optional) Associated auction ID
- `paymentMethod`: Payment method used (UPI, BANK_TRANSFER, etc.)

## Response Format

The Rule Engine returns a response in the following format:

```json
{
  "approved": true,
  "message": "Payment approved",
  "violations": {
    "amount_check": {
      "code": "MAX_AMOUNT_EXCEEDED",
      "message": "Amount exceeds maximum limit",
      "severity": "CRITICAL"
    }
  },
  "warnings": {
    "high_amount": "Amount is unusually high"
  }
}
```

**Field Descriptions:**
- `approved`: Boolean indicating if payment is approved
- `message`: Human-readable approval/rejection message
- `violations`: Map of rule violations (code, message, severity)
- `warnings`: Map of non-blocking warnings

## Severity Levels

Violations can have the following severity levels:

- **CRITICAL**: Payment will be rejected
- **ERROR**: Payment will be rejected
- **WARNING**: Payment may proceed with caution (informational only)

## Integration Flow

### 1. Payment Initiation

When a payment is initiated via the API:

```
PaymentController.initiatePayment()
    ↓
PaymentServiceImpl.initiatePayment()
    ↓
RuleEngineClient.evaluate(paymentRequest)
    ↓
Rule Engine Validation
    ↓
RuleResult (valid/invalid)
    ↓
PaymentEntity Creation (if valid)
    ↓
PaymentResponse
```

### 2. Error Handling

The RuleEngineClient implements retry logic with exponential backoff:

- **Attempt 1**: Wait 100ms before retry
- **Attempt 2**: Wait 200ms before retry
- **Attempt 3**: Final attempt (no more retries)

If all attempts fail, the payment validation returns an error without rejecting the entire transaction.

### 3. Disabled Rule Engine

When `app.rule-engine.enabled=false`, all validations are skipped and payments are treated as approved.

## Usage Example

### In PaymentServiceImpl

```java
@Override
@Transactional
public PaymentResponse initiatePayment(PaymentRequest request) {
    
    // 🔥 STEP 1: CALL RULE ENGINE
    RuleResult ruleResult = ruleEngineClient.evaluate(request);
    
    // 🔥 STEP 2: CHECK VALIDITY
    if (!ruleResult.isValid()) {
        log.error("Payment rejected by rule engine: {}", ruleResult.getErrorMessages());
        throw new PaymentValidationException(
            String.join(", ", ruleResult.getErrorMessages())
        );
    }
    
    // 🔥 STEP 3: OPTIONAL WARNINGS
    if (ruleResult.hasWarnings()) {
        log.warn("Payment warnings: {}", ruleResult.getWarnings());
    }
    
    // Continue with payment processing...
    return processPayment(request);
}
```

## Testing

### Unit Tests

Run the test suite to validate integration:

```bash
mvn test -Dtest=RuleEngineClientTest
```

### Integration Tests

For end-to-end testing with a running Rule Engine:

```bash
mvn test -Dtest=PaymentServiceImplTest
```

### Mock Testing

The test suite includes mocked responses for various scenarios:

1. **Approved Payment**: Payment passes all validations
2. **Rejected Payment**: Violations detected
3. **Failed Connection**: Retry logic verification
4. **Warnings**: Non-blocking warnings handling
5. **Null Response**: Error handling for empty responses

## Monitoring & Logging

### Log Levels

- **INFO**: Rule engine requests, responses, and approvals
- **WARN**: Payment rejections, retries, and warnings
- **ERROR**: Connection failures, unexpected exceptions
- **DEBUG**: HTTP request details, attempt numbers

### Example Logs

```
[INFO] Rule Engine Request: {type=payment, price=50000.0, userId=1, sellerId=2, ...}
[INFO] Rule Engine Response: {approved=true, message=Payment approved}
[WARN] Payment rejected: Amount exceeds maximum limit
[ERROR] Rule Engine unavailable after 3 attempts: Connection refused
```

## Troubleshooting

### Issue: Rule Engine Connection Timeout

**Cause**: Rule Engine service is not running or is unreachable

**Solution**:
1. Verify Rule Engine service is running on the configured URL
2. Check network connectivity to Rule Engine
3. Update `app.rule-engine.url` if service address changed

### Issue: All Payments Being Rejected

**Cause**: Rule Engine is too strict or misconfigured

**Solution**:
1. Review rule engine configuration
2. Check violation details in logs
3. Temporarily disable rule engine with `app.rule-engine.enabled=false` to test

### Issue: Slow Payment Processing

**Cause**: Rule Engine requests are timing out

**Solution**:
1. Increase `app.rule-engine.timeout-ms` value
2. Check Rule Engine performance
3. Optimize rule engine rules

## Migration Guide

### From Direct Hardcoded URL

If you were using a hardcoded URL like:
```java
private static final String RULE_ENGINE_URL = "http://localhost:8055/rules/evaluate";
```

Update to use configuration:
```java
@Value("${app.rule-engine.url}")
private String ruleEngineUrl;
```

### From No Rule Engine Validation

If you're adding rule engine validation for the first time:

1. Ensure Rule Engine microservice is running
2. Add configuration properties to `application.properties`
3. Update `PaymentServiceImpl` to use `RuleEngineClient`
4. Test with sample payments

## Future Enhancements

1. **Async Validation**: Process rule evaluation asynchronously
2. **Caching**: Cache validation results for frequent scenarios
3. **Metrics**: Add Prometheus metrics for rule engine performance
4. **Circuit Breaker**: Implement circuit breaker pattern for resilience
5. **Custom Rules**: Support custom rule definitions per client

## Support

For issues or questions regarding Rule Engine integration:

1. Check the logs for error messages
2. Review the troubleshooting section
3. Contact the Rule Engine team for rule configuration issues

## References

- Rule Engine Microservice Documentation: [Link to be added]
- Payment Service API Documentation: [Link to be added]
- Spring REST Documentation: https://spring.io/guides/gs/consuming-rest/
