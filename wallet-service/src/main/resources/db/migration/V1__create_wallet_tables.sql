CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_wallets_user_id ON wallets(user_id);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    source_wallet_id UUID,
    destination_wallet_id UUID,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    previous_hash VARCHAR(64),
    current_hash VARCHAR(64) NOT NULL,
    nonce BIGINT NOT NULL,
    difficulty INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_transactions_source_wallet_id ON transactions(source_wallet_id);
CREATE INDEX idx_transactions_destination_wallet_id ON transactions(destination_wallet_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE UNIQUE INDEX uk_transactions_idempotency_key ON transactions(idempotency_key);
