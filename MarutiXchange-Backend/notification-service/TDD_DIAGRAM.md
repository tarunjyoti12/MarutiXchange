# Notification Service - TDD Architecture Diagram

## Overview
This document provides a comprehensive Test-Driven Development (TDD) architecture diagram for the Notification Service microservice, including sequence diagrams and data flow diagrams.

---

## 1. SEQUENCE DIAGRAM - Notification Send Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 NOTIFICATION SERVICE - SEND FLOW SEQUENCE                                                      │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

     Client              Controller              Service              RuleEngine              Database              Response
       │                    │                       │                     │                      │                       │
       │   POST /send        │                       │                     │                      │                       │
       ├───────────────────>│                       │                     │                      │                       │
       │                    │  (NotificationRequest) │                     │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │  send(request)        │                     │                      │                       │
       │                    ├──────────────────────>│                     │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │  buildRuleContext() │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │  evaluate(context)  │                      │                       │
       │                    │                       ├────────────────────>│                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │                     │ (evaluate rules)     │                       │
       │                    │                       │<────────────────────┤                      │                       │
       │                    │                       │   RuleContext       │                      │                       │
       │                    │                       │   (approved status) │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │ [If approved]       │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │ override channel    │                      │                       │
       │                    │                       │ (if needed)         │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │ createNotificationLog()                   │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │ save(notif)         │                      │                       │
       │                    │                       ├─────────────────────────────────────────>│                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │                     │            (INSERT)  │                       │
       │                    │                       │<─────────────────────────────────────────┤                       │
       │                    │                       │     NotificationLog │                      │                       │
       │                    │                       │     (with UUID)     │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │ updateStatus(SENT)  │                      │                       │
       │                    │                       │ setSentAt(now)      │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │ save(notif)         │                      │                       │
       │                    │                       ├─────────────────────────────────────────>│                       │
       │                    │                       │                     │                      │                       │
       │                    │                       │                     │            (UPDATE) │                       │
       │                    │                       │<─────────────────────────────────────────┤                       │
       │                    │                       │     NotificationLog │                      │                       │
       │                    │                       │     (Updated)       │                      │                       │
       │                    │                       │                     │                      │                       │
       │                    │ NotificationResponse  │                     │                      │                       │
       │                    │<──────────────────────┤                     │                      │                       │
       │                    │ (with notifId, status)                      │                      │                       │
       │                    │                       │                     │                      │                       │
       │  ApiResponse       │                       │                     │                      │                       │
       │<──────────────────│                       │                     │                      │                       │
       │  (success=true)    │                       │                     │                      │                       │
       │  (data)            │                       │                     │                      │                       │
       │                    │                       │                     │                      │                       │
```

---

## 2. SEQUENCE DIAGRAM - Get Notification by User

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│          NOTIFICATION SERVICE - GET NOTIFICATIONS BY USER SEQUENCE              │
└─────────────────────────────────────────────────────────────────────────────────┘

     Client              Controller              Service              Database
       │                    │                       │                    │
       │ GET /user/{userId} │                       │                    │
       │?page=0&size=20     │                       │                    │
       ├───────────────────>│                       │                    │
       │                    │                       │                    │
       │                    │ getByUserId(userId,   │                    │
       │                    │ PageRequest)          │                    │
       │                    ├──────────────────────>│                    │
       │                    │                       │                    │
       │                    │                       │ SELECT * FROM      │
       │                    │                       │ notification_log   │
       │                    │                       │ WHERE user_id = ?  │
       │                    │                       │ LIMIT ? OFFSET ?   │
       │                    │                       │                    │
       │                    │                       ├───────────────────>│
       │                    │                       │                    │
       │                    │                       │ (query results)    │
       │                    │                       │<───────────────────┤
       │                    │                       │                    │
       │                    │ Page<Notification     │                    │
       │                    │ Response>             │                    │
       │                    │<──────────────────────┤                    │
       │                    │                       │                    │
       │  ApiResponse       │                       │                    │
       │<──────────────────│                       │                    │
       │  (Page<Data>)      │                       │                    │
       │                    │                       │                    │
```

---

## 3. SEQUENCE DIAGRAM - Get Notification by ID

```
┌──────────────────────────────────────────────────────────────────────────────┐
│         NOTIFICATION SERVICE - GET NOTIFICATION BY ID SEQUENCE               │
└──────────────────────────────────────────────────────────────────────────────┘

     Client              Controller              Service              Database
       │                    │                       │                    │
       │ GET /{notificationId}                      │                    │
       ├───────────────────>│                       │                    │
       │                    │                       │                    │
       │                    │ getById(notificationId)                    │
       │                    ├──────────────────────>│                    │
       │                    │                       │                    │
       │                    │                       │ SELECT * FROM      │
       │                    │                       │ notification_log   │
       │                    │                       │ WHERE id = ?       │
       │                    │                       │                    │
       │                    │                       ├───────────────────>│
       │                    │                       │                    │
       │                    │                       │ (one record)       │
       │                    │                       │<───────────────────┤
       │                    │                       │                    │
       │                    │ [If found]            │                    │
       │                    │ NotificationResponse  │                    │
       │                    │<──────────────────────┤                    │
       │                    │                       │                    │
       │  ApiResponse       │                       │                    │
       │<──────────────────│                       │                    │
       │  (success=true)    │                       │                    │
       │  (data)            │                       │                    │
       │                    │                       │                    │
       │  [If NOT found]    │                       │                    │
       │  ApiResponse       │                       │                    │
       │<──────────────────│                       │                    │
       │  (error)           │                       │                    │
       │                    │                       │                    │
```

---

## 4. DATA FLOW DIAGRAM (DFD) - Level 0 (System Context)

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                        DATA FLOW DIAGRAM - LEVEL 0                             │
│                              (System Context)                                  │
└────────────────────────────────────────────────────────────────────────────────┘

                               ┌──────────────────┐
                               │  External Client │
                               │  (API Consumer)  │
                               └────────┬─────────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ▼                   ▼                   ▼
         1. POST /send           2. GET /user/{id}   3. GET /{id}
         (NotificationRequest)   (List notifications) (Get single)
                    │                   │                   │
                    │                   │                   │
                    └───────────────────┼───────────────────┘
                                        │
                                        ▼
                        ┌──────────────────────────────┐
                        │ Notification Service System  │
                        │  (Main Application)          │
                        └──────────┬───────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
                    ▼              ▼              ▼
            ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
            │ Rule Engine  │ │  PostgreSQL  │ │ Notification │
            │  Microservice│ │   Database   │ │   Channels   │
            │   (External) │ │  (Persistence)   │  (Email/SMS) │
            └──────────────┘ └──────────────┘ └──────────────┘
```

---

## 5. DATA FLOW DIAGRAM (DFD) - Level 1 (Detailed Process)

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                        DATA FLOW DIAGRAM - LEVEL 1                                   │
│                              (Detailed Process)                                      │
└──────────────────────────────────────────────────────────────────────────────────────┘


          ┌────────────────────┐
          │   Notification     │
          │   Request (JSON)   │
          │ {userId, type,     │
          │  channel, title,   │
          │  body, refId}      │
          └──────────┬─────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │ 1. NotificationController│
        │  - Validate Request     │
        │  - Auth Check (JWT)     │
        └──────────┬──────────────┘
                   │
                   ▼
        ┌──────────────────────────────┐
        │ 2. NotificationServiceImpl    │
        │    - Build RuleContext       │
        │    - Call Rule Engine        │
        └────────────┬─────────────────┘
                     │
       ┌─────────────┼─────────────┐
       │             │             │
       ▼             ▼             ▼
   [Approved]  [Blocked]      [Error]
       │             │             │
       ▼             ▼             ▼
   Continue   Throw Exception  Throw Exception
       │
       ▼
   ┌──────────────────────────────┐
   │ 3. Override Channel (if any) │
   │    (from RuleEngine)         │
   └────────────┬─────────────────┘
                │
                ▼
   ┌────────────────────────────────────┐
   │ 4. Create NotificationLog Entity   │
   │    - Generate UUID (notificationId)│
   │    - Set Initial Status: PENDING   │
   │    - Set Timestamps               │
   └────────────┬───────────────────────┘
                │
                ▼
   ┌────────────────────────────────────┐
   │ 5. Persist to Database             │
   │    (NotificationLogRepository)     │
   │    INSERT INTO notification_log    │
   └────────────┬───────────────────────┘
                │
                ▼
   ┌────────────────────────────────────┐
   │ 6. Attempt to Send Notification    │
   │    - Email Channel                 │
   │    - SMS Channel                   │
   │    - Push Notification Channel     │
   └────────────┬───────────────────────┘
                │
       ┌───────┴────────┐
       │                │
       ▼                ▼
   [Success]       [Failed]
       │                │
       ▼                ▼
   Status=SENT    Status=FAILED
   Set sentAt      Increment Retry
       │                │
       ▼                ▼
   ┌────────────────────────────────────┐
   │ 7. Update Notification Status      │
   │    UPDATE notification_log         │
   │    SET status=?, sent_at=?         │
   └────────────┬───────────────────────┘
                │
                ▼
   ┌────────────────────────────────────┐
   │ 8. Convert to NotificationResponse │
   │    - Include notificationId        │
   │    - Include status                │
   │    - Include sentAt timestamp      │
   └────────────┬───────────────────────┘
                │
                ▼
   ┌────────────────────────────────────┐
   │ 9. Wrap in ApiResponse             │
   │    {success, message, data}        │
   └────────────┬───────────────────────┘
                │
                ▼
   ┌────────────────────────────────────┐
   │ HTTP 200 Response (JSON)           │
   │ {success: true, data: {notifId...}}│
   └────────────────────────────────────┘
```

---

## 6. DATA FLOW DIAGRAM (DFD) - Component Interaction

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                   COMPONENT DATA FLOW INTERACTION DIAGRAM                       │
└─────────────────────────────────────────────────────────────────────────────────┘


    ┌──────────────────────────────────────────────────────────────────────┐
    │                        Client / API Consumer                         │
    └──────────────────────────┬───────────────────────────────────────────┘
                               │
                               │ (HTTP Request + JWT Token)
                               ▼
    ┌──────────────────────────────────────────────────────────────────────┐
    │              JwtAuthFilter / SecurityConfig                          │
    │              - Validate JWT Token                                    │
    │              - Extract User Context                                  │
    └──────────────┬───────────────────────────────────────────────────────┘
                   │
                   │ (Authenticated Request)
                   ▼
    ┌──────────────────────────────────────────────────────────────────────┐
    │           NotificationController (@RestController)                   │
    │           - POST /send                                               │
    │           - GET /user/{userId}                                       │
    │           - GET /{notificationId}                                    │
    └──────────────┬───────────────────────────────────────────────────────┘
                   │
                   │ (Call Service Layer)
                   ▼
    ┌──────────────────────────────────────────────────────────────────────┐
    │         NotificationServiceImpl (@Service)                            │
    │         - send(request)                                              │
    │         - getByUserId(userId, pageable)                              │
    │         - getById(notificationId)                                    │
    │         - retryFailed()                                              │
    └──────┬────────────────────────────────────────┬──────────────────────┘
           │                                        │
           │ (Evaluate Rules)                       │ (Data Access)
           │                                        │
           ▼                                        ▼
    ┌──────────────────────────┐        ┌──────────────────────────┐
    │  RuleEngineClient        │        │ NotificationLogRepository│
    │  (@Component)            │        │  (@Repository)           │
    │                          │        │                          │
    │ - evaluate(context)      │        │ - save(entity)          │
    │   Calls Rule Engine via  │        │ - findById(id)          │
    │   RestTemplate           │        │ - findByUserId(userId)  │
    │                          │        │ - findByStatus(status)  │
    └──────┬───────────────────┘        └──────┬───────────────────┘
           │                                   │
           │ (HTTP POST)                       │ (JDBC / SQL)
           │                                   │
           ▼                                   ▼
    ┌──────────────────────────┐        ┌──────────────────────────┐
    │  Rule Engine Service     │        │    PostgreSQL Database   │
    │  (External Microservice) │        │                          │
    │                          │        │  Table: notification_log │
    │  Response:               │        │  Columns:                │
    │  - approved (boolean)    │        │  - id (UUID)            │
    │  - channel (override)    │        │  - user_id (Long)       │
    │  - message               │        │  - status (ENUM)        │
    │  - reason                │        │  - notification_type    │
    └──────────────────────────┘        │  - channel (ENUM)       │
                                        │  - title, body          │
                                        │  - created_at, updated_ │
                                        │  - sent_at, retry_count │
                                        └──────────────────────────┘
```

---

## 7. ERROR HANDLING & EXCEPTION FLOW

```
┌──────────────────────────────────────────────────────────────────────────────┐
│              ERROR HANDLING & EXCEPTION FLOW SEQUENCE DIAGRAM                │
└──────────────────────────────────────────────────────────────────────────────┘

   Exception/Error Scenarios:
   
   1. INVALID REQUEST
      └─> ValidationException (Constraint Violations)
          └─> GlobalExceptionHandler catches
              └─> HTTP 400 Bad Request
   
   2. NOTIFICATION BLOCKED BY RULE ENGINE
      └─> RuntimeException ("Notification blocked: {reason}")
          └─> GlobalExceptionHandler catches
              └─> HTTP 400 Bad Request
   
   3. RULE ENGINE SERVICE UNAVAILABLE
      └─> RestClientException
          └─> GlobalExceptionHandler catches
              └─> HTTP 503 Service Unavailable
   
   4. DATABASE ERROR
      └─> DataAccessException
          └─> GlobalExceptionHandler catches
              └─> HTTP 500 Internal Server Error
   
   5. NOTIFICATION NOT FOUND
      └─> NotificationNotFoundException
          └─> GlobalExceptionHandler catches
              └─> HTTP 404 Not Found
   
   6. DELIVERY FAILURE
      └─> NotificationDeliveryException
          └─> Marked as FAILED
              └─> Scheduled for Retry (via @Scheduled retryFailed)
```

---

## 8. DATABASE SCHEMA & DATA MODEL

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         DATABASE SCHEMA DIAGRAM                              │
└──────────────────────────────────────────────────────────────────────────────┘

TABLE: notification_log
┌──────────────────────────────────────────────────────────────────┐
│ Column              │ Type          │ Constraints                 │
├──────────────────────────────────────────────────────────────────┤
│ id                  │ UUID          │ PRIMARY KEY, Generated      │
│ user_id             │ BIGINT        │ NOT NULL                    │
│ notification_type   │ VARCHAR(50)   │ NOT NULL (ENUM)             │
│ channel             │ VARCHAR(20)   │ NOT NULL (ENUM)             │
│ status              │ VARCHAR(20)   │ NOT NULL (ENUM)             │
│ recipient           │ VARCHAR(512)  │ Optional                    │
│ title               │ VARCHAR(200)  │ Optional                    │
│ body                │ TEXT          │ Optional                    │
│ reference_id        │ UUID          │ Optional                    │
│ reference_type      │ VARCHAR(50)   │ Optional                    │
│ retry_count         │ INT           │ Default = 0                 │
│ created_at          │ TIMESTAMP     │ Auto (CreationTimestamp)    │
│ updated_at          │ TIMESTAMP     │ Auto (UpdateTimestamp)      │
│ sent_at             │ TIMESTAMP     │ Optional                    │
└──────────────────────────────────────────────────────────────────┘

INDEXES:
  - idx_notif_user_id      ON user_id
  - idx_notif_ref_id       ON reference_id
  - idx_notif_status       ON status
  - idx_notif_created_at   ON created_at

ENUMS:
  NotificationType: TRANSACTIONAL, PROMOTIONAL, ALERT, REMINDER
  NotificationChannel: EMAIL, SMS, PUSH_NOTIFICATION, IN_APP
  NotificationStatus: PENDING, SENT, FAILED, RETRY, DELIVERED, BOUNCED
```

---

## 9. API ENDPOINTS & CONTRACTS

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                       API ENDPOINT CONTRACTS                                │
└──────────────────────────────────────────────────────────────────────────────┘

ENDPOINT 1: SEND NOTIFICATION
─────────────────────────────────────────────────────────────────────────────────
  Method:      POST
  Path:        /api/notifications/send
  Auth:        JWT Required (Bearer Token)
  
  Request Body (NotificationRequest):
  {
    "userId": 12345,
    "notificationType": "TRANSACTIONAL",
    "channel": "EMAIL",
    "title": "Welcome",
    "body": "Welcome to MarutiXchange!",
    "referenceId": "550e8400-e29b-41d4-a716-446655440000",
    "referenceType": "ORDER",
    "additionalUserIds": [123, 456]
  }
  
  Response (HTTP 200 OK):
  {
    "success": true,
    "message": "Notification sent successfully",
    "data": {
      "notificationId": "550e8400-e29b-41d4-a716-446655440001",
      "userId": 12345,
      "status": "SENT",
      "sentAt": "2026-04-14T10:30:00",
      "title": "Welcome",
      "body": "Welcome to MarutiXchange!"
    }
  }
  
  Error Responses:
  - 400 Bad Request: Invalid input validation
  - 403 Forbidden: JWT Invalid or Expired
  - 503 Service Unavailable: Rule Engine Down


ENDPOINT 2: GET NOTIFICATIONS BY USER ID
─────────────────────────────────────────────────────────────────────────────────
  Method:      GET
  Path:        /api/notifications/user/{userId}
  Auth:        JWT Required
  Query Params:
    - page:    int (default: 0)
    - size:    int (default: 20)
  
  Response (HTTP 200 OK):
  {
    "success": true,
    "message": "Notifications fetched",
    "data": {
      "content": [
        {
          "notificationId": "550e8400-e29b-41d4-a716-446655440001",
          "userId": 12345,
          "status": "SENT",
          "sentAt": "2026-04-14T10:30:00",
          "title": "Order Confirmed",
          "body": "Your order has been confirmed"
        }
      ],
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 150,
      "totalPages": 8
    }
  }


ENDPOINT 3: GET NOTIFICATION BY ID
─────────────────────────────────────────────────────────────────────────────────
  Method:      GET
  Path:        /api/notifications/{notificationId}
  Auth:        JWT Required
  
  Response (HTTP 200 OK):
  {
    "success": true,
    "message": "Notification fetched",
    "data": {
      "notificationId": "550e8400-e29b-41d4-a716-446655440001",
      "userId": 12345,
      "notificationType": "TRANSACTIONAL",
      "channel": "EMAIL",
      "status": "SENT",
      "sentAt": "2026-04-14T10:30:00",
      "title": "Order Confirmed",
      "body": "Your order has been confirmed",
      "referenceId": "550e8400-e29b-41d4-a716-446655440000",
      "referenceType": "ORDER"
    }
  }
  
  Error Response:
  - 404 Not Found: Notification ID doesn't exist
```

---

## 10. TESTING STRATEGY (TDD)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          TESTING STRATEGY (TDD)                              │
└──────────────────────────────────────────────────────────────────────────────┘

1. UNIT TESTS
   ├─ NotificationServiceImplTest
   │  ├─ sendNotification_success_test
   │  ├─ sendNotification_blockedByRuleEngine_test
   │  ├─ sendNotification_retryOnFailure_test
   │  ├─ getByUserId_returnsPaginatedResults_test
   │  └─ getById_throwsNotFoundException_test
   │
   ├─ RuleEngineClientTest
   │  ├─ evaluate_success_test
   │  ├─ evaluate_networkError_test
   │  └─ evaluate_timeoutError_test
   │
   └─ JwtUtilTest
      ├─ validateToken_success_test
      ├─ validateToken_expiredToken_test
      └─ validateToken_invalidSignature_test

2. INTEGRATION TESTS
   ├─ NotificationControllerIntegrationTest
   │  ├─ sendNotification_endToEnd_test
   │  ├─ getByUser_endToEnd_test
   │  ├─ getById_endToEnd_test
   │  └─ sendNotification_withInvalidJwt_test
   │
   ├─ NotificationServiceIntegrationTest
   │  ├─ send_persistToDatabase_test
   │  ├─ getByUserId_fromDatabase_test
   │  └─ retryFailed_updatesStatus_test
   │
   └─ RuleEngineIntegrationTest
      ├─ evaluateRule_approvesNotification_test
      ├─ evaluateRule_blocksNotification_test
      └─ evaluateRule_overridesChannel_test

3. MOCK/STUB STRATEGY
   ├─ Mock: RuleEngineClient (external HTTP calls)
   ├─ Mock: RestTemplate (HTTP client)
   ├─ Stub: NotificationLogRepository (database calls)
   ├─ Real: NotificationServiceImpl (business logic)
   └─ Real: JwtUtil (token validation)

4. TEST COVERAGE TARGETS
   ├─ Controllers:  > 80%
   ├─ Services:    > 90%
   ├─ Repositories: > 85%
   ├─ Utilities:   > 95%
   └─ Overall:    > 85%

5. TDD WORKFLOW
   Step 1: Write failing test (RED)
   Step 2: Write minimal code to pass test (GREEN)
   Step 3: Refactor code while tests pass (REFACTOR)
   Step 4: Repeat for next feature
```

---

## 11. DEPLOYMENT & SCALING ARCHITECTURE

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    DEPLOYMENT & SCALING ARCHITECTURE                         │
└──────────────────────────────────────────────────────────────────────────────┘

                          ┌─────────────────────┐
                          │  Load Balancer      │
                          │  (NGINX/AWS ELB)    │
                          └──────────┬──────────┘
                                     │
                  ┌──────────────────┼──────────────────┐
                  │                  │                  │
                  ▼                  ▼                  ▼
            ┌────────────┐     ┌────────────┐    ┌────────────┐
            │ Pod 1      │     │ Pod 2      │    │ Pod N      │
            │ Notif      │     │ Notif      │    │ Notif      │
            │ Service    │     │ Service    │    │ Service    │
            │ (Replica)  │     │ (Replica)  │    │ (Replica)  │
            └─────┬──────┘     └─────┬──────┘    └─────┬──────┘
                  │                  │                  │
                  └──────────────────┼──────────────────┘
                                     │
                          ┌──────────┴──────────┐
                          │                     │
                          ▼                     ▼
                   ┌────────────────┐  ┌────────────────┐
                   │ PostgreSQL     │  │ Message Queue  │
                   │ Primary        │  │ (Kafka/RabbitMQ)
                   │                │  │ (Async Tasks)  │
                   └────────────────┘  └────────────────┘
                          │
                          ▼
                   ┌────────────────┐
                   │ PostgreSQL     │
                   │ Replica        │
                   │ (Read-only)    │
                   └────────────────┘

SCALING STRATEGY:
  - Horizontal Scaling: Add replicas based on CPU/Memory metrics
  - Vertical Scaling: Increase container resources
  - Cache Layer: Redis for frequently accessed notifications
  - Database Optimization: Connection pooling, Read replicas
  - Message Queue: Async processing of notifications
```

---

## 12. SYSTEM COMPONENTS SUMMARY

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     SYSTEM COMPONENTS SUMMARY                                │
└──────────────────────────────────────────────────────────────────────────────┘

COMPONENT LAYERS:

┌─────────────────────────────────────────────────────────────────────────┐
│ PRESENTATION LAYER                                                      │
├─────────────────────────────────────────────────────────────────────────┤
│ - NotificationController                                                │
│   ├─ @PostMapping /send                                                │
│   ├─ @GetMapping /user/{userId}                                        │
│   └─ @GetMapping /{notificationId}                                     │
│ - Exception Handlers: GlobalExceptionHandler                           │
│ - Validation: @Valid annotations                                       │
└────────────────────────┬────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────────────┐
│ SECURITY LAYER                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│ - JwtAuthFilter: Intercepts all requests                               │
│ - JwtUtil: Token validation & parsing                                  │
│ - SecurityConfig: Spring Security configuration                        │
└────────────────────────┬────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────────────┐
│ BUSINESS LOGIC LAYER                                                    │
├─────────────────────────────────────────────────────────────────────────┤
│ - NotificationService (Interface)                                       │
│ - NotificationServiceImpl                                                │
│   ├─ send(request)                                                      │
│   ├─ getByUserId(userId, pageable)                                     │
│   ├─ getById(notificationId)                                           │
│   └─ retryFailed() [@Scheduled]                                        │
└────────────────────────┬────────────────────────────────────────────────┘
                         │
      ┌──────────────────┼──────────────────┐
      │                  │                  │
      ▼                  ▼                  ▼
┌────────────┐    ┌────────────┐   ┌────────────┐
│ Client     │    │ Data Access│   │ Integration
│ Integration│    │ Layer      │   │ Layer
├────────────┤    ├────────────┤   ├────────────┤
│ RuleEngine │    │ Repository │   │ UserService
│ Client     │    │ (JPA Data) │   │ Client
│ UserService│    └────────────┘   │ Config
│ Client     │                     └────────────┘
└────────────┘
```

---

## 13. CONFIGURATION & PROPERTIES

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    APPLICATION CONFIGURATION                                 │
└──────────────────────────────────────────────────────────────────────────────┘

Key Application Properties:

spring.application.name=notification-service
server.port=8081
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Rule Engine
app.rule-engine.url=http://rule-engine-service:8082/api/rules/evaluate

# Database
spring.datasource.url=jdbc:postgresql://db-host:5432/notification_db
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

# JWT
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration.ms=3600000

# Logging
logging.level.root=INFO
logging.level.com.marutixchange=DEBUG

# Monitoring
management.endpoints.web.exposure.include=health,metrics,prometheus
```

---

## Conclusion

This TDD diagram provides a comprehensive architectural overview of the Notification Service microservice, including:

✅ **Sequence Diagrams** - Show interactions between components
✅ **Data Flow Diagrams** - Illustrate data movement and transformations
✅ **Component Interactions** - Display service layer architecture
✅ **Database Schema** - Define data persistence structure
✅ **API Contracts** - Document endpoint specifications
✅ **Testing Strategy** - Outline TDD approach
✅ **Deployment Architecture** - Show scaling and deployment model

All diagrams follow standard conventions and are suitable for technical documentation, presentations, and development references.
