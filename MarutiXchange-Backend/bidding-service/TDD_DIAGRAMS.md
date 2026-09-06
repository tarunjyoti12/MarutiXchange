# MarutiXchange Bidding Service - Architecture & Flows


## Microservices Architecture

### Two Core Services with JWT Bridge

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         MARUTIXCHANGE PLATFORM                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────┐                                  │
│  │      CLIENT LAYER                │                                  │
│  │  (Web / Mobile / API Client)    │                                  │
│  └────────────────┬─────────────────┘                                  │
│                   │                                                    │
│                   │                                                    │
│    ┌──────────────┴──────────────┐                                    │
│    │                             │                                    │
│    ▼                             ▼                                    │
│
│ ┌─────────────────────────────┐   ┌─────────────────────────────┐    │
│ │   USER SERVICE              │   │   BIDDING SERVICE           │    │
│ │   Port: 8081                │   │   Port: 8080                │    │
│ ├─────────────────────────────┤   ├─────────────────────────────┤    │
│ │                             │   │                             │    │
│ │ • Registration              │   │ • Auction Management        │    │
│ │ • Authentication            │   │ • Bid Processing            │    │
│ │ • JWT Generation ✓ FIXED   │   │ • JWT Validation ✓          │    │
│ │   (includes userId)         │   │ • Business Rules            │    │
│ │ • Login                     │   │ • Chat & Messaging          │    │
│ │ • User Profile Management   │   │ • Test Drive Booking        │    │
│ │ • Token Refresh             │   │ • Bid History               │    │
│ │                             │   │ • Auction Status Mgmt       │    │
│ └────────┬────────────────────┘   └────────┬────────────────────┘    │
│          │                                  │                         │
│          │ ← JWT Token Generated            │                         │
│          │   (with userId ✓)                │                         │
│          │                                  │                         │
│          └──────────────┬───────────────────┘                         │
│                         │                                             │
│          JWT Token with userId Passed in Authorization Header →      │
│                         │                                             │
└─────────────────────────┼─────────────────────────────────────────────┘
                          │
                          ▼ (Both services share same JWT validation)
                   
                    ┌──────────────────┐
                    │ JWT Token Payload│
                    ├──────────────────┤
                    │ userId: 12345 ✓  │
                    │ email: user@x    │
                    │ iat: timestamp   │
                    │ exp: timestamp   │
                    └──────────────────┘
```

---

## System Layered Architecture

### Complete Stack with JWT Security Integration

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                     │
│                  (Web Browser, Mobile App, API Client)                  │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                      HTTP Request with JWT
                         (in Authorization header)
                                 │
                                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                   🔐 SECURITY & GATEWAY LAYER                            │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │              Spring Security Filter Chain                          │ │
│  ├────────────────────────────────────────────────────────────────────┤ │
│  │                                                                    │ │
│  │  1️⃣ JwtAuthFilter (BEFORE UsernamePasswordAuthenticationFilter)  │ │
│  │     ────────────────────────────────────────────────────────    │ │
│  │     ✓ Extract JWT from Authorization header                      │ │
│  │     ✓ Validate token signature                                   │ │
│  │     ✓ Check token expiry                                         │ │
│  │     ✓ Extract userId from token ✓ CRITICAL FIX                  │ │
│  │     ✓ Set request.setAttribute("userId", userId)                │ │
│  │     ✓ Pass to next filter in chain                               │ │
│  │                                                                    │ │
│  │  2️⃣ Stateless Session Configuration                              │ │
│  │     ────────────────────────────────────────────────────────    │ │
│  │     ✓ SessionCreationPolicy.STATELESS                           │ │
│  │     ✓ No server-side sessions                                    │ │
│  │     ✓ No cookie dependency                                       │ │
│  │     ✓ Pure token-based authentication                            │ │
│  │                                                                    │ │
│  │  3️⃣ Endpoint Authorization Rules                                 │ │
│  │     ────────────────────────────────────────────────────────    │ │
│  │     📂 PUBLIC Endpoints (No JWT Required):                       │ │
│  │        ✓ /api/v1/users/register                                 │ │
│  │        ✓ /api/v1/users/login                                    │ │
│  │        ✓ /swagger-ui.html                                        │ │
│  │        ✓ /actuator/health                                        │ │
│  │                                                                    │ │
│  │     🔒 PROTECTED Endpoints (JWT Required):                       │ │
│  │        ✓ /api/v1/bids/**                                         │ │
│  │        ✓ /api/v1/auctions/** (POST/PUT/DELETE)                 │ │
│  │        ✓ /api/v1/chat/**                                         │ │
│  │        ✓ /api/v1/test-drives/**                                 │ │
│  │        ✓ /api/v1/users/{id} (PUT/DELETE)                       │ │
│  │                                                                    │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ✓ Request authenticated with userId extracted                        │
│  ✓ Ready for business logic layer                                     │
└────────────────┬─────────────────────────────────────────────────────────┘
                 │
                 ▼ (userId available in request)
┌──────────────────────────────────────────────────────────────────────────┐
│           PRESENTATION LAYER (Controller Layer)                         │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌─────────────┐  │
│  │ AuctionController    │  │ BidController        │  │ ChatControl │  │
│  ├──────────────────────┤  ├──────────────────────┤  ├─────────────┤  │
│  │ @PostMapping         │  │ @PostMapping         │  │ @PostMap    │  │
│  │ /api/v1/auctions    │  │ /api/v1/bids         │  │ /api/v1/    │  │
│  │                      │  │                      │  │ chat        │  │
│  │ @RequestAttribute    │  │ @RequestAttribute    │  │             │  │
│  │ ("userId") Long      │  │ ("userId") Long      │  │ @RequestAt  │  │
│  │ userId  ✓ FIXED      │  │ userId  ✓ FIXED      │  │ tribute     │  │
│  │                      │  │                      │  │             │  │
│  │ • create()           │  │ • placeBid()         │  │ • send()    │  │
│  │ • getAuctions()      │  │ • getAuctionBids()   │  │ • get()     │  │
│  │ • updateStatus()     │  │ • getUserBids()      │  │             │  │
│  └──────────────────────┘  └──────────────────────┘  └─────────────┘  │
│                                                                          │
└────────────────┬──────────────────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│          BUSINESS LOGIC LAYER (Service & Rule Engine Layer)             │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌─────────────┐  │
│  │ AuctionService       │  │ BidService           │  │ ChatService │  │
│  │ (Interface)          │  │ (Interface)          │  │ (Interface) │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └──────┬──────┘  │
│             │                         │                      │         │
│             ▼                         ▼                      ▼         │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌─────────────┐  │
│  │ AuctionServiceImpl    │  │ BidServiceImpl        │  │ ChatServiceIm│ │
│  ├──────────────────────┤  ├──────────────────────┤  ├─────────────┤  │
│  │ • Auction logic      │  │ • Bid validation     │  │ • Message   │  │
│  │ • Status management  │  │ • Business rules     │  │   logic     │  │
│  │ • Price management   │  │ • RuleEngine call    │  │ • User      │  │
│  │ • Time validation    │  │ • User eligibility   │  │   blocking  │  │
│  │ ✓ Enhanced check     │  │ • Auction validation │  │             │  │
│  │   (ACTIVE + time)    │  │ ✓ Enhanced check     │  │             │  │
│  │                      │  │   (amount + status)  │  │             │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └──────┬──────┘  │
│             │                         │                      │         │
│             └─────────────────────┬───┴──────────────────────┘         │
│                                   │                                    │
│                   ┌───────────────▼────────────────┐                  │
│                   │  🎯 BiddingRuleEngine          │                  │
│                   │  (Centralized Validation)      │                  │
│                   ├────────────────────────────────┤                  │
│                   │ ✓ validateAuction()            │                  │
│                   │   • Check status == ACTIVE     │                  │
│                   │   • Check endTime > now        │                  │
│                   │   • Check startPrice valid     │                  │
│                   │                                │                  │
│                   │ ✓ validateBid()                │                  │
│                   │   • Check minimum bid amount   │                  │
│                   │   • Check > highest bid        │                  │
│                   │   • Check bidder ≠ seller      │                  │
│                   │   • Check user is active       │                  │
│                   │                                │                  │
│                   │ • applyPenalties()             │                  │
│                   │ • applyDiscounts()             │                  │
│                   │ • evaluateRules()              │                  │
│                   └────────────────────────────────┘                  │
│                                                                          │
└────────────────┬──────────────────────────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│        DATA ACCESS LAYER (Repository / JPA Layer)                       │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌─────────────┐  │
│  │ AuctionRepository    │  │ BidRepository        │  │ ChatReposit │  │
│  │ (JPA)                │  │ (JPA)                │  │ (JPA)       │  │
│  │                      │  │                      │  │             │  │
│  │ • findById()         │  │ • findById()         │  │ • save()    │  │
│  │ • findActive()       │  │ • findByAuction()    │  │ • find()    │  │
│  │ • save()             │  │ • findByUser()       │  │             │  │
│  │ • update()           │  │ • getHighestBid()    │  │             │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └──────┬──────┘  │
│             │                         │                      │         │
│             └─────────────────────┬────┴──────────────────────┘         │
│                                   │                                    │
│                    ORM (Hibernate / JPA)                               │
│                    Connection Pooling (HikariCP)                       │
│                                   │                                    │
└────────────────┬──────────────────┼──────────────────────────────────────┘
                 │                  │
                 ▼                  ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER (MySQL)                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ users table  │  │ auctions tbl │  │ bids table   │  │ chat_msgs  │ │
│  ├──────────────┤  ├──────────────┤  ├──────────────┤  ├────────────┤ │
│  │ user_id (PK) │  │ auction_id   │  │ bid_id (PK)  │  │ msg_id (PK)│ │
│  │ username     │  │ title        │  │ auction_id   │  │ auction_id │ │
│  │ email        │  │ start_price  │  │ user_id ✓    │  │ from_user  │ │
│  │ password     │  │ end_time     │  │ bid_amount   │  │ message    │ │
│  │ status       │  │ status       │  │ bid_time     │  │ created_at │ │
│  │ created_at   │  │ created_by   │  │ status       │  │            │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └────────────┘ │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## JWT Token Structure & Flow

### Token Anatomy (Fixed in v2.0)

```
┌──────────────────────────────────────────────────────────────────────────┐
│                      JWT TOKEN STRUCTURE                                 │
│               (With userId - CRITICAL FIX in v2.0)                      │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  THREE PARTS (separated by dots):                                       │
│  ─────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  1️⃣  HEADER                                                             │
│  ┌────────────────────────────┐                                         │
│  │ {                          │                                         │
│  │   "alg": "HS256",          │                                         │
│  │   "typ": "JWT"             │                                         │
│  │ }                          │                                         │
│  └────────────────────────────┘                                         │
│         ⬇ Base64URL Encoded                                             │
│  eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9                                    │
│                                                                          │
│                                                                          │
│  2️⃣  PAYLOAD ✓ FIXED IN V2.0                                           │
│  ┌────────────────────────────┐                                         │
│  │ {                          │                                         │
│  │   "userId": 12345,    ◄─── NOW INCLUDED!                           │
│  │   "email": "buyer@x", │     (was MISSING before)                   │
│  │   "iat": 1704067200,  │                                             │
│  │   "exp": 1704153600   │                                             │
│  │ }                      │                                             │
│  └────────────────────────────┘                                         │
│         ⬇ Base64URL Encoded                                             │
│  eyJ1c2VySWQiOjEyMzQ1LCJlbWFpbCI6ImJ1eWVyQHgiLCJpYXQiOjE3MDQwNjcyMDB9   │
│                                                                          │
│                                                                          │
│  3️⃣  SIGNATURE                                                          │
│  ┌────────────────────────────────────────────────────┐                │
│  │ HMAC256(                                           │                │
│  │   base64(header) + "." + base64(payload),          │                │
│  │   SECRET_KEY                                       │                │
│  │ )                                                  │                │
│  └────────────────────────────────────────────────────┘                │
│         ⬇ Base64URL Encoded                                             │
│  SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c                            │
│                                                                          │
│                                                                          │
│  ✅ FINAL TOKEN (used in requests)                                      │
│  ──────────────────────────────────────────────────────────────────────  │
│  eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.                                  │
│  eyJ1c2VySWQiOjEyMzQ1LCJlbWFpbCI6ImJ1eWVyQHgiLCJpYXQiOjE3MDQwNjcyMDB9. │
│  SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c                            │
│                                                                          │
│  📌 Usage in Request:                                                   │
│  Authorization: Bearer [TOKEN_HERE]                                     │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Authentication & Security Architecture

### JWT Generation & Validation Flow

```
╔══════════════════════════════════════════════════════════════════════════╗
║                 PHASE 1: TOKEN GENERATION (User Service)                 ║
╚══════════════════════════════════════════════════════════════════════════╝

CLIENT                       USER SERVICE
  │                              │
  │  1. POST /api/v1/users/login │
  │     {email, password}         │
  ├─────────────────────────────►│
  │                              │
  │                         2. Validate credentials
  │                            • Check password
  │                            • Load user from DB
  │                              │
  │                         3. Extract user data
  │                            • userId = 12345
  │                            • email = buyer@x.com
  │                              │
  │                         4. Generate JWT
  │                            ┌─────────────────────┐
  │                            │ generateToken()     │
  │                            │ Input:              │
  │                            │  • userId: 12345    │
  │                            │  • email: buyer@x   │
  │                            │                     │
  │                            │ Output:             │
  │                            │ JWT with payload:   │
  │                            │ {                   │
  │                            │  userId: 12345  ✓  │
  │                            │  email: buyer@x │   │
  │                            │  iat, exp       │   │
  │                            │ }                   │
  │                            └─────────────────────┘
  │                              │
  │  5. HTTP 200 OK              │
  │  {                           │
  │   "token": "eyJ...",  ◄─────►│
  │   "userId": 12345,           │
  │   "email": "buyer@x.com"     │
  │  }                           │
  │◄─────────────────────────────┤
  │                              │
  │ ✓ Token stored in
  │   localStorage/sessionStorage
  │                              │


╔══════════════════════════════════════════════════════════════════════════╗
║             PHASE 2: TOKEN VALIDATION (Bidding Service)                  ║
╚══════════════════════════════════════════════════════════════════════════╝

CLIENT (with stored JWT)         SPRING SECURITY              CONTROLLER
  │                                  │                           │
  │ 1. POST /api/v1/bids             │                           │
  │    Header: Authorization:        │                           │
  │    Bearer eyJ...                 │                           │
  │    Body: {auctionId, bidAmount}  │                           │
  ├──────────────────────────────────►│                           │
  │                                  │                           │
  │                            2. Extract token from header      │
  │                               token = "eyJ..."               │
  │                                  │                           │
  │                            3. Validate signature ✓           │
  │                               • Check HMAC256 matches        │
  │                                  │                           │
  │                            4. Check expiry ✓                │
  │                               • now < exp_time               │
  │                                  │                           │
  │                            5. Extract userId ✓ CRITICAL FIX │
  │                               userId = claims.get("userId")  │
  │                               Result: 12345 ✓                │
  │                                  │                           │
  │                            6. Set request attribute          │
  │                               request.setAttribute(          │
  │                                 "userId", 12345)             │
  │                                  │                           │
  │                                  │ Forward request            │
  │                                  ├──────────────────────────►│
  │                                  │                           │
  │                                  │ BidController.placeBid()  │
  │                                  │ @RequestAttribute(        │
  │                                  │   "userId") Long userId   │
  │                                  │ Value: 12345 ✓            │
  │                                  │                           │
  │                                  │ Process request with
  │                                  │ known userId
  │                                  │                           │
  │  7. HTTP 201 Created             │                           │
  │  {                               │                           │
  │   "bidId": 789,        ◄─────────┴───────────────────────────┤
  │   "userId": 12345,                                           │
  │   "status": "ACCEPTED"                                       │
  │  }                                                           │
  │◄──────────────────────────────────────────────────────────────┤
  │                                                              │
  │ ✓ Bid placed with correct userId!                           │
  │                                                              │
```

---

## Inter-Service Communication

### User Service → Bidding Service JWT Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                  SERVICE COMMUNICATION ARCHITECTURE                      │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                      USER SERVICE (Port 8081)                      │ │
│  ├────────────────────────────────────────────────────────────────────┤ │
│  │                                                                    │ │
│  │  Responsibilities:                                                 │ │
│  │  ────────────────                                                  │ │
│  │  ✓ User registration & management                                 │ │
│  │  ✓ Authentication (verify credentials)                            │ │
│  │  ✓ JWT generation ✓ INCLUDES userId                              │ │
│  │  ✓ Token validation utility                                       │ │
│  │  ✓ User profile management                                        │ │
│  │                                                                    │ │
│  │  Public Endpoints:                                                 │ │
│  │  ──────────────────                                                │ │
│  │  • POST   /api/v1/users/register                                  │ │
│  │  • POST   /api/v1/users/login  ◄──── Returns JWT with userId    │ │
│  │  • GET    /api/v1/users/{id}                                      │ │
│  │  • GET    /api/v1/users/profile/me                                │ │
│  │                                                                    │ │
│  │  Protected Endpoints:                                              │ │
│  │  ────────────────────                                              │ │
│  │  • PUT    /api/v1/users/{id}  (requires JWT)                      │ │
│  │  • DELETE /api/v1/users/{id}  (requires JWT)                      │ │
│  │  • POST   /api/v1/users/verify-token                              │ │
│  │                                                                    │ │
│  │  Key Output: JWT Token                                             │ │
│  │  ────────────────────────                                          │ │
│  │  {                                                                  │ │
│  │    "token": "eyJ...",                                              │ │
│  │    "userId": 12345,     ◄──── PROVIDED TO FRONTEND               │ │
│  │    "email": "user@x.com",                                          │ │
│  │    "expiresIn": 86400                                              │ │
│  │  }                                                                  │ │
│  │                                                                    │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                         │                                              │
│                         │ JWT Token Generated                         │
│                         │ (includes userId)                           │
│                         ▼                                              │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    BIDDING SERVICE (Port 8080)                     │ │
│  ├────────────────────────────────────────────────────────────────────┤ │
│  │                                                                    │ │
│  │  Responsibilities:                                                 │ │
│  │  ────────────────                                                  │ │
│  │  ✓ JWT validation (via JwtAuthFilter)                             │ │
│  │  ✓ Extract userId from JWT ✓ CRITICAL                            │ │
│  │  ✓ Auction management                                              │ │
│  │  ✓ Bid processing with validated userId                           │ │
│  │  ✓ Business rules validation                                       │ │
│  │  ✓ Chat & messaging                                                │ │
│  │  ✓ Test drive booking                                              │ │
│  │                                                                    │ │
│  │  Public Endpoints:                                                 │ │
│  │  ──────────────────                                                │ │
│  │  • GET    /api/v1/auctions                                        │ │
│  │  • GET    /api/v1/auctions/{id}                                   │ │
│  │  • GET    /api/v1/auctions/{id}/bids                              │ │
│  │                                                                    │ │
│  │  Protected Endpoints (Require JWT):                                │ │
│  │  ────────────────────────────────────                              │ │
│  │  • POST   /api/v1/auctions  ◄──── userId extracted from JWT     │ │
│  │  • PUT    /api/v1/auctions/{id}                                   │ │
│  │  • POST   /api/v1/bids      ◄──── userId extracted from JWT     │ │
│  │  • GET    /api/v1/bids/user/{id}                                  │ │
│  │  • POST   /api/v1/chat      ◄──── userId extracted from JWT     │ │
│  │  • POST   /api/v1/test-drives                                     │ │
│  │                                                                    │ │
│  │  Endpoint Request Example:                                         │ │
│  │  ──────────────────────────                                        │ │
│  │  POST /api/v1/bids                                                │ │
│  │  Header: Authorization: Bearer eyJ...                              │ │
│  │  Body: {                                                           │ │
│  │    "auctionId": 456,                                               │ │
│  │    "bidAmount": 200000                                             │ │
│  │  }                                                                  │ │
│  │                                                                    │ │
│  │  Flow Inside Service:                                              │ │
│  │  ───────────────────                                               │ │
│  │  1. JwtAuthFilter extracts token                                  │ │
│  │  2. Validates signature & expiry ✓                                │ │
│  │  3. Extracts userId from payload ✓ CRITICAL                      │ │
│  │  4. Sets request.setAttribute("userId", userId)                  │ │
│  │  5. Controller receives userId via @RequestAttribute              │ │
│  │  6. Service processes bid with userId                             │ │
│  │  7. Database saves bid with correct userId                        │ │
│  │                                                                    │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    SAME JWT VALIDATES IN BOTH                      │ │
│  │  User Service generates → Bidding Service validates                │ │
│  │  Both use same SECRET_KEY for HMAC256 signature verification      │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Bug Fix Journey - Before vs After

### The Critical Bug: userId Missing from Token

```
╔══════════════════════════════════════════════════════════════════════════╗
║                    🔴 BEFORE (Version 1.0) - BROKEN                      ║
╚══════════════════════════════════════════════════════════════════════════╝


STEP 1: Token Generation (INCOMPLETE)
──────────────────────────────────────

generateToken(String email)  ◄──── MISSING userId parameter!
│
├─ Claims created: {}
├─ claims.put("email", email)
├─ claims.put("username", username)
│
└─ ✗ NO userId in payload!

Result Token Payload:
{
  "email": "buyer@x.com",
  "username": "john_doe",
  "iat": 1704067200,
  "exp": 1704153600
  // ✗ userId MISSING
}


STEP 2: Token Validation (FAILS)
────────────────────────────────

JwtAuthFilter tries to extract userId:
│
├─ Long userId = claims.get("userId", Long.class)
│
└─ ✗ Returns NULL! (not present in claims)

Result: request.setAttribute("userId", null)  ◄──── NULL STORED


STEP 3: Controller Receives NULL
────────────────────────────────

@RequestAttribute("userId") Long userId  ◄──── Receives NULL
│
└─ bidService.placeBid(null, 456, 150000)
   │
   └─ ✗ NullPointerException!
   
   Logs:
   [ERROR] Cannot process null userId
   [ERROR] HTTP 500 - Internal Server Error
   [ERROR] Request FAILED


STEP 4: System Impact
─────────────────────

✗ Cannot place bids              → BROKEN
✗ Cannot create auctions         → BROKEN
✗ Cannot send messages           → BROKEN
✗ All protected endpoints fail   → BROKEN
✗ System completely non-functional


═══════════════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════════╗
║                   🟢 AFTER (Version 2.0) - FIXED ✓                      ║
╚══════════════════════════════════════════════════════════════════════════╝


STEP 1: Token Generation (COMPLETE) ✓
──────────────────────────────────────

generateToken(Long userId, String email)  ◄──── userId parameter ADDED!
│
├─ Load user from database: User(id=12345, email=buyer@x.com)
├─ Extract userId = 12345
│
├─ Claims created: {}
├─ claims.put("userId", 12345)  ◄──── ✓ CRITICAL FIX
├─ claims.put("email", email)
├─ claims.put("username", username)
│
└─ ✓ userId NOW IN PAYLOAD!

Result Token Payload:
{
  "userId": 12345,          ◄──── ✓ NOW PRESENT
  "email": "buyer@x.com",
  "username": "john_doe",
  "iat": 1704067200,
  "exp": 1704153600
}


STEP 2: Token Validation (SUCCESS) ✓
────────────────────────────────────

JwtAuthFilter extracts userId:
│
├─ Long userId = claims.get("userId", Long.class)
│
└─ ✓ Returns 12345! (present in claims)

Result: request.setAttribute("userId", 12345)  ◄──── 12345 STORED


STEP 3: Controller Receives Correct Value
──────────────────────────────────────────

@RequestAttribute("userId") Long userId  ◄──── Receives 12345
│
└─ bidService.placeBid(12345, 456, 150000)
   │
   └─ ✓ SUCCESS!
   
   Logs:
   [INFO] Processing bid for user 12345
   [INFO] HTTP 201 - Created
   [INFO] Request SUCCESSFUL


STEP 4: System Impact
─────────────────────

✓ Can place bids                    → WORKING
✓ Can create auctions              → WORKING
✓ Can send messages                → WORKING
✓ All protected endpoints function → WORKING
✓ System fully operational


═══════════════════════════════════════════════════════════════════════════════


COMPARISON TABLE
════════════════════════════════════════════════════════════════════════════

Aspect                    │  BEFORE (v1.0)           │  AFTER (v2.0) ✓
──────────────────────────┼──────────────────────────┼──────────────────────
Token generation method   │  generateToken(email)    │  generateToken(userId, email)
JWT payload               │  {email, username}       │  {userId, email, username}
userId in claims          │  ✗ MISSING              │  ✓ PRESENT
Controller receives userId│  NULL                    │  12345
Bid placement             │  ✗ FAILS (500 error)    │  ✓ WORKS (201 created)
System status             │  ✗ BROKEN               │  ✓ OPERATIONAL

```

---

## Complete Flow: Register → Login → Bid

### End-to-End User Journey

```
┌──────────────────────────────────────────────────────────────────────────┐
│                     COMPLETE USER JOURNEY FLOW                          │
└──────────────────────────────────────────────────────────────────────────┘


STEP 1️⃣: USER REGISTRATION
═════════════════════════════════════════════════════════════════════════════

CLIENT                          USER SERVICE                    DATABASE
  │                                │                               │
  │ POST /api/v1/users/register   │                               │
  │ {                             │                               │
  │   "email": "buyer@x.com",     │                               │
  │   "password": "pass123",      │                               │
  │   "name": "John Doe"          │                               │
  │ }                             │                               │
  ├────────────────────────────────►                              │
  │                           Validate input                       │
  │                           ✓ Email format                       │
  │                           ✓ Password strength                  │
  │                                │                               │
  │                           Hash password (BCrypt)               │
  │                                │                               │
  │                           Create User entity                  │
  │                                │                               │
  │                                │ INSERT User                   │
  │                                ├──────────────────────────────►│
  │                                │                               │
  │                                │ ✓ user_id=12345 (auto-inc)   │
  │                                │◄──────────────────────────────┤
  │                                │                               │
  │ HTTP 201 Created               │                               │
  │ {                              │                               │
  │   "userId": 12345,             │                               │
  │   "email": "buyer@x.com",      │                               │
  │   "status": "ACTIVE"           │                               │
  │ }                              │                               │
  │◄────────────────────────────────                              │
  │                                                                │
  │ ✓ Account created!                                            │
  │                                                                │


STEP 2️⃣: USER LOGIN (Gets JWT Token)
═════════════════════════════════════════════════════════════════════════════

CLIENT                          USER SERVICE                    DATABASE
  │                                │                               │
  │ POST /api/v1/users/login       │                               │
  │ {                              │                               │
  │   "email": "buyer@x.com",      │                               │
  │   "password": "pass123"        │                               │
  │ }                              │                               │
  ├────────────────────────────────►                              │
  │                                │                               │
  │                           Load user by email                  │
  │                                │                               │
  │                                │ SELECT * FROM users
  │                                │ WHERE email = 'buyer@x.com'
  │                                ├──────────────────────────────►│
  │                                │                               │
  │                                │ User(id=12345, email=...)    │
  │                                │◄──────────────────────────────┤
  │                                │                               │
  │                           Verify password                      │
  │                           ✓ BCrypt matches                     │
  │                                │                               │
  │                           Generate JWT Token
  │                           ┌─────────────────────────────┐     │
  │                           │ generateToken(             │     │
  │                           │   userId=12345,            │     │
  │                           │   email=buyer@x.com        │     │
  │                           │ )                          │     │
  │                           │                            │     │
  │                           │ Creates JWT with payload:  │     │
  │                           │ {                          │     │
  │                           │   userId: 12345,  ✓        │     │
  │                           │   email: buyer@x.com,      │     │
  │                           │   iat: 1704067200,         │     │
  │                           │   exp: 1704153600 (+24hrs) │     │
  │                           │ }                          │     │
  │                           │                            │     │
  │                           │ Signed with HS256          │     │
  │                           └─────────────────────────────┘     │
  │                                │                               │
  │ HTTP 200 OK                    │                               │
  │ {                              │                               │
  │   "token": "eyJ...",  ◄────────┤────────────────────           │
  │   "userId": 12345,             │                               │
  │   "email": "buyer@x.com",      │                               │
  │   "expiresIn": 86400           │                               │
  │ }                              │                               │
  │◄────────────────────────────────                              │
  │                                                                │
  │ ✓ Logged in!                                                  │
  │ ✓ Token stored in localStorage                               │
  │                                                                │


STEP 3️⃣: CREATE AUCTION (with JWT)
═════════════════════════════════════════════════════════════════════════════

CLIENT (with JWT)              SPRING SECURITY          CONTROLLER  DATABASE
  │                                │                        │           │
  │ POST /api/v1/auctions         │                        │           │
  │ Header: Authorization:         │                        │           │
  │ Bearer eyJ...                  │                        │           │
  │ Body:                          │                        │           │
  │ {                              │                        │           │
  │   "title": "Maruti Swift",     │                        │           │
  │   "startPrice": 150000,        │                        │           │
  │   ...                          │                        │           │
  │ }                              │                        │           │
  ├───────────────────────────────►│                        │           │
  │                                │                        │           │
  │                           JwtAuthFilter:               │           │
  │                           ✓ Extract token from header  │           │
  │                           ✓ Validate signature         │           │
  │                           ✓ Check expiry               │           │
  │                           ✓ Extract userId=12345       │           │
  │                           ✓ request.setAttribute(      │           │
  │                             "userId", 12345)           │           │
  │                                │                        │           │
  │                                ├───────────────────────►│           │
  │                                │         Process
  │                                │         (userId avail.)
  │                                │         │
  │                                │         │ Validate input
  │                                │         │ Create Auction
  │                                │         │ sellerId=12345
  │                                │         │
  │                                │         ├──────────────┐
  │                                │         │              │
  │                                │         │ INSERT      │
  │                                │         ├──────────────►│
  │                                │         │              │
  │                                │         │ auction_id   │
  │                                │         │ =456 (auto)  │
  │                                │         │◄──────────────┤
  │                                │         │              │
  │ HTTP 201 Created               │                        │           │
  │ {                              │                        │           │
  │   "auctionId": 456,            │                        │           │
  │   "title": "Maruti Swift",     │                        │           │
  │   "sellerId": 12345,           │                        │           │
  │   "status": "ACTIVE"           │                        │           │
  │ }                              │                        │           │
  │◄──────────────────────────────────────────────────────────────────┤
  │                                                                    │
  │ ✓ Auction created!                                                │
  │                                                                    │


STEP 4️⃣: DIFFERENT USER PLACES BID (with different JWT)
═════════════════════════════════════════════════════════════════════════════

(Another user: bidder@x.com with userId=99999)

CLIENT (BIDDER)                SPRING SECURITY          CONTROLLER  DATABASE
with JWT for userid=99999           │                        │           │
  │                                │                        │           │
  │ POST /api/v1/bids             │                        │           │
  │ Header: Authorization:         │                        │           │
  │ Bearer eyJ... (different JWT)  │                        │           │
  │ Body:                          │                        │           │
  │ {                              │                        │           │
  │   "auctionId": 456,            │                        │           │
  │   "bidAmount": 200000          │                        │           │
  │ }                              │                        │           │
  ├───────────────────────────────►│                        │           │
  │                                │                        │           │
  │                           JwtAuthFilter:               │           │
  │                           ✓ Extract different token    │           │
  │                           ✓ Validate signature         │           │
  │                           ✓ Check expiry               │           │
  │                           ✓ Extract userId=99999       │           │
  │                             (DIFFERENT USER!) ✓        │           │
  │                           ✓ request.setAttribute(      │           │
  │                             "userId", 99999)           │           │
  │                                │                        │           │
  │                                ├───────────────────────►│           │
  │                                │         Process
  │                                │         (userId=99999)
  │                                │         │
  │                                │         │ Validate:
  │                                │         │ • Fetch auction 456
  │                                │         │ • Check ACTIVE ✓
  │                                │         │ • Check time ✓
  │                                │         │ • Check amount ✓
  │                                │         │ • bidder ≠ seller ✓
  │                                │         │   (99999 ≠ 12345)
  │                                │         │
  │                                │         │ Create Bid:
  │                                │         │ bid {
  │                                │         │   auction_id: 456,
  │                                │         │   user_id: 99999,  ✓
  │                                │         │   bid_amount: 200k,
  │                                │         │   status: ACCEPTED
  │                                │         │ }
  │                                │         │
  │                                │         ├──────────────┐
  │                                │         │              │
  │                                │         │ INSERT       │
  │                                │         ├──────────────►│
  │                                │         │              │
  │                                │         │ bid_id=789   │
  │                                │         │◄──────────────┤
  │                                │         │              │
  │ HTTP 201 Created               │                        │           │
  │ {                              │                        │           │
  │   "bidId": 789,                │                        │           │
  │   "auctionId": 456,            │                        │           │
  │   "userId": 99999,    ✓        │                        │           │
  │   "amount": 200000,            │                        │           │
  │   "status": "ACCEPTED"         │                        │           │
  │ }                              │                        │           │
  │◄──────────────────────────────────────────────────────────────────┤
  │                                                                    │
  │ ✓ Bid placed with CORRECT userId!                                │
  │                                                                    │


╔══════════════════════════════════════════════════════════════════════════╗
║                        ✓ FLOW COMPLETE & SUCCESSFUL                     ║
║                                                                          ║
║  Key Points:                                                             ║
║  • Each user has their own JWT with their own userId                    ║
║  • JWT properly carries userId through the flow                          ║
║  • Different users place bids with correct userId tracking              ║
║  • System fully operational ✓                                            ║
╚══════════════════════════════════════════════════════════════════════════╝
```

---

## Security Configuration Architecture

### Spring Security Filter Chain & Endpoints

```
┌──────────────────────────────────────────────────────────────────────────┐
│              SPRING SECURITY CONFIGURATION ARCHITECTURE                  │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  📍 FILTER CHAIN EXECUTION ORDER                                        │
│  ═══════════════════════════════════════════════════════════════════    │
│                                                                          │
│          Request comes in                                               │
│               │                                                         │
│               ▼                                                         │
│     ┌─────────────────────────────────────────┐                        │
│     │  1. SecurityContextPersistenceFilter    │                        │
│     │     (Load/Save security context)        │                        │
│     └──────────────┬──────────────────────────┘                        │
│                    │                                                    │
│                    ▼                                                    │
│     ┌─────────────────────────────────────────┐                        │
│     │  2. LogoutFilter                        │                        │
│     │     (Handle logout requests)            │                        │
│     └──────────────┬──────────────────────────┘                        │
│                    │                                                    │
│                    ▼                                                    │
│     ┌─────────────────────────────────────────┐                        │
│     │  3. 🔐 JwtAuthFilter  ◄──── OUR FILTER │                        │
│     │     ✓ Extract JWT token                │                        │
│     │     ✓ Validate signature                │                        │
│     │     ✓ Check expiry                     │                        │
│     │     ✓ Extract userId                    │                        │
│     │     ✓ Set request attribute             │                        │
│     └──────────────┬──────────────────────────┘                        │
│                    │                                                    │
│                    ▼                                                    │
│     ┌─────────────────────────────────────────┐                        │
│     │  4. UsernamePasswordAuthenticationFilter│                        │
│     │     (Basic auth - not used in JWT flow) │                        │
│     └──────────────┬──────────────────────────┘                        │
│                    │                                                    │
│                    ▼                                                    │
│     ┌─────────────────────────────────────────┐                        │
│     │  5. RequestCacheAwareFilter             │                        │
│     │     (Handle saved requests)             │                        │
│     └──────────────┬──────────────────────────┘                        │
│                    │                                                    │
│                    ▼                                                    │
│              ... (other filters)                                        │
│                    │                                                    │
│                    ▼                                                    │
│     ┌─────────────────────────────────────────┐                        │
│     │  N. FilterSecurityInterceptor           │                        │
│     │     (Final authorization check)         │                        │
│     │     → Check endpoint rules              │                        │
│     └──────────────┬──────────────────────────┘                        │
│                    │                                                    │
│                    ▼                                                    │
│          Controller / Handler                                           │
│                                                                          │
│                                                                          │
│  🔒 ENDPOINT AUTHORIZATION RULES                                        │
│  ══════════════════════════════════════════════════════════════════    │
│                                                                          │
│  PUBLIC ENDPOINTS (permitAll)                                           │
│  ──────────────────────────────────────────────────────────────        │
│  No JWT required:                                                       │
│                                                                          │
│  ✓ POST   /api/v1/users/register                                       │
│  ✓ POST   /api/v1/users/login                                          │
│  ✓ GET    /api/v1/users/{id}                                           │
│  ✓ GET    /api/v1/auctions                                             │
│  ✓ GET    /api/v1/auctions/{id}                                        │
│  ✓ GET    /api/v1/auctions/{id}/bids                                   │
│  ✓ GET    /swagger-ui.html                                             │
│  ✓ GET    /v3/api-docs/**                                              │
│  ✓ GET    /actuator/health                                             │
│                                                                          │
│                                                                          │
│  PROTECTED ENDPOINTS (authenticated)                                    │
│  ────────────────────────────────────────────────────────────────      │
│  JWT Required via Authorization header:                                 │
│                                                                          │
│  🔒 POST   /api/v1/auctions                                             │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     ├─ Verify user is authenticated                                     │
│     └─ Extract userId from JWT                                          │
│                                                                          │
│  🔒 PUT    /api/v1/auctions/{id}                                        │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     └─ Verify ownership (userId == seller)                              │
│                                                                          │
│  🔒 POST   /api/v1/bids                                                 │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     ├─ Validate auction is ACTIVE                                       │
│     ├─ Validate auction time valid                                      │
│     └─ Process bid with userId                                          │
│                                                                          │
│  🔒 GET    /api/v1/bids/user/{id}                                       │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     └─ Return user's bids                                               │
│                                                                          │
│  🔒 POST   /api/v1/chat                                                 │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     └─ Save message with userId                                         │
│                                                                          │
│  🔒 POST   /api/v1/test-drives                                          │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     └─ Book test drive for user                                         │
│                                                                          │
│  🔒 PUT    /api/v1/users/{id}                                           │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     └─ Verify ownership (userId == {id})                                │
│                                                                          │
│  🔒 DELETE /api/v1/users/{id}                                           │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     └─ Verify ownership or admin                                        │
│                                                                          │
│  🔒 POST   /api/v1/users/verify-token                                   │
│     ├─ @RequestAttribute("userId") Long userId ✓                       │
│     └─ Verify token still valid                                         │
│                                                                          │
│                                                                          │
│  STATELESS SESSION CONFIGURATION                                        │
│  ──────────────────────────────────────────────────────────────        │
│  ✓ SessionCreationPolicy.STATELESS                                      │
│    └─ Never create server-side sessions                                │
│                                                                          │
│  ✓ No JSESSIONID cookies                                                │
│    └─ Pure JWT-based authentication                                     │
│                                                                          │
│  ✓ No session affinity required                                         │
│    └─ Can distribute across multiple instances                          │
│                                                                          │
│  ✓ Each request is independent                                          │
│    └─ JWT contains all needed info                                      │
│                                                                          │
│                                                                          │
│  CORS CONFIGURATION                                                     │
│  ────────────────────────────────────────────────────────────────      │
│  ✓ Enabled for all endpoints                                            │
│  ✓ Allow Authorization header in requests                               │
│  ✓ Allow credentials if needed                                          │
│                                                                          │
│                                                                          │
│  EXCEPTION HANDLING                                                     │
│  ────────────────────────────────────────────────────────────────      │
│  ✓ JwtAuthenticationEntryPoint                                          │
│    └─ Handle 401 (Unauthorized)                                         │
│                                                                          │
│  ✓ AccessDeniedException                                                │
│    └─ Handle 403 (Forbidden)                                            │
│                                                                          │
│  ✓ JWT Exceptions                                                       │
│    └─ Invalid/Expired tokens → 401                                      │
│                                                                          │
│  ✓ Custom Exceptions                                                    │
│    └─ Business logic errors → 400/404/500                               │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## API Endpoints Structure

### Complete API Reference

```
┌──────────────────────────────────────────────────────────────────────────┐
│                     API ENDPOINTS ARCHITECTURE                           │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  BASE URL: http://localhost:8080 (Bidding Service)                      │
│            http://localhost:8081 (User Service)                         │
│                                                                          │
│  ╔════════════════════════════════════════════════════════════════════╗ │
│  ║           USER SERVICE ENDPOINTS (Port 8081)                       ║ │
│  ╚════════════════════════════════════════════════════════════════════╝ │
│                                                                          │
│  📝 Authentication Endpoints                                             │
│  ────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  POST /api/v1/users/register
│  Public  │ No JWT Required
│  ┌───────┴─────────────────────────────────────────────────────┐      │
│  │ Purpose: Create new user account                           │      │
│  │ Request Body:                                              │      │
│  │ {                                                          │      │
│  │   "email": "user@example.com",                            │      │
│  │   "password": "securePassword",                           │      │
│  │   "name": "John Doe"                                      │      │
│  │ }                                                          │      │
│  │ Response: User object with userId                         │      │
│  └──────────────────────────────────────────────────────────┘      │
│                                                                          │
│  POST /api/v1/users/login  ◄──── CRITICAL - Returns JWT with userId
│  Public  │ No JWT Required
│  ┌───────┴─────────────────────────────────────────────────────┐      │
│  │ Purpose: Authenticate user and get JWT token              │      │
│  │ Request Body:                                              │      │
│  │ {                                                          │      │
│  │   "email": "user@example.com",                            │      │
│  │   "password": "securePassword"                            │      │
│  │ }                                                          │      │
│  │ Response:                                                  │      │
│  │ {                                                          │      │
│  │   "token": "eyJ...",          ◄─── JWT Token             │      │
│  │   "userId": 12345,            ◄─── userId in token ✓    │      │
│  │   "email": "user@example.com",                            │      │
│  │   "expiresIn": 86400                                      │      │
│  │ }                                                          │      │
│  └──────────────────────────────────────────────────────────┘      │
│                                                                          │
│  GET /api/v1/users/{id}
│  Public  │ No JWT Required
│  ┌───────┴─────────────────────────────────────────────────────┐      │
│  │ Purpose: Get user profile (public info)                   │      │
│  │ Path Param: id = userId                                    │      │
│  │ Response: User object (limited fields)                    │      │
│  └──────────────────────────────────────────────────────────┘      │
│                                                                          │
│  👤 Profile Endpoints                                                    │
│  ────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  GET /api/v1/users/profile/me
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Get current logged-in user's profile            │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Returns: Complete user profile                           │        │
│  │ Uses: @RequestAttribute("userId") to get current user    │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  PUT /api/v1/users/{id}
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Update user profile                             │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Path Param: id = userId (must match JWT userId)         │        │
│  │ Request Body: Updated user data                          │        │
│  │ Uses: @RequestAttribute("userId") for ownership check    │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  DELETE /api/v1/users/{id}
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Delete user account                             │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Path Param: id = userId                                  │        │
│  │ Uses: @RequestAttribute("userId") for verification       │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  🔐 Token Management                                                     │
│  ────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  POST /api/v1/users/verify-token
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Verify token is still valid                     │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Returns: {valid: true, userId: X, expiresIn: Y}         │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│                                                                          │
│  ╔════════════════════════════════════════════════════════════════════╗ │
│  ║        BIDDING SERVICE ENDPOINTS (Port 8080)                       ║ │
│  ╚════════════════════════════════════════════════════════════════════╝ │
│                                                                          │
│  🏷️  Auction Endpoints                                                  │
│  ────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  GET /api/v1/auctions
│  Public  │ No JWT Required
│  ┌───────┴─────────────────────────────────────────────────────┐      │
│  │ Purpose: List all auctions (with pagination & filters)    │      │
│  │ Query Params: page, size, status, sortBy                  │      │
│  │ Returns: Paginated auction list                           │      │
│  └──────────────────────────────────────────────────────────┘      │
│                                                                          │
│  GET /api/v1/auctions/{id}
│  Public  │ No JWT Required
│  ┌───────┴─────────────────────────────────────────────────────┐      │
│  │ Purpose: Get auction details                              │      │
│  │ Path Param: id = auctionId                                │      │
│  │ Returns: Auction object with stats                        │      │
│  └──────────────────────────────────────────────────────────┘      │
│                                                                          │
│  POST /api/v1/auctions  ◄──── Creates auction with userId from JWT
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Create new auction                              │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Request Body:                                             │        │
│  │ {                                                         │        │
│  │   "title": "Maruti Swift 2020",                          │        │
│  │   "description": "Great condition",                      │        │
│  │   "startPrice": 150000,                                  │        │
│  │   "reservePrice": 120000,                                │        │
│  │   "endTime": "2026-04-10T18:00:00",                     │        │
│  │   "category": "Sedan"                                    │        │
│  │ }                                                         │        │
│  │ Uses: @RequestAttribute("userId") → seller_id           │        │
│  │ Creates: Auction with ACTIVE status                      │        │
│  │ Returns: Created auction object (HTTP 201)              │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  PUT /api/v1/auctions/{id}
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Update auction (before it starts accepting bids)│        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Path Param: id = auctionId                               │        │
│  │ Request Body: Updated fields                              │        │
│  │ Uses: @RequestAttribute("userId") to verify ownership    │        │
│  │ Validations: Only update if no bids placed               │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  DELETE /api/v1/auctions/{id}
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Cancel auction                                  │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Path Param: id = auctionId                               │        │
│  │ Uses: @RequestAttribute("userId") to verify ownership    │        │
│  │ Status: Changes to CANCELLED                             │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  💰 Bid Endpoints                                                        │
│  ────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  POST /api/v1/bids  ◄──── Places bid with userId from JWT
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Place a bid on auction                          │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Request Body:                                             │        │
│  │ {                                                         │        │
│  │   "auctionId": 456,                                      │        │
│  │   "bidAmount": 200000                                    │        │
│  │ }                                                         │        │
│  │ Uses: @RequestAttribute("userId") → bidder              │        │
│  │ Validations:                                              │        │
│  │ ✓ Auction exists                                         │        │
│  │ ✓ Auction is ACTIVE                                      │        │
│  │ ✓ Auction not expired (endTime > now)                   │        │
│  │ ✓ Bid amount >= startPrice                              │        │
│  │ ✓ Bid amount > current highest                          │        │
│  │ ✓ Bidder ≠ seller                                        │        │
│  │ ✓ User account is active                                │        │
│  │ Returns: Created bid object (HTTP 201)                  │        │
│  │ Effect: Updates auction's highest bid                    │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  GET /api/v1/bids/auction/{auctionId}
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Get all bids for an auction                     │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Path Param: auctionId = auction ID                       │        │
│  │ Query Params: page, size, sortBy                         │        │
│  │ Returns: List of bids with bid history                   │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  GET /api/v1/bids/user/{userId}
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Get user's bid history                          │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Path Param: userId = user ID                             │        │
│  │ Query Params: page, size, auctionId (filter)             │        │
│  │ Returns: User's bids with auction details                │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  💬 Chat Endpoints                                                       │
│  ────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  POST /api/v1/chat  ◄──── Sends message with userId from JWT
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Send message on auction                         │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Request Body:                                             │        │
│  │ {                                                         │        │
│  │   "auctionId": 456,                                      │        │
│  │   "message": "Is this car still available?"             │        │
│  │ }                                                         │        │
│  │ Uses: @RequestAttribute("userId") → from_user_id        │        │
│  │ Returns: Message object with timestamp (HTTP 201)       │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  GET /api/v1/chat/auction/{auctionId}
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Get chat messages for auction                   │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Path Param: auctionId = auction ID                       │        │
│  │ Query Params: page, size                                 │        │
│  │ Returns: Paginated message list                          │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  🚗 Test Drive Endpoints                                                │
│  ────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  POST /api/v1/test-drives  ◄──── Books drive with userId from JWT
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Book test drive for vehicle                     │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Request Body:                                             │        │
│  │ {                                                         │        │
│  │   "auctionId": 456,                                      │        │
│  │   "scheduledDate": "2026-04-05",                        │        │
│  │   "scheduledTime": "14:00"                              │        │
│  │ }                                                         │        │
│  │ Uses: @RequestAttribute("userId") → booking user        │        │
│  │ Returns: Test drive booking (HTTP 201)                  │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  GET /api/v1/test-drives/user/{userId}
│  Protected │ JWT Required
│  ┌────────┴──────────────────────────────────────────────────┐        │
│  │ Purpose: Get user's test drive bookings                  │        │
│  │ Header: Authorization: Bearer [JWT_TOKEN]               │        │
│  │ Path Param: userId = user ID                             │        │
│  │ Returns: List of test drives                             │        │
│  └───────────────────────────────────────────────────────────┘        │
│                                                                          │
│  🔧 Utility Endpoints                                                    │
│  ────────────────────────────────────────────────────────────────────  │
│                                                                          │
│  GET /swagger-ui.html
│  Public │ No JWT Required
│  Purpose: API documentation (Swagger UI)                             │
│                                                                          │
│  GET /v3/api-docs/**
│  Public │ No JWT Required
│  Purpose: OpenAPI specification                                       │
│                                                                          │
│  GET /actuator/health
│  Public │ No JWT Required
│  Purpose: Health check endpoint                                       │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Database Schema & Relationships

### E-R Diagram with Key Relationships

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    DATABASE SCHEMA & RELATIONSHIPS                       │
└──────────────────────────────────────────────────────────────────────────┘


                    ┌──────────────────────┐
                    │      USERS           │
                    ├──────────────────────┤
                    │ user_id (PK) ◄─────┐ │
                    │ username             │ │
                    │ email (UNIQUE)       │ │
                    │ password_hash        │ │
                    │ phone                │ │
                    │ address              │ │
                    │ status (ACTIVE)      │ │
                    │ verified             │ │
                    │ created_at           │ │
                    │ updated_at           │ │
                    │ last_login           │ │
                    └──────┬───────────────┘ │
                           │                 │
                           │ 1:N             │
                    ┌──────┴──────┐          │
                    │             │          │
                    ▼             ▼          │
         ┌─────────────────┐  ┌─────────────────────────┐
         │   AUCTIONS      │  │      BIDS               │
         ├─────────────────┤  ├─────────────────────────┤
         │ auction_id (PK) │  │ bid_id (PK)             │
         │ title           │  │ auction_id (FK) ────────┼─┐
         │ description     │  │ user_id (FK) ──────────┐├─┤
         │ start_price     │  │ bid_amount              │││
         │ reserve_price   │  │ bid_time                │││
         │ highest_bid     │  │ status                  │││
         │ highest_bidder  │  │ created_at              │││
         │ seller_id (FK)  │┐ │                         │││
         │ status (ACTIVE) │ │ └─────────────────────────┘││
         │ start_time      │ │                             ││
         │ end_time   ✓    │ │ 1:N                         ││
         │ category        │ │                             ││
         │ created_at      │ │                             ││
         │ updated_at      │ │                             ││
         └─────────────────┘ │                             ││
                │            └─────────────────────────────┘│
                │                                           │
                │ 1:N                                       │
                │                                       (from_user_id,to_user_id)
                │                                           │
                ▼                                           │
         ┌─────────────────────────────┐                   │
         │    CHAT MESSAGES            │                   │
         ├─────────────────────────────┤                   │
         │ msg_id (PK)                 │                   │
         │ auction_id (FK) ────────────┼───┐               │
         │ from_user_id (FK) ──────────┼─┐ │               │
         │ to_user_id (FK) ────────────┤ │ │               │
         │ message_text                │ │ │               │
         │ is_read                     │ │ │               │
         │ created_at                  │ │ │               │
         │ updated_at                  │ │ │               │
         └─────────────────────────────┘ │ │               │
                │                         │ │               │
                │ 1:N                     │ │               │
                │                         │ └───────┐       │
                │                         └─────────┼───────┤
                │                                   │       │
                ▼                                   ▼       ▼
         ┌─────────────────────────────┐    ┌──────────────────┐
         │    TEST DRIVES              │    │    Users (refs)  │
         ├─────────────────────────────┤    └──────────────────┘
         │ drive_id (PK)               │
         │ auction_id (FK) ────────────┼─┐
         │ user_id (FK) ───────────────┼─┼─┐
         │ scheduled_date              │ │ │
         │ scheduled_time              │ │ │
         │ status                      │ │ │
         │ notes                       │ │ │
         │ created_at                  │ │ │
         │ updated_at                  │ │ │
         └─────────────────────────────┘ │ │
                │                         │ │
                │ 1:N                     │ │
                │                         │ │
                └─────────────────────────┘ │
                         1:N                │
                                          ┌─┘
                                          │
                                          └─ All FK's point to Users


RELATIONSHIP DEFINITIONS
════════════════════════════════════════════════════════════════════════════

1. Users ──1:N──► Auctions
   ───────────────────────────────────────────────────────
   • One user can create many auctions (seller)
   • Foreign Key: auctions.seller_id → users.user_id
   • Cardinality: 1 user : N auctions
   • Rule: ON DELETE RESTRICT (can't delete user with active auctions)

2. Users ──1:N──► Bids
   ───────────────────────────────────────────────────────
   • One user can place many bids (bidder)
   • Foreign Key: bids.user_id → users.user_id
   • Cardinality: 1 user : N bids
   • Rule: ON DELETE RESTRICT (preserve bid history)

3. Auctions ──1:N──► Bids
   ───────────────────────────────────────────────────────
   • One auction receives many bids
   • Foreign Key: bids.auction_id → auctions.auction_id
   • Cardinality: 1 auction : N bids
   • Rule: ON DELETE CASCADE (delete bids when auction deleted)

4. Users ──1:N──► ChatMessages (as from_user)
   ───────────────────────────────────────────────────────
   • One user sends many messages
   • Foreign Key: chat_messages.from_user_id → users.user_id
   • Cardinality: 1 user : N messages sent

5. Users ──1:N──► ChatMessages (as to_user)
   ───────────────────────────────────────────────────────
   • One user receives many messages
   • Foreign Key: chat_messages.to_user_id → users.user_id
   • Cardinality: 1 user : N messages received

6. Auctions ──1:N──► ChatMessages
   ───────────────────────────────────────────────────────
   • One auction has many messages
   • Foreign Key: chat_messages.auction_id → auctions.auction_id
   • Cardinality: 1 auction : N messages
   • Rule: ON DELETE CASCADE

7. Auctions ──0..1:N──► Highest Bid
   ───────────────────────────────────────────────────────
   • One auction has at most one highest bid (tracked in auction row)
   • Foreign Key: auctions.highest_bidder_id → users.user_id
   • Cardinality: 0 or 1 user is highest bidder
   • Rule: ON DELETE SET NULL (when bidder deleted)

8. Users ──1:N──► TestDrives
   ───────────────────────────────────────────────────────
   • One user books many test drives
   • Foreign Key: test_drives.user_id → users.user_id
   • Cardinality: 1 user : N test drives

9. Auctions ──1:N──► TestDrives
   ───────────────────────────────────────────────────────
   • One auction has many test drive bookings
   • Foreign Key: test_drives.auction_id → auctions.auction_id
   • Cardinality: 1 auction : N test drives
   • Rule: ON DELETE CASCADE


KEY CONSTRAINTS & INDEXES
════════════════════════════════════════════════════════════════════════════

PRIMARY KEYS (PK):
├─ users.user_id              [AUTO_INCREMENT, UNIQUE]
├─ auctions.auction_id        [AUTO_INCREMENT, UNIQUE]
├─ bids.bid_id                [AUTO_INCREMENT, UNIQUE]
├─ chat_messages.msg_id       [AUTO_INCREMENT, UNIQUE]
└─ test_drives.drive_id       [AUTO_INCREMENT, UNIQUE]

UNIQUE CONSTRAINTS:
├─ users.email                [UNIQUE]
└─ Optional: auctions.title   [UNIQUE per seller]

FOREIGN KEYS:
├─ auctions.seller_id         → users.user_id         [ON DELETE RESTRICT]
├─ auctions.highest_bidder_id → users.user_id         [ON DELETE SET NULL]
├─ bids.auction_id            → auctions.auction_id   [ON DELETE CASCADE]
├─ bids.user_id               → users.user_id         [ON DELETE RESTRICT]
├─ chat_messages.auction_id   → auctions.auction_id   [ON DELETE CASCADE]
├─ chat_messages.from_user_id → users.user_id         [ON DELETE CASCADE]
├─ chat_messages.to_user_id   → users.user_id         [ON DELETE CASCADE]
├─ test_drives.auction_id     → auctions.auction_id   [ON DELETE CASCADE]
└─ test_drives.user_id        → users.user_id         [ON DELETE RESTRICT]

PERFORMANCE INDEXES:
├─ auctions.status            [For quick ACTIVE queries]
├─ auctions.end_time          [For time-based filtering]
├─ bids.auction_id            [Foreign key index]
├─ bids.user_id               [Foreign key index]
├─ chat_messages.auction_id   [For message lookup]
├─ chat_messages.from_user_id [For user message history]
├─ test_drives.user_id        [For user bookings]
└─ test_drives.scheduled_date [For calendar queries]
```

---

## Deployment Architecture

### Production Infrastructure & Security

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    PRODUCTION DEPLOYMENT ARCHITECTURE                    │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│                           INTERNET USERS                                │
│                                 │                                        │
│                          HTTPS (Port 443)                               │
│                                 ▼                                        │
│                    ┌────────────────────────┐                           │
│                    │  Load Balancer (ALB)   │                           │
│                    │  AWS / Nginx / HAProxy │                           │
│                    ├────────────────────────┤                           │
│                    │ ✓ SSL/TLS Termination  │                           │
│                    │ ✓ Health Checks        │                           │
│                    │ ✓ Rate Limiting        │                           │
│                    │ ✓ DDoS Protection      │                           │
│                    │ ✓ Request Logging      │                           │
│                    └──────────┬─────────────┘                           │
│                               │                                         │
│                 ┌─────────────┼─────────────┐                          │
│                 │             │             │                          │
│      ┌──────────▼────┐  ┌──────▼──────┐  ┌──▼───────────┐             │
│      │ Spring Boot   │  │ Spring Boot │  │ Spring Boot  │             │
│      │ Instance 1    │  │ Instance 2  │  │ Instance 3   │             │
│      │ (Port 8081)   │  │ (Port 8081) │  │ (Port 8081)  │             │
│      │ User Service  │  │ User Service│  │User Service  │             │
│      │               │  │             │  │              │             │
│      │ ✓ JWT Auth    │  │ ✓ JWT Auth  │  │ ✓ JWT Auth   │             │
│      │ ✓ Stateless   │  │ ✓ Stateless │  │ ✓ Stateless  │             │
│      │ ✓ No Sessions │  │ ✓ No Sess   │  │ ✓ No Sess    │             │
│      └──────┬─────────┘  └──────┬──────┘  └──┬───────────┘             │
│             │                   │            │                         │
│             └───────────────────┼────────────┘                         │
│                                 │                                      │
│                    ┌────────────▼────────────┐                        │
│                    │  Service Registry       │                        │
│                    │  Eureka / Consul        │                        │
│                    │  (Service Discovery)    │                        │
│                    └────────────┬────────────┘                        │
│                                 │                                      │
│                 ┌─────────────┬─┴──────────┬──────────┐               │
│                 │             │             │          │              │
│      ┌──────────▼────┐  ┌──────▼──────┐  ┌──▼──────────┐            │
│      │ Spring Boot   │  │ Spring Boot │  │ Spring Boot │            │
│      │ Instance 1    │  │ Instance 2  │  │ Instance 3  │            │
│      │ (Port 8080)   │  │ (Port 8080) │  │ (Port 8080) │            │
│      │ Bidding Svc   │  │ Bidding Svc │  │ Bidding Svc │            │
│      │               │  │             │  │             │            │
│      │ ✓ JwtAuthFilt │  │ ✓ JwtAuthFil│  │ ✓ JwtAuthFil           │
│      │ ✓ Stateless   │  │ ✓ Stateless │  │ ✓ Stateless │            │
│      │ ✓ Rule Engine │  │ ✓ Rule Eng  │  │ ✓ Rule Eng  │            │
│      │ ✓ Caching     │  │ ✓ Caching   │  │ ✓ Caching   │            │
│      └──────┬─────────┘  └──────┬──────┘  └──┬──────────┘            │
│             │                   │            │                       │
│             └───────────────────┼────────────┘                       │
│                                 │                                    │
│                    HikariCP Connection Pooling                       │
│                    (Max 20 connections per instance)                │
│                                 │                                    │
│     ┌───────────────────────────┴───────────────────────────┐      │
│     │                                                        │      │
│     ▼                                                        ▼      │
│  ┌───────────────────┐                         ┌──────────────────┐
│  │ MySQL Primary     │                         │ Redis Cache      │
│  │ (Master)          │                         │ (Optional)       │
│  │                   │                         │                  │
│  │ ✓ Read/Write      │  ◄─── Replication      │ ✓ Session Cache  │
│  │ ✓ Transactions    │       (Async)           │ ✓ Query Cache    │
│  │ ✓ ACID Properties │       (<100ms lag)      │ ✓ TTL: 24hrs     │
│  │                   │                         │                  │
│  │ Port: 3306        │                         │ Port: 6379       │
│  └────────┬──────────┘                         └──────────────────┘
│           │
│           │ Replication
│           │
│   ┌───────┼───────┐
│   │       │       │
│   ▼       ▼       ▼
│  Slave1 Slave2 Slave3
│  (Read) (Read) (Read)
│
│
│  SECURITY LAYERS
│  ════════════════════════════════════════════════════════════════
│
│  🔐 Network Level
│  ───────────────
│  • VPC with private & public subnets
│  • Internet Gateway for inbound traffic
│  • NAT Gateway for outbound traffic
│  • Security Groups restricting ports
│  • Network ACLs (stateless filtering)
│  • HTTPS/TLS 1.2+ enforcement
│  • SSH access via Bastion Host only
│
│  🔐 Load Balancer Level
│  ──────────────────────
│  • WAF (Web Application Firewall)
│  • Rate limiting per IP
│  • DDoS protection
│  • SSL/TLS certificate validation
│  • Health checks (HTTP/TCP)
│  • Connection draining
│
│  🔐 Application Level
│  ────────────────────
│  • Spring Security configuration
│  • JwtAuthFilter in request chain
│  • CORS policy enforcement
│  • Input validation (all endpoints)
│  • Output encoding (prevent XSS)
│  • Authentication: JWT tokens
│  • Authorization: Role-based access
│  • Rate limiting per user
│  • Request logging & auditing
│
│  🔐 Database Level
│  ─────────────────
│  • Private subnet (no internet access)
│  • Database user with restricted permissions
│  • Encrypted connections (SSL/TLS)
│  • Encrypted at rest (data encryption)
│  • Regular automated backups
│  • Backup encryption
│  • Backup retention: 30 days
│  • Read replicas for high availability
│  • Master-slave replication
│
│  🔐 Data Level
│  ─────────────
│  • Passwords: BCrypt hashing (cost: 12)
│  • PII: Not logged/stored unnecessarily
│  • Audit trail: All operations logged
│  • Encryption keys: Rotation every 90 days
│
│
│  SCALING & HIGH AVAILABILITY
│  ════════════════════════════════════════════════════════════════
│
│  Horizontal Scaling
│  ──────────────────
│  • Load balancer distributes traffic
│  • Stateless services (easy to scale)
│  • Auto-scaling groups (min 2, max 10)
│  • Scale-up trigger: CPU > 70% for 5 min
│  • Scale-down trigger: CPU < 30% for 10 min
│  • New instances join registry automatically
│
│  Vertical Scaling
│  ────────────────
│  • Increase instance CPU/Memory
│  • Optimize JVM heap settings
│  • Enable JIT compilation
│  • Tune GC settings
│
│  Database Scaling
│  ────────────────
│  • Read operations: Distributed to slaves
│  • Write operations: Only to master
│  • Connection pooling: HikariCP
│  • Query caching: Redis (optional)
│  • Sharding: If needed (future)
│
│
│  MONITORING & LOGGING
│  ════════════════════════════════════════════════════════════════
│
│  Metrics (Prometheus)
│  ────────────────────
│  • Request count & latency
│  • Error rates
│  • CPU & memory usage
│  • Database connection pool
│  • JWT validation failures
│
│  Logs (ELK Stack / CloudWatch)
│  ──────────────────────────────
│  • Application logs
│  • Access logs
│  • Error logs
│  • Audit logs
│  • Security logs
│
│  Alerts
│  ──────
│  • Error rate > 1%
│  • Response time > 2 seconds
│  • CPU > 80%
│  • Memory > 90%
│  • DB connection pool exhausted
│  • JWT validation failures > 10/min
│
└──────────────────────────────────────────────────────────────────────────┘
```


