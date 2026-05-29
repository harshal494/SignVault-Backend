package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.entity.AuditLog;
import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Contract contract, User user, String action, String ipAdress) {

        AuditLog auditLog = AuditLog.builder()
                .contract(contract)
                .user(user)
                .action(action)
                .ipAddress(ipAdress)
                .build();

        auditLogRepository.save(auditLog);

        log.info("AuditLog has been saved");
    }
}
