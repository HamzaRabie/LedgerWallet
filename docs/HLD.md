# High-Level Design

## 1. Overview

The Secure Digital Wallet & Payment Ledger Platform is a backend system for user authentication, wallet operations, payment transactions, transaction history, and notifications.

The system is designed as a group of backend services. The current implementation starts with the User Service, which handles registration, login, JWT generation, JWT validation, and protected user endpoints. Future services include Wallet Service, Notification Service, and API Gateway.

The architecture focuses on secure authentication, clear service boundaries, reliable payment processing, event-driven communication, and auditable transaction records.

## 2. Goals

- Provide secure user registration and login
- Issue JWT access tokens for protected APIs
- Support wallet deposits, withdrawals, and transfers
- Maintain accurate wallet balances and transaction history
- Prevent duplicate payment processing using idempotency
- Handle concurrent wallet updates safely
- Publish wallet events reliably for notifications
- Keep ledger records auditable and tamper-evident

## 3. System Context

Clients call backend APIs through REST endpoints. In the final architecture, an API Gateway acts as the single entry point and routes requests to internal services.

The User Service owns user identity, credentials, profile data, JWT generation, and JWT-based request authentication. The Wallet Service owns wallet balances, payment operations, transactions, idempotency, optimistic locking, and outbox events. The Notification Service consumes wallet events and stores notification delivery records.

Supporting infrastructure includes PostgreSQL for durable storage, Kafka for asynchronous messaging, Redis for caching, AWS S3 for profile image storage, and Docker for local development.

## 4. Core Components

- API Gateway
- User Service
- Wallet Service
- Notification Service
- Outbox Publisher
- PostgreSQL
- Redis
- Kafka
- AWS S3

## 5. Service Responsibilities

### API Gateway

- Provides a single external entry point
- Routes requests to internal services
- Forwards authentication headers
- Can support rate limiting and request filtering

### User Service

- Handles user registration and login
- Hashes passwords using BCrypt
- Issues JWT access tokens
- Validates JWT tokens through a security filter
- Stores user profile information
- Stores profile image keys for AWS S3
- Provides user lookup APIs for other services later

### Wallet Service

- Creates and manages user wallets
- Handles deposits, withdrawals, and wallet-to-wallet transfers
- Maintains wallet balances
- Records transaction history
- Stores idempotency keys in transaction records
- Uses optimistic locking to protect concurrent updates
- Creates tamper-evident transaction hashes
- Writes wallet events to the outbox table

### Outbox Publisher

- Reads pending events from the outbox table
- Publishes wallet events to Kafka
- Marks events as published after successful delivery

### Notification Service

- Consumes wallet events from Kafka
- Stores notification records
- Sends notifications through supported channels such as email or webhook

## 6. Communication Patterns

The system uses synchronous REST communication for direct user requests such as registration, login, wallet balance lookup, deposits, withdrawals, and transfers.

The system uses asynchronous event-driven communication for background work such as notification delivery after wallet events.

Kafka decouples wallet processing from notification delivery so payment operations do not depend on notification success.

## 7. Authentication Flow

### Register

1. Client sends registration data to the User Service.
