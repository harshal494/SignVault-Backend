# SignVault 🔐
### Digital Contract Signing Platform

> A production-grade backend system that enables legally-binding digital contract signing between two parties — without ever needing to meet in person.

Built with a full professional architecture using Java 17, Spring Boot 3.5, Spring Security, JWT, OAuth2, MySQL, and more.

---

## 💡 Problem Statement

In India, most contracts still require physical presence for signing — bond papers, agreements, legal documents. This wastes time, money, and creates unnecessary friction especially for freelancers, small businesses, and rural users.

**SignVault solves this** by providing a secure, verifiable, and tamper-proof digital contract signing experience — with biometric fingerprint validation, OTP-based identity verification, audit trails, and an immutable signed contract vault.

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JWT + OAuth2 (Google) |
| Database | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| File Storage | Cloudinary |
| Email | JavaMailSender (Gmail SMTP) |
| SMS / OTP | Twilio |
| Build Tool | Maven |
| Utilities | Lombok, Bean Validation |
| API Testing | Postman |

---

## 🚀 Features

### Authentication & Identity
- Normal registration with email + phone OTP verification (both compulsory)
- Google OAuth2 login with profile completion gate
- JWT-based stateless authentication
- Fingerprint hash stored with BCrypt + SHA-256 (dual hashing for security + dispute resolution)
- One email and one phone per account (unique enforcement at DB level)
- Role-Based Access Control (RBAC) — `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUPERADMIN`

### Contract Management
- Upload PDF contract → SHA-256 hash generated for tamper detection
- Unique contract ID assigned to every contract
- Contract period configuration — Days / Months / Years / Permanent
- Sender signs first with fingerprint verification
- Contract sent to receiver via email + in-app notification
- Receiver verifies identity (OTP) and signs with fingerprint
- Contract status lifecycle: `PENDING → SENDER_SIGNED → FULLY_SIGNED → EXPIRED`

### Security & Trust
- PDF tamper detection via SHA-256 hash comparison
- Fingerprint dispute resolution — SHA-256 hash stored at signing time for proof
- Full audit trail — every action timestamped with IP address
- Immutable vault — signed contracts cannot be deleted or modified by anyone

### Notifications & Reminders
- In-app notification system
- Email notifications for every contract event
- Expiry reminder schedule:
  - Days 7–3: 1× daily reminder
  - Days 2–1: 3× daily reminder + contract renewal unlocked
  - Last 24 hours: hourly reminders
- Spring `@Scheduled` background job runs every hour

### Contract Renewal
- Renewal only allowed in last 2 days before expiry
- Creates a new contract linked to the original (both immutable)
- Both parties must re-sign the renewed contract

### Vault
- All fully signed contracts saved to both parties' vaults automatically
- Downloadable but never deletable or editable
- Admin can view vault but cannot modify

---

## 🗂️ Project Structure

```
src/main/java/com/harshalkkhade/signvault/
│
├── config/          # SecurityConfig, OAuth2Config, CloudinaryConfig, SchedulerConfig, CorsConfig
├── controller/      # AuthController, ContractController, VaultController, etc.
├── dto/
│   ├── request/     # RegisterRequest, LoginRequest, CreateContractRequest, etc.
│   └── response/    # AuthResponse, ContractResponse, ApiResponse, etc.
├── entity/          # User, Contract, Signature, ContractFile, AuditLog, Notification, OtpVerification
├── enums/           # Role, ContractStatus, PeriodType, OtpType, NotificationType, AuthProvider, SignatureRole
├── exception/       # GlobalExceptionHandler, ResourceNotFoundException, etc.
├── repository/      # JpaRepository interfaces for all entities
├── security/        # JwtUtil, JwtAuthFilter, CustomUserDetailsService, OAuth2SuccessHandler
├── service/         # Business logic — AuthService, ContractService, NotificationService, etc.
└── util/            # HashUtil, OtpGenerator, ContractIdGenerator, DateUtil

src/main/resources/
├── application.properties.example   # Template — copy and fill your own values
├── db/migration/                    # Flyway SQL migration files V1–V5
└── templates/                       # HTML email templates
```

---

## 🗄️ Database Schema

7 MySQL tables managed via Flyway migrations:

| Table | Purpose |
|---|---|
| `users` | User accounts, auth details, fingerprint hashes |
| `contracts` | Contract metadata, period, status, renewal info |
| `signatures` | Individual signatures per party per contract |
| `contract_files` | Cloudinary URL + SHA-256 hash of uploaded PDF |
| `audit_logs` | Tamper-proof activity log for every contract action |
| `notifications` | In-app notification records per user |
| `otp_verifications` | OTP records for email and phone verification |

---

## 🔐 RBAC — Role Based Access Control

| Role | Access |
|---|---|
| `ROLE_USER` | Own contracts, own vault, own mailbox, own notifications |
| `ROLE_ADMIN` | View any contract, resolve disputes, view audit logs |
| `ROLE_SUPERADMIN` | All admin access + promote/demote users + deactivate accounts |

Vault is **immutable for all roles** — no delete or update on signed contracts.

---

## 📡 API Overview

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register with email + phone |
| POST | `/api/auth/login` | Login → JWT token |
| GET | `/oauth2/authorize/google` | Google OAuth2 login |
| POST | `/api/auth/send-email-otp` | Send email OTP |
| POST | `/api/auth/verify-email-otp` | Verify email OTP |
| POST | `/api/auth/send-phone-otp` | Send phone OTP |
| POST | `/api/auth/verify-phone-otp` | Verify phone OTP |
| POST | `/api/auth/register-fingerprint` | Store fingerprint hash |
| POST | `/api/auth/complete-profile` | Complete profile after Google login |

### Contracts
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/contracts/upload` | Upload PDF |
| POST | `/api/contracts/send` | Send contract to receiver |
| GET | `/api/contracts/{contractId}` | Get contract details |
| GET | `/api/contracts/verify/{contractId}` | Public contract verification |
| POST | `/api/contracts/{contractId}/sign` | Sign contract |
| POST | `/api/contracts/{contractId}/renew` | Renew contract (last 2 days only) |

### Vault & Mailbox
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/vault` | My signed contracts |
| GET | `/api/vault/{contractId}/download` | Download signed PDF |
| GET | `/api/mailbox/inbox` | Received contracts |
| GET | `/api/mailbox/outbox` | Sent contracts |

### Notifications
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/notifications` | All my notifications |
| PUT | `/api/notifications/{id}/read` | Mark as read |
| PUT | `/api/notifications/read-all` | Mark all as read |

---

## 🛠️ How to Run Locally

### Prerequisites
- Java 17
- MySQL 8
- Maven

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/harshalkkhade/signvault.git
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
Then fill in your MySQL password, JWT secret, and other credentials.

4. **Run the application**
```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`

Flyway automatically creates all 7 tables on first run.

---

## 📌 Development Approach

This project follows **Agile development** — built feature by feature in sprints:

- ✅ Sprint 1 — Project setup, entities, repositories, Flyway migrations
- 🔄 Sprint 2 — Security layer (JWT, Spring Security, OAuth2) *(in progress)*
- ⏳ Sprint 3 — Auth APIs (register, login, OTP, fingerprint)
- ⏳ Sprint 4 — Contract APIs (upload, send, sign)
- ⏳ Sprint 5 — Notifications, scheduler, vault
- ⏳ Sprint 6 — Admin, audit, deployment

---

## 👨‍💻 Author

**Harshal Khade**
Final Year CSE Student — 
      Sant Gadge Baba Amravati University, Amravati

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue)](http://www.linkedin.com/in/harshal-khade-bb6285213 )
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black)](https://github.com/harshal494 )

---

> ⭐ If you find this project interesting, consider giving it a star!
