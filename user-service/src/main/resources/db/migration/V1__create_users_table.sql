CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       phone VARCHAR(30) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       username VARCHAR(100) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       profile_image_key VARCHAR(500),
                       status VARCHAR(30) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_users_email ON users(email);
CREATE UNIQUE INDEX uk_users_phone ON users(phone);
CREATE UNIQUE INDEX uk_users_username ON users(username);