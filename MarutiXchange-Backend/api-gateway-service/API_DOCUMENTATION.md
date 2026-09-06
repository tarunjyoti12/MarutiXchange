# API Gateway Service - API Documentation & Data Contract

## Overview
This document describes the API contracts, request/response flows, and data structures for the API Gateway Service.

---

## 1. API ENDPOINTS

### 1.1 Public Endpoints (No Authentication Required)

#### 1.1.1 User Registration
```
POST /api/v1/users/register

Request:
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+919876543210"
}

Response (201 CREATED):
{
  "userId": "UUID-12345",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2026-04-07T14:45:00Z",
  "message": "User registered successfully"
}

Error Response (400 BAD REQUEST):
{
  "error": "Email already exists",
  "timestamp": "2026-04-07T14:45:00Z",
  "status": 400
}
```

#### 1.1.2 User Login
```
POST /api/v1/users/login

Request:
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}

Response (200 OK):
{
  "userId": "UUID-12345",
  "email": "user@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "roles": ["USER", "CUSTOMER"]
}

Error Response (401 UNAUTHORIZED):
{
  "error": "Invalid credentials",
  "timestamp": "2026-04-07T14:45:00Z",
  "status": 401
}
```

---

### 1.2 Protected Endpoints (Authentication Required)

#### 1.2.1 Get User Profile
```
GET /api/v1/users/{userId}

Headers:
Authorization: Bearer {JWT_TOKEN}
X-Correlation-ID: {CORRELATION_ID}

Response (200 OK):
{
  "userId": "UUID-12345",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+919876543210",
  "roles": ["USER", "CUSTOMER"],
  "createdAt": "2026-04-07T14:45:00Z",
  "lastLogin": "2026-04-07T15:30:00Z"
}

Headers in Response:
X-Correlation-ID: {CORRELATION_ID}
X-User: john_doe
X-Role: USER,CUSTOMER
```

#### 1.2.2 List Cars
```
GET /api/v1/cars?page=1&pageSize=20&search=maruti

Headers:
Authorization: Bearer {JWT_TOKEN}
X-User: john_doe (Added by Gateway)
X-Role: CUSTOMER (Added by Gateway)

Response (200 OK):
{
  "totalCount": 156,
  "page": 1,
  "pageSize": 20,
  "cars": [
    {
      "carId": "CAR-001",
      "brand": "Maruti Suzuki",
      "model": "Swift",
      "year": 2023,
      "price": 599999,
      "mileage": "23 kmpl",
      "fuelType": "Petrol",
      "transmission": "Manual",
      "owner": {
        "userId": "UUID-54321",
        "name": "Rajesh Kumar"
      }
    }
  ]
}
```

#### 1.2.3 Create Booking
```
POST /api/v1/bookings

Headers:
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

Request:
{
  "carId": "CAR-001",
  "startDate": "2026-04-15",
  "endDate": "2026-04-20",
  "driverName": "John Doe",
  "pickupLocation": "Mumbai Airport",
  "dropoffLocation": "Mumbai Hotel",
  "additionalRequests": "Child seat needed"
}

Response (201 CREATED):
{
  "bookingId": "BK-12345",
  "carId": "CAR-001",
  "userId": "UUID-12345",
  "status": "CONFIRMED",
  "startDate": "2026-04-15",
  "endDate": "2026-04-20",
  "totalDays": 5,
  "dailyRate": 5000,
  "totalAmount": 25000,
  "createdAt": "2026-04-07T14:45:00Z"
}
```

#### 1.2.4 Place Bid
```
POST /api/v1/bids

Headers:
Authorization: Bearer {JWT_TOKEN}

Request:
{
  "carId": "CAR-001",
  "bidAmount": 450000,
  "bidderId": "UUID-12345",
  "message": "Ready to buy immediately"
}

Response (201 CREATED):
{
  "bidId": "BID-789",
  "carId": "CAR-001",
  "bidderId": "UUID-12345",
  "bidAmount": 450000,
  "status": "ACTIVE",
  "createdAt": "2026-04-07T14:45:00Z"
}
```

#### 1.2.5 Process Payment
```
POST /api/v1/payments

Headers:
Authorization: Bearer {JWT_TOKEN}

Request:
{
  "orderId": "ORDER-123",
  "amount": 25000,
  "paymentMethod": "CREDIT_CARD",
  "cardDetails": {
    "cardNumber": "****-****-****-1234",
    "expiryMonth": 12,
    "expiryYear": 2028,
    "cvv": "***"
  }
}

Response (200 OK):
{
  "transactionId": "TXN-456789",
  "orderId": "ORDER-123",
  "amount": 25000,
  "status": "SUCCESS",
  "paymentMethod": "CREDIT_CARD",
  "processedAt": "2026-04-07T14:45:00Z",
  "receiptUrl": "https://api.marutixchange.com/receipts/TXN-456789"
}

Error Response (400 BAD REQUEST):
{
  "error": "Payment failed",
  "transactionId": "TXN-456789",
  "reason": "Insufficient funds",
  "status": 400
}
```

---

## 2. HTTP STATUS CODES

| Code | Meaning | Scenario |
|------|---------|----------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST request (resource created) |
| 204 | No Content | Successful DELETE request |
| 400 | Bad Request | Invalid request data, malformed JSON |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists (duplicate email) |
| 500 | Internal Server Error | Server-side error in microservice |
| 503 | Service Unavailable | Microservice is down |

---

## 3. JWT TOKEN STRUCTURE

### Token Format
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### Token Claims
```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "UUID-12345",                    // User ID
  "username": "john_doe",                 // Username
  "email": "user@example.com",           // Email
  "roles": ["USER", "CUSTOMER"],         // User roles
  "iat": 1712510700,                     // Issued at (timestamp)
  "exp": 1712597100,                     // Expiration (24 hours later)
  "iss": "api-gateway-service",          // Issuer
  "aud": "marutixchange-api"             // Audience
}

Signature: HMAC-SHA256(header + payload, secret_key)
Secret Key: mySecretKey12345678901234567890
```

---

## 4. REQUEST/RESPONSE HEADERS

### Standard Request Headers
```
Authorization: Bearer {JWT_TOKEN}          // JWT token for authentication
Content-Type: application/json             // Request body format
Accept: application/json                   // Expected response format
X-Correlation-ID: {UUID}                  // Unique request tracking ID (optional)
User-Agent: Mozilla/5.0 ...               // Client information
```

### Standard Response Headers
```
Content-Type: application/json             // Response body format
X-Correlation-ID: {UUID}                  // Correlation ID for tracking
X-User: {username}                        // Username (added by gateway)
X-Role: {roles}                           // User roles (added by gateway)
X-RateLimit-Limit: 1000                   // Rate limit quota
X-RateLimit-Remaining: 999                // Remaining requests
X-RateLimit-Reset: 1712513700             // Reset timestamp
Cache-Control: no-cache, no-store         // Cache directives
```

---

## 5. CORRELATION ID FLOW

### What is Correlation ID?
A unique identifier (UUID) that tracks a request across all microservices.

### Flow
```
1. Client → Gateway
   • Check if "X-Correlation-ID" header exists
   • If NOT present: Generate UUID
   • If present: Use provided ID

2. Gateway → Microservices
   • Include X-Correlation-ID in forwarded request

3. Microservices → Logging/Monitoring
   • Log all events with Correlation ID
   • Enables end-to-end request tracing

4. Error Scenarios
   • If error occurs: Return Correlation ID in error response
   • Allows debugging specific request flow
```

### Example Correlation Flow
```
Request 1: POST /api/v1/bookings
X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000

→ API Gateway logs:
  [550e8400...] Received booking request from user UUID-12345
  [550e8400...] JWT validation successful
  [550e8400...] Routing to BOOKING-SERVICE

→ Booking Service logs:
  [550e8400...] Processing booking for car CAR-001
  [550e8400...] Checking availability for 2026-04-15 to 2026-04-20
  [550e8400...] Booking confirmed

→ Payment Service logs (if payment initiated):
  [550e8400...] Processing payment for booking
  [550e8400...] Transaction ID: TXN-456789

→ Response includes:
  X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000
```

---

## 6. ERROR RESPONSE FORMAT

### Standard Error Response
```json
{
  "error": "Error message",
  "errorCode": "ERR_001",
  "message": "Detailed error description",
  "status": 400,
  "timestamp": "2026-04-07T14:45:00Z",
  "path": "/api/v1/bookings",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Authentication Error
```json
{
  "error": "Unauthorized",
  "errorCode": "UNAUTHORIZED",
  "message": "Invalid or expired JWT token",
  "status": 401,
  "timestamp": "2026-04-07T14:45:00Z",
  "path": "/api/v1/bookings/list",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Validation Error
```json
{
  "error": "Bad Request",
  "errorCode": "VALIDATION_ERROR",
  "message": "Invalid request data",
  "status": 400,
  "timestamp": "2026-04-07T14:45:00Z",
  "path": "/api/v1/users/register",
  "validationErrors": [
    {
      "field": "email",
      "message": "Email must be valid",
      "rejectedValue": "invalid-email"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters",
      "rejectedValue": "123456"
    }
  ]
}
```

---

## 7. REQUEST-RESPONSE FLOW EXAMPLES

### Example 1: Successful Protected Request
```
CLIENT SIDE:
================================================================================
POST /api/v1/bookings
Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Body: {"carId": "CAR-001", "startDate": "2026-04-15", "endDate": "2026-04-20"}

GATEWAY PROCESSING:
================================================================================
1. LoggingFilter: Log request (POST /api/v1/bookings)
2. CorrelationIdFilter: Generate X-Correlation-ID: 550e8400...
3. JwtAuthenticationFilter:
   - Extract token from Authorization header
   - Validate signature using secret key
   - Extract claims: username=john_doe, roles=[USER]
   - Add headers: X-User: john_doe, X-Role: USER
4. RouteResolver: Match /api/v1/bookings → BOOKING-SERVICE
5. Eureka: Query for BOOKING-SERVICE instances
6. LoadBalancer: Select instance booking-service-2.local:8081
7. Forward request with all headers and body

MICROSERVICE RESPONSE:
================================================================================
HTTP/1.1 201 CREATED
Headers:
  Content-Type: application/json
  X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000

Body:
{
  "bookingId": "BK-12345",
  "carId": "CAR-001",
  "status": "CONFIRMED",
  "totalAmount": 25000,
  "createdAt": "2026-04-07T14:45:00Z"
}

GATEWAY RESPONSE:
================================================================================
HTTP/1.1 201 CREATED
Headers:
  Content-Type: application/json
  X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000
  X-User: john_doe
  X-Role: USER

Body: (Same as microservice response)
```

### Example 2: Failed Authentication
```
CLIENT SIDE:
================================================================================
GET /api/v1/bookings/list
Headers: Authorization: Bearer invalid_token_xyz

GATEWAY PROCESSING:
================================================================================
1. LoggingFilter: Log request
2. CorrelationIdFilter: Add Correlation ID
3. JwtAuthenticationFilter:
   - Extract token: "invalid_token_xyz"
   - Call JwtUtil.validateToken()
   - SignatureException: "JWT signature does not match"
   - Return 401 UNAUTHORIZED

GATEWAY RESPONSE:
================================================================================
HTTP/1.1 401 UNAUTHORIZED
Headers:
  Content-Type: application/json
  X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000

Body:
{
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "status": 401,
  "timestamp": "2026-04-07T14:45:00Z",
  "path": "/api/v1/bookings/list",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Example 3: Public Endpoint (No Auth Required)
```
CLIENT SIDE:
================================================================================
POST /api/v1/users/register
Headers: Content-Type: application/json
Body: {"email": "newuser@example.com", "password": "Password123!"}

GATEWAY PROCESSING:
================================================================================
1. LoggingFilter: Log request
2. CorrelationIdFilter: Add Correlation ID
3. JwtAuthenticationFilter:
   - Check path: /api/v1/users/register → /api/users/**
   - Match found: SKIP JWT validation
4. RouteResolver: Match /api/v1/users/** → USER-SERVICE
5. Forward request to USER-SERVICE

USER-SERVICE RESPONSE:
================================================================================
HTTP/1.1 201 CREATED
Body:
{
  "userId": "UUID-67890",
  "email": "newuser@example.com",
  "createdAt": "2026-04-07T14:45:00Z",
  "message": "User registered successfully"
}

GATEWAY RESPONSE:
================================================================================
HTTP/1.1 201 CREATED
Headers:
  X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000

Body: (Same as USER-SERVICE response)
```

---

## 8. SERVICE ROUTING CONFIGURATION

| Path | Service | Load Balanced URI | Auth Required |
|------|---------|-------------------|---------------|
| /api/v1/users/** | USER-SERVICE | lb://USER-SERVICE | NO (for register/login) |
| /api/v1/cars/** | CAR-LISTING-SERVICE | lb://CAR-LISTING-SERVICE | YES |
| /api/v1/bookings/** | BOOKING-SERVICE | lb://BOOKING-SERVICE | YES |
| /api/v1/bids/** | BIDDING-SERVICE | lb://BIDDING-SERVICE | YES |
| /api/v1/orders/** | ORDER-SERVICE | lb://ORDER-SERVICE | YES |
| /api/v1/payments/** | PAYMENT-SERVICE | lb://PAYMENT-SERVICE | YES |

---

## 9. TIMEOUT & RATE LIMITING

### Gateway Timeouts
```properties
spring.cloud.gateway.httpclient.connect-timeout=5000     # 5 seconds
spring.cloud.gateway.httpclient.response-timeout=10000   # 10 seconds
```

### Load Balancer Timeout
```properties
spring.cloud.loadbalancer.ribbon.enabled-with-service-discovery=true
ribbon.ConnectTimeout=5000
ribbon.ReadTimeout=10000
```

### Rate Limiting (Per Service)
```
USER-SERVICE: 1000 requests/minute
CAR-LISTING-SERVICE: 500 requests/minute
BOOKING-SERVICE: 300 requests/minute
PAYMENT-SERVICE: 100 requests/minute
```

---

## 10. MONITORING & METRICS

### Metrics Exposed
```
GET /actuator/metrics

Available Metrics:
- http.server.requests: HTTP request latency
- gateway.requests: Gateway-specific metrics
- system.cpu.usage: CPU usage percentage
- jvm.memory.used: JVM memory usage
- eureka.client.* : Eureka discovery metrics
```

### Health Check
```
GET /actuator/health

Response:
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "status": "UP",
      "components": {
        "eureka": {
          "status": "UP"
        }
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "livenessState": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    }
  }
}
```

---

## 11. SECURITY CONSIDERATIONS

### Best Practices
1. **HTTPS Only**: Use TLS 1.2+ for all connections
2. **Token Rotation**: Implement refresh token mechanism
3. **Rate Limiting**: Prevent brute force attacks
4. **Input Validation**: Sanitize all incoming data
5. **CORS**: Configure Cross-Origin Resource Sharing properly
6. **API Key**: Use additional API keys for service-to-service communication
7. **Audit Logging**: Log all access attempts with Correlation ID
8. **Secret Management**: Store JWT secret in secure vault (AWS Secrets Manager, HashiCorp Vault)

### Current Limitations
- JWT secret is hardcoded in properties (⚠️ Move to vault for production)
- No refresh token mechanism implemented
- No API rate limiting implemented
- No CORS configuration visible

---

**Document Version**: 1.0
**Last Updated**: April 7, 2026
**Next Review**: April 21, 2026
