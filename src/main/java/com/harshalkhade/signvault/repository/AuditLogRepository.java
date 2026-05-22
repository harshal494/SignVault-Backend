package com.harshalkhade.signvault.repository;

import com.harshalkhade.signvault.entity.AuditLog;
import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByContract(Contract contract);
    List<AuditLog> findByUser(User user);
}
