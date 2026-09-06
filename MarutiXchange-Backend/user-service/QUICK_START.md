# TDD Diagrams - Quick Start Guide

## 📦 What's Included

This package contains comprehensive Test-Driven Development (TDD) diagrams and documentation for the User Service microservice.

### Files Created

1. **TDD_DIAGRAM.md** (Main Documentation)
   - 12 comprehensive sections covering all aspects
   - ASCII diagrams for easy viewing
   - Data flow diagrams for all major operations
   - Sequence diagrams for component interactions
   - Test coverage mapping
   - Security and deployment information

2. **diagrams/architecture.puml**
   - System architecture diagram
   - Component relationships
   - External service integrations

3. **diagrams/sequences.puml**
   - User Registration Sequence
   - User Login Sequence
   - Failed Login with Account Lock
   - OTP Verification Sequence
   - Password Reset Sequence
   - Document Upload Sequence
   - Account Lock Sequence

4. **diagrams/dataflow.puml**
   - User Registration Data Flow
   - User Login Data Flow
   - OTP Verification Data Flow
   - Password Reset Data Flow
   - Document Upload Data Flow

5. **diagrams/test_coverage.puml**
   - Test Coverage Overview
   - Test Cases Coverage Matrix
   - Test Execution Flow (TDD Cycle)

6. **diagrams/README.md**
   - Guide for using diagrams
   - PlantUML usage instructions
   - References and tools

7. **diagrams/index.html**
   - Interactive HTML documentation
   - Visual overview of all components
   - Easy navigation and search

## 🚀 Quick Start

### View Documentation

1. **Read Main Documentation**
   ```
   Open: TDD_DIAGRAM.md
   ```

2. **View Interactive HTML**
   ```
   Open: diagrams/index.html in web browser
   ```

3. **Read Diagram Guide**
   ```
   Open: diagrams/README.md
   ```

### Generate Diagrams from PlantUML

#### Option 1: Online PlantUML Editor
1. Go to https://www.plantuml.com/plantuml/uml/
2. Copy content from any `.puml` file
3. Paste into editor
4. View and export as PNG/SVG

#### Option 2: Command Line (if PlantUML installed)
```bash
# Generate PNG from PlantUML
plantuml diagrams/architecture.puml -o diagrams/output -tpng
plantuml diagrams/sequences.puml -o diagrams/output -tpng
plantuml diagrams/dataflow.puml -o diagrams/output -tpng
plantuml diagrams/test_coverage.puml -o diagrams/output -tpng
```

#### Option 3: VS Code Extension
1. Install "PlantUML" extension
2. Open any `.puml` file
3. Right-click → "Preview Current Diagram"

## 📊 Diagram Overview

### Architecture Diagram
Shows the complete system architecture with:
- Client layer
- API Gateway
- User Service components
- External services (Database, Email, Storage)

### Sequence Diagrams
Detailed step-by-step interactions for:
- User registration with email notification
- User login with account lock mechanism
- OTP generation and verification
- Password reset flow
- Document upload process

### Data Flow Diagrams
Shows data movement through:
- Registration flow
- Login flow
- OTP verification
- Password reset
- Document upload

### Test Coverage Diagrams
Illustrates:
- Unit test coverage
- Integration test coverage
- Test scenarios (success, error, security)
- TDD workflow (RED → GREEN → REFACTOR)

## 🔑 Key Features Documented

### Authentication & Security
- ✅ JWT-based authentication
- ✅ BCrypt password encoding
- ✅ Account locking (5 attempts, 30 min)
- ✅ OTP verification (6-digit, 10 min expiry)
- ✅ Password reset tokens (30 min expiry)

### Core Features
- ✅ User registration
- ✅ User login
- ✅ User profile management
- ✅ OTP verification
- ✅ Password reset
- ✅ Document upload

### Testing
- ✅ Unit tests (Service layer)
- ✅ Integration tests (Controller layer)
- ✅ Error handling tests
- ✅ Security tests
- ✅ TDD workflow

## 📈 Test Coverage

### Service Layer Tests
- Register user (success & error cases)
- Login user (success, error, account lock)
- Get user by ID
- Update user
- Delete user (soft delete)
- Send OTP
- Verify OTP
- Forgot password
- Reset password
- Get all users

### Controller Layer Tests
- All HTTP endpoints
- Request validation
- Response status codes
- Error handling

## 🔐 Security Features

| Feature | Implementation |
|---------|-----------------|
| Password Encoding | BCrypt with salt |
| JWT Authentication | Bearer token, 24h expiry |
| Account Locking | 5 attempts, 30 min lock |
| OTP Verification | 6-digit, 10 min expiry |
| Token Expiry | Reset tokens, 30 min |
| Input Validation | @Valid annotation |

## 🗄️ Database Schema

### User Table
- id, name, email, password, phone_number, role
- active, is_verified, profile_picture_url
- failed_attempts, account_locked, lock_time
- otp, otp_expiry, reset_token, reset_token_expiry
- created_at, updated_at

### UserDocument Table
- id, user_id, document_type, file_name, file_url
- file_size, uploaded_at, created_at, updated_at

## 🔌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/users/register | Register user |
| POST | /api/v1/users/login | Login user |
| GET | /api/v1/users/{id} | Get user |
| PUT | /api/v1/users/{id} | Update user |
| DELETE | /api/v1/users/{id} | Delete user |
| POST | /api/v1/users/send-otp | Send OTP |
| POST | /api/v1/users/verify-otp | Verify OTP |
| POST | /api/v1/users/forgot-password | Forgot password |
| POST | /api/v1/users/reset-password | Reset password |
| GET | /api/v1/users | Get all users |
| POST | /api/v1/users/{id}/upload-document | Upload document |
| GET | /api/v1/users/{id}/documents | Get documents |

## 📚 How to Use These Diagrams

### For Development
1. Reference data flow diagrams when implementing features
2. Use sequence diagrams to understand component interactions
3. Follow TDD workflow for test-driven development

### For Testing
1. Use test coverage diagrams to identify gaps
2. Reference test cases matrix for comprehensive coverage
3. Follow TDD cycle for each new feature

### For Documentation
1. Include diagrams in API documentation
2. Share with team members for onboarding
3. Use for architecture reviews

### For Figma Integration
1. Export PlantUML diagrams as PNG/SVG
2. Import into Figma for customization
3. Create interactive prototypes
4. Share with stakeholders

## 🛠️ Tools & Technologies

- **PlantUML**: UML diagram generation
- **Markdown**: Documentation format
- **HTML**: Interactive documentation
- **Spring Boot**: Java framework
- **PostgreSQL**: Database
- **JWT**: Authentication
- **BCrypt**: Password encoding

## 📞 Support

For questions about these diagrams:
1. Check TDD_DIAGRAM.md for detailed information
2. Review diagrams/README.md for diagram-specific help
3. Open diagrams/index.html for interactive overview
4. Contact the development team

## 🎯 Next Steps

1. **Review Documentation**
   - Start with TDD_DIAGRAM.md
   - Open index.html for visual overview

2. **Generate Diagrams**
   - Use PlantUML to generate PNG/SVG
   - Import into Figma for further customization

3. **Share with Team**
   - Distribute HTML file for easy access
   - Include diagrams in documentation
   - Use for onboarding new team members

4. **Maintain Documentation**
   - Update diagrams when architecture changes
   - Keep test coverage current
   - Document new features

## 📝 Version History

- **v1.0** (2024): Initial TDD diagrams and documentation

---

**Created**: 2024
**Status**: Complete
**Last Updated**: 2024
