# User Service Architecture - Enhanced Security & Testing Guide

## 📊 DIAGRAM FILES REFERENCE

This document contains comprehensive architecture diagrams in multiple formats:

### PlantUML Diagrams (in /diagrams folder)
- **sequences.puml** - Sequence diagrams for key workflows:
  - User Registration (Register Request → Database → Email)
  - User Login (Login Request → JWT Token Generation)
  - OTP Verification Flow
  - Document Upload Process
  - Password Reset Flow
  
- **dataflow.puml** - Data flow diagrams for processes:
  - User Registration data flow
  - Login flow with database interactions
  - OTP generation and verification
  - Document upload workflow
  
- **architecture.puml** - System architecture diagram:
  - Microservices deployment
  - Service discovery with Eureka
  - Database integration
  
- **test_coverage.puml** - Test pyramid and coverage visualization

To view these diagrams:
1. Open in any PlantUML viewer (IDE plugin, online editor at plantuml.com)
2. Or see the HTML renders at: diagrams/index.html

### Key Changes in v1.0 (Updated 2026-04-15)
- ✅ API endpoints updated to `/api/v1/users` prefix
- ✅ JWT token generation flow clarified (stateless, no DB storage)
- ✅ Database schema corrected (removed jwt_tokens table)
- ✅ Added actual request/response examples
- ✅ Added links to PlantUML sequence & data flow diagrams
- ✅ Updated endpoint list with actual implementation

---

## 1. SYSTEM ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT APPLICATION                          │
│                   (Web, Mobile, Desktop)                             │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTP/HTTPS Requests
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      API GATEWAY / LOAD BALANCER                    │
│  - Rate Limiting  - Request/Response Logging  - Routing             │
└─────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    USER SERVICE (Spring Boot)                       │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              Spring Security Layer                           │   │
│  │  ┌──────────────────────────────────────────────────────┐    │   │
│  │  │  JwtAuthFilter (Intercepts every request)           │    │   │
│  │  │  1. Extract JWT token from Authorization header     │    │   │
│  │  │  2. Validate token signature & expiry via JwtUtil   │    │   │
│  │  │  3. Create Authentication context                  │    │   │
│  │  │  4. Set SecurityContextHolder                       │    │   │
│  │  │  5. Pass to DispatcherServlet                       │    │   │
│  │  └──────────────────────────────────────────────────────┘    │   │
│  │              (CSRF Disabled for REST APIs)                   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                             │                                       │
│                             ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    UserController                            │   │
│  │  (All endpoints protected by @PreAuthorize)                  │   │
│  │  - register() - login()   - getUserById()                    │   │
│  │  - updateUser() - deleteUser()  - sendOtp()                  │   │
│  │  - verifyOtp() - uploadDocument() - getUserDocuments()       │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                             │                                       │
│                             ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                  UserServiceImpl (Business Logic)             │   │
│  │  - Authentication logic  - Authorization checks              │   │
│  │  - User management       - Document handling                 │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                             │                                       │
│        ┌────────────────────┼────────────────────┐                  │
│        │                    │                    │                  │
│        ▼                    ▼                    ▼                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐           │
│  │UserRepository│  │EmailService  │  │DocumentService   │           │
│  └──────────────┘  └──────────────┘  └──────────────────┘           │
│        │                    │                    │                  │
│        ▼                    ▼                    ▼                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐           │
│  │  JwtUtil     │  │PasswordEnc.  │  │DocumentRepository│           │
│  │  (Validation)│  │(BCrypt/Argon)│  │    (File I/O)    │           │
│  └──────────────┘  └──────────────┘  └──────────────────┘           │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              Swagger UI (API Testing Interface)              │   │
│  │  - Interactive API documentation                             │   │
│  │  - Bearer Token Authentication support                       │   │
│  │  - Try-it-out functionality                                  │   │
│  │  - Endpoint: GET /swagger-ui.html                            │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                             │
                    ┌────────┼────────┐
                    │        │        │
                    ▼        ▼        ▼
        ┌──────────────┐ ┌────────────────┐ ┌──────────────┐
        │   MySQL DB   │ │ Eureka Server  │ │ Email Server │
        │  (User Data) │ │ (Service Reg.) │ │   (SMTP)     │
        └──────────────┘ └────────────────┘ └──────────────┘
```

---

---

## 2.5 SEQUENCE DIAGRAMS (PlantUML - See /diagrams/sequences.puml)

The following sequence diagrams illustrate detailed interaction flows:

### 2.5.1 User Registration Sequence
```
Actor: Client / API Gateway / User Service / Database / Email Service

Flow:
1. Client sends POST /api/v1/users/register
   - name, email, password, phone, role
   
2. UserController.register() validates input
   - @Valid triggers validation
   
3. UserServiceImpl.register() processes:
   - Check if email already exists (UserRepository.existsByEmail)
   - If exists → throw EmailAlreadyExistsException (409)
   
4. PasswordEncoder encodes password
   - Uses BCrypt algorithm
   
5. UserRepository saves new User to database
   - INSERT INTO users (...)
   
6. EmailService sends welcome email
   - SMTP connection to Gmail
   
7. Return 201 CREATED with UserResponse
   - id, name, email, phone, role
   
See: /diagrams/sequences.puml (UserRegistration_Sequence)
```

### 2.5.2 User Login Sequence
```
Actor: Client / API Gateway / User Service / Database / JwtUtil

Flow:
1. Client sends POST /api/v1/users/login
   - email, password
   
2. UserController.login() validates input
   
3. UserServiceImpl.login() processes:
   - Find user by email (UserRepository.findByEmail)
   - If not found → throw InvalidCredentialsException (401)
   
4. PasswordEncoder.matches() verifies password
   - Compare provided password with stored hash
   - If mismatch → increment failedAttempts
   - If failedAttempts > 5 → lock account
   
5. Check account status:
   - is_active must be true
   - is_verified must be true
   - account_locked must be false
   
6. JwtUtil.generateToken(userId, email)
   - Create JWT with claims
   - Sign with HMAC-SHA256
   - Set 24-hour expiration
   
7. Return 200 OK with LoginResponse
   - token, expiresIn, tokenType, user object
   
See: /diagrams/sequences.puml (UserLogin_Sequence)
```

### 2.5.3 OTP Verification Sequence
```
Actor: Client / API Gateway / User Service / Database / Email Service

Flow:
1. Client sends POST /api/v1/users/send-otp
   - email
   
2. UserServiceImpl.sendOtp() processes:
   - Find user by email
   - Generate 6-digit OTP
   - Save OTP_tokens with 5-minute expiration
   - Send OTP via email
   
3. Client receives OTP in email
   
4. Client sends POST /api/v1/users/verify-otp
   - email, otp
   
5. UserServiceImpl.verifyOtp() processes:
   - Fetch OTP_tokens for user
   - Verify OTP matches
   - Check if OTP is expired
   - Check if OTP already used
   
6. Update user:
   - is_verified = true
   - Mark OTP as used
   
7. Return 200 OK
   - "OTP verified successfully"
   
See: /diagrams/sequences.puml (OTP_Verification_Sequence)
```

### 2.5.4 Protected API Request Sequence
```
Actor: Client / API Gateway / JwtAuthFilter / SecurityContext / UserController

Flow:
1. Client sends GET /api/v1/users/{id}
   - Header: "Authorization: Bearer eyJhbGc..."
   
2. Spring DispatcherServlet → Filter Chain
   
3. JwtAuthFilter intercepts request:
   - Extract token from Authorization header
   - Remove "Bearer " prefix
   
4. JwtUtil.validateToken(token):
   - Parse JWT using secret key
   - Verify HS256 signature
   - Check expiration date
   - Extract userId and email claims
   
5. Create Authentication object:
   - UsernamePasswordAuthenticationToken
   - Set principal (userId)
   - Set authorities/roles
   
6. SecurityContextHolder.setAuthentication()
   - Store in ThreadLocal context
   
7. Request continues to UserController
   - @PreAuthorize checks role/permissions
   - Controller logic executes
   
8. Return response (200 OK, 403 Forbidden, etc.)
   - SecurityContext is cleared after response
   
See: /diagrams/sequences.puml (JWT_Protected_Request_Sequence)
```

### 2.5.5 Document Upload Sequence
```
Actor: Client / API Gateway / UserController / DocumentService / FileSystem / Database

Flow:
1. Client sends POST /api/v1/users/{id}/upload-document
   - Authorization: Bearer <JWT>
   - Form-data: file, documentType
   
2. JwtAuthFilter validates JWT token
   - Token must be valid and not expired
   
3. UserController.uploadDocument() validates:
   - File cannot be empty
   - documentType is valid enum
   
4. DocumentService.uploadDocument() processes:
   - Generate unique file name (UUID)
   - Create uploads/documents directory if not exists
   - Save file to disk
   - Save metadata to database (user_documents table)
   
5. Return 200 OK with DocumentResponse
   - id, fileName, fileSize, documentType, uploadedAt
   
See: /diagrams/sequences.puml (Document_Upload_Sequence)
```

---

## 2.6 DATA FLOW DIAGRAMS (PlantUML - See /diagrams/dataflow.puml)

Data flow diagrams show how data moves through the system:

### 2.6.1 User Registration Data Flow
```
Input: RegisterRequest
├─ name: String
├─ email: String (unique)
├─ password: String (plain text)
├─ phone: String
└─ role: String (BUYER/ADMIN)

Processing Steps:
1. UserController: Input Validation
   ├─ @Valid annotation triggers validation
   ├─ Check: email format (must be valid email)
   ├─ Check: password strength (min 8 chars recommended)
   └─ If invalid → 400 BAD REQUEST

2. UserServiceImpl: Business Logic
   ├─ Query: SELECT * FROM users WHERE email = ?
   ├─ Check: Email already exists?
   │  └─ If yes → throw EmailAlreadyExistsException (409)
   │
   ├─ PasswordEncoder: Encode password
   │  ├─ BCrypt algorithm
   │  ├─ Generate salt
   │  └─ Create hash: bcrypt($2a$10$...)
   │
   ├─ Create User entity with:
   │  ├─ name
   │  ├─ email
   │  ├─ password (encoded)
   │  ├─ phone
   │  ├─ role
   │  ├─ is_active = true
   │  ├─ is_verified = false
   │  ├─ failed_attempts = 0
   │  ├─ account_locked = false
   │  └─ created_at = NOW()
   │
   ├─ Database: Save User
   │  └─ INSERT INTO users (...)
   │
   ├─ EmailService: Send welcome email
   │  ├─ From: noreply@marutixchange.com
   │  ├─ To: user email
   │  ├─ Subject: Welcome to MarutiXchange
   │  └─ Body: HTML template with user name

Output: 201 CREATED
├─ UserResponse
│  ├─ id: Generated user ID
│  ├─ name
│  ├─ email
│  ├─ phone
│  ├─ role
│  └─ Wrapped in ApiResponse<UserResponse>

Database Changes:
├─ INSERT users table
└─ No email sent if save fails (transaction)

See: /diagrams/dataflow.puml (UserRegistration_DataFlow)
```

### 2.6.2 User Login Data Flow
```
Input: LoginRequest
├─ email: String
└─ password: String (plain text)

Processing Steps:
1. UserController: Input Validation
   └─ @Valid checks format

2. UserServiceImpl: Authentication
   ├─ Query: SELECT * FROM users WHERE email = ?
   ├─ If not found → InvalidCredentialsException (401)
   │
   ├─ PasswordEncoder.matches(plain, hash)
   │  ├─ Compare provided password with stored hash
   │  ├─ If mismatch:
   │  │  ├─ Increment failed_attempts
   │  │  ├─ If failed_attempts >= 5:
   │  │  │  ├─ Set account_locked = true
   │  │  │  ├─ Set locked_until = NOW + 30 minutes
   │  │  │  └─ Save to database
   │  │  └─ Throw InvalidCredentialsException (401)
   │  │
   │  └─ If match → Continue
   │
   ├─ Authorization Checks:
   │  ├─ Check: is_active = true
   │  │  └─ If false → account not active
   │  ├─ Check: is_verified = true
   │  │  └─ If false → email not verified (need OTP)
   │  ├─ Check: account_locked = false
   │  │  └─ If true → account temporarily locked
   │  └─ If any fail → throw exception

3. JwtUtil: Token Generation
   ├─ Create Claims:
   │  ├─ subject (sub): email
   │  ├─ claim (userId): user ID
   │  ├─ issuedAt (iat): current timestamp
   │  └─ expiration (exp): current + 24 hours
   │
   ├─ Sign Token:
   │  ├─ Algorithm: HMAC-SHA256
   │  ├─ Secret: ${app.jwt.secret}
   │  └─ Format: header.payload.signature
   │
   └─ Compact: Convert to Base64 URL-safe string

4. Reset failed_attempts (on success)
   └─ UPDATE users SET failed_attempts = 0

Output: 200 OK
├─ LoginResponse
│  ├─ token: JWT string
│  ├─ expiresIn: 86400 (seconds)
│  ├─ tokenType: "Bearer"
│  ├─ user: UserResponse object
│  └─ Wrapped in ApiResponse<LoginResponse>

Database Changes:
├─ UPDATE users SET failed_attempts = 0, updated_at = NOW()
└─ No persistent token storage (stateless JWT)

See: /diagrams/dataflow.puml (UserLogin_DataFlow)
```

### 2.6.3 OTP Verification Data Flow
```
Send OTP:
Input: OtpRequest { email }
├─ Find user by email
├─ Generate 6-digit random OTP (000000-999999)
├─ Create OtpToken entity:
│  ├─ user_id
│  ├─ otp_code
│  ├─ is_used = false
│  ├─ created_at = NOW()
│  └─ expires_at = NOW() + 5 minutes
│
├─ Save to otp_tokens table
├─ Send email with OTP code
└─ Return: 200 OK { message }

Verify OTP:
Input: OtpVerifyRequest { email, otp }
├─ Find user by email
├─ Query: SELECT * FROM otp_tokens WHERE user_id = ? ORDER BY created_at DESC
│
├─ Validation Checks:
│  ├─ OTP found?
│  ├─ OTP not already used? (is_used = false)
│  ├─ OTP not expired? (expires_at > NOW())
│  └─ OTP code matches input?
│
├─ If all pass:
│  ├─ UPDATE otp_tokens SET is_used = true
│  ├─ UPDATE users SET is_verified = true
│  └─ Return: 200 OK { message: "Verified" }
│
└─ If fail → throw InvalidOtpException (400)

Database Changes:
├─ INSERT otp_tokens
├─ UPDATE otp_tokens SET is_used = true
└─ UPDATE users SET is_verified = true

See: /diagrams/dataflow.puml (OTP_DataFlow)
```

### 2.6.4 Protected Request Data Flow
```
Input: HTTP Request
├─ Method: GET/POST/PUT/DELETE
├─ URL: /api/v1/users/{id}
└─ Headers: Authorization: Bearer eyJhbGc...

Processing Steps:
1. JwtAuthFilter: Token Extraction
   ├─ Get Authorization header
   ├─ Check if starts with "Bearer "
   ├─ Extract token substring
   └─ If no token → Continue to next filter

2. JwtUtil: Token Validation
   ├─ Parse JWT string
   ├─ Extract header, payload, signature
   ├─ Verify signature using secret key
   │  └─ Regenerate: HMAC-SHA256(header.payload, secret)
   │  └─ Compare with provided signature
   ├─ Extract expiration timestamp
   ├─ Check: exp > NOW()
   ├─ Extract claims:
   │  ├─ sub (subject/email)
   │  ├─ userId
   │  └─ iat (issued at)
   └─ Return: true if valid, false if invalid/expired

3. Authentication Creation:
   ├─ If token valid:
   │  ├─ Create UsernamePasswordAuthenticationToken
   │  ├─ Set principal: userId
   │  ├─ Set credentials: null (no need)
   │  ├─ Set authorities: Extract from database
   │  └─ SecurityContextHolder.getContext().setAuthentication(auth)
   │
   └─ If token invalid:
      └─ Continue without authentication

4. Controller Authorization:
   ├─ @PreAuthorize("isAuthenticated()")
   │  └─ Check: principal is authenticated
   ├─ @PreAuthorize("hasRole('BUYER')")
   │  └─ Check: user authority contains BUYER
   └─ @PreAuthorize("hasRole('ADMIN')")
      └─ Check: user authority contains ADMIN

5. Controller Execution:
   ├─ If authorized → Execute business logic
   └─ If denied → Return 403 FORBIDDEN

Output: HTTP Response
├─ 200 OK with data (if successful & authorized)
├─ 401 UNAUTHORIZED (invalid/expired token)
├─ 403 FORBIDDEN (authenticated but no permission)
└─ SecurityContext is cleared after response

Database Queries: None (JWT is stateless)
- Token validity checked via signature only
- No database lookup for token validation

See: /diagrams/dataflow.puml (Protected_Request_DataFlow)
```

### 2.6.5 Document Upload Data Flow
```
Input: Multipart Request
├─ Part 1: file (Binary file content)
├─ Part 2: documentType (DocumentType enum)
└─ Authorization: Bearer <JWT>

Processing Steps:
1. JwtAuthFilter: Validate JWT
   └─ Extract userId from claims

2. UserController: Input Validation
   ├─ Check: file.isEmpty() → false (required)
   ├─ Check: documentType is valid enum
   │  └─ Values: AADHAR, PANCARD, PASSPORT, DRIVING_LICENSE, OTHER
   └─ If invalid → 400 BAD REQUEST

3. DocumentService: File Processing
   ├─ Generate unique filename:
   │  ├─ UUID.randomUUID() for uniqueness
   │  ├─ Extension: original file extension
   │  └─ Example: a3f2b1c9-2d7e-4f6a-9d1b-c2e4f7a9b0d1.pdf
   │
   ├─ Create directories:
   │  ├─ mkdir: uploads/documents/
   │  └─ Permissions: read, write
   │
   ├─ Save file to disk:
   │  ├─ location: uploads/documents/{filename}
   │  ├─ size: Read from MultipartFile
   │  └─ Return: path or throws IOException
   │
   └─ Create UserDocument entity:
      ├─ user_id: From JWT claim
      ├─ file_name: Original filename
      ├─ file_path: /uploads/documents/{uuid}.pdf
      ├─ file_size: In bytes
      ├─ document_type: From request
      ├─ uploaded_at: NOW()
      └─ Save to user_documents table

Output: 200 OK
├─ DocumentResponse
│  ├─ id: Generated document ID
│  ├─ fileName: Original filename
│  ├─ filePath: /uploads/documents/{uuid}.pdf
│  ├─ fileSize: Size in bytes
│  ├─ documentType: enum value
│  ├─ uploadedAt: Timestamp
│  └─ Wrapped in ApiResponse<DocumentResponse>

Database Changes:
├─ INSERT user_documents table
└─ FOREIGN KEY (user_id) REFERENCES users(id)

File System Changes:
└─ Create file: uploads/documents/{uuid}.pdf

Constraints:
├─ Max file size: 10 MB (spring.servlet.multipart.max-file-size)
├─ Max request size: 10 MB
└─ File path must be unique (UNIQUE constraint in DB)

See: /diagrams/dataflow.puml (Document_Upload_DataFlow)
```

---

```
REQUEST PIPELINE
├─ Request arrives at API Gateway
├─ Request → DispatcherServlet
├─ DispatcherServlet → Filter Chain
│  ├─ JwtAuthFilter (Custom Filter)
│  │  ├─ Extract token from header: "Authorization: Bearer <JWT_TOKEN>"
│  │  ├─ JwtUtil.validateToken(token)
│  │  │  ├─ Parse JWT signature
│  │  │  ├─ Check expiry time
│  │  │  ├─ Verify issuer & audience (if configured)
│  │  │  └─ Return Claims or throw JwtException
│  │  ├─ Extract userId & roles from Claims
│  │  ├─ Create UsernamePasswordAuthenticationToken
│  │  └─ Set SecurityContextHolder.getContext().setAuthentication(auth)
│  └─ Continue to DispatcherServlet
├─ DispatcherServlet routes to Controller
├─ @PreAuthorize interceptors check roles
│  ├─ BUYER role: Can access buyer endpoints
│  ├─ ADMIN role: Can access all endpoints
│  └─ ANONYMOUS: Can only access register/login
└─ Controller executes business logic

PROTECTED ENDPOINTS EXAMPLES:
├─ @PreAuthorize("hasRole('BUYER')") 
│  └─ /api/users/{id} (GET, PUT, DELETE)
├─ @PreAuthorize("hasRole('ADMIN')")
│  └─ /api/users (GET all users)
├─ @PreAuthorize("isAuthenticated()")
│  └─ /api/documents (POST, GET)
└─ public (no auth required)
   └─ /api/auth/register, /api/auth/login
```

---

## 3. LOGIN FLOW WITH JWT TOKEN GENERATION (Stateless)

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /api/v1/users/login
       │ {email, password}
       ▼
┌──────────────────────────────────────────────────────┐
│        UserController.login()                        │
│  - Validate email & password                         │
│  - Call UserService.login()                          │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────┐
│        UserServiceImpl.login()                        │
│  1. Find user by email in Database                   │
│  2. Verify password with PasswordEncoder.matches()   │
│  3. Check account lock status                        │
│  4. Validate account is active                       │
│  5. Call JwtUtil.generateToken(userId, email)       │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────┐
│        JwtUtil.generateToken(userId, email)          │
│  STATELESS TOKEN GENERATION (No DB storage)          │
│  1. Create JWT Claims:                               │
│     - sub: email                                     │
│     - claim("userId"): userId (REQUIRED for auth)   │
│  2. Set issuedAt: current timestamp                  │
│  3. Set expiration: now + 86400000ms (24 hours)      │
│  4. Sign with HS256 algorithm                        │
│     - Secret key: ${app.jwt.secret}                  │
│     - Keys.hmacShaKeyFor(secret.getBytes(UTF-8))    │
│  5. Compact & return JWT token string                │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────┐
│        Return LoginResponse (ApiResponse wrapper)    │
│  {                                                    │
│    "success": true,                                  │
│    "message": "Login successful",                    │
│    "data": {                                         │
│      "token": "eyJhbGciOiJIUzI1NiIs...",            │
│      "refreshToken": null,                          │
│      "user": {id, email, role, name, phone},        │
│      "expiresIn": 86400,                            │
│      "tokenType": "Bearer"                          │
│    },                                                │
│    "timestamp": "2026-04-15T10:30:00Z"              │
│  }                                                    │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
            ┌──────────────┐
            │    Client    │
            │(Store token) │
            └──────────────┘

PROTECTED API REQUEST WITH JWT:

Client → HTTP Header: "Authorization: Bearer eyJhbGciOi..."
         │
         ▼
    JwtAuthFilter (Spring Security Filter)
         │
         ├─ Extract token from Authorization header
         ├─ Call JwtUtil.validateToken(token)
         │   ├─ Parse JWT using secret key
         │   ├─ Check signature validity (HS256)
         │   ├─ Check expiration: !isTokenExpired()
         │   └─ Return true if valid, false if expired/invalid
         │
         ├─ Extract claims: email, userId
         ├─ Create UsernamePasswordAuthenticationToken
         ├─ Set SecurityContextHolder.getContext().setAuthentication(auth)
         │
         ▼
    Request continues to Controller
         │
         ├─ 200 OK if authorized
         └─ 401 UNAUTHORIZED if token invalid/expired
         └─ 403 FORBIDDEN if insufficient permissions

TOKEN STRUCTURE (JWT Format: header.payload.signature):

Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload (Claims):
{
  "sub": "user@example.com",        // Subject (email)
  "userId": 1,                      // Custom claim - USER ID
  "iat": 1713179400,                // Issued at timestamp
  "exp": 1713265800                 // Expiration timestamp
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)

TOKEN VALIDATION LOGIC (JwtUtil.validateToken):
1. Try to parse token with secret key
2. If JwtException → return false (invalid signature)
3. Extract expiration date
4. Check if expiration is before current time
5. Return !isExpired
6. Caller checks if token is valid for request

NOTE: No token revocation mechanism
- Tokens are valid until expiration
- Logout is client-side only (delete token)
- For immediate revocation: whitelist revoked tokens (future enhancement)
```

---

## 4. EUREKA SERVICE DISCOVERY INTEGRATION

```
┌──────────────────────────────────────────────────────┐
│          Eureka Server (Service Registry)            │
│  - Maintains list of all active services             │
│  - Health check every 30 seconds                     │
│  - Auto-deregistration on failure                    │
└────────────────────┬─────────────────────────────────┘
                     │
     ┌───────────────┼───────────────┐
     │               │               │
     ▼               ▼               ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│  User    │  │  Order   │  │ Payment  │
│ Service  │  │ Service  │  │ Service  │
│(Instance)│  │(Instance)│  │(Instance)│
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │             │             │
     ▼             ▼             ▼
  ┌──────────────────────────────────┐
  │  Eureka Client Configuration:     │
  │  spring.application.name=         │
  │    user-service                   │
  │  eureka.client.service-url.       │
  │    defaultZone=http://eureka:8761│
  │  eureka.instance.instance-id=     │
  │    user-service-${instance.ip}    │
  └──────────────────────────────────┘

SERVICE DISCOVERY BENEFITS:
├─ Dynamic service registration/deregistration
├─ Client-side load balancing (Ribbon)
├─ Health monitoring & failover
├─ Service-to-service communication via service name
└─ Automatic retry on failure
```

---

## 5. ROLE-BASED ACCESS CONTROL (RBAC)

```
AVAILABLE ROLES:
├─ ADMIN
│  ├─ Full access to all endpoints
│  ├─ User management (view all users, delete users)
│  ├─ View all documents across users
│  └─ System configuration & monitoring
│
├─ BUYER
│  ├─ Manage own user profile (GET /api/users/{id})
│  ├─ Upload & view own documents
│  ├─ Change password & reset via OTP
│  └─ Limited visibility (cannot see other users)
│
└─ ANONYMOUS (No Authentication Required)
   ├─ POST /api/auth/register
   ├─ POST /api/auth/login
   └─ POST /api/auth/send-otp

AUTHORIZATION EXAMPLES:

@RestController
@RequestMapping("/api/users")
public class UserController {
  
  // Public endpoint
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {}
  
  // Authenticated users only
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> getUserById(@PathVariable Long id) {}
  
  // BUYER role required
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('BUYER')")
  public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequest req) {}
  
  // ADMIN role only
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> getAllUsers() {}
  
  // Multiple roles
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or (hasRole('BUYER') and #id == authentication.principal.id)")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {}
}
```

---

## 6. API TESTING WITH SWAGGER UI

```
SWAGGER UI FEATURES:
├─ Endpoint: http://localhost:8080/swagger-ui.html
├─ Auto-generated from @OpenAPI annotations
├─ Features:
│  ├─ Interactive API documentation
│  ├─ Try-it-out functionality
│  ├─ Request/Response examples
│  ├─ Schema validation
│  └─ Bearer Token Authentication UI
│
├─ Bearer Token Setup:
│  1. Click "Authorize" button (lock icon)
│  2. Select "Bearer Token" scheme
│  3. Paste JWT token: eyJhbGciOiJIUzI1NiIs...
│  4. All subsequent requests include token
│
├─ Testing Protected Endpoints:
│  1. First call /api/auth/login → Get token
│  2. Click Authorize & paste token
│  3. Call protected endpoints (GET /api/users/{id}, etc)
│  4. Swagger auto-includes Bearer token in header
│
└─ Response Codes Documented:
   ├─ 200 OK - Successful request
   ├─ 201 CREATED - Resource created
   ├─ 400 BAD REQUEST - Invalid input
   ├─ 401 UNAUTHORIZED - Missing/invalid token
   ├─ 403 FORBIDDEN - Insufficient permissions
   └─ 500 INTERNAL SERVER ERROR - Server error

SPRINGDOC-OPENAPI CONFIGURATION:
├─ Dependency: springdoc-openapi-starter-webmvc-ui
├─ Auto-enables Swagger UI at /swagger-ui.html
├─ Auto-generates OpenAPI spec at /v3/api-docs
└─ No additional configuration needed
```

---

## 7. TESTING LAYER STRATEGY

```
TESTING PYRAMID
        ▲
       ╱ ╲         END-TO-END TESTS
      ╱   ╲        - Full application flow
     ╱─────╲       - Database integration
    ╱       ╲      - API testing (1-2 critical paths)
   ╱─────────╲
  ╱           ╲    INTEGRATION TESTS
 ╱             ╲   - Service + Repository layers
╱───────────────╲  - MockMvc controller tests
╱                 ╲ - 20-30% of test suite
───────────────────
│    UNIT TESTS   │  - Service business logic
│  - Service      │  - Utility classes
│  - Util         │  - 70-80% of test suite
│  - Mapper       │
└─────────────────┘

═══════════════════════════════════════════════════════════════

1. UNIT TESTS (Service Layer)

@SpringBootTest
public class UserServiceImplTest {
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private PasswordEncoder passwordEncoder;
  
  @InjectMocks
  private UserServiceImpl userService;
  
  @Test
  public void testRegisterUserSuccess() {
    // Given
    RegisterRequest request = new RegisterRequest("John", "john@gmail.com", "password123", "1234567890", "BUYER");
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
    
    // When
    UserResponse response = userService.register(request);
    
    // Then
    assertNotNull(response);
    assertEquals("john@gmail.com", response.getEmail());
    verify(userRepository, times(1)).save(any(User.class));
  }
  
  @Test
  public void testLoginUserWithInvalidPassword() {
    // Given
    LoginRequest request = new LoginRequest("john@gmail.com", "wrongpassword");
    User user = new User(1L, "John", "john@gmail.com", "encoded_password", "1234567890", "BUYER", true, false, 0, false, LocalDateTime.now());
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);
    
    // When & Then
    assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
  }
}

═══════════════════════════════════════════════════════════════

2. CONTROLLER TESTS (MockMvc with CSRF & Security)

@AutoConfigureMockMvc
@SpringBootTest
public class UserControllerTest {
  
  @Autowired
  private MockMvc mockMvc;
  
  @MockBean
  private UserService userService;
  
  @Test
  @WithMockUser(roles = "BUYER")
  public void testGetUserByIdWithAuthentication() throws Exception {
    // Given
    UserResponse userResponse = new UserResponse(1L, "John", "john@gmail.com", "1234567890", "BUYER");
    when(userService.getUserById(1L)).thenReturn(userResponse);
    
    // When & Then
    mockMvc.perform(get("/api/users/1")
        .with(csrf())  // Include CSRF token (if enabled)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("john@gmail.com"));
  }
  
  @Test
  public void testLoginReturnsJwtToken() throws Exception {
    // Given
    LoginRequest request = new LoginRequest("john@gmail.com", "password123");
    LoginResponse response = new LoginResponse("jwt_token_here", "refresh_token", 86400, "Bearer", new UserResponse(1L, "John", "john@gmail.com", "1234567890", "BUYER"));
    when(userService.login(request)).thenReturn(response);
    
    // When & Then
    mockMvc.perform(post("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").exists())
      .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }
  
  @Test
  public void testUnauthorizedAccessWithoutToken() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/users/1")
        .with(csrf()))
      .andExpect(status().isUnauthorized());
  }
  
  @Test
  public void testPublicEndpointNoAuthRequired() throws Exception {
    // Given
    RegisterRequest request = new RegisterRequest("Jane", "jane@gmail.com", "password123", "9876543210", "BUYER");
    UserResponse response = new UserResponse(2L, "Jane", "jane@gmail.com", "9876543210", "BUYER");
    when(userService.register(request)).thenReturn(response);
    
    // When & Then
    mockMvc.perform(post("/api/auth/register")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated());
  }
}

═══════════════════════════════════════════════════════════════

3. INTEGRATION TESTS (Service + Repository)

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceIntegrationTest {
  
  @Autowired
  private UserService userService;
  
  @Autowired
  private UserRepository userRepository;
  
  @BeforeEach
  public void setUp() {
    userRepository.deleteAll();
  }
  
  @Test
  public void testRegisterAndLoginFlow() {
    // Register user
    RegisterRequest registerReq = new RegisterRequest("John", "john@gmail.com", "password123", "1234567890", "BUYER");
    UserResponse registered = userService.register(registerReq);
    assertNotNull(registered.getId());
    
    // Verify user exists in DB
    Optional<User> user = userRepository.findByEmail("john@gmail.com");
    assertTrue(user.isPresent());
    
    // Login user
    LoginRequest loginReq = new LoginRequest("john@gmail.com", "password123");
    LoginResponse loginResp = userService.login(loginReq);
    assertNotNull(loginResp.getToken());
    assertTrue(loginResp.getToken().startsWith("eyJ")); // JWT format
  }
}

TEST EXECUTION:
├─ Run unit tests: mvn test
├─ Run integration tests: mvn verify
├─ Generate coverage report: mvn jacoco:report
├─ View coverage: target/site/jacoco/index.html
└─ Target coverage: > 80%
```

---

## 8. CSRF CONFIGURATION FOR REST APIS

```
CSRF (Cross-Site Request Forgery) - REST API Context:

Spring Security Default:
├─ CSRF protection ENABLED for session-based applications
├─ Requires CSRF token for state-changing requests (POST, PUT, DELETE)
└─ Not applicable for stateless REST APIs using JWT tokens

REST API Configuration (JWT-Based):
├─ CSRF protection DISABLED (stateless authentication)
├─ JWT token validates request authenticity
├─ No session state = No CSRF vulnerability
│
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable()) // Disabled for JWT-based REST APIs
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/users/**").authenticated()
        .anyRequest().authenticated())
      .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
  }
}

├─ Testing with CSRF Disabled:
│  └─ MockMvc tests don't need .with(csrf())
│
├─ Testing with CSRF Enabled (Session-Based):
│  └─ MockMvc tests must include .with(csrf())
│
├─ Production Recommendation:
│  ├─ Disable CSRF for stateless REST APIs (JWT)
│  ├─ Keep CSRF enabled for traditional web apps (cookies)
│  └─ Use token validation for API security

SECURITY COMPARISON:
┌────────────────────┬──────────────────┬─────────────────────┐
│ Authentication     │ Session-Based    │ JWT-Based           │
├────────────────────┼──────────────────┼─────────────────────┤
│ Token Storage      │ HttpSession      │ Client-side         │
│ CSRF Risk          │ Yes              │ No (Stateless)      │
│ CSRF Protection    │ Required         │ Not needed          │
│ Scalability        │ Limited          │ Excellent           │
│ Multi-server       │ Requires sticky  │ Works naturally     │
└────────────────────┴──────────────────┴─────────────────────┘
```

---

## 9. KEY WORKFLOWS (Simplified)

### 9.1 User Registration
```
CLIENT                  API GATEWAY           USER SERVICE
  │                         │                      │
  ├─ POST /register ────────→ │                    │
  │   {name, email, pwd}     │ ──────────────────→ │
  │                          │                     ├─ Validate input
  │                          │                     ├─ Hash password
  │                          │                     ├─ Save to DB
  │                          │                     ├─ Send welcome email
  │                          │ ←──────────────────┤
  │ ←────── 201 CREATED ─────┤                    │
  │ {userId, email}          │                    │
```

### 9.2 User Login & JWT Token Generation
```
CLIENT                  API GATEWAY           USER SERVICE
  │                         │                      │
  ├─ POST /login ───────────→ │                    │
  │ {email, password}        │ ──────────────────→ │
  │                          │                     ├─ Find user
  │                          │                     ├─ Verify password
  │                          │ ←──────────────────┤
  │ ←────── 200 OK ──────────┤                    │
  │ {token, refreshToken}    │                    │
  │                          │                    │
  │ (Store token)            │                    │
  │                          │                    │
  ├─ GET /users/1 ──────────→ │                    │
  │ Header: Authorization:   │ ──────────────────→ │
  │ Bearer <JWT_TOKEN>       │   JwtAuthFilter    │
  │                          │   Validates token  │
  │                          │ ←──────────────────┤
  │ ←────── 200 OK ──────────┤                    │
  │ {user data}              │                    │
```

### 9.3 OTP Verification Flow
```
CLIENT                  API GATEWAY           USER SERVICE
  │                         │                      │
  ├─ POST /send-otp ────────→ │                    │
  │ {email}                  │ ──────────────────→ │
  │                          │                     ├─ Generate OTP
  │                          │                     ├─ Save to DB (5min expiry)
  │                          │                     ├─ Send email with OTP
  │                          │ ←──────────────────┤
  │ ←────── 200 OK ──────────┤                    │
  │ {message: "OTP sent"}    │                    │
  │                          │                    │
  │ (User receives OTP via email)                 │
  │                          │                    │
  ├─ POST /verify-otp ──────→ │                    │
  │ {email, otp}             │ ──────────────────→ │
  │                          │                     ├─ Verify OTP
  │                          │                     ├─ Mark user verified
  │                          │ ←──────────────────┤
  │ ←────── 200 OK ──────────┤                    │
  │ {message: "Verified"}    │                    │
```

### 9.4 Protected Document Upload
```
CLIENT (with JWT token)    API GATEWAY        USER SERVICE
  │                             │                   │
  ├─ POST /documents ──────────→ │                   │
  │ Header: Authorization:       │ ──────────────────→ │
  │ Bearer <JWT_TOKEN>           │   JwtAuthFilter     │
  │ File: document.pdf           │   validates token   │
  │                              │   Creates auth ctx  │
  │                              │ ───────────────────→│
  │                              │                    ├─ Verify user authenticated
  │                              │                    ├─ Check role (BUYER/ADMIN)
  │                              │                    ├─ Save file
  │                              │                    ├─ Save metadata to DB
  │                              │ ←──────────────────┤
  │ ←───── 201 CREATED ────────┤                    │
  │ {documentId, fileName, uploadDate}              │
```

---

## 10. DATABASE SCHEMA (Actual Implementation)

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL (Bcrypt-encoded),
  phone VARCHAR(15) NOT NULL,
  role ENUM('ADMIN', 'BUYER') DEFAULT 'BUYER',
  is_active BOOLEAN DEFAULT TRUE,
  is_verified BOOLEAN DEFAULT FALSE,
  failed_attempts INT DEFAULT 0,
  account_locked BOOLEAN DEFAULT FALSE,
  locked_until TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_role (role),
  INDEX idx_created_at (created_at)
);

-- OTP tokens for password reset & email verification
CREATE TABLE otp_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  otp_code VARCHAR(6) NOT NULL,
  is_used BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_expires_at (expires_at),
  UNIQUE KEY uk_user_otp (user_id, otp_code)
);

-- User documents (KYC, identity, etc.)
CREATE TABLE user_documents (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(500) NOT NULL UNIQUE,
  file_size BIGINT NOT NULL,
  document_type ENUM('AADHAR', 'PANCARD', 'PASSPORT', 'DRIVING_LICENSE', 'OTHER') DEFAULT 'OTHER',
  uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_uploaded_at (uploaded_at),
  INDEX idx_document_type (document_type)
);

NOTE: JWT Tokens are NOT persisted in DB
- JwtUtil generates tokens on-the-fly using HS256 (HMAC-SHA256)
- Tokens are signed with secret key: ${app.jwt.secret}
- Default expiration: 86400000ms (24 hours)
- Validation is done via signature verification & expiry check only
- No token revocation mechanism (stateless design)
```

---

## 11. API ENDPOINTS SUMMARY (CORRECTED - v1.0)

```
AUTHENTICATION (Public - No JWT Required)
├─ POST   /api/v1/users/register              - Register new user
├─ POST   /api/v1/users/login                 - Login (returns JWT token)
├─ POST   /api/v1/users/send-otp              - Send OTP via email
├─ POST   /api/v1/users/verify-otp            - Verify OTP & activate account
├─ POST   /api/v1/users/forgot-password       - Initiate password reset
└─ POST   /api/v1/users/reset-password        - Reset password with token

USER MANAGEMENT (JWT Authenticated)
├─ GET    /api/v1/users/{id}                  - Get user by ID (User/ADMIN)
├─ PUT    /api/v1/users/{id}                  - Update user (User/ADMIN)
├─ DELETE /api/v1/users/{id}                  - Delete user (ADMIN only)
└─ GET    /api/v1/users                       - List all users with pagination (ADMIN only)

DOCUMENT MANAGEMENT (JWT Authenticated)
├─ POST   /api/v1/users/{id}/upload-document - Upload document (User/ADMIN)
└─ GET    /api/v1/users/{id}/documents        - Get user's documents (User/ADMIN)

API DOCUMENTATION & MONITORING
├─ GET    /swagger-ui.html                    - Interactive API docs (Swagger UI)
├─ GET    /v3/api-docs                        - OpenAPI 3.0 specification (JSON)
└─ GET    /actuator/health                    - Service health check

EXAMPLE REQUEST/RESPONSE:

POST /api/v1/users/login
Request:
{
  "email": "user@example.com",
  "password": "password123"
}

Response (200 OK):
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": null,
    "expiresIn": 86400,
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "phone": "9876543210",
      "role": "BUYER"
    }
  },
  "timestamp": "2026-04-15T10:30:00Z"
}

All responses are wrapped in ApiResponse with:
- success: boolean
- message: string
- data: T (generic response object)
- timestamp: ISO 8601 datetime
```

---

## 12. RUNNING THE APPLICATION

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Run specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Test
mvn test                      # Unit tests
mvn verify                    # All tests + integration
mvn jacoco:report             # Code coverage

# Access application
├─ API: http://localhost:8080
├─ Swagger UI: http://localhost:8080/swagger-ui.html
├─ OpenAPI docs: http://localhost:8080/v3/api-docs
└─ Actuator: http://localhost:8080/actuator/health
```

---

## 13. SUMMARY

This architecture provides:

✅ **Security**: Spring Security + JWT authentication  
✅ **Authorization**: Role-based access control (RBAC)  
✅ **Service Discovery**: Eureka integration for microservices  
✅ **API Documentation**: Swagger UI with Bearer token support  
✅ **Testing**: Unit, integration, and controller tests  
✅ **Simplicity**: Focused on core flows without verbosity  
✅ **Scalability**: Stateless JWT architecture  
✅ **Professional**: Enterprise-grade implementation

---

