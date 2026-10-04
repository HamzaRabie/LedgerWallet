# Low-Level Design

## 1. Overview

This document describes the low-level design of the Secure Digital Wallet & Payment Ledger Platform, including service responsibilities, database tables, entity fields, indexes, and core payment flows.

The system is divided into three main backend services:

- User Service
- Wallet Service
- Notification Service

The API Gateway acts as the entry point but does not own business tables.

---

## 2. Shared Base Entity

All main database entities extend a shared base entity.

### BaseEntity

| Field | Description |
|---|---|
| id | Primary key |
| createdAt | Record creation timestamp |
| updatedAt | Last update timestamp |

---

## 3. User Service

### Responsibility

The User Service manages user identity, authentication data, profile information, and profile image references.

### Table: users

| Field | Description |
|---|---|
| id | Primary key |
| firstName | User first name |
| lastName | User last name |
| phone | User phone number |
| email | User email address |
| username | Unique username |
| passwordHash | Hashed password |
| profileImageKey | S3 object key for the profile image |
| status | User account status |
| createdAt | Created timestamp |
| updatedAt | Updated timestamp |

### Indexes

- Primary key on `id`
- Unique index on `email`
- Unique index on `phone`
- Unique index on `username`

---

## 4. Wallet Service

### Responsibility

The Wallet Service manages wallet balances, deposits, withdrawals, wallet-to-wallet transfers, transaction history, idempotency, optimistic locking, tamper-evident transaction hashes, and outbox events.

---

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

## 5. Notification Service

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

## 6. Core Payment Flow

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

## 7. Idempotency Design

The system stores the `idempotencyKey` directly inside the `transactions` table.

Before processing a payment operation, the Wallet Service checks whether a transaction already exists with the same idempotency key.

If it exists, the service returns the existing result instead of creating a duplicate transaction.

---

## 8. Optimistic Locking Design

The `wallets` table contains a `version` field.

When two requests try to update the same wallet at the same time, optimistic locking prevents both updates from being applied incorrectly.

If a version conflict happens, the request can fail with a conflict response or be retried.

---

## 9. Tamper-Evident Ledger Design

Each transaction stores:

- `previousHash`
- `currentHash`

The `currentHash` is generated from transaction data and the previous transaction hash.

This creates a hash chain, so changing an old transaction makes later hashes invalid.

---

## 10. Transactional Outbox Design

The Wallet Service does not publish Kafka events directly inside the payment operation.

Instead, it saves an event in the `outbox_events` table in the same database transaction as the wallet and transaction updates.

A separate publisher reads pending outbox events and publishes them to Kafka.

This ensures that payment changes and event creation happen atomically.