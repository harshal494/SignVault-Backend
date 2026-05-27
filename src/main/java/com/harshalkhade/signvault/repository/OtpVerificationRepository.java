package com.harshalkhade.signvault.repository;

import com.harshalkhade.signvault.entity.OtpVerification;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByUserAndTypeAndVerified(User user, OtpType type, boolean verified);

    @Transactional
    void deleteByUser(User user);
}
