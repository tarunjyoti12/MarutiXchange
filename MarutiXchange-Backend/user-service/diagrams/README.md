# TDD Diagrams - User Service

This directory contains comprehensive Test-Driven Development (TDD) diagrams for the User Service microservice, including data flow diagrams, sequence diagrams, and test coverage diagrams.

## 📋 Files Overview

### 1. **TDD_DIAGRAM.md** (Main Documentation)
Comprehensive markdown document containing:
- System Architecture Overview
- Data Flow Diagrams (DFD) for all major flows:
  - User Registration Flow
  - User Login Flow with Account Lock
  - OTP Verification Flow
  - Password Reset Flow
  - Document Upload Flow
- Sequence Diagrams for all interactions
- Test Coverage Mapping
- Error Handling & Exception Flow
- Security Flow (JWT Authentication)
- Database Schema
- API Endpoints Summary
- TDD Workflow
- Deployment Architecture
- Key Metrics & Monitoring

### 2. **diagrams/architecture.puml**
PlantUML diagram showing:
- System architecture components
- Component relationships
- Data flow between layers
- External service integrations

**Usage:**
```bash
# Generate PNG from PlantUML
plantuml diagrams/architecture.puml -o diagrams/output -tpng

# Or use online PlantUML editor
# https://www.plantuml.com/plantuml/uml/
```

### 3. **diagrams/sequences.puml**
PlantUML sequence diagrams for:
- User Registration Sequence
- User Login Sequence
- Failed Login with Account Lock Sequence
- OTP Verification Sequence
- Password Reset Sequence
- Document Upload Sequence
- Account Lock on Failed Attempts

**Usage:**
```bash
# Generate PNG from PlantUML
plantuml diagrams/sequences.puml -o diagrams/output -tpng
```

### 4. **diagrams/dataflow.puml**
PlantUML data flow diagrams for:
- User Registration Data Flow
- User Login Data Flow
- OTP Verification Data Flow
- Password Reset Data Flow
- Document Upload Data Flow

**Usage:**
```bash
# Generate PNG from PlantUML
plantuml diagrams/dataflow.puml -o diagrams/output -tpng
```

### 5. **diagrams/test_coverage.puml**
PlantUML diagrams for:
- Test Coverage Overview
- Test Cases Coverage Matrix
- Test Execution Flow (TDD Cycle)

**Usage:**
```bash
# Generate PNG from PlantUML
plantuml diagrams/test_coverage.puml -o diagrams/output -tpng
```

## 🔄 Data Flow Summary

### User Registration
```
Client → Controller → Service → Repository → Database
                   ↓
              EmailService → SMTP Server
```

### User Login
```
Client → Controller → Service → Repository → Database
                   ↓
              JwtUtil → JWT Token
                   ↓
              PasswordEncoder → Encoded Password
```

### OTP Verification
```
Client → Controller → Service → Repository → Database
                   ↓
              EmailService → SMTP Server
```

### Password Reset
```
Client → Controller → Service → Repository → Database
                   ↓
              EmailService → SMTP Server
```

### Document Upload
```
Client → Controller → Service → Repository → Database
                   ↓
              FileStorage → S3/Local Storage
```

## 🔐 Security Features

1. **Password Encoding**: BCrypt algorithm
2. **JWT Authentication**: Bearer token with 24-hour expiry
3. **Account Locking**: After 5 failed login attempts for 30 minutes
4. **OTP Verification**: 6-digit OTP with 10-minute expiry
5. **Password Reset**: UUID token with 30-minute expiry
6. **Request Validation**: @Valid annotation on all DTOs

## 📊 Test Coverage

### Unit Tests (Service Layer)
- Register user
- Login user
- Get user by ID
- Update user
- Delete user
- Send OTP
- Verify OTP
- Forgot password
- Reset password
- Get all users

### Integration Tests (Controller Layer)
- All HTTP endpoints
- Error handling
- Response status codes
- Request validation

### Test Scenarios
- Success scenarios (happy path)
- Error scenarios (exception handling)
- Security scenarios (authentication, authorization)
- Edge cases

## 🗄️ Database Schema

### User Table
- id (PK)
- name
- email (UNIQUE)
- password (encoded)
- phone_number
- role (ENUM: ADMIN, USER)
- active (BOOLEAN)
- is_verified (BOOLEAN)
- profile_picture_url
- failed_attempts
- account_locked
- lock_time
- otp
- otp_expiry
- reset_token
- reset_token_expiry
- created_at
- updated_at

### UserDocument Table
- id (PK)
- user_id (FK)
- document_type (ENUM)
- file_name
- file_url
- file_size
- uploaded_at
- created_at
- updated_at

## 🔌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/users/register | Register new user |
| POST | /api/v1/users/login | Login user |
| GET | /api/v1/users/{id} | Get user by ID |
| PUT | /api/v1/users/{id} | Update user |
| DELETE | /api/v1/users/{id} | Delete user (soft delete) |
| POST | /api/v1/users/send-otp | Send OTP to email |
| POST | /api/v1/users/verify-otp | Verify OTP |
| POST | /api/v1/users/forgot-password | Request password reset |
| POST | /api/v1/users/reset-password | Reset password with token |
| GET | /api/v1/users | Get all users (paginated) |
| POST | /api/v1/users/{id}/upload-document | Upload document |
| GET | /api/v1/users/{id}/documents | Get user documents |

## 🚀 TDD Workflow

1. **RED**: Write failing test
2. **GREEN**: Write minimal implementation
3. **REFACTOR**: Improve code quality
4. **REPEAT**: Continue with next test

## 📈 Key Metrics

| Metric | Target | Alert |
|--------|--------|-------|
| Response Time (p95) | < 200ms | > 500ms |
| Error Rate | < 0.1% | > 1% |
| CPU Usage | < 70% | > 85% |
| Memory Usage | < 80% | > 90% |
| Test Coverage | > 80% | < 70% |
| Build Success Rate | > 95% | < 90% |

## 🔄 Exception Handling

All exceptions are handled by `GlobalExceptionHandler` and return standardized `ApiResponse`:

- **EmailAlreadyExistsException** → 409 CONFLICT
- **InvalidCredentialsException** → 401 UNAUTHORIZED
- **AccountLockedException** → 403 FORBIDDEN
- **UserNotFoundException** → 404 NOT FOUND
- **InvalidOtpException** → 400 BAD REQUEST
- **InvalidTokenException** → 400 BAD REQUEST
- **MethodArgumentNotValidException** → 400 BAD REQUEST
- **Generic Exception** → 500 INTERNAL SERVER ERROR

## 📝 How to Use These Diagrams

### For Development
1. Reference the data flow diagrams when implementing new features
2. Use sequence diagrams to understand component interactions
3. Follow the TDD workflow for test-driven development

### For Testing
1. Use test coverage diagrams to identify test gaps
2. Reference test cases matrix for comprehensive coverage
3. Follow the TDD cycle for each new feature

### For Documentation
1. Include diagrams in API documentation
2. Share with team members for onboarding
3. Use for architecture reviews

### For Figma Integration
1. Export PlantUML diagrams as PNG/SVG
2. Import into Figma for further customization
3. Create interactive prototypes
4. Share with stakeholders

## 🛠️ Tools Used

- **PlantUML**: For creating UML diagrams
- **Markdown**: For documentation
- **ASCII Art**: For text-based diagrams

## 📚 References

- [PlantUML Documentation](https://plantuml.com/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JWT Documentation](https://jwt.io/)
- [Test-Driven Development](https://en.wikipedia.org/wiki/Test-driven_development)

## 👥 Team

- **Architecture**: Defined system components and interactions
- **Development**: Implements features following TDD
- **QA**: Tests based on test coverage diagrams
- **DevOps**: Deploys using deployment architecture

## 📞 Support

For questions or clarifications about these diagrams, please contact the development team.

---

**Last Updated**: 2024
**Version**: 1.0
