# Low-Level Design

## 1. Overview

This document describes the low-level design of the Secure Digital Wallet & Payment Ledger Platform.

The current implementation focuses on the User Service. The planned services are:

- User Service
- Wallet Service
- Notification Service

The API Gateway acts as the external entry point but does not own business tables.

---

## 2. User Service Package Structure

The User Service uses a package-based layered structure.

```text
com.ledgerwallet.userservice
  application
    common
      AppError
      Result
      UserErrors
    dtos
      RegisterRequest
      LoginRequest
      AuthResponse
      UserResponse
    mappers
      UserMapper
    services
      AuthService
      JwtService
      UserService

  domain
    model
      BaseEntity
      User
      UserStatus

  infrastructure
    persistence
      repository
        UserRepository
    security
      JwtAuthenticationFilter
      JwtServiceImpl
      PasswordConfig
      SecurityConfig
    services
      AuthServiceImpl

  presentation
    controllers
      AuthController
      UserController
```

---

## 3. Shared Base Entity

`BaseEntity` is inherited by JPA entities.

| Field | Description |
|---|---|
| id | UUID primary key |
| createdAt | Record creation timestamp |
| updatedAt | Last update timestamp |

Timestamp behavior:

- `createdAt` and `updatedAt` are set before insert.
- `updatedAt` is refreshed before update.

---

## 4. User Service

### Responsibility

The User Service manages identity, authentication, profile data, password hashing, JWT generation, JWT validation, and protected user endpoints.

### Table: users

| Field | Description |
|---|---|
| id | UUID primary key |
| firstName | User first name |
| lastName | User last name |
| phone | User phone number |
| email | User email address |
| username | Unique username |
| passwordHash | BCrypt hashed password |
| profileImageKey | S3 object key for profile image |
| status | User account status |
| createdAt | Created timestamp |
| updatedAt | Updated timestamp |

### Indexes

- Primary key on `id`
- Unique index on `email`
- Unique index on `phone`
- Unique index on `username`

### UserStatus

```text
ACTIVE
INACTIVE
DELETED
```

---

## 5. User Service DTOs

### RegisterRequest

```text
firstName
lastName
phone
email
username
password
```

### LoginRequest

```text
emailOrUsername
password
```

### AuthResponse

```text
accessToken
user
```

### UserResponse

```text
id
firstName
lastName
phone
email
username
status
createdAt
updatedAt
```

`passwordHash` is never returned in response DTOs.

---

## 6. Result Pattern

Application services return `Result<T>`.

### Result

```text
success
data
error
```

### AppError

```text
code
message
```

### UserErrors

```text
EMAIL_ALREADY_EXISTS
USERNAME_ALREADY_EXISTS
PHONE_ALREADY_EXISTS
INVALID_CREDENTIALS
USER_NOT_FOUND
INACTIVE_USER
```

Repository methods can return `Optional`, while application services return `Result`.

---

## 7. Authentication Design

### AuthService

```text
register(RegisterRequest) -> Result<AuthResponse>
login(LoginRequest) -> Result<AuthResponse>
```

### Register Flow

1. Check username uniqueness.
2. Check email uniqueness.
3. Check phone uniqueness.
4. Hash password using `PasswordEncoder`.
5. Map request to `User`.
6. Save user.
7. Generate JWT using saved user.
8. Return `AuthResponse`.

### Login Flow

1. Find user by email.
2. If not found, find user by username.
3. If still not found, return `INVALID_CREDENTIALS`.
4. Validate password using `passwordEncoder.matches`.
5. Check user status is `ACTIVE`.
6. Generate JWT.
7. Return `AuthResponse`.

Login returns `INVALID_CREDENTIALS` for missing users or wrong passwords to avoid revealing whether an account exists.

---

## 8. JWT Design

### JwtService

```text
generateToken(User) -> String
validateToken(String) -> boolean
extractUserId(String) -> String
```

### JWT Payload

```text
subject = user id
username
email
phone
issuedAt
expiration
```

The JWT is signed with a configured secret key.

---

## 9. Security Filter Design

### SecurityConfig

Security rules:

```text
/auth/register -> public
/auth/login -> public
all other endpoints -> authenticated
```

Security configuration:

- CSRF disabled for REST APIs
- HTTP Basic disabled
- Form login disabled
- Stateless sessions
- JWT filter added before `UsernamePasswordAuthenticationFilter`

### JwtAuthenticationFilter

The filter runs once per request.

Flow:

1. Read `Authorization` header.
2. If missing or not `Bearer`, continue without authentication.
3. Extract token.
4. Validate token.
5. Extract user id from token subject.
6. Create `UsernamePasswordAuthenticationToken`.
7. Store authentication in `SecurityContextHolder`.
8. Continue filter chain.

Current authorities are empty because role-based authorization is not added yet.

---

## 10. User API Endpoints

### AuthController

```text
POST /auth/register
POST /auth/login
```

Register returns `201 Created`.

Login returns `200 OK`.

### UserController

```text
GET /users/me/test
```

This is a protected test endpoint used to verify JWT authentication.

---

## 11. Wallet Service

### Responsibility

The Wallet Service manages wallet balances, deposits, withdrawals, wallet-to-wallet transfers, transaction history, idempotency, optimistic locking, tamper-evident transaction hashes, and outbox events.

### Table: wallets

| Field | Description |
|---|---|
| id | Primary key |
| userId | Owner user ID |
| balance | Current wallet balance |
| currency | Wallet currency |
| version | Optimistic locking version |
| createdAt | Created timestamp |
| updatedAt | Updated timestamp |

### Indexes

- Primary key on `id`
- Unique index on `userId`

---

### Table: transactions

| Field | Description |
|---|---|
| id | Primary key |
| type | `DEPOSIT`, `WITHDRAWAL`, or `TRANSFER` |
| sourceUserId | Sender user ID |
| destinationUserId | Receiver user ID |
| amount | Transaction amount |
| status | Transaction status |
| idempotencyKey | Unique key used to prevent duplicate processing |
| previousHash | Previous transaction hash |
| currentHash | Current transaction hash |
| createdAt | Created timestamp |
| updatedAt | Updated timestamp |

### Notes

- For `DEPOSIT`, `sourceUserId` is null.
- For `WITHDRAWAL`, `destinationUserId` is null.
- For `TRANSFER`, both `sourceUserId` and `destinationUserId` are present.
- `idempotencyKey` is stored directly in the transaction table.
- `currentHash` is generated using transaction data and `previousHash`.

### Indexes

- Primary key on `id`
- Index on `sourceUserId`
- Index on `destinationUserId`
- Index on `type`
- Index on `status`
- Unique index on `idempotencyKey`

---

### Table: outbox_events

| Field | Description |
|---|---|
| id | Primary key |
| aggregateId | Related transaction or wallet operation ID |
| eventType | Event name |
| payload | Event data stored as JSON |
| status | Event publishing status |
| publishedAt | Timestamp when event was published |
| createdAt | Created timestamp |
| updatedAt | Updated timestamp |

### Indexes

- Primary key on `id`
- Index on `aggregateId`
- Index on `eventType`
- Index on `status`

---

## 12. Notification Service

### Responsibility

The Notification Service consumes wallet events and stores notification records.

### Table: notifications

| Field | Description |
|---|---|
| id | Primary key |
| userId | User receiving the notification |
| eventId | Related event ID |
| notificationBody | Notification message |
| status | Notification status |
| channel | Notification channel, such as `EMAIL` or `WEBHOOK` |
| sentAt | Timestamp when notification was sent |
| createdAt | Created timestamp |
| updatedAt | Updated timestamp |

### Indexes

- Primary key on `id`
- Index on `userId`
- Index on `eventId`
- Index on `status`

---

## 13. Core Payment Flow

### Deposit

1. User sends deposit request with an idempotency key.
2. Wallet Service checks if a transaction with the same idempotency key already exists.
3. Wallet balance is increased.
4. Transaction record is created.
5. Transaction hash is generated.
6. Outbox event is saved.
7. Event is later published to Kafka.
8. Notification Service stores and sends the notification.

### Withdrawal

1. User sends withdrawal request with an idempotency key.
2. Wallet Service checks if the idempotency key already exists.
3. Wallet Service validates available balance.
4. Wallet balance is decreased using optimistic locking.
5. Transaction record is created.
6. Transaction hash is generated.
7. Outbox event is saved.
8. Notification is sent asynchronously.

### Transfer

1. User sends transfer request with receiver information and idempotency key.
2. Wallet Service checks if the idempotency key already exists.
3. Sender wallet and receiver wallet are loaded.
4. Sender balance is validated.
5. Sender wallet is debited.
6. Receiver wallet is credited.
7. Transfer transaction is created.
8. Transaction hash is generated.
9. Outbox event is saved.
10. Notification Service sends transfer notifications asynchronously.

---

## 14. Idempotency Design

The system stores the `idempotencyKey` directly inside the `transactions` table.

Before processing a payment operation, the Wallet Service checks whether a transaction already exists with the same idempotency key.

If it exists, the service returns the existing result instead of creating a duplicate transaction.

---

## 15. Optimistic Locking Design

The `wallets` table contains a `version` field.

When two requests try to update the same wallet at the same time, optimistic locking prevents both updates from being applied incorrectly.

If a version conflict happens, the request can fail with a conflict response or be retried.

---

## 16. Tamper-Evident Ledger Design

Each transaction stores:

- `previousHash`
- `currentHash`

The `currentHash` is generated from transaction data and the previous transaction hash.

This creates a hash chain, so changing an old transaction makes later hashes invalid.

---

## 17. Transactional Outbox Design

The Wallet Service does not publish Kafka events directly inside the payment operation.

Instead, it saves an event in the `outbox_events` table in the same database transaction as the wallet and transaction updates.

A separate publisher reads pending outbox events and publishes them to Kafka.

This ensures that payment changes and event creation happen atomically.
