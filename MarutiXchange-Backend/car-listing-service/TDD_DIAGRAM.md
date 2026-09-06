# Car Listing Service - Test-Driven Development (TDD) Diagrams

## Overview
This document contains comprehensive TDD diagrams including data flow diagrams and sequence diagrams for the Car Listing Service microservice.

---

## 1. DATA FLOW DIAGRAM (DFD) - Level 0 (Context Diagram)

```
┌─────────────────────────┐
│   Client Application    │
│  (Web/Mobile/API)       │
└────────────┬────────────┘
             │
        HTTP/REST Requests
        (with JWT Token)
             │
             ▼
┌─────────────────────────────────────────┐
│  Car Listing Service (Microservice)     │
│                                         │
│  • Create Listing                       │
│  • Read Listing                         │
│  • Update Listing                       │
│  • Delete Listing                       │
│  • Upload Images                        │
│  • Mark as Sold                         │
│  • Increment View Count                 │
└────┬────────────────────────┬──────────┘
     │                        │
     ▼                        ▼
┌──────────────┐        ┌──────────────┐
│ MySQL        │        │ Rule Engine  │
│  Database    │        │  (Drools)    │
│              │        │              │
│• Listings    │        │• Validation  │
│• Images      │        │• Rules       │
└──────────────┘        └──────────────┘
```

---

## 2. DATA FLOW DIAGRAM (DFD) - Level 1 (System Decomposition)

```
┌────────────────────────────────────────────────────────────────────────────┐
│                          CLIENT APPLICATION                                │
└────────────────────────┬─────────────────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   Create Request   Read Request    Update/Delete Request
        │                │                │
        └────────────────┼────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  Security Layer (JWT Validation)
         │  (JwtAuthFilter)               │
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  Controller Layer               │
         │  (CarListingController)         │
         │  - @PostMapping /listings       │
         │  - @GetMapping /listings/{id}   │
         │  - @PutMapping /listings/{id}   │
         │  - @DeleteMapping /listings/{id}│
         └───────────────┬────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │  Service Layer                  │
         │  (CarListingServiceImpl)         │
         └────┬────────────────┬───────────┘
              │                │
              ▼                ▼
    ┌──────────────────┐  ┌──────────────────┐
    │  Rule Engine     │  │  Repository      │
    │  Service         │  │  Layer           │
    │  (Validation)    │  │  (Data Access)   │
    │                  │  │                  │
    │  • Price Check   │  │  • Save Entity   │
    │  • Year Check    │  │  • Fetch Entity  │
    │  • Fraud Check   │  │  • Delete Entity │
    └────────┬─────────┘  └────────┬─────────┘
             │                     │
             │          ┌──────────▼─────────┐
             │          │  Database Layer    │
             │          │  (JPA/Hibernate)   │
             │          │  - CarListing      │
             │          │  - CarImage        │
             │          └────────┬───────────┘
             │                   │
             └───────────┬───────┘
                         │
                    ┌────▼──────────┐
                    │  Data Storage  │
                    │  (PostgreSQL)  │
                    └───────────────┘
```

---

## 3. DATA FLOW DIAGRAM (DFD) - Level 2 (Detailed Service Flow)

### 3.1 CREATE LISTING FLOW

```
┌──────────────────────────────────────────────────────────────────────────┐
│                       CREATE LISTING (POST /listings)                    │
└────────────────────────┬─────────────────────────────────────────────────┘
                         │
                         │ CarListingRequest
                         │ {
                         │   sellerId, brand, model, year,
                         │   fuelType, transmission, askingPrice,
                         │   city, state, pincode, description,
                         │   testDriveAvailable, insuranceValidTill
                         │ }
                         ▼
         ┌───────────────────────────────────┐
         │ JwtAuthFilter                     │
         │ - Extract & Validate JWT Token    │
         │ - Set Security Context            │
         └───────────┬───────────────────────┘
                     │
                     │ Token Valid?
                     │ Yes ▼ / No → 401 Unauthorized
                     │
         ┌───────────────────────────────────┐
         │ CarListingController              │
         │ createListing(request)            │
         │ - Validate Request (JSR-303)      │
         │ - Check Seller Authorization      │
         └───────────┬───────────────────────┘
                     │
                     │ Valid Request?
                     │ Yes ▼ / No → 400 Bad Request
                     │
         ┌───────────────────────────────────┐
         │ CarListingServiceImpl              │
         │ createListing(request)            │
         │ - Create CarListing Entity        │
         │ - Set Initial Status: ACTIVE      │
         │ - Initialize View Count: 0        │
         │ - Initialize Inquiry Count: 0     │
         └───────────┬───────────────────────┘
                     │
                     ▼
         ┌───────────────────────────────────┐
         │ DroolsRuleService                 │
         │ evaluateRules(carListing)         │
         │                                   │
         │ ┌─────────────────────────────┐   │
         │ │ Price Validation Rule       │   │
         │ │ IF askingPrice < MIN or     │   │
         │ │    askingPrice > MAX        │   │
         │ │ THEN violation              │   │
         │ └─────────────────────────────┘   │
         │                                   │
         │ ┌─────────────────────────────┐   │
         │ │ Year Validation Rule        │   │
         │ │ IF year > CURRENT_YEAR or   │   │
         │ │    year < MIN_YEAR          │   │
         │ │ THEN violation              │   │
         │ └─────────────────────────────┘   │
         │                                   │
         │ ┌─────────────────────────────┐   │
         │ │ Fraud Detection Rules       │   │
         │ │ IF suspicious_pattern       │   │
         │ │ THEN flag violation         │   │
         │ └─────────────────────────────┘   │
         │                                   │
         │ ┌─────────────────────────────┐   │
         │ │ Description Quality Check   │   │
         │ │ IF description.length < MIN │   │
         │ │ THEN violation              │   │
         │ └─────────────────────────────┘   │
         │                                   │
         │ Return RuleViolations object      │
         └───────────┬───────────────────────┘
                     │
                     │ Has Violations?
                     │ Yes ▼ → 422 Unprocessable Entity
                     │ No ▼
                     │
         ┌───────────────────────────────────┐
         │ CarListingRepository              │
         │ save(carListing)                  │
         │ - Persist to Database             │
         │ - Generate ID (Auto-increment)    │
         │ - Return Entity with ID           │
         └───────────┬───────────────────────┘
                     │
                     ▼
         ┌───────────────────────────────────┐
         │ CarListingResponse                │
         │ fromEntity(savedCarListing)       │
         │ - Map Entity to DTO               │
         │ - Include: id, brand, model,      │
         │   year, askingPrice, status,      │
         │   viewCount, inquiryCount         │
         └───────────┬───────────────────────┘
                     │
                     │ 201 Created
                     │ + Location Header
                     ▼
         ┌───────────────────────────────────┐
         │ Client Application                │
         │ Receives: CarListingResponse      │
         │ Status: 201 CREATED               │
         └───────────────────────────────────┘
```

### 3.2 READ LISTING FLOW

```
┌──────────────────────────────────────────────────────────────────────────┐
│                   GET LISTING (GET /listings/{id})                       │
└────────────────────────┬─────────────────────────────────────────────────┘
                         │
                         │ Path Variable: id
                         ▼
         ┌───────────────────────────────────┐
         │ JwtAuthFilter                     │
         │ - Validate JWT Token              │
         └───────────┬───────────────────────┘
                     │
                     │ Valid? Yes ▼
                     │
         ┌───────────────────────────────────┐
         │ CarListingController              │
         │ getListingById(id)                │
         │ - Validate ID (positive number)   │
         └───────────┬───────────────────────┘
                     │
                     │ Valid? Yes ▼
                     │
         ┌───────────────────────────────────┐
         │ CarListingServiceImpl              │
         │ getListingById(id)                │
         │ - Query Repository by ID          │
         │ - Call incrementViewCount(id)     │
         └───────────┬───────────────────────┘
                     │
         ┌───────────┴──────────────────────┐
         │                                  │
         ▼                                  ▼
    Entity Found?                    Entity Not Found?
    │                                │
    │ Yes ▼                          │ No ▼
    │                                │
    │                    ┌───────────────────────────────────┐
    │                    │ CarListingNotFoundException        │
    │                    │ - Throw 404 Not Found             │
    │                    │ - Return Error Response           │
    │                    └────────────┬────────────────────┘
    │                                 │
    │                                 ▼
    │                    ┌───────────────────────────────────┐
    │                    │ Client Receives: 404 NOT FOUND   │
    │                    └───────────────────────────────────┘
    │
    ├─────────────────────────────────────────────────────────┐
    │ (When entity found)                                     │
    │                                                         │
    ▼                                                         │
    ┌───────────────────────────────────────────────────────┐ │
    │ ViewCount Increment Operation:                        │ │
    │                                                       │ │
    │ ┌─────────────────────────────────────────────────┐ │ │
    │ │ CarListingServiceImpl.incrementViewCount(id)    │ │ │
    │ │ - Fetch Listing from DB                        │ │ │
    │ │ - Increment viewCount by 1                      │ │ │
    │ │ - Save Updated Listing                          │ │ │
    │ └─────────────────────────────────────────────────┘ │ │
    │                                                       │ │
    │ ┌─────────────────────────────────────────────────┐ │ │
    │ │ CarListingRepository.save(listing)              │ │ │
    │ │ - Persist viewCount update to DB                │ │ │
    │ └──────────────────┬────────────────────────────┘ │ │
    │                    │                              │ │
    │                    ▼                              │ │
    │            ┌────────────────┐                    │ │
    │            │  Database      │                    │ │
    │            │  (Update)      │                    │ │
    │            └────────┬───────┘                    │ │
    │                     │                            │ │
    │                     │ Update Complete           │ │
    │                     ▼                            │ │
    │         ┌──────────────────────────────┐        │ │
    │         │ Return to Service            │        │ │
    │         │ (viewCount incremented)      │        │ │
    │         └──────────────────────────────┘        │ │
    │                                                  │ │
    └──────────────────────┬──────────────────────────┘ │
                           │                            │
                           │ Updated Entity             │
                           ▼                            │
         ┌───────────────────────────────────┐        │ │
         │ CarListingResponse                │        │ │
         │ fromEntity(updatedListing)        │        │ │
         │ - Convert Entity to DTO           │        │ │
         │ - Include Updated viewCount       │        │ │
         └───────────┬───────────────────────┘        │ │
                     │                                │ │
                     │ 200 OK                         │ │
                     │ + CarListingResponse Body      │ │
                     ▼                                │ │
         ┌───────────────────────────────────┐        │ │
         │ Client Application                │        │ │
         │ Receives: CarListingResponse      │        │ │
         │ Status: 200 OK                    │        │ │
         │ With Updated viewCount            │        │ │
         └───────────────────────────────────┘        │ │
                                                      │ │
         ┌────────────────────────────────────────────┘ │
         │                                              │
         │ (Concurrent/Independent Operation)           │
         │                                              │
         ▼                                              │
         ┌───────────────────────────────────┐          │
         │ ViewCount Update is Asynchronous  │          │
         │ (Does not block response)         │          │
         └───────────────────────────────────┘          │
                                                        │
         ┌─────────────────────────────────────────────┘
         │
         ▼ (In parallel/separately from response)
         ┌───────────────────────────────────┐
         │ Database Transaction Committed    │
         │ - viewCount persisted             │
         │ - Isolation Level: READ_COMMITTED │
         └───────────────────────────────────┘
```

### 3.3 DELETE LISTING FLOW

```
┌──────────────────────────────────────────────────────────────────────────┐
│              DELETE LISTING (DELETE /listings/{id})                      │
└────────────────────────┬─────────────────────────────────────────────────┘
                         │
                         │ Path Variable: id
                         ▼
         ┌───────────────────────────────────┐
         │ JwtAuthFilter                     │
         │ - Validate JWT Token              │
         │ - Extract User Context            │
         └───────────┬───────────────────────┘
                     │
                     │ Token Valid? Yes ▼
                     │
         ┌───────────────────────────────────┐
         │ CarListingController              │
         │ deleteListing(id)                 │
         │ - Validate ID Format              │
         │ - Check Authorization (Seller)    │
         └───────────┬───────────────────────┘
                     │
                     │ Valid & Authorized? Yes ▼
                     │
         ┌───────────────────────────────────┐
         │ CarListingServiceImpl              │
         │ deleteListing(id)                 │
         │ [@Transactional]                  │
         │ - Begin Transaction               │
         └───────────┬───────────────────────┘
                     │
                     ▼
         ┌───────────────────────────────────┐
         │ CarListingRepository              │
         │ findById(id)                      │
         │ - Query Database for Listing      │
         └───────────┬───────────────────────┘
                     │
         ┌───────────┴──────────────────────┐
         │                                  │
    Found? Yes ▼                      Not Found? No ▼
    │                                 │
    │                    ┌───────────────────────────────────┐
    │                    │ CarListingNotFoundException        │
    │                    │ - Throw 404 Not Found             │
    │                    │ - Rollback Transaction            │
    │                    └────────────┬────────────────────┘
    │                                 │
    │                                 ▼
    │                    ┌───────────────────────────────────┐
    │                    │ Client Receives: 404 NOT FOUND   │
    │                    └───────────────────────────────────┘
    │
    ▼ (When entity found)
    ┌───────────────────────────────────┐
    │ Set Status to EXPIRED             │
    │ listing.setStatus(EXPIRED)        │
    │ - Soft Delete (not hard delete)   │
    │ - Preserve data in Database       │
    │ - Mark as Unavailable             │
    └───────────┬───────────────────────┘
                │
                ▼
    ┌───────────────────────────────────┐
    │ CarListingRepository              │
    │ save(updatedListing)              │
    │ - Persist Status Change           │
    │ - Update Timestamp: updatedAt     │
    └───────────┬───────────────────────┘
                │
                ▼
    ┌───────────────────────────────────┐
    │ Database                          │
    │ UPDATE car_listings               │
    │ SET status = 'EXPIRED'            │
    │ WHERE id = ?                      │
    │                                   │
    │ Row Updated: 1 row affected       │
    └───────────┬───────────────────────┘
                │
                ▼
    ┌───────────────────────────────────┐
    │ Transaction Commit                │
    │ - Changes Persisted               │
    │ - Release Database Lock           │
    └───────────┬───────────────────────┘
                │
                ▼
    ┌───────────────────────────────────┐
    │ Return to Controller              │
    │ void (204 No Content)             │
    └───────────┬───────────────────────┘
                │
                │ 204 No Content
                ▼
    ┌───────────────────────────────────┐
    │ Client Application                │
    │ Receives: 204 No Content Response │
    │ Listing Status: EXPIRED           │
    └───────────────────────────────────┘
```

### 3.4 MARK AS SOLD FLOW

```
┌──────────────────────────────────────────────────────────────────────────┐
│            MARK AS SOLD (PATCH /listings/{id}/sold)                     │
└────────────────────────┬─────────────────────────────────────────────────┘
                         │
                         │ Path Variable: id
                         ▼
         ┌───────────────────────────────────┐
         │ JwtAuthFilter                     │
         │ - Validate JWT Token              │
         └───────────┬───────────────────────┘
                     │
                     │ Token Valid? Yes ▼
                     │
         ┌───────────────────────────────────┐
         │ CarListingController              │
         │ markAsSold(id)                    │
         │ - Validate ID Format              │
         │ - Check Seller Authorization      │
         └───────────┬───────────────────────┘
                     │
                     │ Valid? Yes ▼
                     │
         ┌───────────────────────────────────┐
         │ CarListingServiceImpl              │
         │ markAsSold(id)                    │
         │ [@Transactional]                  │
         │ - Begin Transaction               │
         └───────────┬───────────────────────┘
                     │
                     ▼
         ┌───────────────────────────────────┐
         │ CarListingRepository              │
         │ findById(id)                      │
         │ - Retrieve Listing Entity         │
         └───────────┬───────────────────────┘
                     │
         ┌───────────┴──────────────────────┐
         │                                  │
    Found? Yes ▼                      Not Found? No ▼
    │                                 │
    │                    ┌───────────────────────────────────┐
    │                    │ CarListingNotFoundException        │
    │                    │ - Throw 404 Not Found             │
    │                    │ - Rollback Transaction            │
    │                    └─────────────────────────────────┘
    │
    ▼ (When entity found)
    ┌───────────────────────────────────┐
    │ Update Status to SOLD             │
    │ listing.setStatus(SOLD)           │
    │ - Change Status from ACTIVE       │
    │ - Mark as Completed               │
    └───────────┬───────────────────────┘
                │
                ▼
    ┌───────────────────────────────────┐
    │ Set Sold Timestamp                │
    │ listing.setSoldAt(LocalDateTime   │
    │            .now())                │
    │ - Record when sold                │
    │ - Transaction Timestamp           │
    └───────────┬───────────────────────┘
                │
                ▼
    ┌───────────────────────────────────┐
    │ CarListingRepository              │
    │ save(updatedListing)              │
    │ - Persist Changes                 │
    └───────────┬───────────────────────┘
                │
                ▼
    ┌───────────────────────────────────┐
    │ Database                          │
    │ UPDATE car_listings               │
    │ SET status = 'SOLD',              │
    │     sold_at = NOW()               │
    │ WHERE id = ?                      │
    │                                   │
    │ Row Updated: 1 row affected       │
    └───────────┬───────────────────────┘
                │
                ▼
    ┌───────────────────────────────────┐
    │ Transaction Commit                │
    │ - All Changes Committed           │
    │ - Database Locks Released         │
    └───────────┬───────────────────────┘
                │
                │ 204 No Content
                ▼
    ┌───────────────────────────────────┐
    │ Client Application                │
    │ Receives: 204 No Content          │
    │ Status: SOLD                      │
    │ SoldAt: [timestamp]               │
    └───────────────────────────────────┘
```

---

## 4. SEQUENCE DIAGRAMS

### 4.1 CREATE LISTING SEQUENCE DIAGRAM

```
Client              Controller            Service           RuleEngine        Repository         Database
  │                   │                     │                  │                  │                  │
  │  POST /listings   │                     │                  │                  │                  │
  │  CarListingRequest│                     │                  │                  │                  │
  ├──────────────────>│                     │                  │                  │                  │
  │                   │ JwtAuthFilter       │                  │                  │                  │
  │                   │ Validates Token     │                  │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │ createListing()     │                  │                  │                  │
  │                   ├────────────────────>│                  │                  │                  │
  │                   │                     │ Build CarListing │                  │                  │
  │                   │                     │ Entity           │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ evaluateRules()  │                  │                  │
  │                   │                     ├─────────────────>│                  │                  │
  │                   │                     │                  │ Load Rules       │                  │
  │                   │                     │                  │ Create Session   │                  │
  │                   │                     │                  │ Insert FACT      │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │                  │ Fire Rules:      │                  │
  │                   │                     │                  │ - Price Check    │                  │
  │                   │                     │                  │ - Year Check     │                  │
  │                   │                     │                  │ - Fraud Check    │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │<─────────────────│                  │                  │
  │                   │                     │ RuleViolations   │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ Has Violations?  │                  │                  │
  │                   │                     │ NO               │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ save()           │                  │                  │
  │                   │                     ├──────────────────────────────────> │                  │
  │                   │                     │                  │                  │ INSERT INTO     │
  │                   │                     │                  │                  │ car_listings    │
  │                   │                     │                  │                  │ VALUES (...)    │
  │                   │                     │                  │                  │                  │
  │                   │                     │                  │                  │<─────────────────│
  │                   │                     │                  │                  │ CarListing      │
  │                   │                     │                  │                  │ (with ID)       │
  │                   │                     │<──────────────────────────────────┤                  │
  │                   │                     │ SavedCarListing  │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │ CarListingResponse  │                  │                  │                  │
  │                   │<────────────────────┤                  │                  │                  │
  │                   │                     │                  │                  │                  │
  │ 201 Created       │                     │                  │                  │                  │
  │ CarListingResponse│                     │                  │                  │                  │
  │<──────────────────│                     │                  │                  │                  │
  │                   │                     │                  │                  │                  │
```

### 4.2 GET LISTING SEQUENCE DIAGRAM

```
Client              Controller            Service           ViewCountService   Repository         Database
  │                   │                     │                  │                  │                  │
  │ GET /listings/1   │                     │                  │                  │                  │
  ├──────────────────>│                     │                  │                  │                  │
  │                   │ JwtAuthFilter       │                  │                  │                  │
  │                   │ Validates Token     │                  │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │ getListingById(1)   │                  │                  │                  │
  │                   ├────────────────────>│                  │                  │                  │
  │                   │                     │ findById(1)      │                  │                  │
  │                   │                     ├──────────────────────────────────> │                  │
  │                   │                     │                  │                  │ SELECT * FROM   │
  │                   │                     │                  │                  │ car_listings    │
  │                   │                     │                  │                  │ WHERE id = 1    │
  │                   │                     │                  │                  │                  │
  │                   │                     │                  │                  │<─────────────────│
  │                   │                     │                  │                  │ Optional        │
  │                   │                     │                  │                  │ <CarListing>    │
  │                   │                     │<──────────────────────────────────┤                  │
  │                   │                     │ Optional         │                  │                  │
  │                   │                     │ <CarListing>     │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ incrementViewCount(1)               │                  │
  │                   │                     ├────────────────────────────────────>│                  │
  │                   │                     │                  │                  │ SET viewCount   │
  │                   │                     │                  │                  │ = viewCount + 1 │
  │                   │                     │                  │                  │ WHERE id = 1    │
  │                   │                     │                  │                  │                  │
  │                   │                     │                  │                  │<─────────────────│
  │                   │                     │                  │                  │ 1 row updated   │
  │                   │                     │<──────────────────────────────────┤                  │
  │                   │                     │ void             │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │ CarListingResponse  │                  │                  │                  │
  │                   │<────────────────────┤                  │                  │                  │
  │                   │ fromEntity()        │                  │                  │                  │
  │                   │                     │                  │                  │                  │
  │ 200 OK            │                     │                  │                  │                  │
  │ CarListingResponse│                     │                  │                  │                  │
  │<──────────────────│                     │                  │                  │                  │
  │ (viewCount++)     │                     │                  │                  │                  │
  │                   │                     │                  │                  │                  │
```

### 4.3 DELETE LISTING SEQUENCE DIAGRAM

```
Client              Controller            Service           Repository         Database
  │                   │                     │                  │                  │
  │ DELETE /listings/1│                     │                  │                  │
  ├──────────────────>│                     │                  │                  │
  │                   │ JwtAuthFilter       │                  │                  │
  │                   │ Validates Token     │                  │                  │
  │                   │ Check Auth          │                  │                  │
  │                   │                     │                  │                  │
  │                   │ deleteListing(1)    │                  │                  │
  │                   ├────────────────────>│                  │                  │
  │                   │                     │ @Transactional   │                  │
  │                   │                     │ BEGIN TRANSACTION│                  │
  │                   │                     │                  │                  │
  │                   │                     │ findById(1)      │                  │
  │                   │                     ├────────────────────────────────────>│
  │                   │                     │                  │                  │ SELECT * FROM   │
  │                   │                     │                  │                  │ car_listings    │
  │                   │                     │                  │                  │ WHERE id = 1    │
  │                   │                     │                  │                  │ FOR UPDATE      │
  │                   │                     │                  │                  │ (LOCK ROW)      │
  │                   │                     │                  │                  │                  │
  │                   │                     │                  │                  │<─────────────────│
  │                   │                     │                  │                  │ CarListing      │
  │                   │                     │                  │                  │ (Locked)        │
  │                   │                     │<────────────────────────────────────┤                  │
  │                   │                     │ CarListing       │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ setStatus(EXPIRED)                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ save()           │                  │                  │
  │                   │                     ├────────────────────────────────────>│                  │
  │                   │                     │                  │                  │ UPDATE          │
  │                   │                     │                  │                  │ car_listings    │
  │                   │                     │                  │                  │ SET status =    │
  │                   │                     │                  │                  │ 'EXPIRED'       │
  │                   │                     │                  │                  │ WHERE id = 1    │
  │                   │                     │                  │                  │                  │
  │                   │                     │                  │                  │<─────────────────│
  │                   │                     │                  │                  │ 1 row updated   │
  │                   │                     │<────────────────────────────────────┤                  │
  │                   │                     │ Saved            │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ COMMIT           │                  │                  │
  │                   │                     │                  │                  │ COMMIT          │
  │                   │                     │                  │                  │ (UNLOCK ROW)    │
  │                   │                     │                  │                  │                  │
  │ 204 No Content    │                     │                  │                  │                  │
  │<──────────────────│                     │                  │                  │                  │
  │                   │                     │                  │                  │                  │
```

### 4.4 MARK AS SOLD SEQUENCE DIAGRAM

```
Client              Controller            Service           Repository         Database
  │                   │                     │                  │                  │
  │ PATCH /listings/1/│                     │                  │                  │
  │ sold              │                     │                  │                  │
  ├──────────────────>│                     │                  │                  │
  │                   │ JwtAuthFilter       │                  │                  │
  │                   │ Validates Token     │                  │                  │
  │                   │ Extract User Context│                  │                  │
  │                   │                     │                  │                  │
  │                   │ markAsSold(1)       │                  │                  │
  │                   ├────────────────────>│                  │                  │
  │                   │                     │ @Transactional   │                  │
  │                   │                     │ BEGIN TRANSACTION│                  │
  │                   │                     │                  │                  │
  │                   │                     │ findById(1)      │                  │
  │                   │                     ├────────────────────────────────────>│
  │                   │                     │                  │                  │ SELECT * FROM   │
  │                   │                     │                  │                  │ car_listings    │
  │                   │                     │                  │                  │ WHERE id = 1    │
  │                   │                     │                  │                  │ FOR UPDATE      │
  │                   │                     │                  │                  │                  │
  │                   │                     │                  │                  │<─────────────────│
  │                   │                     │                  │                  │ CarListing      │
  │                   │                     │<────────────────────────────────────┤ (Locked)        │
  │                   │                     │ CarListing       │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ setStatus(SOLD)  │                  │                  │
  │                   │                     │ setSoldAt(NOW()) │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ save()           │                  │                  │
  │                   │                     ├────────────────────────────────────>│                  │
  │                   │                     │                  │                  │ UPDATE          │
  │                   │                     │                  │                  │ car_listings    │
  │                   │                     │                  │                  │ SET status =    │
  │                   │                     │                  │                  │ 'SOLD',         │
  │                   │                     │                  │                  │ sold_at = NOW()│
  │                   │                     │                  │                  │ WHERE id = 1    │
  │                   │                     │                  │                  │                  │
  │                   │                     │                  │                  │<─────────────────│
  │                   │                     │                  │                  │ 1 row updated   │
  │                   │                     │<────────────────────────────────────┤                  │
  │                   │                     │ Saved            │                  │                  │
  │                   │                     │                  │                  │                  │
  │                   │                     │ COMMIT           │                  │                  │
  │                   │                     │                  │                  │ COMMIT          │
  │                   │                     │                  │                  │ (UNLOCK ROW)    │
  │                   │                     │                  │                  │                  │
  │ 204 No Content    │                     │                  │                  │                  │
  │<──────────────────│                     │                  │                  │                  │
  │                   │                     │                  │                  │                  │
```

---

## 5. TEST COVERAGE MATRIX

```
┌─────────────────────────────────────────────────────────────────┐
│            TDD Test Coverage - Car Listing Service             │
├─────────────────────────────────────────────────────────────────┤
│ Operation       │ Unit Test           │ Integration Test        │
├─────────────────────────────────────────────────────────────────┤
│ CREATE          │ ✅ createListing_   │ ✅ Full flow with      │
│ LISTING         │   shouldReturnResp  │   RuleEngine &         │
│                 │   ✅ Mock Rules     │   Database             │
│                 │   ✅ Mock Repo      │                        │
├─────────────────────────────────────────────────────────────────┤
│ GET BY ID       │ ✅ getListingById_  │ ✅ View count          │
│                 │   shouldReturnList  │   increment verified   │
│                 │   ✅ getListingById │   ✅ Exception throws  │
│                 │   _shouldThrow_when │   for not found        │
│                 │   NotFound          │                        │
├─────────────────────────────────────────────────────────────────┤
│ DELETE          │ ✅ deleteListing_   │ ✅ Status EXPIRED      │
│ LISTING         │   shouldExpireListing
│                 │   ✅ Mock findById  │   persisted in DB      │
│                 │   ✅ Mock save      │   ✅ Soft delete works │
├─────────────────────────────────────────────────────────────────┤
│ MARK AS SOLD    │ ✅ markAsSold_      │ ✅ Status SOLD & time  │
│                 │   shouldUpdateStatus
│                 │   ✅ Mock findById  │   persisted in DB      │
│                 │   ✅ Mock save      │                        │
├─────────────────────────────────────────────────────────────────┤
│ RULE ENGINE     │ ✅ evaluateRules_   │ ✅ Rules fire correctly│
│ VALIDATION      │   shouldValidate    │   ✅ Violations return │
│                 │   ✅ Price check    │   correct status       │
│                 │   ✅ Year check     │                        │
├─────────────────────────────────────────────────────────────────┤
│ VIEW COUNT      │ ✅ incrementView    │ ✅ DB persists count   │
│ INCREMENT       │   Count_shouldIncr  │   ✅ Atomic operation  │
│                 │   ✅ Mock repo      │                        │
├─────────────────────────────────────────────────────────────────┤
│ ERROR HANDLING  │ ✅ Exception throws │ ✅ 404 response        │
│                 │   ✅ Invalid input  │   ✅ Error messages    │
│                 │   ✅ Mock failures  │   ✅ Transaction rollback
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. TDD CYCLE FLOWCHART

```
┌─────────────────────────────────────────────────────────┐
│              TDD DEVELOPMENT CYCLE                      │
│              (Red → Green → Refactor)                   │
└──────────────────┬──────────────────────────────────────┘
                   │
        ┌──────────▼──────────┐
        │  1️⃣ RED PHASE      │
        │  Write Failing Test │
        │  (Test First)       │
        └──────────┬──────────┘
                   │
                   │ Test fails
                   │
        ┌──────────▼──────────────────────────┐
        │ Example: createListing_             │
        │ shouldReturnResponse()              │
        │                                     │
        │ @Test                               │
        │ void createListing_should...() {    │
        │   // Arrange                        │
        │   CarListingRequest req = ...;      │
        │   when(ruleEngine.evaluateRules()) │
        │     .thenReturn(mockResponse);      │
        │   when(repo.save(any()))            │
        │     .thenReturn(mockEntity);        │
        │                                     │
        │   // Act                            │
        │   CarListingResponse resp =         │
        │     service.createListing(req);     │
        │                                     │
        │   // Assert                         │
        │   assertNotNull(resp);              │
        │   assertEquals("Maruti",            │
        │     resp.getBrand());               │
        │ }                                   │
        └──────────┬──────────────────────────┘
                   │
        ┌──────────▼──────────┐
        │  2️⃣ GREEN PHASE   │
        │  Make Test Pass     │
        │  (Minimal Code)     │
        └──────────┬──────────┘
                   │
                   │ Test passes
                   │
        ┌──────────▼──────────────────────────┐
        │ Implement: CarListingServiceImpl     │
        │                                     │
        │ @Override                           │
        │ public CarListingResponse           │
        │ createListing(CarListingRequest r) {│
        │   // Create entity from request     │
        │   CarListing listing = ...;         │
        │                                     │
        │   // Call rule engine               │
        │   ruleEngineClient.evaluateRules(  │
        │     listing);                       │
        │                                     │
        │   // Save to repository             │
        │   return repo.save(listing);        │
        │ }                                   │
        │                                     │
        │ (Bare minimum to pass test)        │
        └──────────┬──────────────────────────┘
                   │
        ┌──────────▼──────────────┐
        │ 3️⃣ REFACTOR PHASE    │
        │ Improve Code Quality    │
        │ (Keep Tests Passing)    │
        └──────────┬──────────────┘
                   │
                   │ Code improved
                   │
        ┌──────────▼──────────────────────────┐
        │ Refactored: CarListingServiceImpl    │
        │                                     │
        │ • Extract validation logic          │
        │ • Add error handling                │
        │ • Improve readability               │
        │ • Add comments                      │
        │ • Follow SOLID principles           │
        │ • Add logging                       │
        │                                     │
        │ (All tests still pass)              │
        └──────────┬──────────────────────────┘
                   │
        ┌──────────▼─────────────────────┐
        │ Next Feature?                   │
        │ YES → Repeat Cycle              │
        │ NO → Release                    │
        │                                 │
        │ ✅ Complete Test Coverage       │
        │ ✅ Code Quality Assured         │
        │ ✅ Refactoring Tracked          │
        └─────────────────────────────────┘
```

---

## 7. TESTING STRATEGY

### Unit Test Structure

```
CarListingServiceImplTest.java
├── @ExtendWith(MockitoExtension.class)
├── @Mock Dependencies
│   ├── CarListingRepository listingRepository
│   ├── CarImageRepository imageRepository
│   └── RuleEngineClient ruleEngineClient
│
├── @InjectMocks
│   └── CarListingServiceImpl listingService
│
├── @BeforeEach setUp()
│   └── Initialize Test Data & Mocks
│
├── Test Group 1: CREATE Operation
│   └── createListing_shouldReturnResponse()
│       ├── Arrange: Setup mocks
│       ├── Act: Call service method
│       └── Assert: Verify response
│
├── Test Group 2: READ Operations
│   ├── getListingById_shouldReturnListing()
│   │   └── Happy path test
│   └── getListingById_shouldThrow_whenNotFound()
│       └── Exception handling test
│
├── Test Group 3: DELETE Operation
│   └── deleteListing_shouldExpireListing()
│       └── Soft delete verification
│
├── Test Group 4: UPDATE Operation (SOLD)
│   └── markAsSold_shouldUpdateStatus()
│       └── Status change & timestamp verification
│
└── Mock Verification
    └── verify(repository, times(n)).method()
        └── Ensure correct method calls
```

### Mockito Setup Pattern

```
// ✅ Mock the rule engine response
RuleContext mockResponse = new RuleContext();
mockResponse.setApproved(true);
mockResponse.setRuleMatched(true);

when(ruleEngineClient.evaluateRules(any()))
    .thenReturn(mockResponse);

// ✅ Mock the database save
when(listingRepository.save(any()))
    .thenReturn(mockListing);

// ✅ Call service method
CarListingResponse response = 
    listingService.createListing(listingRequest);

// ✅ Assert response
assertNotNull(response);
assertEquals("Maruti", response.getBrand());

// ✅ Verify interactions
verify(listingRepository, times(1)).save(any());
```

---

## 8. DATA MODEL FLOW

```
API Request (JSON)
        │
        ▼
┌──────────────────────┐
│ CarListingRequest    │ (DTO)
│ - sellerId           │
│ - brand              │
│ - model              │
│ - year               │
│ - fuelType           │
│ - transmission       │
│ - askingPrice        │
│ - description        │
└──────────┬───────────┘
           │ Mapper/Builder
           ▼
┌──────────────────────┐
│ CarListing           │ (Entity)
│ (JPA @Entity)        │
│ - id (PK)            │
│ - sellerId (FK)      │
│ - brand              │
│ - model              │
│ - year               │
│ - fuelType (Enum)    │
│ - transmission (Enum)│
│ - askingPrice        │
│ - status (Enum)      │ ◄─── ACTIVE/SOLD/EXPIRED
│ - viewCount          │
│ - inquiryCount       │
│ - createdAt          │
│ - updatedAt          │
│ - soldAt (nullable)  │
└──────────┬───────────┘
           │ JPA/Hibernate
           │ ORM Mapping
           ▼
┌──────────────────────┐
│ Database Row         │
│ (car_listings table) │
│                      │
│ id        | INTEGER  │
│ seller_id | INTEGER  │
│ brand     | VARCHAR  │
│ model     | VARCHAR  │
│ year      | INTEGER  │
│ fuel_type | VARCHAR  │
│ status    | VARCHAR  │
│ view_cnt  | INTEGER  │
│ created_at| DATETIME │
│ sold_at   | DATETIME │
└──────────┬───────────┘
           │ Query Result
           ▼
┌──────────────────────┐
│ CarListing Entity    │
│ (Fetched from DB)    │
└──────────┬───────────┘
           │ Mapper
           ▼
┌──────────────────────┐
│ CarListingResponse   │ (DTO)
│ - id                 │
│ - brand              │
│ - model              │
│ - year               │
│ - askingPrice        │
│ - status             │
│ - viewCount          │
│ - inquiryCount       │
└──────────┬───────────┘
           │ JSON Serialization
           ▼
┌──────────────────────┐
│ API Response (JSON)  │
│ Sent to Client       │
└──────────────────────┘
```

---

## 9. Exception Handling Flow

```
Request Flow
     │
     ▼
Validation Checks
     │
     ├─ Invalid JWT Token
     │  └─ 401 Unauthorized
     │     (JwtAuthFilter throws exception)
     │
     ├─ Invalid Request Body
     │  └─ 400 Bad Request
     │     (@Valid annotation)
     │
     ├─ Listing Not Found (ID = 99)
     │  │
     │  ▼
     │  CarListingRepository.findById(99)
     │  returns Optional.empty()
     │  │
     │  ▼
     │  Service throws CarListingNotFoundException
     │  │
     │  ▼
     │  GlobalExceptionHandler catches exception
     │  │
     │  ▼
     │  404 Not Found
     │  {
     │    "message": "Listing 99 not found",
     │    "timestamp": "2026-04-14T11:55:51Z",
     │    "status": 404
     │  }
     │
     ├─ Rule Engine Violations
     │  │
     │  ▼
     │  RuleViolations returned by evaluateRules()
     │  │
     │  ▼
     │  Service throws RuleViolationException
     │  │
     │  ▼
     │  422 Unprocessable Entity
     │  {
     │    "violations": [
     │      "Price out of range",
     │      "Year cannot be future"
     │    ]
     │  }
     │
     └─ Database Error
        │
        ▼
        DataIntegrityViolationException
        │
        ▼
        500 Internal Server Error
```

---

## 10. Transaction & Concurrency Management

### Transaction Boundaries

```
@Transactional
public void deleteListingId) {
    │
    ├─ BEGIN TRANSACTION
    │
    ├─ Query: SELECT ... WHERE id = ? (WITH LOCK)
    │  └─ Acquires Row Lock (Pessimistic Locking)
    │
    ├─ Modify: setStatus(EXPIRED)
    │
    ├─ Persist: repository.save()
    │  └─ Update: UPDATE car_listings SET ...
    │
    └─ COMMIT TRANSACTION
       └─ Release Lock
          │
          └─ Other Transactions Can Proceed
```

### Isolation Levels

```
Default Isolation: READ_COMMITTED

Prevents:
├─ Dirty Reads ✅ (Read uncommitted changes)
├─ Non-Repeatable Reads ⚠️ (May occur)
└─ Phantom Reads ⚠️ (May occur)

For Higher Consistency:
└─ Can upgrade to REPEATABLE_READ or SERIALIZABLE
   if needed using @Transactional(isolation = ...)
```

---

## 11. Summary Table

```
┌────────────────────────────────────────────────────────────────┐
│        TDD DIAGRAM SUMMARY - Car Listing Service              │
├────────────────────────────────────────────────────────────────┤
│ Component            │ Description                            │
├────────────────────────────────────────────────────────────────┤
│ DFD Level 0          │ System context & external entities    │
│ DFD Level 1          │ System decomposition into layers      │
│ DFD Level 2          │ Detailed flow for each operation      │
│ Sequence Diagrams    │ Interaction timeline between objects  │
│ Test Coverage        │ Unit & integration test matrix        │
│ TDD Cycle            │ Red → Green → Refactor process       │
│ Data Model           │ Request → Entity → Response flow      │
│ Exception Handling   │ Error flow & HTTP status codes        │
│ Transactions         │ ACID properties & isolation levels    │
└────────────────────────────────────────────────────────────────┘
```

---

## 12. Integration Points

```
Car Listing Service
├── External Systems
│   ├── Drools Rule Engine
│   │   └── Validates listings (price, year, fraud detection)
│   │
│   ├── PostgreSQL Database
│   │   ├── Stores car listings
│   │   └── Persists images metadata
│   │
│   └── JWT Security
│       └── Validates user tokens from API Gateway
│
├── Internal Layers
│   ├── Controller ↔ Service ↔ Repository (Dependency Injection)
│   ├── @Transactional boundary at Service layer
│   └── @Mock all external dependencies in tests
│
└── Test Dependencies
    ├── Mockito (Mocking framework)
    ├── JUnit Jupiter (Test engine)
    ├── H2 Database (In-memory DB for integration tests)
    └── Spring Test Context (Application context loading)
```

---

**Document Version:** 1.0  
**Last Updated:** April 14, 2026  
**Microservice:** Car Listing Service  
**Framework:** Spring Boot 3.2.5  
**Test Framework:** JUnit 5 + Mockito
