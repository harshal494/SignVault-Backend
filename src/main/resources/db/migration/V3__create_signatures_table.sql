CREATE TABLE signatures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL ,
    user_id BIGINT NOT NULL ,
    role VARCHAR(50),
    fingerprint_sha256 VARCHAR(255),
    signed_at DATETIME(6),

    CONSTRAINT fk_contract FOREIGN KEY (contract_id) REFERENCES contracts(id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
)