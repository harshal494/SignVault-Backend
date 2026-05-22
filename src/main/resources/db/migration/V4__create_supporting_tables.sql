CREATE TABLE contract_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL UNIQUE ,
    cloudinary_url VARCHAR(500) NOT NULL ,
    original_hash VARCHAR(255) NOT NULL ,
    uploaded_at DATETIME(6),

    CONSTRAINT fk_cf_contract FOREIGN KEY (contract_id) REFERENCES contracts(id)
);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL ,
    user_id BIGINT NOT NULL ,
    action VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50),
    metadata TEXT,
    created_at DATETIME(6),

    CONSTRAINT fk_al_contract FOREIGN KEY (contract_id) REFERENCES contracts(id),
    CONSTRAINT fk_al_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL ,
    contract_id BIGINT ,
    type VARCHAR(100),
    message TEXT NOT NULL ,
    `read` BOOLEAN DEFAULT FALSE,
    email_sent BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6),

    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE otp_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL ,
    `type` VARCHAR(50),
    otp_hash VARCHAR(255) NOT NULL ,
    verified BOOLEAN DEFAULT FALSE ,
    expires_at DATETIME(6) NOT NULL ,
    created_at DATETIME(6),

    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id)
);