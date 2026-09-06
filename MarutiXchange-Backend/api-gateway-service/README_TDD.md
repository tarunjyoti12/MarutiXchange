# API Gateway Service - TDD SUMMARY & QUICK REFERENCE

## 📋 Document Overview

This folder contains comprehensive Technical Design Documentation (TDD) for the MarutiXchange API Gateway Service. All diagrams follow industry standards and best practices.

---

## 📁 FILES INCLUDED

### 1. **TDD_DIAGRAM.md** (Main Document)
   - Complete technical design document
   - Data Flow Diagram (DFD) with 10 detailed levels
   - Sequence diagrams for all scenarios
   - Component interaction diagrams
   - Security architecture
   - Error handling flows
   - Configuration parameters

### 2. **DATA_FLOW_DIAGRAM.puml**
   - PlantUML format DFD
   - Request journey through gateway layers
   - Sequence of filter processing
   - Eureka service discovery flow
   - Load balancing mechanism
   - **To view**: Use PlantUML viewer or convert to PNG/SVG

### 3. **SEQUENCE_DIAGRAM_AUTHENTICATED.puml**
   - PlantUML sequence diagram
   - Complete authenticated request flow
   - JWT token validation steps
   - User information extraction
   - Downstream service routing
   - Response aggregation

### 4. **SEQUENCE_DIAGRAM_PUBLIC.puml**
   - Public endpoint flow (no authentication)
   - User registration/login processing
   - Path-based routing decisions
   - Direct service forwarding

### 5. **SEQUENCE_DIAGRAM_ERROR.puml**
   - Authentication error scenarios
   - Exception handling flow
   - 401 Unauthorized responses
   - GlobalExceptionHandler processing

### 6. **COMPONENT_DIAGRAM.puml**
   - System component architecture
   - Filter chain components
   - Security layer components
   - Routing & configuration components
   - Load balancing components
   - Interaction relationships

### 7. **DEPLOYMENT_DIAGRAM.puml**
   - Production environment layout
   - Gateway cluster (3 instances)
   - Microservices clusters
   - Database layer
   - Load balancer configuration
   - Monitoring & logging infrastructure

### 8. **API_DOCUMENTATION.md**
   - All API endpoints (public & protected)
   - Request/response examples
   - JWT token structure
   - HTTP status codes
   - Error response formats
   - Correlation ID flow
   - Security considerations
   - Rate limiting & timeouts

---

## 🎯 QUICK START GUIDE

### Understanding the Architecture

```
CLIENT REQUESTS
        ↓
    GATEWAY (Port 8064)
    ├─ LoggingFilter
    ├─ CorrelationIdFilter
    └─ JwtAuthenticationFilter
        ↓
    Route Resolution
        ↓
    Eureka Service Discovery
        ↓
    Load Balancing
        ↓
    MICROSERVICES
        ↓
    Response Aggregation
        ↓
    RETURN TO CLIENT
```

### Key Components

| Component | Purpose | File Reference |
|-----------|---------|-----------------|
| LoggingFilter | Log all request details | TDD_DIAGRAM.md (Section 2.0) |
| CorrelationIdFilter | Generate/Track request ID | TDD_DIAGRAM.md (Section 2.0) |
| JwtAuthenticationFilter | Validate JWT tokens | SEQUENCE_DIAGRAM_AUTHENTICATED.puml |
| GatewayConfig | Route configuration | TDD_DIAGRAM.md (Section 2.0) |
| GlobalExceptionHandler | Error handling | SEQUENCE_DIAGRAM_ERROR.puml |
| Eureka Client | Service discovery | DEPLOYMENT_DIAGRAM.puml |

---

## 📊 FLOW DIAGRAMS AT A GLANCE

### Flow 1: Authenticated Request (Protected Resource)
```
CLIENT
  ↓ (JWT Token in Authorization header)
GATEWAY: Validate JWT
  ↓ (If valid: Extract user info)
ADD HEADERS: X-User, X-Role
  ↓ (Route request)
MICROSERVICE
  ↓ (Use X-User, X-Role for authorization)
RETURN RESPONSE
```

### Flow 2: Public Request (User Registration)
```
CLIENT
  ↓ (No auth header required)
GATEWAY: Check path
  ↓ (/api/users/** = Public)
SKIP JWT VALIDATION
  ↓ (Route directly)
MICROSERVICE (USER-SERVICE)
  ↓ (Register user)
RETURN RESPONSE
```

### Flow 3: Authentication Error
```
CLIENT
  ↓ (Invalid/Missing token)
GATEWAY: JWT Validation
  ↓ (FAIL: Signature/Expiration invalid)
401 UNAUTHORIZED
  ↓ (No service call made)
ERROR RESPONSE WITH CORRELATION-ID
```

---

## 🔐 SECURITY FLOW

```
┌─ Token Generation (Auth Service) ─────────┐
│  JWT Created with Secret Key              │
│  Claims: username, roles, expiration      │
└───────────────────────────────────────────┘
                ↓
┌─ Token Transmission (Client → Gateway) ──┐
│  Authorization: Bearer {JWT_TOKEN}        │
│  HTTPS Only (Recommended)                 │
└───────────────────────────────────────────┘
                ↓
┌─ Token Validation (Gateway) ──────────────┐
│  1. Extract token                         │
│  2. Verify signature                      │
│  3. Check expiration                      │
│  4. Extract claims                        │
└───────────────────────────────────────────┘
                ↓
┌─ Authorization (Gateway → Services) ──────┐
│  Add Headers:                             │
│  • X-User: {username}                     │
│  • X-Role: {roles}                        │
│  Services use for business logic auth     │
└───────────────────────────────────────────┘
```

---

## 📍 ROUTING CONFIGURATION

| Endpoint Pattern | Target Service | Auth? |
|------------------|-----------------|-------|
| /api/v1/users/** | USER-SERVICE | ❌ (Register/Login) / ✅ (Profile) |
| /api/v1/cars/** | CAR-LISTING-SERVICE | ✅ |
| /api/v1/bookings/** | BOOKING-SERVICE | ✅ |
| /api/v1/bids/** | BIDDING-SERVICE | ✅ |
| /api/v1/orders/** | ORDER-SERVICE | ✅ |
| /api/v1/payments/** | PAYMENT-SERVICE | ✅ |

---

## 🔍 CORRELATION ID TRACKING

**What**: Unique identifier for tracking requests across all services

**How**: UUID generated at gateway, included in all downstream calls

**Where**: 
- Request header: `X-Correlation-ID`
- Log entries (for end-to-end tracing)
- Error responses

**Example**:
```
CLIENT REQUEST
  ↓
X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000
  ↓
GATEWAY: [550e8400...] JWT validation successful
  ↓
BOOKING-SERVICE: [550e8400...] Processing booking for car CAR-001
  ↓
PAYMENT-SERVICE: [550e8400...] Processing payment for booking
  ↓
RESPONSE includes X-Correlation-ID for tracking
```

---

## 📝 DATA STRUCTURES

### JWT Token Payload
```json
{
  "sub": "UUID-12345",
  "username": "john_doe",
  "email": "user@example.com",
  "roles": ["USER", "CUSTOMER"],
  "iat": 1712510700,
  "exp": 1712597100
}
```

### Request Header Format
```
POST /api/v1/bookings
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000
```

### Response Header Format
```
HTTP/1.1 200 OK
Content-Type: application/json
X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000
X-User: john_doe
X-Role: USER,CUSTOMER
```

---

## ⚙️ CONFIGURATION PARAMETERS

| Parameter | Value | Purpose |
|-----------|-------|---------|
| Server Port | 8064 | Gateway service port |
| Eureka URL | http://localhost:8761/eureka/ | Service registry |
| JWT Secret | mySecretKey... | Token signing key |
| Auth Path Exception | /api/users/** | Public endpoints |
| Connect Timeout | 5000ms | Service connection timeout |
| Response Timeout | 10000ms | Service response timeout |

---

## 🚨 ERROR CODES & RESPONSES

### Common Errors

| Status | Error | When | Solution |
|--------|-------|------|----------|
| 400 | Bad Request | Invalid JSON, missing fields | Check request format |
| 401 | Unauthorized | Missing/invalid JWT token | Provide valid token |
| 403 | Forbidden | Valid token but insufficient permissions | Check user roles |
| 404 | Not Found | Resource doesn't exist | Verify resource ID |
| 500 | Internal Server Error | Microservice error | Check microservice logs |
| 503 | Service Unavailable | Microservice down | Check service health |

### Error Response Format
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

---

## 🔗 REQUEST FLOW SUMMARY

### Complete Request Lifecycle

```
1. CLIENT SENDS REQUEST
   POST /api/v1/bookings
   Authorization: Bearer {TOKEN}
   Body: {booking details}

2. GATEWAY RECEIVES REQUEST
   LoggingFilter: Log incoming request
   CorrelationIdFilter: Add X-Correlation-ID header
   JwtAuthenticationFilter:
     - Check path: /api/v1/bookings (requires auth)
     - Extract token from Authorization header
     - Validate token (signature, expiration)
     - Extract username & roles
     - Add X-User, X-Role headers

3. ROUTE RESOLUTION
   GatewayConfig: Match /api/v1/bookings/** → BOOKING-SERVICE
   
4. SERVICE DISCOVERY
   Eureka: Query for BOOKING-SERVICE instances
   Return: [booking-service-1, booking-service-2, booking-service-3]
   
5. LOAD BALANCING
   Select: booking-service-2 (round-robin)
   
6. FORWARD REQUEST
   Send request with all headers + body to selected instance
   
7. SERVICE PROCESSING
   BOOKING-SERVICE processes request
   Uses X-User header for authorization
   Creates booking in database
   Returns 201 CREATED response
   
8. RESPONSE HANDLING
   Gateway receives response
   Add Correlation ID to response headers
   Return to client with 201 CREATED status
   
9. CLIENT RECEIVES RESPONSE
   Status: 201 CREATED
   Headers: X-Correlation-ID
   Body: New booking details
```

---

## 📊 DEPLOYMENT TOPOLOGY

```
┌─ LOAD BALANCER ─────────────────────────┐
│ Nginx / AWS ALB / Azure LB              │
│ • SSL/TLS Termination                   │
│ • Distribute traffic across gateways    │
└─────────────────┬───────────────────────┘
        ┌─────────┼─────────┐
        ↓         ↓         ↓
    ┌─────┐   ┌─────┐   ┌─────┐
    │ GW1 │   │ GW2 │   │ GW3 │  (3 Gateway Instances)
    └──┬──┘   └──┬──┘   └──┬──┘
       │         │         │
       └─────────┼─────────┘
               ↓
        ┌─────────────────┐
        │ Eureka Registry │ (Service Discovery)
        │ :8761           │
        └────────┬────────┘
                 │
    ┌────────────┼────────────────┐
    ↓            ↓                 ↓
┌──────────┐ ┌──────────┐  ┌──────────┐
│ USER SVC │ │ CAR SVC  │  │ ORDER SVC│  (Multiple services)
│ x3       │ │ x2       │  │ x2       │
└──────────┘ └──────────┘  └──────────┘
```

---

## 🔐 SECURITY CHECKLIST

- [x] JWT token validation
- [x] Path-based authentication rules
- [x] Correlation ID logging
- [x] Error handling without exposing sensitive data
- [ ] Rate limiting (Not implemented)
- [ ] CORS configuration (Not visible)
- [ ] API key for service-to-service (Not implemented)
- [ ] Refresh token mechanism (Not implemented)
- [ ] JWT secret in secure vault (⚠️ Currently hardcoded)
- [ ] HTTPS enforcement (Not configured)

---

## 📚 HOW TO USE THESE DOCUMENTS

### For System Architects
1. Review **DEPLOYMENT_DIAGRAM.puml** for infrastructure layout
2. Check **API_DOCUMENTATION.md** for service contracts
3. Use **TDD_DIAGRAM.md** Section 9-10 for deployment architecture

### For Developers
1. Start with **SEQUENCE_DIAGRAM_AUTHENTICATED.puml** to understand flow
2. Review **API_DOCUMENTATION.md** for request/response formats
3. Check **SEQUENCE_DIAGRAM_ERROR.puml** for error handling
4. Reference **TDD_DIAGRAM.md** Section 4 for component interactions

### For QA/Testers
1. Use **API_DOCUMENTATION.md** for test cases
2. Reference **SEQUENCE_DIAGRAM_ERROR.puml** for edge cases
3. Check **TDD_DIAGRAM.md** Section 1 for DFD levels

### For DevOps/SRE
1. Review **DEPLOYMENT_DIAGRAM.puml** for infrastructure
2. Check **TDD_DIAGRAM.md** Section 10 for configuration parameters
3. Reference **API_DOCUMENTATION.md** Section 10 for monitoring

---

## 🔄 CONVERTING PUML FILES TO IMAGES

### Using PlantUML Online Editor
1. Visit: https://www.plantuml.com/plantuml/uml/
2. Copy contents of .puml file
3. Export as PNG, SVG, or PDF

### Using PlantUML CLI
```bash
# Install PlantUML
brew install plantuml

# Convert to PNG
plantuml DATA_FLOW_DIAGRAM.puml -o png

# Convert to SVG
plantuml SEQUENCE_DIAGRAM_AUTHENTICATED.puml -o svg
```

### Using VS Code Extension
1. Install "PlantUML" extension
2. Right-click on .puml file
3. Select "Export Current Diagram"

---

## 📞 CONTACT & UPDATES

**Document Version**: 1.0
**Last Updated**: April 7, 2026
**Next Review Date**: April 21, 2026

For updates or corrections, please:
- Update the relevant markdown/puml file
- Increment version number
- Update "Last Updated" date
- Add change log entry

---

## 📖 RELATED DOCUMENTATION

- **application.properties**: Gateway configuration (routes, Eureka, JWT)
- **JwtAuthenticationFilter.java**: JWT validation logic
- **JwtUtil.java**: Token utility functions
- **GatewayConfig.java**: Route definitions
- **GlobalExceptionHandler.java**: Error handling
- **LoggingFilter.java**: Request logging
- **CorrelationIdFilter.java**: Correlation ID management

---

**END OF TDD SUMMARY**

*This document package provides comprehensive technical design documentation for the MarutiXchange API Gateway Service, including data flow diagrams, sequence diagrams, component diagrams, and detailed API specifications.*
