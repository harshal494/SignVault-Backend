# SignVault 🔐
### Digital Contract Signing Platform

> A production-grade backend system that enables legally-binding digital contract signing between two parties — without ever needing to meet in person.

Built with a full professional architecture using Java 17, Spring Boot 3.5, Spring Security, JWT, OAuth2, MySQL, Cloudinary, and Twilio.

**Backend status: 100% complete — 30+ endpoints.**


## 💡 Problem Statement

In India, most agreements between two parties — rental agreements, freelance contracts, employment letters, NDAs — still rely on physical paper. Printing, signing, scanning, and couriering documents back and forth is slow, inconvenient, and easy to forge or dispute later.

**SignVault solves this** by providing a secure, verifiable, and tamper-proof digital contract signing experience — with fingerprint-based signing, OTP-based identity verification, a complete audit trail, and an immutable vault for completed contracts.

---

## ⚙️ Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 17 | Core language |
| Framework | Spring Boot 3.5.14 | REST API, dependency injection, app lifecycle |
| Security | Spring Security 6.5 + JWT + OAuth2 (Google) | Stateless auth, 3-layer role-based access control |
| Database | MySQL 8 | Persistent storage for users, contracts, audit logs |
| ORM | Spring Data JPA + Hibernate | Database access layer |
| Migrations | Flyway | Version-controlled schema changes |
| File Storage | Cloudinary | PDF contract storage with SHA-256 tamper detection |
| Email | JavaMailSender (Gmail SMTP) | Async notifications on every contract event |
| SMS / OTP | Twilio | Phone OTP verification |
| API Docs | springdoc-openapi + Swagger UI | Interactive API documentation |
| Build Tool | Maven | Dependency & build management |
| Deployment | Docker + Railway | Containerized deployment |
| API Testing | Postman (exported collection included) | End-to-end endpoint testing |

---

## 🚀 Features

### Authentication & Identity
- Email/password registration with email OTP verification
- Google OAuth2 login (email auto-verified by Google)
- Optional phone OTP verification via Twilio (post-login, part of profile completion — not a login blocker)
- JWT-based stateless authentication (24-hour token expiry)
- Fingerprint stored as dual hash — BCrypt (verification) + SHA-256 (dispute proof). Raw fingerprint never stored.
- Role-Based Access Control — `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUPERADMIN`

### Contract Management
- Upload PDF contract → SHA-256 hash generated and stored for tamper detection
- Unique contract ID assigned to every contract (`SV-2026-XXXXXX`)
- Configurable contract period — Days / Months / Years / Permanent
- Sender signs first, then receiver — strict order enforced
- Contract delivery via email + in-app notification
- **Reject** — receiver can decline a contract's terms before or after sender signs
- **Cancel** — sender can withdraw their own contract while it's still PENDING
- Contract status lifecycle: `PENDING → SENDER_SIGNED → FULLY_SIGNED → EXPIRED / RENEWED` (with `CANCELLED` and `REJECTED` as terminal exit states)

### Security & Trust
- PDF tamper detection via SHA-256 hash comparison
- Fingerprint dispute resolution via SHA-256 hash captured at signing time
- Full audit trail — every action logged with user, action type, timestamp, and IP address
- Immutable vault — fully signed contracts cannot be edited or deleted by regular users
- Public contract verification endpoint that hides cancelled contracts from public view (returns 404, no status leak)

### Notifications & Reminders
- In-app notification system (read/unread, mark all as read)
- Async email notifications for every contract event
- Automated expiry reminder scheduler — runs every 5 minutes, covers 8 reminder windows (7 days, 3 days, 2 days, 1 day, 24 hours, 1 hour, 30 minutes, 10 minutes before expiry)
- Duplicate-reminder prevention — each user gets each reminder window exactly once per contract

### Admin Panel (RBAC)
- **ROLE_ADMIN** — view all users & contracts, flag suspicious contracts, deactivate user accounts, view full audit log
- **ROLE_SUPERADMIN** — everything ADMIN can do, plus promote/demote users, unflag contracts, and **cancel any contract at any status** — including contracts already in the immutable vault, for legal/fraud takedown scenarios
- Flagging workflow: ADMIN flags a suspicious contract → SUPERADMIN reviews → either unflags (cleared) or cancels (terminated)

---

## 🗂️ Project Structure

```
src/main/java/com/harshalkhade/signvault/
│
├── config/          # SecurityConfig, CloudinaryConfig, SchedulerConfig, CorsConfig, PasswordConfig
├── controller/      # AuthController, ContractController, SignatureController,
│                     NotificationController, UserController, VaultController, AdminController
├── dto/
│   ├── request/     # RegisterRequest, LoginRequest, CreateContractRequest, SignContractRequest, etc.
│   └── response/    # AuthResponse, ContractResponse, ApiResponse, AuditLogResponse, etc.
├── entity/          # User, Contract, Signature, ContractFile, AuditLog, Notification, OtpVerification
├── enums/           # Role, ContractStatus, PeriodType, OtpType, NotificationType, AuthProvider, SignatureRole
├── exception/       # GlobalExceptionHandler, ResourceNotFoundException, UnauthorizedException, ContractException
├── repository/      # JpaRepository interfaces for all entities
├── security/        # JwtUtil, JwtAuthFilter, CustomUserDetailsService, OAuth2SuccessHandler
├── service/         # AuthService, ContractService, SignatureService, NotificationService,
│                     UserService, VaultService, SchedulerService, AdminService, EmailService, SmsService
└── util/            # HashUtil, OtpGenerator, ContractIdGenerator, DateUtil

src/main/resources/
├── application.properties.example   # Template — copy and fill your own values
└── db/migration/                    # Flyway SQL migration files V1–V7

postman/
└── SignVault.postman_collection.json   # Full exported collection with test scripts
```

---

## 🗄️ Database Schema

7 MySQL tables managed via Flyway migrations (V1–V7):

| Table | Purpose |
|---|---|
| `users` | User accounts, auth details, roles, fingerprint hashes |
| `contracts` | Contract metadata, period, status, renewal info, flagged status |
| `signatures` | Individual signatures per party per contract |
| `contract_files` | Cloudinary URL + SHA-256 hash of uploaded PDF |
| `audit_logs` | Tamper-proof activity log for every contract action |
| `notifications` | In-app notification records per user |
| `otp_verifications` | OTP records for email and phone verification |

---

## 🔐 RBAC — Role Based Access Control

| Role | Access |
|---|---|
| `ROLE_USER` | Create/send contracts, sign, reject (as receiver), cancel own PENDING contracts (as sender), view own vault, notifications, profile |
| `ROLE_ADMIN` | Everything USER can do **+** view all users/contracts, flag suspicious contracts, deactivate any user, view audit logs |
| `ROLE_SUPERADMIN` | Everything ADMIN can do **+** promote/demote users, unflag contracts, **cancel any contract at any status (including the vault)** |

Security is enforced at 3 layers: URL-level (`SecurityConfig`), method-level (`@PreAuthorize`), and business-logic level (service layer checks).

---

## 📡 Complete API Reference

### Authentication — `/api/auth/**`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register with email, phone, password. Sends email OTP automatically. Returns JWT. |
| POST | `/api/auth/login` | Public | Login with email + password. Returns JWT. Requires `emailVerified = true`. |
| POST | `/api/auth/send-email-otp` | Public | Send/resend OTP to email |
| POST | `/api/auth/verify-email-otp` | Public | Verify email OTP — sets `emailVerified = true` |
| POST | `/api/auth/send-phone-otp` | JWT | Send SMS OTP to user's registered phone (via Twilio) |
| POST | `/api/auth/verify-phone-otp` | JWT | Verify phone OTP — sets `phoneVerified = true` |
| POST | `/api/auth/register-fingerprint` | JWT | Register fingerprint hash (BCrypt + SHA-256) |
| POST | `/api/auth/complete-profile` | JWT | Update name, phone, age — sets `profileComplete = true` once email + phone verified |
| GET | `/oauth2/authorization/google` | Public | Initiate Google OAuth2 login |

### Contracts — `/api/contracts/**` — `ROLE_USER`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/contracts` | Create + send a contract. **multipart/form-data**: part `data` (JSON, `Content-Type: application/json`) + part `file` (PDF) |
| GET | `/api/contracts/{contractId}` | Get full contract details — sender or receiver only |
| GET | `/api/contracts/verify/{contractId}` | **Public.** Verify a contract's legitimacy. Returns minimal info. Cancelled contracts return 404. |
| PUT | `/api/contracts/{contractId}/cancel` | Sender withdraws their own contract — only while status is `PENDING` |
| PUT | `/api/contracts/{contractId}/reject` | Receiver declines the contract — while status is `PENDING` or `SENDER_SIGNED` |

### Signatures — `/api/signatures/**` — `ROLE_USER`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/signatures/sign` | Sign a contract with fingerprint data. Sender must sign first, then receiver. |

### Notifications — `/api/notifications/**` — `ROLE_USER`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/notifications` | All notifications for the logged-in user, newest first |
| GET | `/api/notifications/unread-count` | Count of unread notifications |
| PUT | `/api/notifications/{id}/read` | Mark a single notification as read |
| PUT | `/api/notifications/read-all` | Mark all notifications as read |

### Users — `/api/users/**` — `ROLE_USER`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/profile` | Get logged-in user's profile |
| PUT | `/api/users/profile` | Update profile (name, phone, age) |
| DELETE | `/api/users/deactivate` | Deactivate own account |

### Vault — `/api/vault/**` — `ROLE_USER`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/vault` | All `FULLY_SIGNED` contracts for the logged-in user |
| GET | `/api/vault/{contractId}` | A single fully-signed contract from the vault |

### Admin — `/api/admin/**` — `ROLE_ADMIN` / `ROLE_SUPERADMIN`

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/admin/users` | ADMIN + SUPERADMIN | List all users |
| GET | `/api/admin/users/{id}` | ADMIN + SUPERADMIN | Get a single user by ID |
| PUT | `/api/admin/users/{id}/deactivate` | ADMIN + SUPERADMIN | Deactivate any user account |
| PUT | `/api/admin/users/{id}/promote` | SUPERADMIN | Promote a user to `ROLE_ADMIN` |
| PUT | `/api/admin/users/{id}/demote` | SUPERADMIN | Demote an admin back to `ROLE_USER` |
| GET | `/api/admin/contracts` | ADMIN + SUPERADMIN | List every contract in the system |
| GET | `/api/admin/contracts/flagged` | ADMIN + SUPERADMIN | List all flagged contracts |
| PUT | `/api/admin/contracts/{contractId}/flag` | ADMIN + SUPERADMIN | Flag a contract for review |
| PUT | `/api/admin/contracts/{contractId}/unflag` | SUPERADMIN | Clear a flag |
| PUT | `/api/admin/contracts/{contractId}/cancel` | SUPERADMIN | Cancel **any** contract, at **any** status, including the vault |
| GET | `/api/admin/audit-logs` | ADMIN + SUPERADMIN | Full audit trail (sanitized — no passwords/hashes exposed) |

---

## 🔄 Contract Status Flow

```
PENDING ──signs──► SENDER_SIGNED ──signs──► FULLY_SIGNED ──► Vault (immutable)
   │                     │                        │
   ├──reject──► REJECTED ┘                        ├──► EXPIRED (auto, via scheduler)
   │                                               └──► CANCELLED (SUPERADMIN only)
   └──cancel (sender)──► CANCELLED
   └──SUPERADMIN cancel──► CANCELLED   (any status, overrides vault immutability)
```


## 📦 Postman Collection

A full Postman collection with environment variables and automated test scripts for all 30+ endpoints is included in the repo:

```
postman/SignVault.postman_collection.json
```

Import it into Postman, set the `baseUrl` environment variable to `https://localhost:8080/`, run the **Login** request (auto-saves your JWT token), and you're ready to go — including the file upload endpoint mentioned above.

---

## 🛠️ How to Run Locally

### Prerequisites
- Java 17
- MySQL 8
- Maven
- (Optional) Docker

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/harshal494/signvault.git
cd signvault
```

2. **Create MySQL database**
```sql
CREATE DATABASE signvault_db;
```

3. **Configure application properties**
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
Fill in your MySQL credentials, JWT secret, Cloudinary, Gmail SMTP, and Twilio credentials.

4. **Run the application**
```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`. Flyway automatically applies all 7 migrations on first run.

5. **Explore the API**

Visit `http://localhost:8080/swagger-ui/index.html`

---

## 📌 Development Approach

Built feature-by-feature across 7 completed sprints:

- ✅ **Sprint 1** — Project setup, entities, repositories, Flyway migrations
- ✅ **Sprint 2** — Security layer (JWT, Spring Security, OAuth2)
- ✅ **Sprint 3** — Auth APIs (register, login, OTP, fingerprint)
- ✅ **Sprint 4** — Contract APIs (create, send, verify)
- ✅ **Sprint 5** — Signature APIs (sign, status transitions)
- ✅ **Sprint 6** — Notifications, expiry scheduler, vault, CORS
- ✅ **Sprint 7** — Admin panel (flag/unflag/cancel, RBAC, audit logs)
- ✅ **Post-sprint** — Cancel/Reject for regular users, AuditLog sanitization, full Postman test suite, Docker + Railway deployment

---

## 👨‍💻 Author

**Harshal Khade**
B.E. Computer Science & Engineering — Sant Gadge Baba Amravati University (2026)

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue)](http://www.linkedin.com/in/harshal-khade-bb6285213)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black)](https://github.com/harshal494)

---

> ⭐ If you find this project interesting, consider giving it a star!
