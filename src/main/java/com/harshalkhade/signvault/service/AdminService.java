package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.dto.response.AuditLogResponse;
import com.harshalkhade.signvault.dto.response.ContractResponse;
import com.harshalkhade.signvault.dto.response.UserResponse;
import com.harshalkhade.signvault.entity.AuditLog;
import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.ContractStatus;
import com.harshalkhade.signvault.enums.NotificationType;
import com.harshalkhade.signvault.enums.Role;
import com.harshalkhade.signvault.exception.ContractException;
import com.harshalkhade.signvault.exception.ResourceNotFoundException;
import com.harshalkhade.signvault.repository.AuditLogRepository;
import com.harshalkhade.signvault.repository.ContractRepository;
import com.harshalkhade.signvault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("Admin fetching all users.");
        List<UserResponse> result = users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        return result;
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        log.info("Admin fetching user id: {}", user.getId());
        return mapToUserResponse(user);
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
        log.info("Admin deactivated user with id: {}", user.getId());
    }

    public void promoteToAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ROLE_ADMIN ) {
            throw new ContractException("User is already admin");
        }
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
        log.info("User is promoted to admin with id: {}", user.getId());
    }

    public void demoteToUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ROLE_USER) {
            throw new ContractException("User is already regular user");
        }
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
        log.info("Admin is demoted to user with id: {}", user.getId());
    }

    public List<ContractResponse> getAllContracts() {
        List<Contract> contracts = contractRepository.findAll();
        log.info("Admin fetching all contracts.");
        List<ContractResponse> result = contracts.stream()
                .map(this::mapToContractResponse)
                .collect(Collectors.toList());
        return result;
    }

    public List<ContractResponse> getFlaggedContracts() {
        List<Contract> contracts = contractRepository.findAll();
        log.info("Admin fetching flagged contracts.");
        List<ContractResponse> result = contracts.stream()
                .filter(contract -> contract.isFlagged())
                .map(this::mapToContractResponse).toList();
        return result;
    }

    public ContractResponse flagContract(String contractId, String adminEmail) {
        Contract contract = contractRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        User adminUser =  userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (contract.isFlagged()){
            throw new ContractException("Contract is already flagged");
        }
        contract.setFlagged(true);
        contractRepository.save(contract);
        auditLogService.log(contract, adminUser, "CONTRACT_FLAGGED", null);

        notificationService.createNotification(contract.getSender(), contract, NotificationType.CONTRACT_FLAGGED, "Your contract has been flagged for review by admin.");
        notificationService.createNotification(contract.getReceiver(), contract, NotificationType.CONTRACT_FLAGGED, "Your contract has been flagged for review by admin.");
        log.info("Admin: {} flagged contract with id: {}", adminUser, contract.getId());
        return mapToContractResponse(contract);
    }

    public ContractResponse unflagContract(String contractId, String adminEmail) {
        Contract contract = contractRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!contract.isFlagged()){
            throw new ContractException("Contract is not flagged");
        }
        contract.setFlagged(false);
        contractRepository.save(contract);
        auditLogService.log(contract, adminUser, "CONTRACT_UNFLAGGED", null);
        log.info("Admin: {} unflagged contract with id: {}", adminUser, contract.getId());
        notificationService.createNotification(contract.getSender(), contract, NotificationType.CONTRACT_FLAGGED, "Your contract has been reviewed and unflagged by admin.");
        notificationService.createNotification(contract.getReceiver(), contract, NotificationType.CONTRACT_FLAGGED, "Your contract has been reviewed and unflagged by admin.");
        return mapToContractResponse(contract);
    }

    public ContractResponse cancelContract(String contractId, String adminEmail) {
        Contract contract = contractRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        User adminUser =  userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (contract.getStatus().equals(ContractStatus.CANCELLED)){
            throw new ContractException("Contract is already cancelled");
        }
        contract.setStatus(ContractStatus.CANCELLED);
        contract.setFlagged(false);
        contractRepository.save(contract);

        auditLogService.log(contract, adminUser, "CONTRACT_CANCELLED_BY_SUPERADMIN", null);
        notificationService.createNotification(contract.getSender(), contract,NotificationType.CONTRACT_CANCELLED, "Your contract has been cancelled by SuperAdmin");
        notificationService.createNotification(contract.getReceiver(), contract,NotificationType.CONTRACT_CANCELLED, "Your contract has been cancelled by SuperAdmin");

        emailService.sendContractNotification(contract.getSender().getEmail(), adminUser.getFullName(), contract.getTitle(),contractId);
        emailService.sendContractNotification(contract.getReceiver().getEmail(), adminUser.getFullName(), contract.getTitle(),contractId);
        log.info("Admin: {} cancelled contract with id: {}", adminUser, contract.getId());
        return mapToContractResponse(contract);
    }

    public List<AuditLogResponse> getAuditLogs() {
        log.info("Admin fetching all audit logs.");
        List<AuditLogResponse> logs = auditLogRepository.findAll()
                .stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
        return logs;

    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .profileComplete(user.isProfileComplete())
                .active(user.isActive())
                .age(user.getAge())
                .authProvider(user.getAuthProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private ContractResponse mapToContractResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .contractId(contract.getContractId())
                .title(contract.getTitle())
                .senderName(contract.getSender().getFullName())
                .receiverName(contract.getReceiver().getFullName())
                .status(contract.getStatus())
                .fileHash(contract.getFileHash())
                .periodType(contract.getPeriodType())
                .periodValue(contract.getPeriodValue())
                .periodFrom(contract.getPeriodFrom())
                .periodTo(contract.getPeriodTo())
                .permanent(contract.isPermanent())
                .renewal(contract.isRenewal())
                .parentContractId(contract.getParentContract() != null ? contract.getParentContract().getContractId() : null)
                .expiresAt(contract.getExpiresAt())
                .createdAt(contract.getCreatedAt())
                .build();
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .contractId(auditLog.getContract().getContractId())
                .contractTitle(auditLog.getContract().getTitle())
                .userId(auditLog.getUser().getId())
                .userFullname(auditLog.getUser().getFullName())
                .userEmail(auditLog.getUser().getEmail())
                .userRole(auditLog.getUser().getRole())
                .build();
    }
}
