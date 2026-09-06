# MarutiXchange Discovery Service - TDD Architecture & Diagrams

## 📋 Overview
This document provides Test-Driven Development (TDD) architecture diagrams and data flow visualizations for the MarutiXchange Discovery Service (Eureka Server).

---

## 1. SYSTEM ARCHITECTURE DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          MARUTIXCHANGE PLATFORM                             │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────────────┐
                    │   CLIENT APPLICATIONS       │
                    │  (Web, Mobile, Dashboard)   │
                    └─────────────┬───────────────┘
                                  │ HTTP/REST
                                  ▼
                    ┌─────────────────────────────┐
                    │      API GATEWAY (8090)     │
                    │  (Service: api-gateway)     │
                    └─────────────┬───────────────┘
                                  │ Service Discovery
                    ┌─────────────┴───────────────┐
                    │ Service Registry (DNS)      │
                    └─────────────┬───────────────┘
                                  ▼
            ╔═════════════════════════════════════════════╗
            ║   DISCOVERY SERVICE (Eureka Server 8761)    ║
            ║    [Service Registry & Discovery Manager]   ║
            ║                                             ║
            ║  • Service Registration                     ║
            ║  • Service Discovery                        ║
            ║  • Health Checks                            ║
            ║  • Heartbeat Monitoring                     ║
            ╚════════════┬════════════════════════════════╝
                         │
        ┌────────────────┼────────────────┬──────────────┬──────────────┐
        │                │                │              │              │
        ▼                ▼                ▼              ▼              ▼
    ┌────────────┐  ┌────────────┐  ┌────────────┐ ┌────────────┐ ┌────────────┐
    │ User       │  │ Car Listing│  │ Payment    │ │ Booking    │ │Notification│
    │ Service    │  │ Service    │  │ Service    │ │ Service    │ │ Service    │
    │ (8081)     │  │ (8068)     │  │ (8069)     │ │ (8084)     │ │ (8085)     │
    └────────────┘  └────────────┘  └────────────┘ └────────────┘ └────────────┘
         ✅ UP           ⬜ PENDING      ⬜ PENDING     ⬜ PENDING     ⬜ PENDING
```

---

## 2. DATA FLOW DIAGRAM (DFD)

### 2.1 Level 0 - System Context
```
┌────────────────────────────────────────────────────────────────────┐
│                                                                    │
│              ╔════════════════════════════════════════╗            │
│              ║   MARUTIXCHANGE DISCOVERY SERVICE     ║            │
│              ║        (Eureka Server 8761)           ║            │
│              ║                                        ║            │
│              ║  • Service Registration               ║            │
│              ║  • Service Discovery                  ║            │
│              ║  • Load Balancing Info                ║            │
│              ║  • Health Monitoring                  ║            │
│              ╚════════════════════════════════════════╝            │
│                  △                              ▽                  │
│                  │                              │                  │
│         Service  │                              │  Discovery       │
│         Instances│                              │  Requests        │
│                  │                              │                  │
│              ┌───┴──────┐              ┌────────┴───┐              │
│              │           │              │            │              │
│       ┌──────▼─────┐   ┌─┴──────────┐  │   ┌────────▼──────┐     │
│       │  Services  │   │ Monitoring │  │   │  API Gateway  │     │
│       │ [Registry] │   │  Dashboard │  │   │  [Router]     │     │
│       └────────────┘   └────────────┘  │   └───────────────┘     │
│                                        │                          │
│                                  ┌─────┴──────┐                  │
│                                  │   Clients  │                  │
│                                  └────────────┘                  │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

### 2.2 Level 1 - Service Registration & Discovery Flow

```
      ┌─ REGISTRATION FLOW ───────────────────────────────┐
      │                                                     │
      ▼                                                     │
  ┌─────────────────┐                                       │
  │  Microservice   │ (1) Register                          │
  │   Instance      │────────────────────────────────────►  │
  │  (Port 8081)    │  ServiceInstance {                    │
  │                 │    id, host, port,                   │
  │                 │    status, metadata                  │
  │                 │  }                                    │
  └─────────────────┘                                       │
                                          ┌───────────────────────────┐
                                          │  DISCOVERY SERVICE        │
                                          │  (Eureka Server 8761)     │
                                          │                           │
                                          │  ┌───────────────────┐   │
                                          │  │  Service Registry │   │
                                          │  │  ┌─────────────┐  │   │
      ┌─ DISCOVERY FLOW ──────────────────│◄─┤ user-service │◄─┤   │
      │                                   │  │ ┌─────────────┐  │   │
      ▼                                   │  │ │car-listing  │  │   │
  ┌─────────────────┐                     │  │ │  bidding    │  │   │
  │  API Gateway    │ (2) Discover        │  │ │  booking    │  │   │
  │  (Port 8090)    │◄────────────────────┤  │ │ notification│  │   │
  │                 │  [Services List]    │  │ └─────────────┘  │   │
  │                 │                     │  └───────────────────┘   │
  └─────────────────┘                     │                           │
                                          │  ┌───────────────────┐   │
      ┌─ HEARTBEAT FLOW ────────────────►│  │ Heartbeat Monitor │   │
      │                                   │  │ (5s interval)     │   │
      ▼                                   │  │                   │   │
  ┌─────────────────┐  (3) Heartbeat      │  │ ✅ Verify Status  │   │
  │  Microservice   │─────────────────────►  │ ⚠️  Mark Stale    │   │
  │   Instance      │  StatusUpdate {      │  │ ❌ Remove Down   │   │
  │  (Port 8081)    │    healthy: true     │  └───────────────────┘   │
  │                 │  }                    │                           │
  └─────────────────┘                       └───────────────────────────┘
       (repeats every 30s)
```

### 2.3 Data Entities Flow

```
                   ┌──────────────────────────────┐
                   │   INCOMING DATA STREAM       │
                   └──────────┬───────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
                    ▼                   ▼
            ┌──────────────────┐  ┌──────────────────┐
            │  Registration    │  │   Heartbeat      │
            │  Request (8761)  │  │  Signal (8761)   │
            └────────┬─────────┘  └────────┬─────────┘
                     │                     │
                     └──────────┬──────────┘
                                ▼
                    ┌──────────────────────────┐
                    │   VALIDATION & PARSING   │
                    │  • Schema Check          │
                    │  • Auth Verification     │
                    │  • Data Normalization    │
                    └────────────┬─────────────┘
                                 ▼
                    ┌──────────────────────────┐
                    │   PROCESSING ENGINE      │
                    │  • Register/Update       │
                    │  • Health Check          │
                    │  • Status Transition     │
                    └────────────┬─────────────┘
                                 ▼
                    ┌──────────────────────────┐
                    │   DATA STORAGE (Memory)  │
                    │  • Service Registry      │
                    │  • Instance Metadata     │
                    │  • Status History        │
                    └────────────┬─────────────┘
                                 ▼
                    ┌──────────────────────────┐
                    │   OUTGOING DATA STREAM   │
                    │  • Service Lists         │
                    │  • Heartbeat ACK         │
                    │  • Status Updates        │
                    │  • Metrics/Stats         │
                    └──────────────────────────┘
```

---

## 3. SEQUENCE DIAGRAM - SERVICE REGISTRATION & DISCOVERY

### 3.1 Complete Registration Flow Sequence

```
 Microservice      Discovery Service    Service Registry    API Gateway
 (User Service)    (Eureka Server)      (In Memory)         (Router)
      │                    │                   │                 │
      │ (1) Register       │                   │                 │
      ├────────POST────────►                   │                 │
      │  /eureka/apps      │                   │                 │
      │                    │ (2) Validate      │                 │
      │                    ├──────────►        │                 │
      │                    │ Credentials,      │                 │
      │                    │ Schema OK         │                 │
      │                    │◄────────Verified──┤                 │
      │                    │                   │                 │
      │                    │ (3) Store         │                 │
      │                    │ Instance          │                 │
      │                    ├──────────────────►                 │
      │                    │ user-service:     │                 │
      │                    │ 8081 UP           │                 │
      │                    │◄──────────────────┤                 │
      │                    │                   │                 │
      │◄──────204 Created──┤                   │                 │
      │ (Registration OK)  │                   │                 │
      │                    │                   │                 │
      ││════════════════════════════════════════════════════════╪════╪════════════
      ││         HEARTBEAT CYCLE (Every 30 seconds)            ║    ║
      ││════════════════════════════════════════════════════════╪════╪════════════
      │                    │                   │                 │
      │  (4) Heartbeat     │                   │                 │
      ├─────GET/PUT────────►                   │                 │
      │ /eureka/apps/      │                   │                 │
      │ USER-SERVICE/...   │ (5) Update       │                 │
      │                    │ Status           │                 │
      │                    ├──────────────────►                 │
      │                    │ user-service:    │                 │
      │                    │ 8081 RENEWING    │                 │
      │                    │◄──────────────────┤                 │
      │                    │                   │                 │
      │◄──────200 OK───────┤                   │                 │
      │ (Heartbeat OK)     │                   │                 │
      │                    │                   │                 │
      ││════════════════════════════════════════════════════════╪════╪════════════
      ││         DISCOVERY QUERY FLOW (From API Gateway)        ║    ║
      ││════════════════════════════════════════════════════════╪════╪════════════
      │                    │                   │                 │
      │                    │                   │                 │◄──GET /eureka/
      │                    │                   │             apps/    (6) Query
      │                    │ (7) Retrieve      │                 │
      │                    │ Registry          │                 │
      │                    ├──────────────────►                 │
      │                    │ Active Services  │                 │
      │                    │ List              │                 │
      │                    │◄──────────────────┤                 │
      │                    │                   │                 │
      │                    │                   │                 │──Config Cache──►
      │                    │                   │               (8) Use for      
      │                    │                   │                Routing Logic
      │                    │                   │                 │
      ▼                    ▼                   ▼                 ▼
```

### 3.2 Simplified Discovery Sequence

```
     Client          API Gateway       Discovery Service     Microservice
       │                  │                   │                   │
       │ GET /api/user    │                   │                   │
       ├─────────────────►│                   │                   │
       │                  │ Query: WHERE user-service?            │
       │                  ├──────────────────►│                   │
       │                  │                   │ Check Registry:   │
       │                  │                   │ user-service:    │
       │                  │◄──────────Respond──┤  8081 (UP)       │
       │                  │ [instance list]    │                   │
       │                  │                   │                   │
       │                  │ Select: 8081       │                   │
       │                  ├──────────────────────────── Forward───►
       │                  │                   │      Request       
       │                  │                   │                   │ Process
       │                  │                   │                   ├─────►
       │                  │                   │                   │
       │                  │◄──────────Response ───────────────────┤
       │◄─────Response────┤                   │                   │
       │                  │                   │                   │
       ▼                  ▼                   ▼                   ▼
```

---

## 4. COMPONENT INTERACTION DIAGRAM

```
                ┌────────────────────────────────────────┐
                │  DISCOVERY SERVICE COMPONENTS          │
                └────────────────────────────────────────┘

    ┌─────────────────────────────────────────────────────────────────┐
    │                                                                 │
    │   ╔══════════════════════════════════════════════════════════╗ │
    │   ║        SPRING BOOT APPLICATION (8761)                   ║ │
    │   ╚═══════════════┬══════════════════════════════════════════╝ │
    │                   │                                            │
    │      ┌────────────┼────────────┬─────────────┬────────────┐   │
    │      │            │            │             │            │   │
    │      ▼            ▼            ▼             ▼            ▼   │
    │  ┌────────┐  ┌────────────┐ ┌──────────┐ ┌─────────┐ ┌─────┐ │
    │  │Eureka  │  │Eureka      │ │Eureka    │ │Eureka   │ │Sec- │ │
    │  │Server  │  │Controller  │ │Instance  │ │Registry │ │urity│ │
    │  │Engine  │  │(REST API)  │ │Registry  │ │(Store)  │ │Cfg  │ │
    │  └────────┘  └────────────┘ └──────────┘ └─────────┘ └─────┘ │
    │      │            │            │             │            │   │
    │      └────────────┼────────────┴─────────────┴────────────┘   │
    │                   │                                            │
    │                   ▼                                            │
    │        ┌──────────────────────┐                               │
    │        │ HTTP Server (Jetty)  │                               │
    │        └──────────────────────┘                               │
    │                   │                                            │
    │      ┌────────────┼────────────┐                              │
    │      │            │            │                              │
    │      ▼            ▼            ▼                              │
    │  ┌─────────┐  ┌─────────┐  ┌─────────┐                       │
    │  │ REST    │  │ REST    │  │ REST    │                       │
    │  │Endpoints│  │Endpoints│  │Endpoints│                       │
    │  │Register │  │Discovery│  │Heartbeat│                       │
    │  └─────────┘  └─────────┘  └─────────┘                       │
    │                                                                 │
    └─────────────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
   ┌─────────┐       ┌─────────┐       ┌─────────┐
   │Port 8761│       │Eureka   │       │Spring   │
   │Listener │       │Dashboard│       │Actuator │
   └─────────┘       │(UI)     │       │Endpoints│
                     └─────────┘       └─────────┘
```

---

## 5. PROCESS FLOW - SERVICE HEALTH CHECK

```
                    START
                      │
                      ▼
      ┌──────────────────────────────┐
      │ Timer Triggered: Every 60s   │
      │ (Health Check Cycle)         │
      └──────────┬───────────────────┘
                 │
                 ▼
      ┌──────────────────────────────┐
      │ Iterate All Registered       │
      │ Service Instances            │
      └──────────┬───────────────────┘
                 │
                 ▼
      ┌──────────────────────────────┐
      │ Check Last Heartbeat Time    │
      │ Current - Last > Threshold?  │
      └──────────┬───────────────────┘
                 │
         ┌───────┴────────┐
         │ YES            │ NO
         ▼                ▼
    ┌─────────┐     ┌──────────┐
    │No       │     │ Instance │
    │Response │     │ is Alive │
    │(Stale)  │     └─────┬────┘
    └────┬────┘           │
         │                ▼
         │         ┌────────────────┐
         │         │ Mark Renewing  │
         │         │ Status:        │
         │         │ RENEWING       │
         │         └────────┬───────┘
         │                  │
         ▼                  ▼
    ┌─────────────────────────────┐
    │ Mark as STALE/EXPIRED       │
    │ Set Status: UP -> DOWN      │
    └────────┬────────────────────┘
             │
             ▼
    ┌─────────────────────────────┐
    │ Notify All Listeners        │
    │ (Update Subscribers)         │
    └────────┬────────────────────┘
             │
             ▼
    ┌─────────────────────────────┐
    │ Publish Event:              │
    │ InstanceStatusEvent         │
    └────────┬────────────────────┘
             │
             ▼
    ┌─────────────────────────────┐
    │ Other Services Update       │
    │ Their Cache                 │
    └────────┬────────────────────┘
             │
             ▼
    ┌─────────────────────────────┐
    │ Health Check Complete       │
    │ Cycle Ends                  │
    └────────┬────────────────────┘
             │
             ▼
           END
```

---

## 6. REST API ENDPOINTS - DATA FLOW

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    DISCOVERY SERVICE REST ENDPOINTS                     │
└─────────────────────────────────────────────────────────────────────────┘

1️⃣  SERVICE REGISTRATION
    ┌──────────────────────────────────────────────────────────────────┐
    │  POST /eureka/apps/{applicationName}                            │
    │  Content-Type: application/json                                 │
    │  Auth: Basic (admin/admin123)                                   │
    │                                                                  │
    │  Request Body:                                                  │
    │  {                                                              │
    │    "instance": {                                                │
    │      "instanceId": "user-service:8081",                        │
    │      "hostName": "localhost",                                   │
    │      "app": "USER-SERVICE",                                     │
    │      "ipAddr": "127.0.0.1",                                     │
    │      "port": 8081,                                              │
    │      "status": "UP",                                            │
    │      "metadata": {...}                                          │
    │    }                                                            │
    │  }                                                              │
    │                                                                  │
    │  Response: 204 No Content  ────────►  ✅ Registered             │
    └──────────────────────────────────────────────────────────────────┘

2️⃣  HEARTBEAT/RENEWAL
    ┌──────────────────────────────────────────────────────────────────┐
    │  PUT /eureka/apps/{appName}/{instanceId}                        │
    │  (Every 30 seconds from microservices)                          │
    │                                                                  │
    │  Response: 200 OK  ────────►  ✅ Heartbeat Renewed              │
    └──────────────────────────────────────────────────────────────────┘

3️⃣  SERVICE DISCOVERY
    ┌──────────────────────────────────────────────────────────────────┐
    │  GET /eureka/apps                                               │
    │  GET /eureka/apps/{applicationName}                             │
    │                                                                  │
    │  Response: 200 OK                                               │
    │  {                                                              │
    │    "applications": {                                            │
    │      "application": [                                           │
    │        {                                                        │
    │          "name": "USER-SERVICE",                                │
    │          "instances": [                                        │
    │            {                                                    │
    │              "instanceId": "user-service:8081",                │
    │              "hostName": "localhost",                           │
    │              "ipAddr": "127.0.0.1",                             │
    │              "port": 8081,                                      │
    │              "status": "UP",                                    │
    │              "homePageUrl":                                     │
    │                "http://localhost:8081/"                       │
    │            }                                                    │
    │          ]                                                      │
    │        }                                                        │
    │      ]                                                          │
    │    }                                                            │
    │  }  ────────►  ✅ Service List Returned                         │
    └──────────────────────────────────────────────────────────────────┘

4️⃣  STATUS CHECK (Discovery Controller)
    ┌──────────────────────────────────────────────────────────────────┐
    │  GET /discovery/status                                          │
    │  GET /discovery/health                                          │
    │  GET /discovery/info                                            │
    │  GET /discovery/stats                                           │
    │                                                                  │
    │  Response: 200 OK                                               │
    │  {                                                              │
    │    "service": "MarutiXchange Discovery Service",               │
    │    "status": "UP",                                              │
    │    "timestamp": "2026-03-29T12:25:04.523",                      │
    │    "version": "1.0.0"                                           │
    │  }  ────────►  ✅ Service Status Returned                       │
    └──────────────────────────────────────────────────────────────────┘
```

---

## 7. STATE MACHINE - SERVICE INSTANCE LIFECYCLE

```
                        ┌─────────────────┐
                        │   NOT_STARTED   │
                        └────────┬────────┘
                                 │ (Service boots & registers)
                                 ▼
                        ┌─────────────────┐
                        │   REGISTERING   │
                        │  (0-10 seconds) │
                        └────────┬────────┘
                                 │ (Register request accepted)
                                 ▼
                        ╔═════════════════╗
        ┌──────────────►║      UP ✅      ║◄──────────────┐
        │               ║  (Active)       ║               │
        │               ╚════════╤════════╝               │
        │                        │                        │
        │          (Heartbeat OK)│                        │
        │              (every 30s)                  (No missed heartbeats)
        │                        │                        │
        │                        ▼                        │
        │         ┌──────────────────────┐                │
        └─────────┤    RENEWING 🔄       │────────────────┘
                  │  (Last beat < 1 min) │
                  └──────────┬───────────┘
                             │
                    (Missed heartbeat)
                    (Last beat > 1 min)
                             │
                             ▼
                  ┌──────────────────────┐
                  │   STALE ⚠️            │
                  │ (Not responding)     │
                  └──────────┬───────────┘
                             │
                  (Hard eviction)
                  (exceeds timeout)
                             │
                             ▼
                  ╔═════════════════╗
                  ║   DOWN ❌        ║
                  ║ (Removed)        ║
                  ╚══════════════════╝
```

---

## 8. TDD TEST COVERAGE MAP

```
┌─────────────────────────────────────────────────────────────────────┐
│           TEST PYRAMID - DISCOVERY SERVICE TDD STRUCTURE           │
└─────────────────────────────────────────────────────────────────────┘

                              /\
                             /  \
                            /    \          🔴  E2E Tests (5%)
                           /  UI  \         • Full workflow
                          /________\        • Integration scenarios
                         /          \       • Contract tests
                        /    REST    \      
                       /  Endpoints  \      🟡  Integration (20%)
                      /              \      • REST endpoints
                     /________________\     • Eureka controller
                    /                  \    • Security
                   /    Service Core    \   
                  /    (Business Logic)  \   🟢  Unit (75%)
                 /                        \  • Registration logic
                /                          \ • Discovery queries
               /____________________________\ • Health checks
                                            • Data models
```

### Unit Tests (75%)
```
✅ DiscoveryServiceApplicationTest
   ├─ testApplicationContextLoads()
   ├─ testEurekaServerEnabled()
   └─ testSecurityConfiguration()

✅ DiscoveryControllerTest
   ├─ testGetStatus()
   ├─ testGetStatusResponse()
   ├─ testGetInfo()
   ├─ testGetInfoServices()
   ├─ testGetHealth()
   ├─ testGetStats()
   └─ testStatsStructure()

✅ SecurityConfigTest
   ├─ testBasicAuthConfiguration()
   ├─ testUnauthorizedAccess()
   └─ testAuthorizedAccess()
```

### Integration Tests (20%)
```
✅ EurekaRegistrationIntegrationTest
   ├─ testServiceRegistration()
   ├─ testServiceDiscovery()
   ├─ testHeartbeatRenewal()
   └─ testInstanceEviction()

✅ RestEndpointIntegrationTest
   ├─ testDiscoveryEndpointAccess()
   ├─ testUnauthorizedEndpointAccess()
   └─ testMetricsEndpointAccess()
```

### E2E Tests (5%)
```
✅ DiscoveryServiceE2ETest
   ├─ testCompleteServiceLifecycle()
   │  ├─ Register service
   │  ├─ Query registry
   │  ├─ Send heartbeat
   │  └─ Verify health
   │
   └─ testMultipleServicesScenario()
      ├─ Register multiple services
      ├─ Discover all services
      └─ Handle partial failures
```

---

## 9. DEPLOYMENT ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CONTAINERIZED DEPLOYMENT                        │
└─────────────────────────────────────────────────────────────────────┘

              Docker Registry
                   │
                   ▼
         ┌──────────────────┐
         │  Docker Image:   │
         │  discovery-srv   │
         │  v1.0.0          │
         └────────┬─────────┘
                  │
                  ▼
      ┌──────────────────────────┐
      │   Docker Container 🐳     │
      │  (Eureka Server Instance) │
      │                           │
      │  ┌──────────────────┐    │
      │  │ Java 21 Runtime  │    │
      │  │ Spring Boot 3.2.5│    │
      │  │ Eureka Server    │    │
      │  └──────────────────┘    │
      │                           │
      │ Exposed Ports:            │
      │ • 8761  (Eureka/HTTP)     │
      │ • 8761  (SSH optional)    │
      │                           │
      │ Environment:              │
      │ • EUREKA_HOST=localhost   │
      │ • EUREKA_USERNAME=admin   │
      │ • EUREKA_PASSWORD=***     │
      │ • SPRING_PROFILE=prod     │
      └────────┬─────────────────┘
               │
               ▼
      ┌──────────────────────────┐
      │  Kubernetes Service 🚀   │
      │  discovery-service       │
      │  Type: ClusterIP         │
      │  Port: 8761              │
      └────────┬─────────────────┘
               │
               ▼
      ┌──────────────────────────┐
      │ Load Balancer (Ingress)  │
      │ discovery.cluster.local  │
      └──────────────────────────┘
```

---

## 10. ERROR HANDLING & RESILIENCE FLOW

```
┌─────────────────────────────────────────────────────────────────────┐
│              ERROR HANDLING & RECOVERY MECHANISMS                  │
└─────────────────────────────────────────────────────────────────────┘

Registration Failure:
┌──────────────┐      Invalid        ┌─────────────┐
│  Service     │  ◄─── Request   ◄─  │ 400/401/403 │
│  Instance    │      Body           │ Bad Request │
└──────┬───────┘                      └─────────────┘
       │
       ├─ Retry Logic:
       │  • Exponential backoff
       │  • Max 3 retries
       │  • 2^n seconds delay
       │
       └─ Fallback:
          • Use local cache
          • Delayed registration

Heartbeat Failure:
┌──────────────┐      Network        ┌──────────────┐
│   Instance   │  ◄─── Error    ◄─   │  Connection  │
│  Heartbeat   │                      │   Timeout    │
└──────┬───────┘                      └──────────────┘
       │
       ├─ Self-preservation:
       │  • Don't evict during failures
       │  • Assume network partition
       │
       └─ Recovery:
          • Auto-retry on reconnect
          • Eventual consistency

Discovery Query Failure:
┌──────────────┐      Cache Miss      ┌──────────────┐
│  API Gateway │  ────────────────►   │  Local Cache │
│  Discovery   │◄─────────────────    │  (fallback)  │
│  Query       │                      │              │
└──────────────┘                      └──────────────┘
```

---

## 11. MONITORING & METRICS FLOW

```
┌─────────────────────────────────────────────────────────────────────┐
│              METRICS & MONITORING COLLECTION                       │
└─────────────────────────────────────────────────────────────────────┘

                   Discovery Service (8761)
                           │
                ┌──────────┼──────────┐
                │          │          │
                ▼          ▼          ▼
         ┌──────────┐ ┌──────────┐ ┌──────────┐
         │Eureka    │ │Metrics   │ │Health    │
         │Events    │ │Collection│ │Check     │
         └────┬─────┘ └────┬─────┘ └────┬─────┘
              │             │            │
              └─────────────┼────────────┘
                            ▼
                   ┌──────────────────┐
                   │ Spring Actuator  │
                   │ /actuator/metrics│
                   └────────┬─────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
         ┌─────────┐ ┌─────────┐ ┌─────────┐
         │Prometheus│ │Grafana  │ │ ELK     │
         │Scrape    │ │Dashboard│ │Logging  │
         └──────────┘ └──────────┘ └─────────┘

Key Metrics Exposed:
• eureka.instances.registered (count)
• eureka.instances.renewed (count)
• eureka.instances.evicted (count)
• eureka.instances.stale-evictions (count)
• eureka.http.requests.total (rate)
• jvm.memory.usage (gauge)
• http.server.requests (histogram)
```

---

## 12. SECURITY FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────┐
│                     SECURITY ARCHITECTURE                          │
└─────────────────────────────────────────────────────────────────────┘

                     Incoming Request (HTTP)
                              │
                              ▼
                    ┌──────────────────┐
                    │ SSL/TLS Layer    │
                    │ (Optional HTTPS) │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │ Authentication   │
                    │ (Basic Auth)     │
                    └────────┬─────────┘
                             │
                ┌────────────┴────────────┐
                │ YES                    │ NO
                ▼                        ▼
        ┌────────────────┐      ┌──────────────┐
        │ Credentials    │      │ Reject 401   │
        │ Valid?         │      │ Unauthorized │
        └────────┬───────┘      └──────────────┘
                 │
                 ▼
        ┌────────────────────┐
        │ Authorization      │
        │ Check (Role/Scope) │
        └────────┬───────────┘
                 │
        ┌────────┴────────┐
        │ YES             │ NO
        ▼                 ▼
   ┌─────────┐    ┌──────────────┐
   │ Allow   │    │ Reject 403   │
   │ Request │    │ Forbidden    │
   └────┬────┘    └──────────────┘
        │
        ▼
   ┌────────────┐
   │ Process    │
   │ Request    │
   └─────┬──────┘
         │
         ▼
   ┌────────────────────┐
   │ Return Response    │
   │ with appropriate   │
   │ data               │
   └────────────────────┘
```

---

## 13. CONFIGURATION & ENVIRONMENT FLOW

```
┌─────────────────────────────────────────────────────────────────────┐
│              CONFIGURATION MANAGEMENT FLOW                          │
└─────────────────────────────────────────────────────────────────────┘

Application Start:
    │
    └─► Load application.properties
        │
        ├─ spring.application.name = discovery-service
        ├─ server.port = 8761
        ├─ eureka.client.register-with-eureka = false
        ├─ eureka.client.fetch-registry = false
        ├─ spring.security.user.name = ${EUREKA_USERNAME:admin}
        ├─ spring.security.user.password = ${EUREKA_PASSWORD:admin123}
        └─ spring.profiles.active = ${SPRING_PROFILE:dev}
            │
            ▼
        Load Profile-Specific Config:
        ├─ application-dev.properties (DEV env)
        ├─ application-prod.properties (PROD env)
        └─ application-test.properties (TEST env)
            │
            ▼
        Override with Environment Variables:
        ├─ EUREKA_HOST = localhost
        ├─ EUREKA_USERNAME = admin
        ├─ EUREKA_PASSWORD = ***
        ├─ SPRING_PROFILE = prod
        ├─ SERVER_PORT = 8761
        └─ More...
            │
            ▼
        Final Configuration Object:
        ┌──────────────────────────┐
        │ EurekaServerConfig       │
        │ SecurityConfig           │
        │ ServerConfig             │
        │ ManagementConfig         │
        └──────────────────────────┘
```

---

## 14. QUICK REFERENCE - API ENDPOINTS

```
╔═════════════════════════════════════════════════════════════════╗
║           DISCOVERY SERVICE - API ENDPOINTS REFERENCE           ║
╠═════════════════════════════════════════════════════════════════╣
║                                                                 ║
║ 🔵 SERVICE REGISTRATION                                         ║
║   POST   /eureka/apps/{applicationName}                        ║
║   Auth: Basic (admin/admin123)                                 ║
║   Status: 204 No Content (Success)                             ║
║                                                                 ║
║ 🟢 SERVICE DISCOVERY                                            ║
║   GET    /eureka/apps                                          ║
║   GET    /eureka/apps/{applicationName}                        ║
║   Status: 200 OK (Returns service list in XML/JSON)           ║
║                                                                 ║
║ 🟡 HEARTBEAT / RENEWAL                                          ║
║   PUT    /eureka/apps/{appName}/{instanceId}                   ║
║   Auth: Basic                                                  ║
║   Status: 200 OK (Heartbeat renewed)                           ║
║           404 Not Found (Instance needs re-registration)       ║
║                                                                 ║
║ 🔵 CUSTOM DISCOVERY ENDPOINTS                                   ║
║   GET    /discovery/status      → Service Status              ║
║   GET    /discovery/health      → Health Info                 ║
║   GET    /discovery/info        → Platform Info               ║
║   GET    /discovery/stats       → Usage Stats                 ║
║   Status: 200 OK (Returns JSON)                               ║
║                                                                 ║
║ ⚙️  ACTUATOR / MONITORING                                        ║
║   GET    /actuator/health       → System Health               ║
║   GET    /actuator/metrics      → Metrics List                ║
║   GET    /actuator/info         → App Info                    ║
║   Status: 200 OK                                              ║
║                                                                 ║
║ 🌐 EUREKA DASHBOARD (UI)                                        ║
║   GET    http://localhost:8761  → Web Console                 ║
║   Auth: admin / admin123                                       ║
║                                                                 ║
╚═════════════════════════════════════════════════════════════════╝
```

---

## 15. SUMMARY TABLE

| Aspect | Details |
|--------|---------|
| **Service Type** | Service Discovery & Registry (Eureka Server) |
| **Port** | 8761 |
| **Protocol** | HTTP/REST |
| **Framework** | Spring Boot 3.2.5, Spring Cloud 2023.0.1 |
| **Java Version** | 21 (LTS) |
| **Authentication** | Basic Auth (admin/admin123) |
| **Heartbeat Interval** | 30 seconds |
| **Renewal Timeout** | 90 seconds (~3 heartbeats) |
| **Eviction Timeout** | 60 seconds (configurable) |
| **Data Storage** | In-Memory (ConcurrentHashMap) |
| **Clustering** | Single-server mode |
| **Dashboard** | Built-in Eureka Dashboard at port 8761 |
| **Monitoring** | Spring Boot Actuator endpoints |
| **Logging** | SLF4J with Logback |
| **Build Tool** | Maven 3.8.1 |
| **Test Coverage** | JUnit 5, Mockito, Spring Test |

---

Generated: March 29, 2026  
Service: MarutiXchange Discovery Service v1.0.0  
Documentation Type: TDD Architecture Diagrams
