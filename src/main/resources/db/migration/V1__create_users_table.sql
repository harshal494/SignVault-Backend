CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL ,
    email VARCHAR(255) NOT NULL UNIQUE ,
    phone VARCHAR(20) NOT NULL UNIQUE ,
    password_hash VARCHAR(255),
    fingerprint_bcrypt VARCHAR(255),
    fingerprint_sha256 VARCHAR(255),
    role VARCHAR(50),
    auth_provider VARCHAR(50),
    google_id VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    profile_complete BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    age INT,
    created_at DATETIME(6),
    updated_at DATETIME(6)
)