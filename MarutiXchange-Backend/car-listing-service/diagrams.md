# Car Listing Microservice - Production-Ready Architecture
## Complete System Architecture with DROOLS Rule Engine, Security & Exception Handling

---

## 1. COMPREHENSIVE SYSTEM ARCHITECTURE OVERVIEW

```
┌────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT APPLICATION                               │
│                    (Web Browser / Mobile App)                               │
└─────────────────────────────┬──────────────────────────────────────────────┘
                              │
                        HTTP/REST Requests
                        (with JWT Token)
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                  CAR LISTING SERVICE (Spring Boot)                         │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │                    SECURITY LAYER                                 │    │
│  │  ┌──────────────────────────────────────────────────────────┐    │    │
│  │  │       SecurityFilterChain → JwtAuthFilter                │    │    │
│  │  │  (Token Validation → UserContext Setup)                  │    │    │
│  │  └──────────────────────────────────────────────────────────┘    │    │
│  └─────────────────────────┬──────────────────────────────────────┘    │
│                            │                                            │
│                            ▼                                            │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │                    CONTROLLER LAYER                               │    │
│  │  ┌──────────────────────┐  ┌──────────────────────┐              │    │
│  │  │CarListingController  │  │CarSearchController   │              │    │
│  │  │- createListing()     │  │- search()            │              │    │
│  │  │- getListingById()    │  │- compare()           │              │    │
│  │  │- updateListing()     │  │- recommendations()   │              │    │
│  │  │- deleteListing()     │  │- getPriceEstimate()  │              │    │
│  │  │- uploadImages()      │  │                      │              │    │
│  │  │- markAsSold()        │  │                      │              │    │
│  │  └──────────┬───────────┘  └──────────┬───────────┘              │    │
│  │             │                        │                          │    │
│  │             └────────────┬───────────┘                          │    │
│  │                          │                                      │    │
│  │  ┌────────────────────────────────────────────────┐            │    │
│  │  │  SellerAnalyticsController                     │            │    │
│  │  │  - getSellerAnalytics()                        │            │    │
│  │  └──────────────┬─────────────────────────────────┘            │    │
│  │                │                                               │    │
│  └────────────────┼───────────────────────────────────────────────┘    │
│                   │                                                     │
│                   ▼                                                     │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │                    SERVICE LAYER                                   │    │
│  │  ┌─────────────────────────────────────────────────────────────┐  │    │
│  │  │  CarListingServiceImpl                                      │  │    │
│  │  │  - createListing() → calls DroolsRuleService               │  │    │
│  │  │  - updateListing()                                          │  │    │
│  │  │  - incrementViewCount()                                     │  │    │
│  │  │  - changeStatus()                                           │  │    │
│  │  └──────────────────┬─────────────────────────────────────────┘  │    │
│  │                     │                                              │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │  DroolsRuleService (Rule Engine Layer)                      │  │    │
│  │  │  ┌────────────────────────────────────────────────────────┐ │  │    │
│  │  │  │ validateListing(carListing)                            │ │  │    │
│  │  │  │  └─ Initialize KieContainer from KieBase              │ │  │    │
│  │  │  │  └─ Load Rules from rule files (.drl)                 │ │  │    │
│  │  │  │  └─ Create KieSession                                 │ │  │    │
│  │  │  │  └─ Insert carListing as FACT                        │ │  │    │
│  │  │  │  └─ Fire Rules:                                       │ │  │    │
│  │  │  │     • Price Validation (min/max bounds)               │ │  │    │
│  │  │  │     • Year Validation (not future year)               │ │  │    │
│  │  │  │     • Mileage Validation (logical bounds)             │ │  │    │
│  │  │  │     • Fraud Detection Rules                           │ │  │    │
│  │  │  │     • Description Quality Checks                      │ │  │    │
│  │  │  │  └─ Collect Violations in RuleViolations object      │ │  │    │
│  │  │  │  └─ Return RuleViolations                             │ │  │    │
│  │  │  └────────────────────────────────────────────────────────┘ │  │    │
│  │  │                                                              │  │    │
│  │  │  KieContainer (Drools Runtime)                             │  │    │
│  │  │  ├─ KieBase (compiled rules)                               │  │    │
│  │  │  └─ KieSession (rule execution context)                    │  │    │
│  │  └──────────────────┬──────────────────────────────────────────┘  │    │
│  │                     │                                              │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │  CarSearchServiceImpl                                        │  │    │
│  │  │  - search()                                                 │  │    │
│  │  │  - getRecommendations()                                     │  │    │
│  │  │  - compare()                                                │  │    │
│  │  └──────────────┬───────────────────────────────────────────────┘  │    │
│  │                 │                                                  │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │  PricingServiceImpl                                          │  │    │
│  │  │  - getEstimate()                                            │  │    │
│  │  └──────────────┬───────────────────────────────────────────────┘  │    │
│  │                 │                                                  │    │
│  │  ┌──────────────────────────────────────────────────────────────┐  │    │
│  │  │  FileUploadServiceImpl                                       │  │    │
│  │  │  - uploadImage()                                            │  │    │
│  │  │  - deleteImage()                                            │  │    │
│  │  │  - generateImageUrl()                                       │  │    │
│  │  └──────────────┬───────────────────────────────────────────────┘  │    │
│  │                 │                                                  │    │
│  └─────────────────┼──────────────────────────────────────────────────┘    │
│                    │                                                        │
│                    ▼                                                        │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │              EXCEPTION HANDLING LAYER                             │    │
│  │  ┌────────────────────────────────────────────────────────────┐  │    │
│  │  │  GlobalExceptionHandler (@ControllerAdvice)               │  │    │
│  │  │  ┌──────────────────────────────────────────────────────┐ │  │    │
│  │  │  │ @ExceptionHandler(ValidationException)               │ │  │    │
│  │  │  │  └─ Returns ApiResponse (400 Bad Request)            │ │  │    │
│  │  │  │  └─ Includes violation details                       │ │  │    │
│  │  │  ├──────────────────────────────────────────────────────┤ │  │    │
│  │  │  │ @ExceptionHandler(ResourceNotFoundException)         │ │  │    │
│  │  │  │  └─ Returns ApiResponse (404 Not Found)              │ │  │    │
│  │  │  ├──────────────────────────────────────────────────────┤ │  │    │
│  │  │  │ @ExceptionHandler(RuleViolationException)            │ │  │    │
│  │  │  │  └─ Returns ApiResponse (400 Bad Request)            │ │  │    │
│  │  │  │  └─ Includes rule violation details                  │ │  │    │
│  │  │  ├──────────────────────────────────────────────────────┤ │  │    │
│  │  │  │ @ExceptionHandler(Exception)                         │ │  │    │
│  │  │  │  └─ Returns ApiResponse (500 Internal Server Error)  │ │  │    │
│  │  │  │  └─ Logs error for debugging                         │ │  │    │
│  │  │  └──────────────────────────────────────────────────────┘ │  │    │
│  │  └────────────────────────────────────────────────────────────┘  │    │
│  │                                                                   │    │
│  └───────────────────────────────────────────────────────────────────┘    │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │                REPOSITORY LAYER                                   │    │
│  │  ┌──────────────────────┐  ┌──────────────────────┐              │    │
│  │  │CarListingRepository  │  │CarImageRepository    │              │    │
│  │  │(JpaRepository)       │  │(JpaRepository)       │              │    │
│  │  └──────────────┬───────┘  └──────────────┬───────┘              │    │
│  │                │                         │                      │    │
│  │  ┌─────────────────────────────────────────────────────────┐    │    │
│  │  │  CarReviewRepository                                    │    │    │
│  │  │  (JpaRepository)                                         │    │    │
│  │  └──────────────┬──────────────────────────────────────────┘    │    │
│  │                │                                               │    │
│  └────────────────┼───────────────────────────────────────────────┘    │
│                   │                                                     │
│                   ▼                                                     │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │           LOGGING & MONITORING LAYER                              │    │
│  │  ┌────────────────────────────────────────────────────────────┐  │    │
│  │  │  SLF4J Logger (Logback)                                    │  │    │
│  │  │  ├─ Audit Logs (who created/modified listings)            │  │    │
│  │  │  ├─ Rule Validation Logs                                  │  │    │
│  │  │  ├─ Error Logs (exceptions & failures)                    │  │    │
│  │  │  └─ Performance Logs (response times)                     │  │    │
│  │  │                                                            │  │    │
│  │  │  Spring Boot Actuator                                     │  │    │
│  │  │  ├─ /actuator/health (service health)                     │  │    │
│  │  │  ├─ /actuator/metrics (performance metrics)               │  │    │
│  │  │  ├─ /actuator/prometheus (Prometheus metrics)             │  │    │
│  │  │  └─ /actuator/loggers (runtime log level management)      │  │    │
│  │  └────────────────────────────────────────────────────────────┘  │    │
│  │                                                                   │    │
│  └───────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└────────────────────────────────────────────────────────────────────────────┘
                                     │
                 ┌───────────────────┼───────────────────┐
                 │                   │                   │
                 ▼                   ▼                   ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌───────────────────┐
│    MySQL Database    │  │   File Storage       │  │  Elasticsearch    │
│  (Persisting Data)   │  │   (Image Files)      │  │ (Search Index)    │
│  ├─ car_listings     │  │                      │  │                   │
│  ├─ car_images       │  │  /images/cars/...    │  │ (Optional)        │
│  ├─ car_reviews      │  │                      │  │                   │
│  └─ user_profiles    │  │  S3 / Local Storage  │  └───────────────────┘
└──────────────────────┘  └──────────────────────┘
```

---

## 2. REQUEST FLOW WITH SECURITY & VALIDATION

```
┌─────────────────┐
│  HTTP Request   │
│ (JWT Token)     │
└────────┬────────┘
         │
         ▼
┌──────────────────────────────────────────────────────┐
│  SecurityFilterChain                                 │
│  ┌────────────────────────────────────────────────┐  │
│  │  JwtAuthFilter                                 │  │
│  │  - Extract token from header                   │  │
│  │  - Validate token signature                    │  │
│  │  - Extract user info & authorities             │  │
│  │  - Set SecurityContext                         │  │
│  └────────────────────────────────────────────────┘  │
└───────────────┬──────────────────────────────────────┘
                │
                ▼
         ┌──────────────┐
         │  Authorized? │
         └──┬───────┬──┘
       YES  │       │  NO
            │       └─────────────────┐
            ▼                         │
 ┌─────────────────────────┐          │
 │  CarListingController   │          │
 │  .createListing()       │          │
 └────────────┬────────────┘          │
              │                       │
              ▼                       │
 ┌─────────────────────────┐          │
 │  Input Validation       │          │
 │  (@Valid annotation)    │          │
 └────────────┬────────────┘          │
              │                       │
              ▼                       │
        ┌──────────────┐              │
        │  Valid Input?│              │
        └──┬───────┬──┘               │
      YES  │       │  NO              │
           │       └────────┐         │
           ▼                │         │
 ┌──────────────────────┐   │         │
 │  CarListingService   │   │         │
 │  .createListing()    │   │         │
 └────────────┬─────────┘   │         │
              │             │         │
              ▼             │         │
 ┌──────────────────────────────────┐ │         │
 │  DroolsRuleService               │ │         │
 │  .validateListing()              │ │         │
 │  ┌──────────────────────────────┐│ │         │
 │  │ Execute Validation Rules:    ││ │         │
 │  │ • Price (100K-1Cr bounds)    ││ │         │
 │  │ • Year (not in future)       ││ │         │
 │  │ • Mileage (logical ranges)   ││ │         │
 │  │ • Fraud Detection            ││ │         │
 │  │ • Description Quality        ││ │         │
 │  └──────────────────────────────┘│ │         │
 └────────────┬─────────────────────┘ │         │
              │                       │         │
              ▼                       │         │
        ┌─────────────┐               │         │
        │  Violations?│               │         │
        └──┬───────┬──┘               │         │
      NO   │       │  YES             │         │
           │       └────────┐         │         │
           ▼                │         │         │
 ┌──────────────────────┐   │         │         │
 │  Save to Database    │   │         │         │
 │  .save(carListing)   │   │         │         │
 └────────────┬─────────┘   │         │         │
              │             │         │         │
              ▼             │         │         │
 ┌──────────────────────┐   │         │         │
 │  Return 201 Created  │   │         │         │
 │  + CarListingResponse│   │         │         │
 └────────────┬─────────┘   │         │         │
              │             ▼         │         │
              │   ┌──────────────────────────┐  │
              │   │ GlobalExceptionHandler   │  │
              │   │ ValidationException      │  │
              │   │ → 400 Bad Request        │  │
              │   │ + Violation Details      │  │
              │   └──────────────────────────┘  │
              │             ▲                   │
              │             │                   │
              └─────────────┼───────────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │  HTTP Error  │
                    │  Response    │
                    └──────────────┘
                            │
                            ▼
                    ┌──────────────────┐
                    │  Client          │
                    │  (Error Info)    │
                    └──────────────────┘
```

---

## 3. DATA FLOW DIAGRAMS

### 3.1 Create Car Listing with Validation Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /listings
       │ CarListingRequest
       │ (brand, model, year, price,
       │  mileage, description, city)
       ▼
┌────────────────────────────────────────────────────────────┐
│  CarListingController.createListing()                      │
│  - @Valid annotation validates basic constraints          │
│  - Extract JWT user context (seller)                       │
│  - Pass to service layer                                   │
└──────────────┬──────────────────────────────────────────────┘
               │
               ▼
┌────────────────────────────────────────────────────────────┐
│  CarListingServiceImpl.createListing()                      │
│  1. Validate seller exists in database                     │
│  2. Create CarListing entity with all fields               │
│  3. Call DroolsRuleService.validateListing()              │
└──────────────┬──────────────────────────────────────────────┘
               │
               ▼
        ┌──────────────────┐
        │ Drools Validation│
        │ (Rule Engine)    │
        └────┬─────────┬───┘
             │         │
       YES   │         │  NO (Violations Found)
    (Valid)  │         │
             │         └──────────────────────────┐
             ▼                                     │
    ┌──────────────────┐                         │
    │ Proceed to Save  │                         │
    └────────┬─────────┘                         │
             │                                   │
             ▼                                   │
    ┌──────────────────────────┐               │
    │ CarListingRepository     │               │
    │ .save(carListing)        │               │
    │ (INSERT INTO car_listings│               │
    │  with status = PENDING)  │               │
    └────────┬─────────────────┘               │
             │                                 │
             ▼                                 │
    ┌──────────────────────────┐               │
    │  MySQL Database          │               │
    │  (Listing Saved)         │               │
    └────────┬─────────────────┘               │
             │                                 │
             ▼                                 │
    ┌──────────────────────────┐               │
    │ Return ApiResponse       │               │
    │ Status: 201 CREATED      │               │
    │ + CarListingResponse DTO │               │
    └────────┬─────────────────┘               │
             │                                 │
             └─────────────┬────────────────────┤
                           │                    │
                           ▼                    ▼
                  ┌───────────────┐   ┌──────────────────┐
                  │  Client       │   │ GlobalException  │
                  │  Success 201  │   │ Handler          │
                  └───────────────┘   │                  │
                                      │ RuleViolation    │
                                      │ Exception        │
                                      │                  │
                                      │ Returns 400      │
                                      │ Bad Request      │
                                      │ + Violation Info │
                                      └──────────────────┘
                                           │
                                           ▼
                                      ┌──────────────┐
                                      │  Client      │
                                      │  Error 400   │
                                      └──────────────┘
```

### 3.2 Image Upload Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /listings/{id}/images
       │ MultipartFile[] imageFiles
       ▼
┌──────────────────────────────────────┐
│  CarListingController                │
│  .uploadImages()                     │
│  - Verify listing exists             │
│  - Verify user owns listing          │
│  - Validate files (type, size)       │
└──────────────┬──────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│  FileUploadServiceImpl                │
│  .uploadImage()                      │
│  - Generate unique filename          │
│  - Validate file format              │
│  - Compress image if needed          │
│  - Upload to file system / S3        │
└──────────────┬──────────────────────┘
               │
               ▼
        ┌─────────────────┐
        │  File System    │
        │  /images/cars/  │
        │  {timestamp}... │
        │  .jpg           │
        └────────┬────────┘
                 │
                 ▼
        ┌──────────────────────────┐
        │  Generate Image URL      │
        │  /api/images/{imageId}   │
        └────────┬─────────────────┘
                 │
                 ▼
    ┌───────────────────────────────┐
    │  Create CarImage Entity       │
    │  - listingId                  │
    │  - imageUrl                   │
    │  - uploadedAt                 │
    └────────┬──────────────────────┘
             │
             ▼
    ┌───────────────────────────────┐
    │  CarImageRepository           │
    │  .save(carImage)              │
    │  (INSERT INTO car_images)     │
    └────────┬──────────────────────┘
             │
             ▼
    ┌───────────────────────────────┐
    │  MySQL Database               │
    │  car_images table updated     │
    └────────┬──────────────────────┘
             │
             ▼
    ┌───────────────────────────────┐
    │  ApiResponse                  │
    │  Status: 201 CREATED          │
    │  + List<CarImageResponse>     │
    │  (with URLs)                  │
    └───────────┬──────────────────┘
                │
                ▼
           ┌──────────────┐
           │  Client      │
           │  Success 201 │
           └──────────────┘
```

### 3.3 Search Car Listings Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /search
       │ CarSearchRequest
       │ (brand, model, priceRange,
       │  city, transmission, fuelType)
       ▼
┌────────────────────────────────────────────────────────────┐
│  CarSearchController.search()                              │
│  - Validate search criteria                                │
│  - Extract pagination params                               │
└──────────────┬──────────────────────────────────────────────┘
               │
               ▼
┌────────────────────────────────────────────────────────────┐
│  CarSearchServiceImpl.search()                              │
│  1. Build Criteria specification                           │
│  2. Apply filters (brand, model, price, city, etc.)       │
│  3. Add pagination                                         │
│  4. Execute search query                                   │
└──────────────┬──────────────────────────────────────────────┘
               │
               ▼
┌────────────────────────────────────────────────────────────┐
│  CarListingRepository (Spring Data JPA)                    │
│  .findAll(specification, pageable)                         │
└──────────────┬──────────────────────────────────────────────┘
               │
               ▼
        ┌──────────────────┐
        │  MySQL Database  │
        │  SELECT * FROM   │
        │  car_listings    │
        │  WHERE status=.. │
        │  AND brand=..    │
        │  AND price...    │
        │  LIMIT OFFSET    │
        └────────┬─────────┘
                 │
                 ▼
    ┌─────────────────────────────────┐
    │  List<CarListing> (Database)    │
    │  Retrieved from DB              │
    └────────┬───────────────────────┘
             │
             ▼
    ┌─────────────────────────────────┐
    │  Convert to DTOs                │
    │  CarListing → CarSearchResponse │
    │  (map attributes)               │
    └────────┬───────────────────────┘
             │
             ▼
    ┌─────────────────────────────────┐
    │  Page<CarSearchResponse>         │
    │  + Metadata (totalElements,      │
    │    totalPages, currentPage)      │
    └────────┬───────────────────────┘
             │
             ▼
    ┌─────────────────────────────────┐
    │  ApiResponse<Page<CarResponse>>  │
    │  Status: 200 OK                 │
    └───────────┬────────────────────┘
                │
                ▼
           ┌──────────────┐
           │  Client      │
           │  Success 200 │
           └──────────────┘
```

---

## 4. DROOLS RULE ENGINE - DETAILED FLOW

```
┌──────────────────────────────────────────────────────────────────┐
│                  DROOLS RULE ENGINE ARCHITECTURE                  │
└──────────────────────────────────────────────────────────────────┘

DroolsRuleService.validateListing(CarListing)
│
├─ Step 1: Initialize KieContainer
│  ├─ Load KieBase from src/main/resources/rules/
│  ├─ Rules files (.drl):
│  │  ├─ PriceValidation.drl
│  │  ├─ YearValidation.drl
│  │  ├─ MileageValidation.drl
│  │  ├─ FraudDetection.drl
│  │  └─ DescriptionQuality.drl
│  └─ Return KieContainer
│
├─ Step 2: Create KieSession
│  └─ KieSession handles rule execution context
│
├─ Step 3: Initialize Facts & Working Memory
│  ├─ Insert CarListing as FACT
│  ├─ Insert RuleViolations object (empty)
│  └─ Insert RuleContext (metadata)
│
├─ Step 4: Fire Rules (Execute ALL matching rules)
│  │
│  ├─ RULE 1: Price Validation
│  │  ├─ Condition: IF (price < 100000 OR price > 1_00_00_000)
│  │  ├─ Action: Add violation "Price must be between 1L - 1Cr"
│  │  └─ Status: FIRED ✓
│  │
│  ├─ RULE 2: Year Validation
│  │  ├─ Condition: IF (year > currentYear)
│  │  ├─ Action: Add violation "Year cannot be in future"
│  │  └─ Status: FIRED ✓
│  │
│  ├─ RULE 3: Mileage Validation
│  │  ├─ Condition: IF (mileage < 0 OR mileage > 500000)
│  │  ├─ Action: Add violation "Mileage out of valid range"
│  │  └─ Status: FIRED ✓
│  │
│  ├─ RULE 4: Fraud Detection
│  │  ├─ Condition: IF (price too low for year AND high mileage)
│  │  ├─ OR (seller has fraud history)
│  │  ├─ Action: Add violation "Listing flagged for fraud review"
│  │  └─ Status: FIRED ✓
│  │
│  ├─ RULE 5: Description Quality
│  │  ├─ Condition: IF (description.length < 50)
│  │  ├─ Action: Add warning "Add more details for better visibility"
│  │  └─ Status: FIRED ✓
│  │
│  └─ (Additional rules can be added/modified)
│
├─ Step 5: Retrieve Results
│  ├─ Get RuleViolations object from working memory
│  ├─ Extract violations list
│  └─ Return RuleViolations object
│
└─ Step 6: Decision Point in Service
   ├─ IF (violations.isEmpty())
   │  └─ Proceed to save listing
   │
   └─ ELSE (violations exist)
      └─ Throw RuleViolationException
         ├─ Message: "Listing failed validation"
         ├─ Details: List of all violations
         └─ HTTP Status: 400 Bad Request


EXAMPLE RULE FILE (PriceValidation.drl):
───────────────────────────────────────
package com.listing.rules;

import com.listing.entity.CarListing;
import com.listing.model.RuleViolations;

rule "Price_Must_Be_Within_Bounds"
    when
        $listing : CarListing( 
            askingPrice < 100000 || askingPrice > 100000000 
        )
        $violations : RuleViolations()
    then
        $violations.addViolation(
            "Price must be between ₹1,00,000 and ₹1,00,00,000"
        );
end

rule "Minimum_Price_Check"
    when
        $listing : CarListing( askingPrice < 50000 )
        $violations : RuleViolations()
    then
        $violations.addViolation(
            "Asking price is unusually low - please review"
        );
end
```

---

## 5. EXCEPTION HANDLING FLOW

```
┌──────────────────────────────────────────────────────────────────┐
│              GLOBAL EXCEPTION HANDLER (@ControllerAdvice)         │
└──────────────────────────────────────────────────────────────────┘

                           Exception Thrown
                                  │
                                  ▼
                    ┌──────────────────────────┐
                    │  GlobalExceptionHandler  │
                    │  (catches all exceptions)│
                    └──────────────┬───────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                            │
                    ▼                            ▼
           ┌────────────────────┐    ┌─────────────────────────┐
           │ Checked Exception  │    │  Unchecked Exception    │
           └─────────┬──────────┘    └────────────┬────────────┘
                     │                           │
          ┌──────────┼──────────┐      ┌─────────┼─────────┐
          │          │          │      │         │         │
          ▼          ▼          ▼      ▼         ▼         ▼
    ┌──────────┐ ┌──────────┐ ┌──────────────────────┐ ┌──────────────┐
    │Validation│ │Resource  │ │RuleViolation         │ │General       │
    │Exception │ │NotFound  │ │Exception             │ │Exception     │
    │          │ │Exception │ │                      │ │              │
    │HTTP 400  │ │HTTP 404  │ │HTTP 400              │ │HTTP 500      │
    └────┬─────┘ └────┬─────┘ └──────────┬──────────┘ └──────┬───────┘
         │            │                  │                   │
         ▼            ▼                  ▼                   ▼
    ┌────────────────────────────────────────────────────────────┐
    │  Build ApiResponse<ErrorResponse>                          │
    │  {                                                         │
    │    "status": "ERROR",                                      │
    │    "statusCode": 400 | 404 | 500,                          │
    │    "message": "Human readable error message",              │
    │    "timestamp": "2024-01-15T10:30:45Z",                    │
    │    "path": "/api/listings",                                │
    │    "errors": [                                             │
    │      {                                                     │
    │        "field": "askingPrice",                             │
    │        "message": "Price must be > 0",                     │
    │        "rejectedValue": -5000                              │
    │      }                                                     │
    │    ]  [for validation errors]                             │
    │    OR                                                      │
    │    "violations": [                                         │
    │      "Price must be between 1L - 1Cr",                     │
    │      "Mileage out of valid range"                          │
    │    ]  [for rule violations]                               │
    │  }                                                         │
    └────────────────┬──────────────────────────────────────────┘
                     │
                     ▼
         ┌──────────────────────────┐
         │  Log Error               │
         │  - Stack trace           │
         │  - Request context       │
         │  - User info             │
         │  - Timestamp             │
         └──────────────┬───────────┘
                        │
                        ▼
         ┌──────────────────────────┐
         │  Return HTTP Response    │
         │  - Status code           │
         │  - Error body            │
         │  - Headers               │
         └──────────────┬───────────┘
                        │
                        ▼
         ┌──────────────────────────┐
         │  Client receives error   │
         │  response with details   │
         └──────────────────────────┘
```

---

## 6. SEQUENCE DIAGRAMS

### 6.1 Create Car Listing Sequence (Complete with Drools)

```
Time ────────────────────────────────────────────────────────────────────►

Client      JwtFilter      Controller      Service      Drools      Repository      Database
  │            │               │              │           │             │              │
  │ POST /listings             │              │           │             │              │
  │ (JWT Token) ────────────►  │              │           │             │              │
  │                            │              │           │             │              │
  │                            │ Validate token & extract user context   │              │
  │                            │              │           │             │              │
  │                            │ createListing(req)       │             │              │
  │                            │ ───────────────────►    │             │              │
  │                            │              │           │             │              │
  │                            │              │ validateSeller()        │              │
  │                            │              │           │             │              │
  │                            │              │ createEntity()          │              │
  │                            │              │           │             │              │
  │                            │              │ validateListing()       │              │
  │                            │              │ ──────────────────►    │              │
  │                            │              │           │             │              │
  │                            │              │           │ Load Rules from KieBase   │
  │                            │              │           │             │              │
  │                            │              │           │ Create KieSession        │
  │                            │              │           │             │              │
  │                            │              │           │ Insert CarListing FACT   │
  │                            │              │           │             │              │
  │                            │              │           │ Fire Rules:             │
  │                            │              │           │  • Price validation      │
  │                            │              │           │  • Year validation       │
  │                            │              │           │  • Mileage validation    │
  │                            │              │           │  • Fraud detection       │
  │                            │              │           │  • Description quality   │
  │                            │              │           │             │              │
  │                            │              │           │ Collect violations       │
  │                            │              │           │             │              │
  │                            │              │ ◄──────────────────     │              │
  │                            │              │ RuleViolations          │              │
  │                            │              │           │             │              │
  │                         IF violations exist:          │             │              │
  │                            │              │ ─────────────────────┐  │              │
  │                            │              │ Throw RuleViolation  │  │              │
  │                            │              │ Exception            │  │              │
  │                            │ ◄─────────────────────────┘          │              │
  │ GlobalExceptionHandler catches exception                          │              │
  │                            │              │           │             │              │
  │ ApiResponse (400) ◄────────│              │           │             │              │
  │ {                          │              │           │             │              │
  │   "errors": [...],         │              │           │             │              │
  │   "violations": [...]      │              │           │             │              │
  │ }                          │              │           │             │              │
  │                            │              │           │             │              │
  │                         ELSE (no violations):         │             │              │
  │                            │              │ save()               │              │
  │                            │              │ ─────────────────────────►          │
  │                            │              │           │             │              │
  │                            │              │           │             │ INSERT INTO  │
  │                            │              │           │             │ car_listings │
  │                            │              │           │             │ ───────────► │
  │                            │              │           │             │              │
  │                            │              │           │             │ ◄───────────│
  │                            │              │           │             │ (Saved)     │
  │                            │              │ ◄────────────────────────│            │
  │                            │              │ CarListingResponse      │            │
  │                            │ ◄───────────────────     │             │              │
  │ ApiResponse ◄──────────────│             │           │             │              │
  │ (201 Created)              │             │           │             │              │
```

### 6.2 Image Upload Sequence

```
Time ────────────────────────────────────────────────────────────────────►

Client      Controller      FileService      FileSystem      Repository      Database
  │            │               │                 │             │              │
  │ POST /listings/{id}/       │                 │             │              │
  │ images (MultipartFile[])   │                 │             │              │
  │ ──────────────────────►     │                 │             │              │
  │                            │                 │             │              │
  │                            │ validateUser()  │             │              │
  │                            │                 │             │              │
  │                            │ uploadImage()   │             │              │
  │                            │ ────────────────►            │             │              │
  │                            │                 │             │              │
  │                            │                 │ For each image:            │
  │                            │                 │             │              │
  │                            │                 │ generateFilename()         │
  │                            │                 │             │              │
  │                            │                 │ validateFormat()           │
  │                            │                 │             │              │
  │                            │                 │ saveToFileSystem()         │
  │                            │                 │ ──────────►  │             │              │
  │                            │                 │              │ (File saved)│              │
  │                            │                 │ ◄──────────  │             │              │
  │                            │                 │              │             │              │
  │                            │                 │ generateImageUrl()         │
  │                            │                 │             │              │
  │                            │ ◄─────────────────           │             │              │
  │                            │ List<ImageUrl>  │             │              │
  │                            │                 │             │              │
  │                            │ Create CarImage entities     │              │
  │                            │ saveImages()              │              │
  │                            │ ────────────────────────────────►          │
  │                            │                 │             │              │
  │                            │                 │             │ INSERT INTO  │
  │                            │                 │             │ car_images   │
  │                            │                 │             │ ───────────► │
  │                            │                 │             │              │
  │                            │                 │             │ ◄───────────│
  │                            │                 │             │ (Saved)     │
  │                            │ ◄────────────────────────────────│           │
  │                            │ List<CarImageResponse>          │           │
  │                            │ (with URLs)                     │           │
  │ ApiResponse ◄──────────────│                 │             │              │
  │ (201 Created)              │                 │             │              │
  │ + Images                   │                 │             │              │
```

### 6.3 Search Car Listings Sequence

```
Time ────────────────────────────────────────────────────────────────────►

Client      Controller      Service      Repository      Database
  │            │               │             │              │
  │ POST /search                │             │              │
  │ CarSearchRequest ──────►     │             │              │
  │                            │             │              │
  │                            │ search()    │              │
  │                            │ ──────────►  │              │
  │                            │             │              │
  │                            │             │ buildCriteria()
  │                            │             │              │
  │                            │             │ findAll(spec, pageable)
  │                            │             │ ───────────────────►
  │                            │             │              │
  │                            │             │              │ SELECT * FROM
  │                            │             │              │ car_listings
  │                            │             │              │ WHERE ...
  │                            │             │              │ LIMIT OFFSET
  │                            │             │              │ ───────────►
  │                            │             │              │
  │                            │             │              │ ◄──────────
  │                            │             │              │ Page<CarListing>
  │                            │             │ ◄───────────────────
  │                            │             │ Page<CarListing>
  │                            │             │
  │                            │ convertToDTOs()
  │                            │ applyProjection()
  │                            │
  │                            │ Page<CarSearchResponse>
  │ ApiResponse ◄──────────────│             │              │
  │ (200 OK)                   │             │              │
  │ Page<CarSearchResponse>     │             │              │
```

---

## 7. COMPONENT DEPENDENCIES

```
DEPENDENCY INJECTION HIERARCHY:

CarListingController
    │
    ├─ depends on CarListingService
    ├─ depends on JwtTokenUtil
    └─ depends on FileUploadService

CarListingServiceImpl (implements CarListingService)
    │
    ├─ depends on CarListingRepository
    ├─ depends on DroolsRuleService
    ├─ depends on FileUploadService
    ├─ depends on CarListingMapper (DTO conversion)
    └─ depends on Logger (SLF4J)

DroolsRuleService
    │
    ├─ depends on KieContainer (Drools)
    ├─ depends on RuleViolationException
    └─ depends on Logger (SLF4J)

FileUploadServiceImpl
    │
    ├─ depends on CarImageRepository
    ├─ depends on File System operations
    └─ depends on Logger (SLF4J)

CarListingRepository
    └─ JpaRepository<CarListing, Long>

CarImageRepository
    └─ JpaRepository<CarImage, Long>

GlobalExceptionHandler (@ControllerAdvice)
    │
    ├─ handles ValidationException → 400
    ├─ handles RuleViolationException → 400
    ├─ handles ResourceNotFoundException → 404
    ├─ handles Exception (generic) → 500
    └─ depends on Logger (SLF4J)
```

---

## 8. RULE FILES STRUCTURE

```
src/main/resources/rules/
│
├─ PriceValidation.drl
│  ├─ Rule: Price_Must_Be_Within_Bounds
│  ├─ Rule: Minimum_Price_Check
│  └─ Rule: Price_Depreciation_Check
│
├─ YearValidation.drl
│  ├─ Rule: Year_Not_In_Future
│  ├─ Rule: Year_Not_Too_Old
│  └─ Rule: Year_Validity_Check
│
├─ MileageValidation.drl
│  ├─ Rule: Mileage_Within_Bounds
│  ├─ Rule: Mileage_Year_Consistency
│  └─ Rule: High_Mileage_Warning
│
├─ FraudDetection.drl
│  ├─ Rule: Suspiciously_Low_Price
│  ├─ Rule: Seller_Fraud_History
│  ├─ Rule: Multiple_Listing_Per_Day
│  └─ Rule: Conflicting_Attributes
│
└─ DescriptionQuality.drl
   ├─ Rule: Minimum_Description_Length
   ├─ Rule: Prohibited_Keywords
   └─ Rule: Contact_Info_In_Description
```

---

## 9. ERROR RESPONSE EXAMPLES

### 9.1 Validation Error (400 Bad Request)

```json
{
  "status": "ERROR",
  "statusCode": 400,
  "message": "Input validation failed",
  "timestamp": "2024-01-15T10:30:45Z",
  "path": "/api/listings",
  "errors": [
    {
      "field": "askingPrice",
      "message": "must be greater than 0",
      "rejectedValue": -5000
    },
    {
      "field": "brand",
      "message": "must not be blank",
      "rejectedValue": null
    }
  ]
}
```

### 9.2 Rule Violation Error (400 Bad Request)

```json
{
  "status": "ERROR",
  "statusCode": 400,
  "message": "Listing failed validation rules",
  "timestamp": "2024-01-15T10:30:45Z",
  "path": "/api/listings",
  "violations": [
    "Price must be between ₹1,00,000 and ₹1,00,00,000",
    "Year cannot be in future",
    "Mileage out of valid range"
  ]
}
```

### 9.3 Resource Not Found (404)

```json
{
  "status": "ERROR",
  "statusCode": 404,
  "message": "Resource not found",
  "timestamp": "2024-01-15T10:30:45Z",
  "path": "/api/listings/12345",
  "details": "Car listing with ID 12345 not found"
}
```

### 9.4 Internal Server Error (500)

```json
{
  "status": "ERROR",
  "statusCode": 500,
  "message": "Internal server error",
  "timestamp": "2024-01-15T10:30:45Z",
  "path": "/api/listings",
  "details": "An unexpected error occurred. Please contact support."
}
```

---

## 10. ACTUATOR ENDPOINTS FOR MONITORING

```
SPRING BOOT ACTUATOR ENDPOINTS:

/actuator/health
    └─ Returns service health status
       Example: { "status": "UP" }

/actuator/metrics
    └─ Lists available metrics
       Examples:
       - jvm.memory.used
       - process.cpu.usage
       - http.server.requests

/actuator/metrics/http.server.requests
    └─ HTTP request metrics
       - count: total requests
       - totalTime: total response time
       - max: maximum response time

/actuator/prometheus
    └─ Prometheus metrics format
       (for integration with monitoring stack)

/actuator/loggers
    └─ View and manage log levels
       Example: GET /actuator/loggers/com.listing
       POST /actuator/loggers/com.listing
       { "configuredLevel": "DEBUG" }

/actuator/info
    └─ Application information
       (version, name, description)
```

---

## 11. LOGGING PATTERNS (SLF4J with Logback)

```
LOGGING LEVELS & USE CASES:

ERROR:
    - GlobalExceptionHandler catches exception
    - Log message: "Request processing failed"
    - Stack trace: Full exception details
    - HTTP Response: 400/404/500

WARN:
    - Rule violation detected
    - Log message: "Listing failed validation: [violations]"
    - User can retry with corrected data
    - HTTP Response: 400

INFO:
    - Successful listing creation
    - Log message: "Listing created: id=123, seller=456"
    - File upload successful
    - Search executed

DEBUG:
    - Service method entry/exit
    - Rule engine actions
    - Database query details
    - Repository operations

EXAMPLE LOG OUTPUTS:
──────────────────────────────────────────────
2024-01-15 10:30:45.123 [http-nio-8080-exec-1] DEBUG
com.listing.service.CarListingServiceImpl - 
Entering createListing() with request: CarListingRequest(...)

2024-01-15 10:30:45.456 [http-nio-8080-exec-1] DEBUG
com.listing.rule.DroolsRuleService - 
Executing validation rules for listing: CarListing(id=null, ...)

2024-01-15 10:30:45.789 [http-nio-8080-exec-1] WARN
com.listing.rule.DroolsRuleService - 
Validation failed with violations: [Price too low, High mileage]

2024-01-15 10:30:45.890 [http-nio-8080-exec-1] ERROR
com.listing.exception.GlobalExceptionHandler - 
RuleViolationException caught: Request processing failed
java.lang.Exception: at com.listing.service...
    at com.listing.controller...

2024-01-15 10:30:46.100 [http-nio-8080-exec-1] INFO
com.listing.service.CarListingServiceImpl - 
Listing created successfully: id=789, seller=456, status=PENDING
```

---

## 12. KEY IMPROVEMENTS SUMMARY

| # | Feature | Before | After |
|---|---------|--------|-------|
| 1 | Validation | Basic @Valid | Drools Rule Engine + GlobalExceptionHandler |
| 2 | Security | None mentioned | JwtAuthFilter + SecurityFilterChain |
| 3 | Exception Handling | Not shown | Comprehensive GlobalExceptionHandler with 400/404/500 |
| 4 | File Upload | Listed but no detail | Complete upload flow with FileUploadService |
| 5 | Rule Engine | Not present | Full Drools integration with KieContainer |
| 6 | Monitoring | None | Spring Boot Actuator endpoints |
| 7 | Logging | None | SLF4J with comprehensive logging patterns |
| 8 | Architecture | Mixed concerns | Layered: Controller → Service → Rule Engine → Repository |
| 9 | Data Validation | Single validation | Multi-layer: Input validation + Rule validation |
| 10 | Documentation | Basic diagrams | Production-ready with detailed flows |

---

## 13. DEPLOYMENT CONSIDERATIONS

```
PRODUCTION DEPLOYMENT CHECKLIST:

┌─────────────────────────────────────────────────────┐
│  1. DROOLS CONFIGURATION                            │
│  ├─ KieModule properly packaged                     │
│  ├─ Rule files (.drl) in classpath                 │
│  ├─ KieContainer initialized on startup            │
│  └─ Rule updates strategy (reload at runtime)      │
│                                                     │
│  2. SECURITY SETUP                                  │
│  ├─ JWT secret configured (properties/env)         │
│  ├─ Token expiration time set                       │
│  ├─ Roles/authorities properly configured          │
│  └─ CORS settings for frontend                     │
│                                                     │
│  3. FILE STORAGE                                    │
│  ├─ File system permissions set                    │
│  ├─ Backup strategy for images                     │
│  ├─ S3/Cloud storage configuration (if applicable) │
│  └─ File cleanup policies for orphaned images      │
│                                                     │
│  4. DATABASE                                        │
│  ├─ Connection pooling configured                  │
│  ├─ Migrations executed                            │
│  ├─ Indexes created for search queries             │
│  └─ Backup and recovery strategy                   │
│                                                     │
│  5. MONITORING & LOGGING                           │
│  ├─ Logging level set to INFO/WARN                 │
│  ├─ Log aggregation configured (ELK/Splunk)       │
│  ├─ Actuator endpoints secured                     │
│  └─ Alerts configured for errors                   │
│                                                     │
│  6. PERFORMANCE TUNING                             │
│  ├─ Cache configuration (Redis/Caffeine)           │
│  ├─ Thread pool sizes optimized                    │
│  ├─ Database query optimization                    │
│  └─ Image compression/resizing                     │
│                                                     │
│  7. ERROR HANDLING                                  │
│  ├─ GlobalExceptionHandler tested                  │
│  ├─ Fallback responses defined                     │
│  ├─ Circuit breaker for external calls             │
│  └─ Graceful degradation strategy                  │
│                                                     │
│  8. SECURITY HARDENING                             │
│  ├─ HTTPS enforced                                 │
│  ├─ CSRF protection enabled                        │
│  ├─ SQL injection prevention (parameterized)       │
│  ├─ Rate limiting configured                       │
│  └─ Input sanitization applied                     │
│                                                     │
│  9. TESTING                                         │
│  ├─ Unit tests for services                        │
│  ├─ Integration tests for rule engine              │
│  ├─ Controller tests with MockMvc                  │
│  └─ Load testing completed                         │
│                                                     │
│  10. DOCUMENTATION                                  │
│  ├─ API documentation (Swagger/OpenAPI)            │
│  ├─ Rule documentation                             │
│  ├─ Deployment runbook                             │
│  └─ Troubleshooting guide                          │
└─────────────────────────────────────────────────────┘
```

---

## 14. TECHNOLOGY STACK REFERENCE

```
┌─────────────────────────────────────────────────────┐
│  TECHNOLOGY STACK                                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  FRAMEWORK & RUNTIME:                              │
│  • Spring Boot 3.x (REST API framework)            │
│  • Spring Data JPA (ORM & database access)         │
│  • Spring Security (authentication/authorization)  │
│  • Drools 8.x (Rule Engine)                        │
│                                                     │
│  DATABASE:                                          │
│  • MySQL 8.0 (relational database)                 │
│  • Hibernate (JPA implementation)                  │
│  • Liquibase/Flyway (database migrations)          │
│                                                     │
│  SECURITY:                                          │
│  • JWT (JSON Web Tokens)                           │
│  • Spring Security (SecurityFilterChain)           │
│  • BCrypt (password hashing)                       │
│                                                     │
│  LOGGING & MONITORING:                              │
│  • SLF4J (logging facade)                          │
│  • Logback (logging implementation)                │
│  • Spring Boot Actuator (metrics)                  │
│  • Micrometer (metrics library)                    │
│                                                     │
│  FILE HANDLING:                                     │
│  • Spring Web (MultipartFile support)              │
│  • AWS S3 SDK (optional cloud storage)             │
│                                                     │
│  TESTING:                                           │
│  • JUnit 5                                          │
│  • Mockito (mocking framework)                     │
│  • Spring Test (integration tests)                 │
│                                                     │
│  DOCUMENTATION:                                     │
│  • Springdoc OpenAPI (Swagger integration)         │
│  • Markdown (architecture documentation)           │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 15. CONFIGURATION EXAMPLE (application.yml)

```yaml
# Application Configuration
spring:
  application:
    name: car-listing-service
    version: 1.0.0
  
  # Database Configuration
  datasource:
    url: jdbc:mysql://localhost:3306/car_listing_db
    username: root
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
    show-sql: false
  
  # File Upload
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB
  
  # Security
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400000  # 24 hours
  
  # Drools Configuration
  drools:
    kie-base: kbase1
    kie-session: ksession1
    rules-path: classpath*:rules/

# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /api
  error:
    include-message: always
    include-binding-errors: always

# Actuator Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,prometheus,loggers
  endpoint:
    health:
      show-details: when-authorized

# Logging Configuration
logging:
  level:
    root: WARN
    com.listing: INFO
    com.listing.rule: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/car-listing-service.log
    max-size: 10MB
    max-history: 30

# File Storage
file:
  storage:
    path: /var/data/car-listing-images
    max-size: 5242880  # 5MB
    allowed-types: image/jpeg,image/png,image/webp
```

---

## 16. NEXT STEPS & RECOMMENDATIONS

```
PRIORITY 1 - IMMEDIATE IMPLEMENTATION:
  ✓ Integrate Drools Rule Engine
  ✓ Implement GlobalExceptionHandler
  ✓ Add JwtAuthFilter to SecurityFilterChain
  ✓ Update service layer for validation flow
  ✓ Add comprehensive logging

PRIORITY 2 - TESTING & VALIDATION:
  □ Write unit tests for Drools rules
  □ Write integration tests for validation flow
  □ Test exception handling scenarios
  □ Perform security testing (JWT, XSS, CSRF)
  □ Load testing

PRIORITY 3 - DEPLOYMENT & MONITORING:
  □ Set up Actuator monitoring
  □ Configure ELK/Splunk for log aggregation
  □ Set up alerts for errors/failures
  □ Create deployment documentation
  □ Performance tuning and optimization

PRIORITY 4 - ENHANCEMENTS:
  □ Implement caching (Redis) for search
  □ Add API rate limiting
  □ Implement image compression
  □ Add async processing for heavy operations
  □ Consider event-driven architecture (Kafka)
```

---

**Document Generated:** 2024-01-15
**Version:** 2.0 (Production-Ready)
**Status:** Ready for Implementation