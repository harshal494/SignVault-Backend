CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_google_id ON users(google_id);

CREATE INDEX idx_contracts_contract_id ON contracts(contract_id);
CREATE INDEX idx_contracts_sender_id ON contracts(sender_id);
CREATE INDEX idx_contracts_receiver_id ON contracts(receiver_id);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contracts_expires_at ON contracts(expires_at);

CREATE INDEX idx_signatures_contract_id ON signatures(contract_id);
CREATE INDEX idx_signatures_user_id ON signatures(user_id);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);

CREATE INDEX idx_otp_verifications_user_id ON otp_verifications(user_id);