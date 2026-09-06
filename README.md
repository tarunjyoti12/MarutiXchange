# MarutiXchange
### Microservices-Based Full-Stack C2C Car Resale Platform

MarutiXchange is a Customer-to-Customer (C2C) marketplace built specifically for the resale of Maruti Suzuki vehicles. It was developed as part of a 4–6 month industrial training program at **Maruti Suzuki India Limited**, aimed at bringing transparency, efficiency, and direct buyer-seller interaction to the used car market — without relying on dealer intermediaries.

---

## 📌 Problem Statement

The used car resale market in India is fragmented:
- **Manufacturer-backed platforms** (dealer-driven) don't support direct C2C interaction.
- **General classifieds** (OLX, Quikr) lack brand focus and structured processes.
- **Hybrid platforms** (Cars24, Spinny) act as intermediaries, reducing user control.

There was no platform combining **direct C2C interaction**, a **brand-specific focus**, **real-time bidding**, and **rule-based validation** — MarutiXchange was built to close that gap.

---

## ✨ Key Features

- 🔐 **Secure Authentication** — OTP-based login with JWT-secured sessions
- 🚗 **Car Listing** — Sellers can list vehicles with detailed specs, pricing, and images
- 🔍 **Advanced Search & Filter** — Filter listings by price, model, year, fuel type, and more
- 📈 **Real-Time Bidding** — Live bidding system for competitive, transparent price discovery
- ⚖️ **Rule Engine** — Dynamic validation of bidding, pricing, and booking rules without touching core code
- 📅 **Booking & Payments** — Secure booking workflow with integrated payment processing
- 📊 **User Dashboard** — Track listings, bids, inquiries, and test drives in one place
- 💬 **Chat Support** — In-app assistance for buyers and sellers

---

## 🏗️ System Architecture

The platform follows a **microservices architecture**, where independent, loosely-coupled services communicate via RESTful APIs. This allows each service to be developed, deployed, and scaled independently.

```
Client Layer  →  API Layer  →  Microservices Layer  →  Data Layer
                                        ↑
                                  Rule Engine
```

**Microservices:**

| Service | Responsibility |
|---|---|
| `user-service` | Registration, authentication, profile management |
| `car-listing-service` | Car listing creation, retrieval, updates |
| `bidding-service` | Bid placement, validation, and history |
| `order-service` | Booking and order lifecycle management |
| `payment-service` | Transaction processing |
| `notification-service` | Alerts and notifications |
| `test-drive-service` | Test drive scheduling |
| `rules-engine-service` | Dynamic business rule validation (pricing, bidding, booking) |
| `discovery-service` | Service registration and discovery |

Each microservice maintains its **own MySQL database**, ensuring data isolation and independent scalability.

---

## 🛠️ Tech Stack

**Backend**
- Java, Spring Boot, Spring Data JPA
- MySQL
- JWT Authentication

**Frontend**
- React

**DevOps & Tooling**
- Docker
- Jenkins (CI/CD)
- SonarQube (code quality)
- Git & GitHub
- Postman (API testing)
- JIRA (project management)

---

## 📁 Project Structure

```
MarutiXchange/
├── MarutiXchange-Backend/     # Spring Boot microservices
├── MarutiXchange-Frontend/    # React application
├── prototype/                 # Early HTML/CSS UI prototype
├── docs/                      # Project report & presentations
└── README.md
```

---

## 🎨 Prototype

Before building the full React + Spring Boot application, a high-fidelity HTML/CSS prototype was created to validate the UI/UX flow — covering login, car listing, search & filter, bidding, and booking screens. This helped identify design issues early and shape the final interface. See the [`/prototype`](./prototype) folder.

---

## 🚀 Getting Started

### Prerequisites
- Java 17+ and Maven
- Node.js and npm
- MySQL

### Backend Setup
```bash
cd MarutiXchange-Backend
# Configure your MySQL connection in each service's application.properties
mvn spring-boot:run
```

### Frontend Setup
```bash
cd MarutiXchange-Frontend/Frontend
npm install
npm run dev
```

> ⚠️ Make sure to set up your own `.env` file for the frontend (see `.env.example`) with your API keys — never commit real credentials.

---

## 📸 Screenshots

<img width="1602" height="982" alt="LoginScreen" src="https://github.com/user-attachments/assets/d1a83d5d-87a9-4b55-9e7a-f58fd50b2311" />
<img width="1254" height="1254" alt="ListingCar" src="https://github.com/user-attachments/assets/88cdd6d4-85bf-4d58-9de0-692ef11d9c20" />
<img width="1309" height="1201" alt="SellingScreen" src="https://github.com/user-attachments/assets/d8eaf505-aa85-453b-a3a7-1ec63f68bc06" />
<img width="1821" height="864" alt="BiddingScreen" src="https://github.com/user-attachments/assets/af65216c-7553-4bde-9cb8-393c76c3220f" />

---

## 🌍 Applications

- Digital car buying/selling marketplace for used Maruti vehicles
- Transparent, live bidding-based price discovery
- Scalable model adaptable to other automobile brands or dealership networks

---

## 📄 Documentation

- [Full Project Report (PDF)](./docs/FinalYear_Project_Report(12205023)TarunJyoti.pdf)
- [Project Presentation (PPTX)](./docs/marutiXchange_presentation.pptx)
- [Microservices Architecture Presentation (Google Drive)](PASTE-YOUR-GOOGLE-DRIVE-LINK-HERE)

  ---
## 🙏 Acknowledgment

This project was developed as part of a B.Tech industrial training program (Electronics & Computer Engineering, Punjabi University, Patiala) at **Maruti Suzuki India Limited**, under the mentorship of Mr. Ramachandran Kumar.

---

## 👤 Author

**Tarun Jyoti**
