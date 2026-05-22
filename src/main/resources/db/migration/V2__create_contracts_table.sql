CREATE TABLE contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id VARCHAR(100) NOT NULL UNIQUE ,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL ,
    title VARCHAR(255) NOT NULL ,
    `status` VARCHAR(50),
    file_hash VARCHAR(255),
    period_type VARCHAR(50),
    period_value INT,
    period_from DATE,
    period_to DATE,
    permanent BOOLEAN DEFAULT FALSE,
    parent_contract_id BIGINT,
    renewal BOOLEAN DEFAULT FALSE,
    renewal_done BOOLEAN DEFAULT FALSE,
    expires_at DATETIME(6),
    created_at DATETIME(6),

    CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES users(id),
    CONSTRAINT fk_parent_contract FOREIGN KEY (parent_contract_id) REFERENCES contracts(id)

)