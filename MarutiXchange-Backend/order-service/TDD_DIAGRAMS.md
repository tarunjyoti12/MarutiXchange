# Order Service Microservice - TDD Diagrams & Architecture

## Table of Contents
1. [Test-Driven Development (TDD) Diagram](#test-driven-development-tdd-diagram)
2. [Data Flow Diagram (DFD)](#data-flow-diagram-dfd)
3. [Sequence Diagram](#sequence-diagram)
4. [Architecture Overview](#architecture-overview)
5. [Test Cases](#test-cases)

---

## Test-Driven Development (TDD) Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          TDD CYCLE                                   │
└─────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────┐
                    │   START          │
                    └────────┬─────────┘
                             │
                   ┌─────────▼─────────┐
                   │  RED PHASE        │  
                   │  Write Test Cases │
                   │  ❌ Tests Fail    │
                   └─────────┬─────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
     ┌────▼─────┐   ┌────────▼────────┐  ┌────▼──────┐
     │ Unit      │   │ Integration     │  │ E2E       │
     │ Tests     │   │ Tests           │  │ Tests     │
     │           │   │                 │  │           │
     │ Test DAO  │   │ Test Service    │  │ Test API  │
     │ Test DTO  │   │ Test Controller │  │ Endpoints │
     └────┬─────┘   └────────┬────────┘  └────┬──────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
                   ┌─────────▼─────────┐
                   │  GREEN PHASE      │
                   │  Implement Code   │
                   │  ✅ Tests Pass    │
                   └─────────┬─────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
     ┌────▼─────┐   ┌────────▼────────┐  ┌────▼──────┐
     │ OrderDAO  │   │ OrderService    │  │ Controller│
     │ (JPA Repo)│   │ Business Logic  │  │ REST      │
     │           │   │ Drools Rules    │  │ Endpoints │
     │ Entity    │   │ Payment Client  │  │           │
     └────┬─────┘   └────────┬────────┘  └────┬──────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
                   ┌─────────▼──────────┐
                   │  REFACTOR PHASE    │
                   │  Improve Code      │
                   │  ✅ Tests Pass     │
                   └─────────┬──────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
     ┌────▼─────┐   ┌────────▼────────┐  ┌────▼──────┐
     │ Optimize  │   │ Add Error       │  │ Add Logging
     │ Queries   │   │ Handling        │  │ & Monitoring
     │           │   │ Security Checks │  │
     │ Indexing  │   │ Validation      │  │
     └───────────┘   └─────────────────┘  └────────────┘
                             │
                   ┌─────────▼──────────┐
                   │  REPEAT CYCLE      │
                   │  Next Feature      │
                   └────────────────────┘
```

---

## Data Flow Diagram (DFD)

### Level 0 - System Context Diagram (External View)

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                          EXTERNAL SYSTEM VIEW                                  │
└───────────────────────────────────────────────────────────────────────────────┘

                           ┌─────────────────────┐
                           │   CLIENT / Mobile   │
                           │   Web / Third-Party │
                           └──────────┬──────────┘
                                      │
                         [HTTP/REST API Calls]
                                      │
                           ┌──────────▼──────────┐
                           │   API GATEWAY       │
                           │   (Load Balancer)   │
                           │   (Rate Limiting)   │
                           │   (Auth)            │
                           └──────────┬──────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        │                             │                             │
   [Service Registry]            [Order Service]              [Payment Service]
        │                             │                             │
        ▼                             ▼                             ▼
   ┌────────────┐          ┌──────────────────┐        ┌──────────────────┐
   │  EUREKA    │          │ ORDER SERVICE    │        │ PAYMENT SERVICE  │
   │ (Discovery)│          │  :8066           │        │  :8085           │
   └────────────┘          └──────────┬───────┘        └──────────────────┘
                                      │
                                      │
                           [Database Connection]
                                      │
                                      ▼
                           ┌──────────────────┐
                           │  MySQL Database  │
                           │  (marutixchange_ │
                           │   orders)        │
                           └──────────────────┘
```

### Level 1 - Detailed Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                     PRODUCTION-READY ORDER SERVICE - DATA FLOW                        │
└──────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│ CLIENT LAYER                                                                        │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐        │
│  │  Mobile App  │   │  Web Browser │   │ Third-Party  │   │   Admin UI   │        │
│  │              │   │              │   │   System     │   │              │        │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘   └──────┬───────┘        │
│         │                  │                  │                  │                 │
│         └──────────────────┼──────────────────┼──────────────────┘                 │
│                            │ HTTP/HTTPS       │                                    │
│                            │ REST API         │                                    │
└────────────────────────────┼──────────────────┼────────────────────────────────────┘
                             │                  │
                     ┌───────▼──────────────────▼──────┐
                     │    API GATEWAY / LOAD BALANCER   │
                     │    - Route Requests              │
                     │    - Rate Limiting               │
                     │    - Authentication              │
                     │    - Request Validation          │
                     └───────┬────────────────────────┬──┘
                             │                        │
               ┌─────────────┴──────────┐   ┌────────▼──────────┐
               │                        │   │                   │
               ▼                        │   │                   │
     ┌──────────────────┐              │   ▼                    ▼
     │  EUREKA SERVER   │              │   ┌────────────────────────────┐
     │  (Service        │              │   │    ORDER SERVICE           │
     │   Discovery)     │              │   │         :8066              │
     └──────────────────┘              │   │                            │
            ▲                          │   │  ┌──────────────────────┐  │
            │ Heartbeat               │   │  │ PRESENTATION LAYER   │  │
            │ Register/Deregister    │   │  ├──────────────────────┤  │
            │                        │   │  │ OrderController      │  │
            │                        │   │  │ • POST /api/orders   │  │
            │                        │   │  │ • PATCH /orders/{id} │  │
            │                        │   │  │   /complete          │  │
            │                        │   │  │ • PATCH /orders/{id} │  │
            │                        │   │  │   /cancel            │  │
            │                        │   │  │ • Global Exception   │  │
            │                        │   │  │   Handler            │  │
            │                        │   │  └──────────┬───────────┘  │
            │                        │   │             │ RequestBody  │
            │                        │   │             │ Response     │
            │                        │   │             │ Status       │
            │                        │   │  ┌──────────▼───────────┐  │
            │                        │   │  │ SERVICE LAYER        │  │
            │                        │   │  ├──────────────────────┤  │
            │                        │   │  │ OrderService(Intf)   │  │
            │                        │   │  │ OrderServiceImpl      │  │
            │                        │   │  │                      │  │
            │                        │   │  │ BUSINESS LOGIC:      │  │
            │                        │   │  │ • Create Order       │  │
            │                        │   │  │   - Set PENDING      │  │
            │                        │   │  │   - Drools Rules     │  │
            │                        │   │  │   - Save to DB       │  │
            │                        │   │  │   - Call Payment Svc │  │
            │                        │   │  │   - Update with PayID│  │
            │                        │   │  │                      │  │
            │                        │   │  │ • Complete Order     │  │
            │                        │   │  │   - Find Order       │  │
            │                        │   │  │   - Set Status=PAID  │  │
            │                        │   │  │   - Save to DB       │  │
            │                        │   │  │                      │  │
            │                        │   │  │ • Cancel Order       │  │
            │                        │   │  │   - Find Order       │  │
            │                        │   │  │   - Set CANCELLED    │  │
            │                        │   │  │   - Save to DB       │  │
            │                        │   │  └────┬──────┬──────┬────┘  │
            │                        │   │       │      │      │       │
            │ ┌─────────┐       ┌────┴───┴──┐ ┌─┴──┐ ┌─┴──┐ ┌┴────┐   │
            │ │         │       │           │ │    │ │    │ │     │   │
            │ ▼         ▼       │           │ ▼    │ ▼    │ ▼     │   │
            │     ┌─────────────────────────────────────────────┐  │
            │     │      DATA ACCESS LAYER                      │  │
            │     ├─────────────────────────────────────────────┤  │
            │     │ OrderRepository (JPA)                       │  │
            │     │ • save(Order)                               │  │
            │     │ • findById(Long id)                         │  │
            │     │ • update(Order)                             │  │
            │     │ • delete(Order)                             │  │
            │     │                                             │  │
            │     │ Query Methods:                              │  │
            │     │ • SELECT * FROM orders WHERE id = ?         │  │
            │     │ • UPDATE orders SET status = ? WHERE id = ? │  │
            │     │ • INSERT INTO orders (...)                  │  │
            │     └────────────┬──────────────────────────────┘  │
            │                  │ JDBC/Hibernate                  │
            │  ┌───────────────┼──────────────────────────┐      │
            │  │               │                          │      │
            │  ▼               ▼                          ▼      │
            │  ┌────────────────────────────────┐ ┌──────────────┐│
            │  │ DROOLS RULE ENGINE             │ │ FEIGN CLIENT││
            │  ├────────────────────────────────┤ ├──────────────┤│
            │  │ • Validate Order               │ │ PaymentClient││
            │  │ • Insert Order into KieSession │ │              ││
            │  │ • fireAllRules()               │ │ @FeignClient:│
            │  │                                │ │ payment-svc  │
            │  │ RULES:                         │ │              ││
            │  │ • Min Amount Check             │ │ POST Endpoint:
            │  │ • Valid Buyer/Seller           │ │ /api/v1/     ││
            │  │ • Car Listing Valid            │ │ payments/    ││
            │  │ • Payment Method Valid         │ │ initiate     ││
            │  │                                │ │              ││
            │  │ Validation Errors → Thrown    │ │ Returns:     ││
            │  │ as RuntimeException            │ │ PaymentId    ││
            │  │                                │ │ Status       ││
            │  └────────────────────────────────┘ │ TransId      ││
            │                                     └──────────────┘│
            │                                          │          │
            │  ┌─────────────────────────────────────┘           │
            │  │                                                  │
            │  │ [Service Discovery - Eureka Lookup]             │
            │  │ PaymentClient queries Eureka for "payment-svc"  │
            │  │ Routes to: http://payment-service:8085/...      │
            │  │                                                  │
            └──────────────────────────────────────────────────────┘
                             │
                ┌────────────┘
                │
                │ [MySQL Connection]
                │ JDBC Connection Pool
                │ Hikari / Spring DataSource
                │
                ▼
    ┌──────────────────────────────────────┐
    │       DATABASE LAYER (MySQL)         │
    ├──────────────────────────────────────┤
    │ Database: marutixchange_orders       │
    │ Table: orders                        │
    │                                      │
    │ Columns:                             │
    │ ├─ id (BIGINT) - Primary Key         │
    │ ├─ buyer_id (BIGINT)                 │
    │ ├─ seller_id (BIGINT)                │
    │ ├─ car_id (BIGINT)                   │
    │ ├─ amount (DECIMAL)                  │
    │ ├─ status (VARCHAR) - Enum           │
    │ │  ├─ PAYMENT_PENDING                │
    │ │  ├─ PAID                           │
    │ │  ├─ CANCELLED                      │
    │ │  └─ FAILED                         │
    │ ├─ payment_id (VARCHAR)              │
    │ └─ created_at (TIMESTAMP)            │
    │                                      │
    │ Indices:                             │
    │ ├─ PRIMARY KEY (id)                  │
    │ ├─ INDEX (buyer_id)                  │
    │ ├─ INDEX (status)                    │
    │ └─ INDEX (payment_id)                │
    └──────────────────────────────────────┘
                │ Read/Write
                │
    ┌───────────▼──────────────────────────┐
    │  PERSISTENCE (Data at Rest)          │
    │  • Order Records                     │
    │  • Transaction Log                   │
    │  • Audit Trail                       │
    └──────────────────────────────────────┘
```

### Level 2 - Inter-Service Communication Flow

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                  INTER-SERVICE COMMUNICATION (Feign + REST)                   │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ ORDER SERVICE REQUEST PATH (Forward)                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Controller sends OrderRequest                                             │
│         │                                                                  │
│         ▼                                                                  │
│  OrderServiceImpl.createOrder()                                            │
│         │                                                                  │
│         ├─ 1. CREATE Order Entity (status = PAYMENT_PENDING)             │
│         │                                                                  │
│         ├─ 2. INSERT into Database                                        │
│         │        ├─ SQL: INSERT INTO orders (buyer_id, seller_id, ...)   │
│         │        └─ Returns: orderId (Primary Key)                        │
│         │                                                                  │
│         ├─ 3. VALIDATE with Drools                                        │
│         │        ├─ kieSession.insert(order)                             │
│         │        ├─ kieSession.fireAllRules()                            │
│         │        ├─ Check for validation errors                          │
│         │        └─ Throw RuntimeException if failed                     │
│         │                                                                  │
│         ├─ 4. CALL Payment Service (via Feign)                            │
│         │        ├─ PaymentClient.createPayment(paymentRequest)          │
│         │        │   ├─ Eureka: Lookup "payment-service"                │
│         │        │   ├─ Build URL: http://payment-service:8085/...      │
│         │        │   └─ HTTP POST with JSON                             │
│         │        │       {                                               │
│         │        │         orderId,                                       │
│         │        │         buyerId,                                       │
│         │        │         sellerId,                                      │
│         │        │         amount,                                        │
│         │        │         paymentMethod,                                 │
│         │        │         carListingId                                   │
│         │        │       }                                               │
│         │        └─ Returns PaymentResponse:                             │
│         │            {                                                    │
│         │              paymentId: "PAY-xxxx",                            │
│         │              transactionId: "TXN-xxxx",                        │
│         │              status: "INITIATED"                               │
│         │            }                                                    │
│         │                                                                  │
│         ├─ 5. UPDATE Order with paymentId                                │
│         │        ├─ SQL: UPDATE orders                                   │
│         │        │       SET payment_id = ?, updated_at = NOW()         │
│         │        │       WHERE id = ?                                    │
│         │        └─ Persist payment reference                            │
│         │                                                                  │
│         └─ 6. RETURN OrderResponse                                        │
│                ├─ orderId: Long                                           │
│                ├─ status: "PAYMENT_PENDING"                              │
│                └─ paymentId: String                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ PAYMENT SERVICE CALLBACK (Reverse Flow - Asynchronous Webhook)              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  SCENARIO: Payment Confirmed Successfully                                  │
│                                                                             │
│  Payment Service (External)                                                │
│         │                                                                  │
│         │ [Async Event: PAYMENT_SUCCESS]                                  │
│         │ paymentId = "PAY-xxxx"                                          │
│         │ status = "COMPLETED"                                            │
│         │                                                                  │
│         ├─ WEBHOOK: PATCH /api/orders/{orderId}/complete                  │
│         │                                                                  │
│         ▼                                                                  │
│  Order Service (OrderController)                                           │
│         │                                                                  │
│         ├─ Receive callback with orderId                                  │
│         │                                                                  │
│         └─ Call completeOrder(orderId)                                    │
│                ├─ Find Order by ID                                        │
│                │  └─ SQL: SELECT * FROM orders WHERE id = ?             │
│                │                                                          │
│                ├─ Update status to PAID                                  │
│                │  └─ SQL: UPDATE orders                                  │
│                │          SET status = 'PAID', updated_at = NOW()        │
│                │          WHERE id = ?                                   │
│                │                                                          │
│                └─ Return 200 OK                                          │
│                   └─ Message: "Order marked as PAID"                     │
│                                                                             │
│  ─────────────────────────────────────────────────────────────────────── │
│                                                                             │
│  SCENARIO: Payment Failed                                                  │
│                                                                             │
│  Payment Service (External)                                                │
│         │                                                                  │
│         │ [Async Event: PAYMENT_FAILED]                                   │
│         │ paymentId = "PAY-xxxx"                                          │
│         │ status = "DECLINED"                                             │
│         │ reason = "Insufficient funds"                                   │
│         │                                                                  │
│         ├─ WEBHOOK: PATCH /api/orders/{orderId}/cancel                    │
│         │                                                                  │
│         ▼                                                                  │
│  Order Service (OrderController)                                           │
│         │                                                                  │
│         ├─ Receive callback with orderId                                  │
│         │                                                                  │
│         └─ Call cancelOrder(orderId)                                      │
│                ├─ Find Order by ID                                        │
│                │  └─ SQL: SELECT * FROM orders WHERE id = ?             │
│                │                                                          │
│                ├─ Update status to CANCELLED                             │
│                │  └─ SQL: UPDATE orders                                  │
│                │          SET status = 'CANCELLED', updated_at = NOW() │
│                │          WHERE id = ?                                   │
│                │                                                          │
│                └─ Return 200 OK                                          │
│                   └─ Message: "Order cancelled"                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ FAILURE PATHS & ERROR HANDLING                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ 1. PAYMENT SERVICE TIMEOUT                                                 │
│    └─ Order stays in PAYMENT_PENDING status                               │
│    └─ Manual intervention required via Admin Panel                        │
│    └─ Retry mechanism: Task Queue / Scheduled Job                         │
│                                                                             │
│ 2. PAYMENT SERVICE DOWN (503)                                              │
│    ├─ Circuit Breaker catches exception                                   │
│    ├─ Order saved with PAYMENT_PENDING + paymentId = null                │
│    └─ Return 202 ACCEPTED to client                                       │
│       └─ Client receives: "Payment service unavailable. Retry later."     │
│                                                                             │
│ 3. DATABASE CONNECTION FAILURE                                             │
│    ├─ RuntimeException thrown                                             │
│    └─ GlobalExceptionHandler catches & returns 500 INTERNAL_SERVER_ERROR  │
│       └─ Order not persisted                                              │
│                                                                             │
│ 4. VALIDATION FAILURE (Drools Rules)                                       │
│    ├─ Rules validation fails                                              │
│    └─ Return 400 BAD_REQUEST with error message                          │
│       └─ "Order validation failed: [list of errors]"                     │
│                                                                             │
│ 5. ORDER NOT FOUND                                                         │
│    └─ OrderNotFoundException thrown                                        │
│    └─ GlobalExceptionHandler catches & returns 404 NOT_FOUND             │
│       └─ "Order with ID {id} not found"                                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Level 3 - Security & Authentication Flow

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    SECURITY LAYER - JWT AUTHENTICATION                        │
└──────────────────────────────────────────────────────────────────────────────┘

Client Request                                           Order Service
    │                                                            │
    │ [HTTP Header]                                              │
    │ Authorization: Bearer eyJhbGciOiJIUzI1NiIs...             │
    │                                                            │
    ├───────────────────────────────────────────────────────────►│
    │                                                            │
    │                                   SecurityFilterChain
    │                                            │
    │                                            ├─ JwtAuthFilter
    │                                            │  ├─ Extract token
    │                                            │  ├─ Validate token
    │                                            │  ├─ Extract username
    │                                            │  └─ Set SecurityContext
    │                                            │
    │                                   OrderController
    │                                            │
    │                                   OrderServiceImpl
    │                                            │
    │                                   OrderRepository
    │                                            │
    │                                     Database
    │                                            │
    │                          [Process Request & Generate Response]
    │                                            │
    │◄───────────────────────────────────────────┤
    │ [HTTP Response]                            │
    │ {status: 200, data: OrderResponse}         │
    │                                            │
```

---

## Sequence Diagram

### Scenario 1: Create Order with Payment (SUCCESS FLOW)

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                    SUCCESS FLOW - ORDER CREATION & PAYMENT                      │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘

Client      API Gateway      OrderController    OrderService     Drools    Database    PaymentService
  │              │                  │                │            │           │              │
  │──POST /api/  │                  │                │            │           │              │
  │  orders      │                  │                │            │           │              │
  │  {Request}   │                  │                │            │           │              │
  ├─────────────►│                  │                │            │           │              │
  │              │─────────────────►│                │            │           │              │
  │              │                  │  createOrder() │            │           │              │
  │              │                  ├──────────────►│            │           │              │
  │              │                  │                │            │           │              │
  │              │                  │                ├──────────►│           │              │
  │              │                  │                │ 1. Insert │           │              │
  │              │                  │                │    Order  │           │              │
  │              │                  │                ├─────────────────────►│              │
  │              │                  │                │ INSERT INTO orders   │              │
  │              │                  │                │ (status=PAYMENT_     │              │
  │              │                  │                │  PENDING)            │              │
  │              │                  │                │◄─────────────────────┤  Order ID    │
  │              │                  │                │                      │  returned    │
  │              │                  │                ├─────────────────────────────────────┐
  │              │                  │                │ 2. Validate Rules    │           │ │
  │              │                  │                ◄──────────┤           │           │ │
  │              │                  │                │ Rules OK  │           │           │ │
  │              │                  │                └─────────────────────────────────┘ │
  │              │                  │                │            │           │          │
  │              │                  │                ├──────────────────────────────────────────►
  │              │                  │                │ 3. POST /api/v1/payments/initiate        │
  │              │                  │                │    {orderId, buyerId, sellerId,          │
  │              │                  │                │     amount, paymentMethod}               │
  │              │                  │                │◄──────────────────────────────────────────
  │              │                  │                │ PaymentResponse:                        │
  │              │                  │                │ {paymentId, status: "INITIATED"}        │
  │              │                  │                │            │           │              │
  │              │                  │                ├─────────────────────►│              │
  │              │                  │                │ 4. UPDATE orders     │              │
  │              │                  │                │    SET payment_id=?  │              │
  │              │                  │                │    WHERE id=?        │              │
  │              │                  │                │◄─────────────────────┤  Updated    │
  │              │                  │                │                      │              │
  │              │                  │  OrderResponse │            │           │              │
  │              │                  │◄──────────────┤            │           │              │
  │              │◄─────────────────┤                │            │           │              │
  │◄─────────────┤                  │                │            │           │              │
  │ 200 OK       │                  │                │            │           │              │
  │ {orderId,    │                  │                │            │           │              │
  │  status:     │                  │                │            │           │              │
  │  PAYMENT_    │                  │                │            │           │              │
  │  PENDING,    │                  │                │            │           │              │
  │  paymentId}  │                  │                │            │           │              │
  │              │                  │                │            │           │              │
```

### Scenario 1B: AUTO ORDER UPDATE - Payment Service Callback (SUCCESS)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│              AUTO ORDER UPDATE AFTER PAYMENT SUCCESS (REVERSE FLOW)            │
│              Payment Service → Order Service Webhook                           │
└──────────────────────────────────────────────────────────────────────────────┘

PaymentService    [Network]    API Gateway    OrderController    OrderService    Database
  │                   │              │              │                │              │
  │─ Async Event or   │              │              │                │              │
  │  Webhook          │              │              │                │              │
  │  PAYMENT_SUCCESS  │              │              │                │              │
  │                   │              │              │                │              │
  ├──────────────────►│              │              │                │              │
  │                   │──────────────────────────────┐                │              │
  │                   │ PATCH /api/orders/{orderId}/  │                │              │
  │                   │        complete               │                │              │
  │                   │              │◄───────────────┤                │              │
  │                   │              │                │ completeOrder()│              │
  │                   │              │                ├──────────────►│              │
  │                   │              │                │                │              │
  │                   │              │                │                ├────────────►│
  │                   │              │                │                │ UPDATE      │
  │                   │              │                │                │ orders      │
  │                   │              │                │                │ status=PAID │
  │                   │              │                │                │◄────────────┤
  │                   │              │                │                │              │
  │                   │              │                │ Order PAID     │              │
  │                   │              │                ├────────────────┤              │
  │                   │              │◄───────────────┤                │              │
  │                   │◄─────────────┤                │                │              │
  │                   │ 200 OK       │                │                │              │
  │                   │ "Order PAID" │                │                │              │
  │                   │              │                │                │              │
```

### Scenario 2: Payment Failure (FAILURE FLOW)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    FAILURE FLOW - PAYMENT DECLINED/FAILED                      │
└──────────────────────────────────────────────────────────────────────────────┘

PaymentService    [Network]    API Gateway    OrderController    OrderService    Database
  │                   │              │              │                │              │
  │─ Webhook Event    │              │              │                │              │
  │  PAYMENT_FAILED   │              │              │                │              │
  │                   │              │              │                │              │
  ├──────────────────►│              │              │                │              │
  │                   │──────────────────────────────┐                │              │
  │                   │ PATCH /api/orders/{orderId}/  │                │              │
  │                   │        cancel                 │                │              │
  │                   │              │◄───────────────┤                │              │
  │                   │              │                │  cancelOrder() │              │
  │                   │              │                ├──────────────►│              │
  │                   │              │                │                │              │
  │                   │              │                │                ├────────────►│
  │                   │              │                │                │ UPDATE      │
  │                   │              │                │                │ orders      │
  │                   │              │                │                │ status=     │
  │                   │              │                │                │ CANCELLED   │
  │                   │              │                │                │◄────────────┤
  │                   │              │                │                │              │
  │                   │              │                │ Order         │              │
  │                   │              │                │ CANCELLED     │              │
  │                   │              │                ├────────────────┤              │
  │                   │              │◄───────────────┤                │              │
  │                   │◄─────────────┤                │                │              │
  │                   │ 200 OK       │                │                │              │
  │                   │ "Order       │                │                │              │
  │                   │  CANCELLED"  │                │                │              │
  │                   │              │                │                │              │
```

### Scenario 3: Payment Timeout (TIMEOUT FLOW - Order Remains Pending)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│              TIMEOUT FLOW - PAYMENT PROCESSING TIMEOUT                         │
│              Order Status: PAYMENT_PENDING (awaiting payment confirmation)     │
└──────────────────────────────────────────────────────────────────────────────┘

Client      API Gateway      OrderController    OrderService    PaymentService
  │              │                  │                │              │
  │──POST /api/  │                  │                │              │
  │  orders      │                  │                │              │
  ├─────────────►│                  │                │              │
  │              │─────────────────►│                │              │
  │              │                  │  createOrder() │              │
  │              │                  ├──────────────►│              │
  │              │                  │                ├─────────────►│
  │              │                  │                │ POST /api/   │
  │              │                  │                │ payments/    │
  │              │                  │                │ initiate     │
  │              │                  │                │              │
  │              │                  │                │   [TIMEOUT]  │
  │              │                  │                │◄─ No Response
  │              │                  │                │              │
  │              │                  │                ├───────────┐  │
  │              │                  │                │ Retry     │  │
  │              │                  │                │ (3 times) │  │
  │              │                  │                └─────────────►
  │              │                  │                │              │
  │              │                  │                │   [TIMEOUT]  │
  │              │                  │                │◄─ No Response
  │              │                  │                │              │
  │              │                  │                ├───────┐      │
  │              │                  │                │ Order │      │
  │              │                  │                │ stays │      │
  │              │                  │                │PENDING       │
  │              │                  │  OrderResponse │      │      │
  │              │                  │◄──────────────┤      │      │
  │              │◄─────────────────┤                │      │      │
  │◄─────────────┤                  │                │      │      │
  │ 200 OK       │                  │                │      │      │
  │ {orderId,    │                  │                │      │      │
  │  status:     │                  │                │      │      │
  │  PAYMENT_    │                  │                │      │      │
  │  PENDING,    │                  │                │      │      │
  │  paymentId:  │                  │                │      │      │
  │  null}       │                  │                │      │      │
  │              │                  │                │      │      │
  │              │                  │                │ Manual retry │
  │              │                  │                │ needed       │
  │              │                  │                │      │      │
```

### Scenario 4: Payment Service DOWN (ERROR RECOVERY FLOW)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│        ERROR FLOW - PAYMENT SERVICE UNAVAILABLE (Circuit Breaker)              │
│        Order created with PAYMENT_PENDING - Payment call fails gracefully      │
└──────────────────────────────────────────────────────────────────────────────┘

Client      API Gateway      OrderController    OrderService    PaymentService
  │              │                  │                │              │
  │──POST /api/  │                  │                │              │
  │  orders      │                  │                │              │
  ├─────────────►│                  │                │              │
  │              │─────────────────►│                │              │
  │              │                  │  createOrder() │              │
  │              │                  ├──────────────►│              │
  │              │                  │                │              │
  │              │                  │                ├─────────────►│
  │              │                  │                │              │ 503 Service
  │              │                  │                │◄─────────────┤ Unavailable
  │              │                  │                │              │
  │              │                  │                │ [Circuit Breaker]
  │              │                  │                │ Exception caught
  │              │                  │                │              │
  │              │                  │ Order created  │              │
  │              │                  │ with PENDING   │              │
  │              │                  │ status         │              │
  │              │                  │◄──────────────┤              │
  │              │  OrderResponse    │                │              │
  │              │◄─────────────────┤                │              │
  │◄─────────────┤                  │                │              │
  │ 202 Accepted │                  │                │              │
  │ {orderId,    │                  │                │              │
  │  status:     │                  │                │              │
  │  PAYMENT_    │                  │                │              │
  │  PENDING,    │                  │                │              │
  │  paymentId:  │                  │                │              │
  │  null,       │                  │                │              │
  │  message:    │                  │                │              │
  │  "Payment    │                  │                │              │
  │   service    │                  │                │              │
  │   unavailable.│                  │                │              │
  │   Retry      │                  │                │              │
  │   later"}    │                  │                │              │
  │              │                  │                │              │
  │  NOTE: Admin │                  │                │              │
  │  should      │                  │                │              │
  │  monitor and │                  │                │              │
  │  retry via   │                  │                │              │
  │  task queue  │                  │                │              │
  │              │                  │                │              │
```

---

## Architecture Overview

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Order Service                              │
│                                                                 │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Presentation Layer                                   │    │
│  │  ┌──────────────────────────────────────────────────┐ │    │
│  │  │ OrderController                                 │ │    │
│  │  │ - REST API Endpoints                           │ │    │
│  │  │ - Request/Response Handling                    │ │    │
│  │  │ - Exception Handling                           │ │    │
│  │  └──────────────────────────────────────────────────┘ │    │
│  └──────────┬─────────────────────────────────────────────┘    │
│             │                                                   │
│  ┌──────────▼─────────────────────────────────────────────┐    │
│  │  Service Layer                                        │    │
│  │  ┌──────────────────────────────────────────────────┐ │    │
│  │  │ OrderService (Interface)                        │ │    │
│  │  │ ┌────────────────────────────────────────────┐  │ │    │
│  │  │ │ OrderServiceImpl (Implementation)          │  │ │    │
│  │  │ │ - createOrder()                           │  │ │    │
│  │  │ │ - completeOrder()                         │  │ │    │
│  │  │ │ - cancelOrder()                           │  │ │    │
│  │  │ │ - Business Logic                          │  │ │    │
│  │  │ │ - Drools Rule Engine Integration          │  │ │    │
│  │  │ │ - Feign Client Integration                │  │ │    │
│  │  │ └────────────────────────────────────────────┘  │ │    │
│  │  └──────────────────────────────────────────────────┘ │    │
│  └──────────┬──────────────┬──────────────┬──────────────┘    │
│             │              │              │                    │
│  ┌──────────▼────┐  ┌──────▼─────┐  ┌───▼──────────┐          │
│  │ Data Access   │  │ Rule Engine │  │ Feign Client│          │
│  │ Layer         │  │ (Drools)    │  │ Integration │          │
│  │               │  │             │  │             │          │
│  │ OrderRepository   Drools       PaymentClient   │          │
│  │ - JPA/Hibernate   - Validation - Payment API   │          │
│  │ - Query Methods   - Rules      - REST Call     │          │
│  │ - CRUD Ops        - KieSession │             │          │
│  └─────────┬────┘  └──────┬─────┘  └───┬────────┘          │
│            │              │             │                    │
└────────────┼──────────────┼─────────────┼────────────────────┘
             │              │             │
   ┌─────────▼──────┐      │       ┌──────▼──────────┐
   │   MySQL DB     │      │       │ Payment Service │
   │   (Orders)     │      │       │ (Remote)        │
   └────────────────┘      │       └─────────────────┘
                           │
                    ┌──────▼────────┐
                    │ Eureka Server │
                    │ (Discovery)   │
                    └───────────────┘
```


## Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────┐
│          ORDERS TABLE           │
├─────────────────────────────────┤
│ PK │ id (Long)                  │
├────┼─────────────────────────────┤
│    │ buyer_id (Long)            │ ──────┐
│    │ seller_id (Long)           │ ──────┐
│    │ car_id (Long)              │ ──────┐ References External Services
│    │ amount (Double)            │       │ (via Feign clients)
│    │ status (VARCHAR)           │       │
│    │ payment_id (VARCHAR)       │ ──────┘ Links to Payment Service
│    │ created_at (TIMESTAMP)     │
├────┴─────────────────────────────┤
│ OrderStatus Enum:               │
│ - CREATED                       │
│ - PAYMENT_PENDING (Initial)     │
│ - PAID (Success)                │
│ - FAILED                        │
│ - CANCELLED (Manual)            │
│ - COMPLETED                     │
└─────────────────────────────────┘
```

---

## State Diagram - Order Lifecycle

```
                    ┌──────────────┐
                    │   CREATED    │
                    └──────┬───────┘
                           │
                           │ Create Order Request
                           │
                    ┌──────▼────────────┐
                    │ PAYMENT_PENDING   │
                    │ (Initial Status)  │
                    └──────┬─────────────┘
                           │
                ┌──────────┴───────────┐
                │                      │
        ┌───────▼────────┐    ┌───────▼────────┐
        │   Payment OK   │    │  Payment Failed│
        │                │    │  Manual Cancel │
        └───────┬────────┘    └───────┬────────┘
                │                     │
        ┌───────▼────────┐    ┌───────▼────────┐
        │     PAID       │    │   CANCELLED    │
        │ (Success)      │    │ (Failed/Manual)│
        └───────┬────────┘    └────────────────┘
                │
        ┌───────▼────────┐
        │   COMPLETED    │
        │ (End State)    │
        └────────────────┘
```



```
┌──────────────────────────────────────────────────────────────────────────┐
│           EVENT-DRIVEN ORDER PROCESSING (Future State)                   │
└──────────────────────────────────────────────────────────────────────────┘

CURRENT STATE (Synchronous):
  Order Service ──(Feign: Sync REST)──► Payment Service
                      (Blocking)

FUTURE STATE (Asynchronous with Kafka):
  
  ┌──────────────────────────────────────────────────────────────────────┐
  │                          ORDER SERVICE                               │
  │  ┌───────────────┐        ┌─────────────┐       ┌──────────────┐    │
  │  │ OrderService  │───────►│  Kafka Topic│       │   Listener   │    │
  │  │               │ Publish│: ORDER_INIT │       │  Consumer    │    │
  │  │ createOrder() │ Event  │             │       │              │    │
  │  └───────────────┘        └─────────────┘       └──────────────┘    │
  └────────────────────────────────┬──────────────────────────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │    KAFKA MESSAGE BROKER    │
                    │                            │
                    │  Topics:                   │
                    │  • ORDER.INITIATED         │
                    │  • PAYMENT.SUCCEEDED       │
                    │  • PAYMENT.FAILED          │
                    │  • ORDER.COMPLETED         │
                    └─────────────┬──────────────┘
                                  │
                    ┌─────────────▼──────────────┐
                    │   PAYMENT SERVICE          │
                    │                            │
                    │  • Listen: ORDER.INITIATED │
                    │  • Process Payment         │
                    │  • Publish Event:          │
                    │    PAYMENT.SUCCEEDED or    │
                    │    PAYMENT.FAILED          │
                    └────────────────────────────┘

