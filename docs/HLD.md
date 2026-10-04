# High-Level Design

## 1. Overview

The Secure Digital Wallet & Payment Ledger Platform is a backend system designed to handle user authentication, wallet operations, payment transactions, and notifications in a reliable and secure way.

The system uses an API Gateway as the single entry point for client requests. Core business capabilities are separated into services such as Auth/User Service, Wallet Service, and Notification Service. Persistent data is stored in PostgreSQL, frequently accessed data can be cached in Redis, wallet events are published through Kafka, and user profile files are stored in AWS S3.

The architecture focuses on consistency, reliability, and auditability by using patterns such as idempotent APIs, optimistic locking, transactional outbox, idempotent event consumers, and tamper-evident ledger records.

## 2. Goals

- Provide secure user authentication and authorization
- Support wallet deposits, withdrawals, and transfers
- Maintain accurate wallet balances and transaction history
- Prevent duplicate payment processing using idempotency
- Handle concurrent wallet updates safely
- Publish wallet events reliably for notifications
- Keep ledger records auditable and tamper-evident

## 3. System Context

Clients interact with the platform through the API Gateway. The gateway routes requests to internal backend services based on the requested resource.

The Auth/User Service handles registration, login, JWT generation, user profile management, and profile image storage. The Wallet Service handles wallet balances, deposits, withdrawals, transfers, transaction records, and ledger integrity. The Notification Service consumes wallet events from Kafka and sends email or webhook notifications.

Supporting infrastructure includes PostgreSQL for durable storage, Redis for caching, Kafka for asynchronous messaging, AWS S3 for file storage, and Docker for local development.

## 4. Core Components

- API Gateway
- Auth/User Service
- Wallet Service
- Notification Service
- Outbox Publisher
- PostgreSQL
- Redis
- Kafka
- AWS S3

## 5. Service Responsibilities

### API Gateway

- Exposes a single entry point for clients
- Routes requests to internal services
- Validates or forwards authentication headers
- Can support rate limiting and request filtering

### Auth/User Service

- Handles user registration and login
- Issues JWT access tokens
- Stores user profile information
- Uploads and stores profile images in AWS S3
- Provides user identity data to other services

### Wallet Service

- Creates and manages user wallets
- Handles deposits, withdrawals, and wallet-to-wallet transfers
- Maintains wallet balances
- Records transaction history
- Applies idempotency checks for payment operations
- Uses optimistic locking to protect concurrent updates
- Creates tamper-evident ledger records
- Writes wallet events to the outbox table

### Outbox Publisher

- Reads pending events from the outbox table
- Publishes wallet events to Kafka
- Marks events as published after successful delivery

### Notification Service

- Consumes wallet events from Kafka
- Sends notifications through email or webhook channels
- Uses idempotent consumers to avoid duplicate notification processing
- Stores notification delivery status

## 6. Communication Patterns

The system uses two communication styles:

- **Synchronous REST communication** for direct user requests such as login, wallet balance lookup, deposits, withdrawals, and transfers.
- **Asynchronous event-driven communication** for background processes such as sending notifications after wallet events occur.

Kafka is used to decouple the Wallet Service from the Notification Service so payment processing does not depend on notification delivery.

## 7. Main User Flows

### Registration and Login

1. User sends registration or login request through the API Gateway.
2. API Gateway routes the request to the Auth/User Service.
3. Auth/User Service validates credentials and issues a JWT token.
4. Client uses the JWT token for protected requests.

### Deposit

1. User sends a deposit request with an idempotency key.
2. Wallet Service validates the request and checks if the idempotency key was already used.
3. Wallet balance is updated inside a database transaction.
4. A ledger record and outbox event are saved.
5. Outbox Publisher later publishes the event to Kafka.
6. Notification Service consumes the event and sends a notification.

### Withdrawal

1. User sends a withdrawal request with an idempotency key.
2. Wallet Service validates balance availability.
3. Wallet balance is updated using optimistic locking.
4. A ledger record and outbox event are saved.
5. Notification is sent asynchronously through Kafka.

### Transfer

1. User sends a transfer request with receiver information and an idempotency key.
2. Wallet Service validates the sender wallet, receiver wallet, and available balance.
3. Sender wallet is debited and receiver wallet is credited inside one database transaction.
4. Ledger records are created for both wallets.
5. An outbox event is saved and later published to Kafka.
6. Notification Service sends transfer notifications.

## 8. Data Storage

- **PostgreSQL** stores users, wallets, transactions, ledger records, idempotency keys, outbox events, and notifications.
- **Redis** stores cached data such as user or wallet lookup results and can support rate limiting.
- **AWS S3** stores user profile images.
- **Kafka** stores wallet events temporarily for asynchronous processing by consumers.

## 9. Reliability and Consistency

- Idempotency keys prevent duplicate processing of repeated payment requests.
- Optimistic locking protects wallet balances during concurrent updates.
- Database transactions ensure wallet updates and ledger records are saved atomically.
- Transactional outbox ensures wallet events are not lost after successful payment operations.
- Idempotent Kafka consumers prevent duplicate notification handling.
- Chained SHA-256 hashes make ledger tampering detectable.

## 10. Security

- JWT is used for authenticating protected API requests.
- Passwords are stored as secure hashes.
- Sensitive operations require authenticated users.
- Request validation is applied to payment APIs.
- AWS S3 is used for controlled profile image storage.
- Services should avoid exposing internal implementation details through error responses.

## 11. Deployment View

The system can be deployed using Dockerized services. Each backend service runs as a separate container, while PostgreSQL, Redis, and Kafka run as supporting infrastructure containers in local development.

In production, managed services can be used for PostgreSQL, Redis, Kafka, and AWS S3.

## 12. Tradeoffs and Future Improvements

- Start with one wallet per user, then extend to multi-currency wallets later.
- Keep ledger logic inside the Wallet Service first, then extract it into a separate Ledger Service if needed.
- Add distributed tracing and metrics for better observability.
- Add rate limiting to protect payment APIs.
- Add webhook retry policies for failed notification deliveries.
- Add admin tools for auditing and support operations.