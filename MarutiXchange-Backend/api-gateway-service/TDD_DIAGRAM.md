# API Gateway Service - Technical Design Document (TDD)


## 1. DATA FLOW DIAGRAM (DFD)

### System Architecture Overview
```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                      │
│                    (Web/Mobile/Desktop Applications)                        │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                    HTTP Request (with JWT Token)
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY SERVICE (Port 8064)                          │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                                                                      │   │
│  │  1. REQUEST ENTRY POINT                                            │   │
│  │     • Receives HTTP Request                                        │   │
│  │     • Logs Correlation ID                                         │   │
│  │     • Records incoming data                                       │   │
│  │                                                                    │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │  2. LOGGING FILTER                                              │   │
│  │     ┌────────────────────────────────────────────────────────┐  │   │
│  │     │ LoggingFilter.java                                     │  │   │
│  │     │ • Log request method, path, headers                   │  │   │
│  │     │ • Add X-Correlation-ID (if not present)              │  │   │
│  │     │ • Proceed to next filter                             │  │   │
│  │     └────────────────────────────────────────────────────────┘  │   │
│  │                                                                    │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │  3. CORRELATION ID FILTER                                       │   │
│  │     ┌────────────────────────────────────────────────────────┐  │   │
│  │     │ CorrelationIdFilter.java                              │  │   │
│  │     │ • Generate/Extract Correlation ID                     │  │   │
│  │     │ • Attach to request headers                           │  │   │
│  │     │ • Forward through system                              │  │   │
│  │     └────────────────────────────────────────────────────────┘  │   │
│  │                                                                    │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │  4. JWT AUTHENTICATION FILTER                                   │   │
│  │     ┌────────────────────────────────────────────────────────┐  │   │
│  │     │ JwtAuthenticationFilter.java                           │  │   │
│  │     │                                                        │  │   │
│  │     │ Check Path:                                           │  │   │
│  │     │  ├─ /api/users/** → ALLOW (No Auth needed)           │  │   │
│  │     │  └─ Other paths → REQUIRE Auth                       │  │   │
│  │     │                                                        │  │   │
│  │     │ Extract Token from Authorization Header:              │  │   │
│  │     │  • Format: "Bearer {JWT_TOKEN}"                       │  │   │
│  │     │  • If missing → 401 UNAUTHORIZED                      │  │   │
│  │     │                                                        │  │   │
│  │     │ Validate Token (JwtUtil.java):                        │  │   │
│  │     │  • Use Secret Key: "mySecretKey12345678901234567890"  │  │   │
│  │     │  • Check signature & expiration                       │  │   │
│  │     │  • If invalid → 401 UNAUTHORIZED                      │  │   │
│  │     │                                                        │  │   │
│  │     │ Extract User Information:                             │  │   │
│  │     │  • Username from token claims                         │  │   │
│  │     │  • Roles from token claims                            │  │   │
│  │     │                                                        │  │   │
│  │     │ Add Headers for Downstream Services:                  │  │   │
│  │     │  • X-User: {username}                                 │  │   │
│  │     │  • X-Role: {roles}                                    │  │   │
│  │     └────────────────────────────────────────────────────────┘  │   │
│  │                                                                    │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │  5. ROUTE RESOLUTION & LOAD BALANCING (via Eureka)             │   │
│  │     ┌────────────────────────────────────────────────────────┐  │   │
│  │     │ Route Configuration (application.properties)          │  │   │
│  │     │                                                        │  │   │
│  │     │ Route Mapping:                                         │  │   │
│  │     │  ├─ /api/v1/users/** → lb://USER-SERVICE            │  │   │
│  │     │  ├─ /api/v1/cars/** → lb://CAR-LISTING-SERVICE      │  │   │
│  │     │  ├─ /api/v1/bookings/** → lb://BOOKING-SERVICE      │  │   │
│  │     │  ├─ /api/v1/bids/** → lb://BIDDING-SERVICE          │  │   │
│  │     │  ├─ /api/v1/orders/** → lb://ORDER-SERVICE          │  │   │
│  │     │  └─ /api/v1/payments/** → lb://PAYMENT-SERVICE      │  │   │
│  │     │                                                        │  │   │
│  │     │ Load Balancing (lb://):                               │  │   │
│  │     │  • Query Eureka Service Registry                      │  │   │
│  │     │  • Get available service instances                    │  │   │
│  │     │  • Round-robin selection                              │  │   │
│  │     └────────────────────────────────────────────────────────┘  │   │
│  │                                                                    │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                 │
                    HTTP Request (with X-User, X-Role headers)
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       EUREKA SERVICE REGISTRY                               │
│                         (Port 8761)                                         │
│                                                                             │
│  Service Instances:                                                         │
│  ├─ USER-SERVICE (multiple instances)                                      │
│  ├─ CAR-LISTING-SERVICE (multiple instances)                               │
│  ├─ BOOKING-SERVICE (multiple instances)                                   │
│  ├─ BIDDING-SERVICE (multiple instances)                                   │
│  ├─ ORDER-SERVICE (multiple instances)                                     │
│  └─ PAYMENT-SERVICE (multiple instances)                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                 │
             Load Balanced to one of the available instances
                                 │
                    ┌────────────┼────────────┐
                    ▼            ▼            ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                        DOWNSTREAM MICROSERVICES                              │
│                                                                              │
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐   │
│  │  USER-SERVICE      │  │ CAR-LISTING        │  │  BOOKING-SERVICE   │   │
│  │  • Register        │  │  • List cars       │  │  • Create booking  │   │
│  │  • Login           │  │  • Get car details │  │  • Update booking  │   │
│  │  • Profile         │  │  • Search cars     │  │  • Cancel booking  │   │
│  └────────────────────┘  └────────────────────┘  └────────────────────┘   │
│                                                                              │
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐   │
│  │  BIDDING-SERVICE   │  │  ORDER-SERVICE     │  │  PAYMENT-SERVICE   │   │
│  │  • Place bid       │  │  • Create order    │  │  • Process payment │   │
│  │  • Accept bid      │  │  • List orders     │  │  • Refund          │   │
│  │  • Reject bid      │  │  • Update order    │  │  • Transaction log │   │
│  └────────────────────┘  └────────────────────┘  └────────────────────┘   │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                 │
                    HTTP Response (200/400/500 etc)
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY SERVICE (Return Response)                    │
│                                                                             │
│  • Aggregate response data                                                  │
│  • Add correlation headers                                                 │
│  • Log response status                                                     │
│  • Handle errors via GlobalExceptionHandler                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                 │
                    HTTP Response to Client
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                      │
│                    (Web/Mobile/Desktop Applications)                        │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. DATA SEQUENCE DIAGRAM

### Sequence Diagram: Authenticated Request Flow

```
Client              LoggingFilter       CorrelationIdFilter    JwtAuthenticationFilter
  │                      │                      │                        │
  │  1. HTTP Request     │                      │                        │
  │─ (with JWT Token) ──→│                      │                        │
  │                      │                      │                        │
  │                      │ 2. Log Request       │                        │
  │                      │ (Method, Path)       │                        │
  │                      │ ────────────────→ ✓  │                        │
  │                      │                      │                        │
  │                      │                      │ 3. Extract/Generate    │
  │                      │                      │ Correlation ID         │
  │                      │                      │ ────────────────────→ ✓ │
  │                      │                      │                        │
  │                      │                      │                        │ 4. Check Path
  │                      │                      │                        │ /api/users/**?
  │                      │                      │                        │ ↓
  │                      │                      │                        │ NO
  │                      │                      │                        │
  │                      │                      │ 5. Extract Auth Header │
  │                      │                      │←─────────────────────  │
  │                      │                      │                        │
  │                      │                      │ 6. Parse "Bearer {JWT}"│
  │                      │                      │ ────────────────────→ ✓ │
  │                      │                      │                        │
  │                      │                      │    7. Validate Token   │
  │                      │                      │       (JwtUtil)        │
  │                      │                      │ ────────────────────→ ✓ │
  │                      │                      │                        │
  │                      │                      │ 8. Extract Claims      │
  │                      │                      │    (username, roles)   │
  │                      │                      │ ────────────────────→ ✓ │
  │                      │                      │                        │
  │                      │                      │ 9. Add Response Headers│
  │                      │                      │ X-User, X-Role        │
  │                      │                      │ ────────────────────→ ✓ │
  │                      │                      │                        │
  │                      │ 10. Chain to next filter/route                │
  │                      │←──────────────────────────────────────────────│
  │                      │                      │                        │
  ▼                      ▼                      ▼                        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  Route Resolution & Load Balancing (Eureka)                                 │
│  • Query Eureka Registry for target service                                 │
│  • Select instance (round-robin)                                           │
└──────────────────────────────────────────────────────────────────────────────┘
  │                      │                      │                        │
  │ 11. Forward Request to Downstream Service (with X-User, X-Role)       │
  ├────────────────────────────────────────────────────────────────────→ ┌──────────┐
  │                      │                      │                        │ Service  │
  │                      │                      │                        │ Instance │
  │                      │                      │                        └──────────┘
  │                      │                      │                        │
  │                      │                      │ 12. Receive Response   │
  │                      │                      │←───────────────────────│
  │                      │                      │                        │
  │ 13. Log Response     │                      │                        │
  │←─────────────────────│                      │                        │
  │                      │                      │                        │
  │ 14. Return Response to Client               │                        │
  │←──────────────────────────────────────────────────────────────────────
```

---

### Sequence Diagram: Unauthenticated Request Flow

```
Client              LoggingFilter       CorrelationIdFilter    JwtAuthenticationFilter
  │                      │                      │                        │
  │  1. HTTP Request     │                      │                        │
  │ (NO JWT Token)      │                      │                        │
  │─────────────────────→│                      │                        │
  │                      │                      │                        │
  │                      │ 2. Log Request       │                        │
  │                      │ ────────────────→ ✓  │                        │
  │                      │                      │                        │
  │                      │                      │ 3. Extract Correlation │
  │                      │                      │ ID                     │
  │                      │                      │ ────────────────────→ ✓ │
  │                      │                      │                        │
  │                      │                      │ 4. Check Path           │
  │                      │                      │ /api/users/**?          │
  │                      │                      │ ↓                       │
  │                      │                      │ IF YES → Allow          │
  │                      │                      │ ────────────────→ ✓     │
  │                      │                      │                        │
  │                      │                      │ Chain to Route          │
  │                      │                      │←──────────────────────  │
  │                      │                      │                        │
  ▼                      ▼                      ▼                        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  Route Resolution → Forward to USER-SERVICE                                 │
└──────────────────────────────────────────────────────────────────────────────┘
  │
  │ 5. Forward to Downstream Service
  │──────────────────────→ ┌──────────────────┐
  │                        │ USER-SERVICE     │
  │                        │ (Register/Login) │
  │                        └──────────────────┘
  │                        │
  │ 6. Service Response    │
  │←───────────────────────│
  │
  │ 7. Return to Client
  │
  ▼
```

---

### Sequence Diagram: Authentication Error Flow

```
Client              JwtAuthenticationFilter
  │                      │
  │  1. HTTP Request     │
  │ (Invalid/No Token)   │
  │─────────────────────→│
  │                      │
  │                      │ 2. Check Auth Header
  │                      │ NULL or NOT "Bearer ..."
  │                      │ ↓
  │                      │ ✗ INVALID
  │                      │
  │ 3. 401 UNAUTHORIZED   │
  │←─────────────────────│
  │ (Response.Complete)  │
  │
  ▼
```

---

## 3. DATA FLOW LEVELS

### Level 0 - Context Diagram
```
┌─────────────┐
│   CLIENTS   │
└──────┬──────┘
       │
       │ HTTP Requests/Responses
       │
       ▼
┌───────────────────────────┐
│  API GATEWAY SERVICE      │
│  (Port 8064)              │
└────────┬──────────────────┘
         │
         │ Routing & Load Balancing
         │
         ▼
┌───────────────────────────┐
│  MICROSERVICES &          │
│  EUREKA REGISTRY          │
│  (Port 8761)              │
└───────────────────────────┘
```

### Level 1 - Main Process Decomposition
```
┌──────────────────────────────────────────────────────────────────┐
│                    API GATEWAY SERVICE                           │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐      │
│  │ 1.0 RECEIVE  │  │ 2.0 VALIDATE │  │ 3.0 ROUTE &      │      │
│  │ REQUEST      │→→│ & FILTER     │→→│ FORWARD REQUEST  │      │
│  │              │  │              │  │                  │      │
│  │ • Log method │  │ • JWT Auth   │  │ • Query Eureka   │      │
│  │ • Log path   │  │ • Correlation│  │ • Load balance   │      │
│  └──────────────┘  │   ID         │  │ • Route to svc   │      │
│                    │ • Add headers│  │                  │      │
│                    │ • Exception  │  └──────────────────┘      │
│                    │   handling   │                            │
│                    └──────────────┘                            │
│                                                                │
└──────────────────────────────────────────────────────────────────┘
         │                                      │
         │  EUREKA REGISTRY                    │  Service Response
         │  & MICROSERVICES                    │
         │                                      │
         └──────────────────────────────────────┘
                            │
                            ▼
                  ┌──────────────────────┐
                  │ 4.0 RETURN RESPONSE  │
                  │                      │
                  │ • Log response code  │
                  │ • Send to client     │
                  │ • Add correlation ID │
                  └──────────────────────┘
```

### Level 2 - JWT Validation Process
```
┌─────────────────────────────────────────────────────────────────┐
│                  2.1 JWT AUTHENTICATION                         │
│                                                                 │
│  START                                                          │
│   │                                                             │
│   ▼                                                             │
│  ┌───────────────────────────┐                                │
│  │ Check Request Path        │                                │
│  │ /api/users/**?            │                                │
│  └────────┬────────┬─────────┘                                │
│           │        │                                          │
│        YES│        │NO                                         │
│           ▼        ▼                                           │
│      ┌────┴──┐  ┌─────────────────────┐                       │
│      │ALLOW  │  │ Check Auth Header   │                       │
│      │PROCEED│  │ "Authorization: ..."│                       │
│      └───────┘  └────────┬─────────┬──┘                       │
│                          │         │                          │
│                       FOUND│      │NULL/INVALID               │
│                          ▼         ▼                          │
│                      ┌─────────────────┐                       │
│                      │ Extract JWT     │ → ┌──────────┐      │
│                      │ from Bearer     │   │401 ERROR │      │
│                      └────────┬────────┘   └──────────┘      │
│                               │                               │
│                               ▼                               │
│                      ┌─────────────────┐                       │
│                      │ Validate Token  │                       │
│                      │ (Check Signature│                       │
│                      │  & Expiration)  │                       │
│                      └────────┬────────┘                       │
│                               │                               │
│                        ┌──────┴──────┐                        │
│                     VALID│           │INVALID                 │
│                        ▼             ▼                        │
│                    ┌───────┐     ┌──────────┐                │
│                    │SUCCESS│     │401 ERROR │                │
│                    └───┬───┘     └──────────┘                │
│                        │                                      │
│                        ▼                                      │
│             ┌──────────────────────┐                          │
│             │ Extract User Claims  │                          │
│             │ • Username           │                          │
│             │ • Roles              │                          │
│             │ • Authorities        │                          │
│             └──────────┬───────────┘                          │
│                        │                                      │
│                        ▼                                      │
│             ┌──────────────────────┐                          │
│             │ Add Response Headers │                          │
│             │ • X-User: {username} │                          │
│             │ • X-Role: {roles}    │                          │
│             └──────────┬───────────┘                          │
│                        │                                      │
│                        ▼                                      │
│             ┌──────────────────────┐                          │
│             │ CONTINUE TO NEXT     │                          │
│             │ FILTER/ROUTE         │                          │
│             └──────────────────────┘                          │
│                                                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. COMPONENT INTERACTION DIAGRAM

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY SERVICE                              │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ INCOMING REQUEST                                                  │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ LoggingFilter                                                      │ │
│  │ ├─ Log request details (method, URI, headers)                    │ │
│  │ └─ Pass to next filter                                           │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ CorrelationIdFilter                                               │ │
│  │ ├─ Extract or generate Correlation ID                            │ │
│  │ └─ Attach to ServerWebExchange                                   │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ JwtAuthenticationFilter (extends AbstractGatewayFilterFactory)    │ │
│  │                                                                    │ │
│  │ ├─ Check if path requires authentication                         │ │
│  │ │   └─ /api/users/** → Allow without auth                       │ │
│  │ │   └─ Other paths → Require JWT                                │ │
│  │ ├─ Extract Authorization header                                 │ │
│  │ ├─ Parse JWT token from "Bearer {token}" format                │ │
│  │ ├─ Call JwtUtil to validate token                              │ │
│  │ │   └─ Validate signature using secret key                     │ │
│  │ │   └─ Check token expiration                                  │ │
│  │ ├─ If valid: Extract claims (username, roles)                  │ │
│  │ ├─ Add X-User and X-Role headers for downstream services       │ │
│  │ └─ If invalid: Return 401 UNAUTHORIZED                         │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ GatewayConfig (Route Configuration)                               │ │
│  │ ├─ Route 1: /api/v1/users/** → lb://USER-SERVICE               │ │
│  │ ├─ Route 2: /api/v1/cars/** → lb://CAR-LISTING-SERVICE         │ │
│  │ ├─ Route 3: /api/v1/bookings/** → lb://BOOKING-SERVICE         │ │
│  │ ├─ Route 4: /api/v1/bids/** → lb://BIDDING-SERVICE             │ │
│  │ ├─ Route 5: /api/v1/orders/** → lb://ORDER-SERVICE             │ │
│  │ └─ Route 6: /api/v1/payments/** → lb://PAYMENT-SERVICE         │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Eureka Discovery                                                  │ │
│  │ ├─ Query Eureka Registry (http://localhost:8761/eureka/)        │ │
│  │ ├─ Find available instances of target service                   │ │
│  │ ├─ Load Balance (Round-robin/Least connections)                │ │
│  │ └─ Select target instance                                       │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Forward Request to Microservice                                   │ │
│  │ ├─ Include X-User header                                         │ │
│  │ ├─ Include X-Role header                                         │ │
│  │ ├─ Include X-Correlation-ID header                               │ │
│  │ └─ Maintain original request body/queryParams                    │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Receive Response from Microservice                                │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ GlobalExceptionHandler                                            │ │
│  │ ├─ Catch any exceptions during request processing               │ │
│  │ ├─ Return appropriate error response (400, 401, 500, etc.)      │ │
│  │ └─ Log error with Correlation ID                                │ │
│  └─────┬──────────────────────────────────────────────────────────────┘ │
│        │                                                                 │
│        ▼                                                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Return Response to Client                                         │ │
│  │ ├─ Include response status                                       │ │
│  │ ├─ Include response body                                         │ │
│  │ └─ Include Correlation ID header                                 │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---


---

## 6. SECURITY FLOW

```
┌──────────────────────────────────────────────────────────────────┐
│                     SECURITY ARCHITECTURE                        │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 1. Token Generation (by Auth Service)                      │ │
│  │    • User credentials verified                            │ │
│  │    • JWT token created with secret key:                  │ │
│  │      "mySecretKey12345678901234567890"                   │ │
│  │    • Token includes: username, roles, expiration         │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 2. Token Transmission (Client → Gateway)                  │ │
│  │    • Client includes token in Authorization header       │ │
│  │    • Format: "Authorization: Bearer {JWT_TOKEN}"         │ │
│  │    • HTTPS recommended for production                    │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 3. Token Validation (Gateway)                             │ │
│  │    • Extract token from Authorization header             │ │
│  │    • Verify signature using secret key                   │ │
│  │    • Check token expiration timestamp                    │ │
│  │    • If valid: Extract claims (username, roles)          │ │
│  │    • If invalid: Reject with 401 UNAUTHORIZED            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 4. Authorization (Gateway → Microservices)               │ │
│  │    • Attach user info headers to request:               │ │
│  │      - X-User: {username}                               │ │
│  │      - X-Role: {roles}                                  │ │
│  │    • Microservices use these headers for business-level  │ │
│  │      authorization checks                               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 5. Request Path Exceptions                               │ │
│  │    • /api/users/** → No authentication required          │ │
│  │      (allows public registration & login)                │ │
│  │    • All other paths → JWT token required                │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 7. ERROR HANDLING FLOW

```
REQUEST PROCESSING
       │
       ▼
┌──────────────────┐
│ Is token missing │
│ or invalid?      │
└────┬─────────┬───┘
     │         │
    YES        NO
     │         │
     ▼         ▼
  401      Continue
  UNAUTH     │
             ▼
        ┌──────────────────────┐
        │ Route to microservice│
        └────┬─────────┬───────┘
             │         │
          SUCCESS    ERROR
             │         │
             ▼         ▼
        Response   GlobalException
        (2xx)      Handler
                      │
                      ▼
                   Format Error
                   Response (4xx/5xx)
                      │
                      ▼
                   Return to Client
```