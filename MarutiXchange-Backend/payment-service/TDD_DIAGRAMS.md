# TDD Diagrams - MarutiXchange Payment Service

## Table of Contents
1. [System Architecture](#system-architecture)
2. [High-Level Data Flow](#high-level-data-flow)
3. [Payment Processing Sequence](#payment-processing-sequence)
4. [Escrow & Refund Sequence](#escrow--refund-sequence)
5. [Database Entity Model](#database-entity-model)
6. [API Endpoints Architecture](#api-endpoints-architecture)
7. [Drools Rules Engine Architecture](#drools-rules-engine-architecture)
8. [Rule Categories & Agenda Groups](#rule-categories--agenda-groups)
9. [Warning Handling Strategy](#warning-handling-strategy)

---

## System Architecture

### ASCII Art - Payment Service Component Structure with Drools

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT / API GATEWAY                     │
└────────────────┬────────────────┬────────────────┬───────────────┘
                 │                │                │
                 ▼                ▼                ▼
        ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
        │  Payment API   │ │  Escrow API    │ │  Refund API    │
        │  /v1/payments  │ │  /v1/escrow    │ │  /v1/refunds   │
        └────────┬───────┘ └────────┬───────┘ └────────┬───────┘
                 │                  │                  │
                 └──────────────────┼──────────────────┘
                                    ▼
                    ┌───────────────────────────────┐
                    │   Security Filter Chain       │
                    │   (Security Configuration)    │
                    └───────────┬───────────────────┘
                                │
                ┌───────────────┼───────────────┐
                ▼               ▼               ▼
        ┌─────────────────┐ ┌─────────────────┐ ┌──────────────┐
        │  PaymentSvc     │ │  EscrowSvc      │ │  RefundSvc   │
        │  Implementation │ │ Implementation  │ │Implementation│
        └────────┬────────┘ └────────┬────────┘ └──────┬───────┘
                 │                   │                 │
                 └───────────────────┼─────────────────┘
                                     ▼
                    ┌───────────────────────────────┐
                    │       DroolsRuleService       │
                    │   (Drools Rules Engine)       │
                    │                               │
                    │  ┌─────────────────────────┐  │
                    │  │     KieContainer        │  │
                    │  │  (Compiled Rules)       │  │
                    │  └─────────────────────────┘  │
                    └────────────┬──────────────────┘
                                 │
                ┌────────────────┼────────────────┐
                ▼                ▼                ▼
        ┌─────────────────┐ ┌──────────────┐ ┌─────────────┐
        │  PaymentRepo    │ │  EscrowRepo  │ │ RefundRepo  │
        │ (JPA/Hibernate) │ │  (JPA/...)   │ │ (JPA/...)   │
        └────────┬────────┘ └──────┬───────┘ └─────┬───────┘
                 │                 │               │
                 └─────────────────┼───────────────┘
                                   ▼
                    ┌──────────────────────────┐
                    │    MySQL Database     │
                    │ ┌──────────┐             │
                    │ │ payments  │  payments  │
                    │ │ escrows   │  entity    │
                    │ │ refunds   │  storage   │
                    │ └──────────┘             │
                    └──────────────────────────┘

```

---

## High-Level Data Flow

```
┌──────────────┐
│ Client sends │
│   Payment    │
│   Request    │
└──────┬───────┘
       │
       ▼
┌──────────────────────────────────────┐
│  API Endpoint: POST /api/v1/payments │
│  (receives PaymentRequest DTO)       │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│   PaymentController.initiatePayment()│
│   (HTTP → Business Logic Bridge)     │
└──────┬───────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────┐
│           PaymentService.initiate()                       │
│   ┌─────────────────────────────────────────────────────┐ │
│   │ 1. Validate Request (Basic Checks)                  │ │
│   │ 2. Generate Transaction ID + Idempotency Key        │ │
│   │ 3. Call DroolsRuleService.validatePayment()         │ │
│   └─────────────────────────────────────────────────────┘ │
└──────┬───────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────┐
│ DroolsRuleService.validatePayment()              │
│ ┌────────────────────────────────────────────┐  │
│ │ Creates KieSession (Stateful Session)      │  │
│ │ Sets Global Variables                       │  │
│ │ Inserts Facts (PaymentRequest, RuleResult) │  │
│ │ Activates Agenda Groups                    │  │
│ │ Fires Rules                                │  │
│ │ Collects Results in RuleResult             │  │
│ └────────────────────────────────────────────┘  │
└──────┬────────────────┬────────────────────────┘
       │                │
       ▼                ▼
   ┌────────────┐  ┌──────────────────┐
   │   PASS ✓   │  │  FAIL/WARNING ⚠  │
   └─────┬──────┘  └────────┬─────────┘
         │                  │
         ▼                  ▼
    ┌─────────────┐   ┌──────────────────────┐
    │  No Issues  │   │ Check Result Type    │
    │  (Empty []) │   │                      │
    └─────┬───────┘   ├──────────────────────┤
          │           │ Violations (Critical)│
          │           │ → Throw Exception    │
          │           │                      │
          │           │ Warnings (Non-Block.)│
          │           │ → Continue & Include │
          │           │    in Response       │
          │           └────────┬─────────────┘
          │                    │
          └─────────┬──────────┘
                    ▼
       ┌────────────────────────────────┐
       │  Create Payment Entity & Save  │
       │  Status: PENDING               │
       │  (with warnings[] if any)      │
       └────────┬───────────────────────┘
                ▼
    ┌──────────────────────────────┐
    │ Return PaymentResponse (200 OK)
    │ {                            │
    │   "id": "PAY-123",           │
    │   "status": "PENDING",       │
    │   "transactionId": "TXN-ABC",│
    │   "warnings": [],  (if any)  │
    │   "createdAt": timestamp     │
    │ }                            │
    └──────────────────────────────┘
       │
       ▼
    CLIENT RECEIVES RESPONSE

```

---

## Payment Processing Sequence

```
Client / API Gateway
│
├─ POST /api/v1/payments
│  └─ PaymentRequest {
│      id, amount, currency,
│      buyerId, sellerId, carListingId,
│      paymentMethod, idempotencyKey
│     }
│
▼
PaymentController.initiatePayment()
│  HTTP 200 OK (Success)
│  HTTP 200 OK with warnings[] (Success with warnings)
│  HTTP 400 Bad Request (Validation/Fraud errors)
│  HTTP 409 Conflict (Duplicate - idempotent)
│  HTTP 500 Internal Server Error (System errors)
│
├─ Response: PaymentResponse {
│     "id": "PAY-UUID",
│     "transactionId": "TXN-XXXXXXXXXX",
│     "status": "PENDING",
│     "amount": 50000,
│     "currency": "INR",
│     "buyerId": "BUYER-123",
│     "sellerId": "SELLER-456",
│     "createdAt": "2026-03-31T10:15:30Z",
│     "warnings": [  ← May include non-blocking warnings
│       "High amount transaction detected"
│     ]
│   }
│
▼
PaymentService.initiate()
  │
  ├─ 1. Check Idempotency (if key already exists, return existing)
  │
  ├─ 2. Call DroolsRuleService.validatePayment()
  │    │
  │    ├─ KieSession created
  │    ├─ Facts inserted (PaymentRequest, RuleResult)
  │    ├─ Rules activated:
  │    │  ├─ payment-validation agenda group
  │    │  └─ fraud-detection agenda group
  │    ├─ Rules fire, violations & warnings collected
  │    └─ Results returned in RuleResult object
  │
  ├─ 3a. IF violations exist (blocking errors):
  │      └─ Throw PaymentValidationException
  │         → Controller catches → 400 Bad Request
  │
  ├─ 3b. IF only warnings exist (non-blocking):
  │      └─ Continue processing
  │         Include warnings[] in response
  │         Return 200 OK with warnings
  │
  ├─ 4. Create Payment entity (status = PENDING)
  │
  ├─ 5. Save to PaymentRepository
  │
  ├─ 6. Return PaymentResponse
  │
  └─ 7. (Optional) Schedule expiration task (24h timeout)

Scheduled Task (PaymentScheduler):
  Every 5 minutes:
    └─ Find all PENDING payments older than 24 hours
       └─ Update status to TIMEOUT
       └─ Log state transition

Confirm Payment Flow:
  Client: POST /api/v1/payments/{paymentId}/confirm
  │
  └─ PaymentService.confirmPayment(paymentId)
     │
     ├─ Fetch Payment by ID
     │
     ├─ Validate current status = PENDING
     │
     ├─ Update status to SUCCESS
     │
     ├─ Save Payment
     │
     └─ Return 200 OK with updated status

Fail Payment Flow:
  Client: POST /api/v1/payments/{paymentId}/fail
  │
  └─ PaymentService.failPayment(paymentId, reason)
     │
     ├─ Fetch Payment by ID
     │
     ├─ Validate current status = PENDING
     │
     ├─ Update status to FAILED
     │
     ├─ Store failure reason
     │
     ├─ Save Payment
     │
     └─ Return 200 OK with updated status

```

---

## Escrow & Refund Sequence

```
ESCROW PROCESSING:
══════════════════

Client: POST /api/v1/escrow
  └─ EscrowRequest {
      paymentId, amount, releaseDays, releaseCondition
     }
     │
     ▼
  EscrowService.create()
     │
     ├─ Fetch Payment by paymentId
     │
     ├─ Call DroolsRuleService.validateEscrow()
     │    └─ Checks:
     │       ├─ Payment exists and status = SUCCESS
     │       ├─ Escrow amount <= payment amount
     │       ├─ No existing escrow for this payment
     │       └─ Duration within limits
     │
     ├─ IF violations exist → 400 Bad Request
     │
     ├─ IF valid:
     │    ├─ Create Escrow entity (status = ACTIVE)
     │    ├─ Save to EscrowRepository
     │    └─ Return 200 OK
     │
     └─ EscrowResponse {
          "id": "ESC-UUID",
          "paymentId": "PAY-123",
          "amount": 25000,
          "status": "ACTIVE",
          "releaseDate": "2026-04-15T10:15:30Z",
          "createdAt": "2026-03-31T10:15:30Z"
        }

REFUND PROCESSING:
══════════════════

Client: POST /api/v1/refunds
  └─ RefundRequest {
      paymentId, amount, reason
     }
     │
     ▼
  RefundService.initiate()
     │
     ├─ Fetch Payment by paymentId
     │
     ├─ Call DroolsRuleService.validateRefund()
     │    └─ Checks:
     │       ├─ Payment exists and status = SUCCESS
     │       ├─ Refund within 30-day window
     │       ├─ Refund amount <= payment amount
     │       ├─ No prior full refund
     │       ├─ Reason validation
     │       └─ Calculate refund with fees
     │
     ├─ IF violations exist → 400 Bad Request
     │
     ├─ IF valid:
     │    ├─ Create Refund entity (status = PENDING)
     │    ├─ Save to RefundRepository
     │    └─ Return 200 OK
     │
     └─ RefundResponse {
          "id": "REF-UUID",
          "paymentId": "PAY-123",
          "amount": 50000,
          "status": "PENDING",
          "reason": "Customer request",
          "createdAt": "2026-03-31T10:15:30Z"
        }

```

---

## Database Entity Model

```
┌──────────────────────────────────────────────────────────────────┐
│                         PAYMENTS TABLE                           │
├──────────────────────────────────────────────────────────────────┤
│ Column Name        │ Type         │ Constraints / Notes           │
├────────────────────┼──────────────┼───────────────────────────────┤
│ id (PK)            │ UUID/String  │ Primary Key, Auto-generated   │
│ transaction_id     │ VARCHAR(50)  │ Unique, Business Identifier   │
│ amount             │ DECIMAL(15,2)│ NOT NULL, > 0                 │
│ currency           │ VARCHAR(3)   │ NOT NULL, Enum: INR, USD      │
│ status             │ ENUM         │ PENDING, SUCCESS, FAILED,     │
│                    │              │ TIMEOUT, CANCELLED            │
│ buyer_id           │ VARCHAR(50)  │ NOT NULL, Foreign Key (users) │
│ seller_id          │ VARCHAR(50)  │ NOT NULL, Foreign Key (users) │
│ car_listing_id     │ VARCHAR(50)  │ NOT NULL, Foreign Key (cars)  │
│ payment_method     │ VARCHAR(50)  │ CREDIT_CARD, DEBIT_CARD, etc. │
│ idempotency_key    │ VARCHAR(100) │ Unique, Duplicate prevention  │
│ created_at         │ TIMESTAMP    │ Auto-set on creation          │
│ updated_at         │ TIMESTAMP    │ Auto-updated on state change  │
│ expires_at         │ TIMESTAMP    │ Calculated: created_at + 24h  │
│ failure_reason     │ TEXT         │ NULL if status != FAILED      │
├──────────────────────────────────────────────────────────────────┤
│ Indexes:                                                         │
│ - buyer_id (for buyer payment history)                           │
│ - seller_id (for seller payment history)                         │
│ - transaction_id (for transaction lookup)                        │
│ - idempotency_key (for duplicate detection)                      │
│ - status + updated_at (for expiration queries)                   │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                         ESCROWS TABLE                            │
├──────────────────────────────────────────────────────────────────┤
│ Column Name        │ Type         │ Constraints / Notes           │
├────────────────────┼──────────────┼───────────────────────────────┤
│ id (PK)            │ UUID/String  │ Primary Key, Auto-generated   │
│ payment_id (FK)    │ VARCHAR(50)  │ Foreign Key → payments.id     │
│ amount             │ DECIMAL(15,2)│ NOT NULL, <= payment.amount   │
│ status             │ ENUM         │ ACTIVE, RELEASED, FORFEITED   │
│ release_date       │ TIMESTAMP    │ Calculated: created_at + days │
│ release_condition  │ VARCHAR(100) │ Description of release terms  │
│ created_at         │ TIMESTAMP    │ Auto-set on creation          │
│ updated_at         │ TIMESTAMP    │ Auto-updated on state change  │
│ released_at        │ TIMESTAMP    │ NULL until released           │
├──────────────────────────────────────────────────────────────────┤
│ Indexes:                                                         │
│ - payment_id (for escrow lookup by payment)                      │
│ - status + release_date (for scheduled release tasks)            │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                         REFUNDS TABLE                            │
├──────────────────────────────────────────────────────────────────┤
│ Column Name        │ Type         │ Constraints / Notes           │
├────────────────────┼──────────────┼───────────────────────────────┤
│ id (PK)            │ UUID/String  │ Primary Key, Auto-generated   │
│ payment_id (FK)    │ VARCHAR(50)  │ Foreign Key → payments.id     │
│ amount             │ DECIMAL(15,2)│ NOT NULL, <= payment.amount   │
│ reason             │ VARCHAR(200) │ Refund reason from client     │
│ status             │ ENUM         │ PENDING, COMPLETED, FAILED    │
│ created_at         │ TIMESTAMP    │ Auto-set on creation          │
│ updated_at         │ TIMESTAMP    │ Auto-updated on state change  │
│ completed_at       │ TIMESTAMP    │ NULL until completed          │
├──────────────────────────────────────────────────────────────────┤
│ Indexes:                                                         │
│ - payment_id (for refund lookup by payment)                      │
│ - status (for pending refund queries)                            │
└──────────────────────────────────────────────────────────────────┘

Database Relationships:
═══════════════════════

payments (1) ─── (Many) escrows
  └─ FK: escrows.payment_id → payments.id

payments (1) ─── (Many) refunds
  └─ FK: refunds.payment_id → payments.id

payments.buyer_id → users table
payments.seller_id → users table
payments.car_listing_id → car_listings table

```

---

## API Endpoints Architecture

```
╔══════════════════════════════════════════════════════════════════╗
║              PAYMENT MICROSERVICE - API ENDPOINTS                ║
╚══════════════════════════════════════════════════════════════════╝

PAYMENT ENDPOINTS:
══════════════════

1. INITIATE PAYMENT
   ─────────────────
   Method:    POST
   Endpoint:  /api/v1/payments
   
   Request Body:
   {
     "amount": 50000,
     "currency": "INR",
     "buyerId": "BUYER-123",
     "sellerId": "SELLER-456",
     "carListingId": "LISTING-789",
     "paymentMethod": "CREDIT_CARD",
     "idempotencyKey": "unique-key-xyz"
   }
   
   Success Response: 200 OK
   {
     "id": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "transactionId": "TXN-20260331001",
     "status": "PENDING",
     "amount": 50000,
     "currency": "INR",
     "buyerId": "BUYER-123",
     "sellerId": "SELLER-456",
     "createdAt": "2026-03-31T10:15:30Z",
     "warnings": []  ← Empty if no warnings
   }
   
   Success with Warnings: 200 OK
   {
     "id": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "transactionId": "TXN-20260331001",
     "status": "PENDING",
     "amount": 50000,
     "currency": "INR",
     "buyerId": "BUYER-123",
     "sellerId": "SELLER-456",
     "createdAt": "2026-03-31T10:15:30Z",
     "warnings": [  ← Non-blocking warnings
       "High amount transaction detected",
       "Unusual time window"
     ]
   }
   
   Error Responses:
   400 Bad Request
   {
     "error": "Validation Failed",
     "violations": [
       "Amount must be greater than 0",
       "Buyer ID cannot be null",
       "Invalid payment method"
     ],
     "timestamp": "2026-03-31T10:15:30Z"
   }
   
   409 Conflict (Duplicate - Idempotent)
   {
     "error": "Duplicate Payment",
     "message": "Payment already exists for this idempotency key",
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "timestamp": "2026-03-31T10:15:30Z"
   }
   
   500 Internal Server Error
   {
     "error": "System Error",
     "message": "Unexpected error processing payment",
     "timestamp": "2026-03-31T10:15:30Z"
   }

2. CONFIRM PAYMENT
   ────────────────
   Method:    POST
   Endpoint:  /api/v1/payments/{paymentId}/confirm
   
   Success Response: 200 OK
   {
     "id": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "transactionId": "TXN-20260331001",
     "status": "SUCCESS",
     "amount": 50000,
     "currency": "INR",
     "updatedAt": "2026-03-31T10:20:15Z"
   }
   
   Error Response: 404 Not Found
   {
     "error": "Payment Not Found",
     "message": "Payment with ID PAY-xyz not found",
     "timestamp": "2026-03-31T10:15:30Z"
   }
   
   Error Response: 400 Bad Request
   {
     "error": "Invalid State Transition",
     "message": "Cannot confirm payment with status FAILED",
     "timestamp": "2026-03-31T10:15:30Z"
   }

3. FAIL PAYMENT
   ─────────────
   Method:    POST
   Endpoint:  /api/v1/payments/{paymentId}/fail
   
   Request Body:
   {
     "reason": "Insufficient funds"
   }
   
   Success Response: 200 OK
   {
     "id": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "transactionId": "TXN-20260331001",
     "status": "FAILED",
     "amount": 50000,
     "failureReason": "Insufficient funds",
     "updatedAt": "2026-03-31T10:25:45Z"
   }
   
   Error Response: 404 Not Found
   {
     "error": "Payment Not Found",
     "message": "Payment with ID PAY-xyz not found",
     "timestamp": "2026-03-31T10:15:30Z"
   }

4. GET PAYMENT BY ID
   ──────────────────
   Method:    GET
   Endpoint:  /api/v1/payments/{paymentId}
   
   Success Response: 200 OK
   {
     "id": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "transactionId": "TXN-20260331001",
     "status": "PENDING",
     "amount": 50000,
     "currency": "INR",
     "buyerId": "BUYER-123",
     "sellerId": "SELLER-456",
     "carListingId": "LISTING-789",
     "paymentMethod": "CREDIT_CARD",
     "createdAt": "2026-03-31T10:15:30Z",
     "updatedAt": "2026-03-31T10:15:30Z"
   }

5. LIST BUYER PAYMENTS
   ────────────────────
   Method:    GET
   Endpoint:  /api/v1/payments/buyer/{buyerId}
   Query Params: page=0, size=20, sort=createdAt,desc
   
   Success Response: 200 OK
   {
     "content": [
       {
         "id": "PAY-550e8400-e29b-41d4-a716-446655440000",
         "transactionId": "TXN-20260331001",
         "status": "SUCCESS",
         "amount": 50000,
         "currency": "INR",
         "createdAt": "2026-03-31T10:15:30Z"
       }
     ],
     "totalElements": 42,
     "totalPages": 3,
     "currentPage": 0,
     "hasNext": true
   }

6. LIST SELLER PAYMENTS
   ─────────────────────
   Method:    GET
   Endpoint:  /api/v1/payments/seller/{sellerId}
   Query Params: page=0, size=20, sort=createdAt,desc
   
   Success Response: 200 OK (Same structure as buyer payments)

ESCROW ENDPOINTS:
═════════════════

1. CREATE ESCROW
   ──────────────
   Method:    POST
   Endpoint:  /api/v1/escrow
   
   Request Body:
   {
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "amount": 25000,
     "releaseDays": 15,
     "releaseCondition": "Upon delivery"
   }
   
   Success Response: 200 OK
   {
     "id": "ESC-550e8400-e29b-41d4-a716-446655440001",
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "amount": 25000,
     "status": "ACTIVE",
     "releaseDate": "2026-04-15T10:15:30Z",
     "createdAt": "2026-03-31T10:15:30Z"
   }
   
   Error Response: 400 Bad Request
   {
     "error": "Escrow Validation Failed",
     "violations": [
       "Payment must exist and have SUCCESS status",
       "Escrow amount cannot exceed payment amount"
     ]
   }

2. RELEASE ESCROW
   ───────────────
   Method:    POST
   Endpoint:  /api/v1/escrow/{escrowId}/release
   
   Success Response: 200 OK
   {
     "id": "ESC-550e8400-e29b-41d4-a716-446655440001",
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "status": "RELEASED",
     "releasedAt": "2026-04-15T10:15:30Z"
   }

3. GET ESCROW BY ID
   ─────────────────
   Method:    GET
   Endpoint:  /api/v1/escrow/{escrowId}
   
   Success Response: 200 OK
   {
     "id": "ESC-550e8400-e29b-41d4-a716-446655440001",
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "amount": 25000,
     "status": "ACTIVE",
     "releaseDate": "2026-04-15T10:15:30Z",
     "createdAt": "2026-03-31T10:15:30Z"
   }

4. LIST ESCROWS BY PAYMENT
   ────────────────────────
   Method:    GET
   Endpoint:  /api/v1/escrow/payment/{paymentId}
   
   Success Response: 200 OK (Array of escrow objects)

REFUND ENDPOINTS:
═════════════════

1. INITIATE REFUND
   ────────────────
   Method:    POST
   Endpoint:  /api/v1/refunds
   
   Request Body:
   {
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "amount": 50000,
     "reason": "Customer request"
   }
   
   Success Response: 200 OK
   {
     "id": "REF-550e8400-e29b-41d4-a716-446655440002",
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "amount": 50000,
     "status": "PENDING",
     "reason": "Customer request",
     "createdAt": "2026-03-31T10:15:30Z"
   }
   
   Error Response: 400 Bad Request
   {
     "error": "Refund Validation Failed",
     "violations": [
       "Payment must exist and have SUCCESS status",
       "Refund must be within 30-day window"
     ]
   }

2. COMPLETE REFUND
   ────────────────
   Method:    POST
   Endpoint:  /api/v1/refunds/{refundId}/complete
   
   Success Response: 200 OK
   {
     "id": "REF-550e8400-e29b-41d4-a716-446655440002",
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "status": "COMPLETED",
     "completedAt": "2026-03-31T10:30:00Z"
   }

3. GET REFUND BY ID
   ─────────────────
   Method:    GET
   Endpoint:  /api/v1/refunds/{refundId}
   
   Success Response: 200 OK
   {
     "id": "REF-550e8400-e29b-41d4-a716-446655440002",
     "paymentId": "PAY-550e8400-e29b-41d4-a716-446655440000",
     "amount": 50000,
     "status": "PENDING",
     "reason": "Customer request",
     "createdAt": "2026-03-31T10:15:30Z"
   }

4. LIST REFUNDS BY PAYMENT
   ────────────────────────
   Method:    GET
   Endpoint:  /api/v1/refunds/payment/{paymentId}
   
   Success Response: 200 OK (Array of refund objects)

HTTP STATUS CODE SUMMARY:
═════════════════════════

200 OK                     - Successful operations (POST, GET)
400 Bad Request            - Validation/Fraud violations (blocking errors)
404 Not Found              - Resource not found
409 Conflict               - Duplicate payment (idempotent)
500 Internal Server Error  - System/database errors

```

---

## Drools Rules Engine Architecture

### ASCII Art - Drools Integration Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DROOLS RULES ENGINE ARCHITECTURE                  │
└─────────────────────────────────────────────────────────────────────┘

    ┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
    │  DroolsConfig  │    │ DroolsRuleService│    │   Rule Files     │
    │                 │    │                  │    │   (.drl)        │
    │ • KieServices  │    │ • KieContainer   │    │                 │
    │ • KieFileSystem│    │ • KieSession     │    │ payment-        │
    │ • KieBuilder   │    │ • Agenda Groups  │    │ validation.drl  │
    │ • KieContainer │    │ • Global Vars    │    │                 │
    └─────────┬───────┘    └─────────┬────────┘    │ fraud-          │
              │                      │             │ detection.drl   │
              │                      │             │                 │
              ▼                      ▼             │ escrow-rules.drl│
    ┌─────────────────┐    ┌──────────────────┐    │                 │
    │ KieContainer    │    │   KieSession     │    │ refund-         │
    │ (Compiled Rules)│◄──►│ (Rule Execution) │    │ eligibility.drl │
    │                 │    │                  │    └─────────────────┘
    │ • Rule Packages │    │ • Facts Insert  │
    │ • Agenda Groups │    │ • Rules Fire    │
    │ • Global Vars   │    │ • Results Collect│
    └─────────────────┘    └──────────────────┘

RULE EXECUTION FLOW:
════════════════════

1. Service calls DroolsRuleService.validateXxx()
2. KieSession created from KieContainer
3. Global variables set (maxAmount, maxAttempts, etc.)
4. Facts inserted (PaymentRequest, RuleResult, etc.)
5. Agenda group activated (payment-validation, fraud-detection, etc.)
6. Rules fired based on conditions and salience
7. Results collected in RuleResult object:
   - violations[] (blocking errors)
   - warnings[] (non-blocking alerts)
8. KieSession disposed
9. Validation results returned to service

```

---

## Rule Categories & Agenda Groups

The Drools rules engine organizes business logic into 4 specialized rule files, each with dedicated agenda groups:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        RULE CATEGORIES                              │
├─────────────────────────────────────────────────────────────────────┤
│ PAYMENT-VALIDATION.DRL                                              │
│ Agenda Group: payment-validation                                    │
│ ├─ Amount must be > 0                                               │
│ ├─ Currency must be supported                                       │
│ ├─ Buyer ID must be valid                                           │
│ ├─ Seller ID must be valid                                          │
│ ├─ Payment method must be supported                                 │
│ └─ No duplicate transaction ID                                      │
├─────────────────────────────────────────────────────────────────────┤
│ FRAUD-DETECTION.DRL                                                 │
│ Agenda Group: fraud-detection                                       │
│ ├─ Failed attempts < maxAttempts (default: 3)                       │
│ ├─ Amount < maxAmount (default: 10000)                              │
│ ├─ Time window check (1 hour)                                       │
│ ├─ IP address validation                                            │
│ └─ Suspicious pattern detection                                     │
├─────────────────────────────────────────────────────────────────────┤
│ ESCROW-RULES.DRL                                                    │
│ Agenda Group: escrow-rules                                          │
│ ├─ Payment must exist and be SUCCESS                                │
│ ├─ Escrow amount <= payment amount                                  │
│ ├─ No existing escrow for transaction                               │
│ ├─ Escrow duration within limits                                    │
│ └─ Release conditions validation                                    │
├─────────────────────────────────────────────────────────────────────┤
│ REFUND-ELIGIBILITY.DRL                                              │
│ Agenda Group: refund-eligibility                                    │
│ ├─ Payment must be SUCCESS                                          │
│ ├─ Refund within time window (30 days)                              │
│ ├─ Refund amount <= payment amount                                  │
│ ├─ No prior full refund                                             │
│ ├─ Refund reason validation                                         │
│ └─ Calculate refund amount (with fees)                              │
└─────────────────────────────────────────────────────────────────────┘
```

### Rule Execution Flow

```
DROOLS EXECUTION SEQUENCE:
══════════════════════════

1. KieContainer loaded with compiled .drl files
2. KieSession created for each validation request
3. Global variables set (maxAmount, maxAttempts, etc.)
4. Payment facts inserted into working memory
5. Agenda groups activated in priority order:
   - payment-validation (highest priority)
   - fraud-detection (medium priority)
   - business-rules (lowest priority)
6. Rules fire based on conditions and salience values
7. Violations and warnings accumulated in RuleResult
8. After all rules complete:
   ├─ IF violations.isEmpty():
   │  └─ Processing continues (return violations, warnings)
   │
   └─ IF violations.isNotEmpty():
      └─ Exception will be thrown by service layer (400 Bad Request)
9. KieSession disposed, resources cleaned up
10. Results returned to service for response generation

VIOLATIONS vs WARNINGS DIFFERENTIATION:
════════════════════════════════════════

┌────────────────────────────────────────────────────────────────┐
│                     VALIDATION RESULT                          │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  RuleResult {                                                  │
│    violations[]: [Blocking errors]                            │
│    warnings[]: [Non-blocking alerts]                          │
│  }                                                             │
│                                                                │
│  ┌─ violations (Critical)                                      │
│  │  └─ Prevent transaction processing                         │
│  │     └─ Throw Exception → 400 Bad Request                  │
│  │        Example: "Amount must be > 0"                      │
│  │                 "Fraud pattern detected"                  │
│  │                 "Buyer ID cannot be null"                │
│  │                                                            │
│  └─ warnings (Non-blocking)                                   │
│     └─ Allow processing to continue                          │
│        └─ Return 200 OK with warnings[]                      │
│           Example: "High amount detected"                    │
│                    "Unusual time window"                     │
│                    "Frequent transactions"                   │
│                                                                │
│  Processing Decision:                                         │
│  ├─ IF violations.isEmpty() && warnings.isEmpty()            │
│  │  └─ Return 200 OK { status: PENDING, warnings: [] }      │
│  │                                                            │
│  ├─ IF violations.isEmpty() && !warnings.isEmpty()           │
│  │  └─ Return 200 OK { status: PENDING, warnings: [...] }   │
│  │                                                            │
│  └─ IF !violations.isEmpty()                                 │
│     └─ Throw PaymentValidationException                      │
│        ↓                                                       │
│        ControllerAdvice catches                              │
│        ↓                                                       │
│        Return 400 Bad Request with violations[]              │
│                                                                │
└────────────────────────────────────────────────────────────────┘

```

---

## Warning Handling Strategy

### Overview

The system differentiates between **blocking validation errors** and **non-blocking warnings**. Errors prevent transaction processing, while warnings allow processing but notify the client.

### Warning Flow in Rule Engine

```
┌──────────────────────────────────────────────────────────────────┐
│                   RULE EXECUTION RESULT HANDLING                 │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  DroolsRuleService.validatePayment() executes rules             │
│                                                                  │
│  └─ RuleResult object populated with:                           │
│     ├─ violations[] (Critical blocking errors)                  │
│     └─ warnings[] (Non-blocking alerts)                         │
│                                                                  │
│  ┌─ Check RuleResult                                            │
│  │                                                               │
│  ├─── IF violations.size() > 0                                  │
│  │    └─ BLOCKING: Throw Exception                             │
│  │       └─ PaymentValidationException                         │
│  │          └─ Controller catches & returns 400 Bad Request    │
│  │             {                                               │
│  │               "error": "Validation Failed",                 │
│  │               "violations": [                               │
│  │                 "Amount must be > 0",                       │
│  │                 "Fraud pattern detected"                    │
│  │               ]                                             │
│  │             }                                               │
│  │                                                               │
│  └─── ELSE (violations.isEmpty())                              │
│       └─ NON-BLOCKING: Continue Processing                     │
│          ├─ Create Payment entity                              │
│          ├─ Save to database (status = PENDING)                │
│          └─ Return 200 OK Response:                            │
│             {                                                  │
│               "id": "PAY-123",                                  │
│               "status": "PENDING",                             │
│               "transactionId": "TXN-ABC",                       │
│               "warnings": [  ← Include warnings if present     │
│                 "High amount transaction detected",            │
│                 "Unusual time window"                          │
│               ],                                               │
│               "createdAt": "2026-03-31T10:15:30Z"              │
│             }                                                  │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘

WARNING EXAMPLE SCENARIOS:
══════════════════════════

Scenario 1: High Amount Detection
──────────────────────────────────
  Rule: "IF amount > 50000 THEN add warning"
  
  Result:
  ├─ violations: [] (empty - no blocking errors)
  ├─ warnings: ["High amount transaction detected"]
  └─ Action: Return 200 OK with warnings included

Scenario 2: High Amount + Blocking Violation
──────────────────────────────────────────────
  Rules:
  ├─ "IF amount > 50000 THEN add warning"
  └─ "IF amount <= 0 THEN add violation"
  
  Request with amount = -100:
  ├─ violations: ["Amount must be > 0"]
  ├─ warnings: [] (warning condition not met)
  └─ Action: Return 400 Bad Request (violations present)

Scenario 3: Fraud Pattern + Warnings
─────────────────────────────────────
  Rules:
  ├─ "IF failedAttempts > 3 THEN add violation" (blocking)
  └─ "IF amount > 75000 THEN add warning"
  
  Request with failedAttempts=4, amount=80000:
  ├─ violations: ["Blocked: Fraud pattern detected"]
  ├─ warnings: ["High amount transaction detected"]
  └─ Action: Return 400 Bad Request (violations take precedence)

Scenario 4: Only Warnings, No Violations
──────────────────────────────────────────
  Request with amount=60000 (no violations, just warnings):
  ├─ violations: [] (empty)
  ├─ warnings: [
  │   "High amount transaction detected",
  │   "Unusual time window for this buyer"
  │ ]
  └─ Action: Return 200 OK with warnings array included
            (Transaction proceeds, client is alerted)

DIFFERENCES AT A GLANCE:
════════════════════════

┌─────────────────┬──────────────────┬──────────────────┐
│                 │ VIOLATIONS       │ WARNINGS         │
│                 │ (Blocking)       │ (Non-blocking)   │
├─────────────────┼──────────────────┼──────────────────┤
│ Processing      │ STOPS            │ CONTINUES        │
│ HTTP Status     │ 400 Bad Request  │ 200 OK           │
│ Stored in DB    │ failure_reason   │ (in response)    │
│ User Action     │ Must fix & retry │ Optional alert   │
│ Examples        │ "Amount < 1"     │ "High amount"    │
│                 │ "Fraud detected" │ "Unusual timing" │
│                 │ "Buyer null"     │ "Risky pattern"  │
├─────────────────┼──────────────────┼──────────────────┤
│ Transaction     │ NOT created      │ CREATED          │
│ in PENDING      │                  │ (status:PENDING) │
└─────────────────┴──────────────────┴──────────────────┘

```

---

## Payment Validation Deep-Dive

```
PAYMENT VALIDATION PROCESS:
═══════════════════════════

    ┌───────────────────────────────────────┐
    │   PaymentRequest Received             │
    │   (API /v1/payments)                  │
    └─────────┬─────────────────────────────┘
              │
              ▼
    ┌──────────────────────────────────────┐
    │ Call DroolsRuleService               │
    │ .validatePayment(request)            │
    └─────────┬────────────────────────────┘
              │
              ▼
    ┌─────────────────────────────────────────────────────┐
    │  Rule Engine Validation (Drools KieSession)        │
    │  ┌───────────────────────────────────────────────┐  │
    │  │ VALIDATION RULES                             │  │
    │  │ ────────────────────────────────────────────  │  │
    │  │ ┌─────────────────────────────────────────┐  │  │
    │  │ │ AMOUNT CHECKS:                          │  │  │
    │  │ │ ├─ amount > 0 ?                         │  │  │
    │  │ │ │  (violation if false)                 │  │  │
    │  │ │ │  "Amount must be greater than 0"     │  │  │
    │  │ │ │                                       │  │  │
    │  │ │ ├─ amount > 50000 ?                     │  │  │
    │  │ │ │  (warning if true)                    │  │  │
    │  │ │ │  "High amount transaction detected"   │  │  │
    │  │ │ │                                       │  │  │
    │  │ └─ amount < maxAmount (10000 limit)?       │  │  │
    │  │    (violation if false - fraud pattern)    │  │  │
    │  │    "Amount exceeds fraud threshold"        │  │  │
    │  │                                             │  │
    │  │ ┌─────────────────────────────────────────┐  │  │
    │  │ │ CURRENCY CHECKS:                        │  │  │
    │  │ │ └─ currency in [INR, USD, EUR] ?       │  │  │
    │  │    (violation if false)                   │  │  │
    │  │    "Currency must be supported"           │  │  │
    │  │                                             │  │
    │  │ ┌─────────────────────────────────────────┐  │  │
    │  │ │ BUYER CHECKS:                           │  │  │
    │  │ │ ├─ buyerId != null ?                     │  │  │
    │  │ │ │  (violation if false)                 │  │  │
    │  │ │ │  "Buyer ID cannot be null"            │  │  │
    │  │ │ │                                       │  │  │
    │  │ │ └─ buyer exists in DB ?                 │  │  │
    │  │    (violation if false)                   │  │  │
    │  │    "Buyer not found in system"            │  │  │
    │  │                                             │  │
    │  │ ┌─────────────────────────────────────────┐  │  │
    │  │ │ SELLER CHECKS:                          │  │  │
    │  │ │ ├─ sellerId != null ?                    │  │  │
    │  │ │ │  (violation if false)                 │  │  │
    │  │ │ │  "Seller ID cannot be null"           │  │  │
    │  │ │ │                                       │  │  │
    │  │ │ └─ seller exists in DB ?                │  │  │
    │  │    (violation if false)                   │  │  │
    │  │    "Seller not found in system"           │  │  │
    │  │                                             │  │
    │  │ ┌─────────────────────────────────────────┐  │  │
    │  │ │ PAYMENT METHOD CHECKS:                  │  │  │
    │  │ │ └─ method in [CREDIT_CARD, ...] ?      │  │  │
    │  │    (violation if false)                   │  │  │
    │  │    "Invalid payment method"               │  │  │
    │  │                                             │  │
    │  │ ┌─────────────────────────────────────────┐  │  │
    │  │ │ IDEMPOTENCY & DUPLICATE CHECKS:         │  │  │
    │  │ │ └─ idempotencyKey unique ?              │  │  │
    │  │    (return existing if duplicate)         │  │  │
    │  │    "Duplicate payment prevented"          │  │  │
    │  │                                             │  │
    │  │ ┌─────────────────────────────────────────┐  │  │
    │  │ │ FRAUD DETECTION RULES:                  │  │  │
    │  │ │ ├─ failedAttempts <= 3 ?               │  │  │
    │  │ │ │  (violation if false)                 │  │  │
    │  │ │ │  "Blocked: Fraud pattern detected"    │  │  │
    │  │ │ │  "(3+ failed attempts in 1 hour)"     │  │  │
    │  │ │ │                                       │  │  │
    │  │ │ ├─ !suspiciousIpPattern ?               │  │  │
    │  │ │ │  (violation if true - blocked IP)     │  │  │
    │  │ │ │  "IP address flagged as suspicious"   │  │  │
    │  │ │ │                                       │  │  │
    │  │ │ └─ withinTimeWindow (1h) ?              │  │  │
    │  │    (warning if false - unusual timing)    │  │  │
    │  │    "Unusual time window for this buyer"   │  │  │
    │  │                                             │  │
    │  └─────────────────────────────────────────────┘  │
    │                                                     │
    │  Aggregate Results: violations[], warnings[]      │
    └──────────────────────┬──────────────────────────┘
                           │
                           ▼
                    ┌──────────────────┐
                    │ Aggregate ALL    │
                    │ violations[] &   │
                    │ warnings[]       │
                    └────────┬─────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
              VIOLATIONS        WARNINGS
                EMPTY?            ONLY?
                    │                 │
                    │YES              │
                    ├─────────┬───────┘
                    │         │
                    ▼         ▼
              ┌─────────────────────────┐
              │ Continue Processing     │
              │ (Create Payment entity) │
              │                         │
              │ Response: 200 OK        │
              │ Status: PENDING         │
              │                         │
              │ violations: []          │
              │ warnings: [...]  or []  │
              └──────────────┬──────────┘
                             │
                             ▼
                        CLIENT GETS
                        200 OK + Payment

           IF violations PRESENT:
                    │
                    ▼
            ┌─────────────────────┐
            │ Throw Exception:    │
            │ PaymentValidation   │
            │ Exception           │
            │                     │
            │ Controller catches: │
            │ Return 400 Bad Req  │
            │ violations: [...]   │
            └─────────────────────┘
                    │
                    ▼
               CLIENT GETS
               400 Bad Request

EXAMPLES OF RULE VIOLATIONS:
════════════════════════════

Validation Block (No Processing):
─────────────────────────────────

┌─────────────────┬──────────────────────────┬────────────────┐
│ Violation Type  │ Condition                │ Error Message  │
├─────────────────┼──────────────────────────┼────────────────┤
│ Amount          │ request.amount <= 0      │ "Amount must be│
│                 │                          │  greater than  │
│                 │                          │  0"            │
├─────────────────┼──────────────────────────┼────────────────┤
│ Buyer ID        │ request.buyerId == null  │ "Buyer ID      │
│                 │                          │  cannot be     │
│                 │                          │  null"         │
├─────────────────┼──────────────────────────┼────────────────┤
│ Seller ID       │ request.sellerId ==null  │ "Seller ID     │
│                 │                          │  cannot be     │
│                 │                          │  null"         │
├─────────────────┼──────────────────────────┼────────────────┤
│ Car Listing     │ carListing not in DB     │ "Car listing   │
│                 │                          │  not found"    │
├─────────────────┼──────────────────────────┼────────────────┤
│ Payment Method  │ method not in enum       │ "Invalid       │
│                 │                          │  payment       │
│                 │                          │  method"       │
├─────────────────┼──────────────────────────┼────────────────┤
│ Fraud Pattern   │ failedAttempts > 3       │ "Blocked:      │
│                 │                          │  Fraud pattern │
│                 │                          │  detected"     │
├─────────────────┼──────────────────────────┼────────────────┤
│ Suspicious IP   │ IP in fraud DB           │ "IP address    │
│                 │                          │  flagged as    │
│                 │                          │  suspicious"   │
├─────────────────┼──────────────────────────┼────────────────┤
│ Currency        │ currency not in          │ "Currency must │
│                 │ [INR, USD, EUR]          │  be supported" │
└─────────────────┴──────────────────────────┴────────────────┘

```

---

## Testing Strategy - TDD Approach

### Unit Test Flow Diagram

```
┌────────────────────────────────────────────────────────────────┐
│               TDD TEST HIERARCHY - Payment Service             │
└───────────────────────────────┬────────────────────────────────┘

Level 1: UNIT TESTS
══════════════════════════════════════════════════════════════════
                │
    ┌───────────┼───────────┬──────────────┬────────────────┐
    │           │           │              │                │
    ▼           ▼           ▼              ▼                ▼
┌────────┐ ┌────────┐ ┌──────────┐ ┌────────────┐ ┌──────────────┐
│Service │ │Rule    │ │Repository│ │Controller  │ │Security      │
│Tests   │ │Engine  │ │Tests     │ │Tests       │ │Util Tests    │
│        │ │Tests   │ │          │ │            │ │              │
│- Init  │ │- Valid │ │- Save    │ │- Endpoint  │ │- Token Gen   │
│- Conf  │ │  Rules │ │  Entity  │ │  Mapping   │ │- Extract Usr │
│- Fail  │ │- Fraud │ │- Query   │ │- Response  │ │- Validate    │
│- List  │ │  Check │ │  Methods │ │  Format    │ │- Parse       │
└────────┘ └────────┘ └──────────┘ └────────────┘ └──────────────┘

Level 2: INTEGRATION TESTS
══════════════════════════════════════════════════════════════════
                │
    ┌───────────┘
    │
    ▼
┌─────────────────────────────────┐
│ Payment Processing Integration  │
│  - initiatePayment() flow        │
│  - confirmPayment() flow         │
│  - failPayment() flow            │
│  Uses: @SpringBootTest,          │
│        @DataJpaTest,             │
│        TestRestTemplate          │
└─────────────────────────────────┘

Level 3: CONTRACT/API TESTS
══════════════════════════════════════════════════════════════════
                │
    ┌───────────┼───────────────┐
    │           │               │
    ▼           ▼               ▼
┌────────┐ ┌───────────┐ ┌──────────────┐
│Request │ │Response   │ │Status Codes  │
│Schema  │ │Schema     │ │ HTTP Codes   │
│Valid   │ │Valid      │ │  200 OK      │
│Invalid │ │Errors     │ │  400 Bad Req │
│        │ │Fields OK  │ │  409 Conflict│
└────────┘ └───────────┘ └──────────────┘

TEST CASE MATRIX - Payment Scenarios
═════════════════════════════════════════════════════════════════

Scenario                         | Expected Status | Rule Violations
─────────────────────────────────────────────────────────────────────
Valid payment request            | PENDING         | []
Duplicate idempotency key        | Return existing | N/A
Negative amount                  | Validation Err  | "Amount > 0"
Null buyer ID                    | Validation Err  | "Buyer not null"
Fraud pattern (3+ fails/hour)    | Validation Err  | "Fraud blocked"
Invalid payment method           | Validation Err  | "Invalid method"
Payment timeout expires          | Task: auto-fail │ N/A
Confirm with wrong status        | Not Found Err   | N/A
Confirm payment success          | SUCCESS         | []
Get by transaction ID            | SUCCESS         | N/A
Get buyer payments (pagination)  | List response   | N/A

```
