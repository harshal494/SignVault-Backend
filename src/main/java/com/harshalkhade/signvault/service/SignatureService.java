package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.dto.request.SignContractRequest;
import com.harshalkhade.signvault.dto.response.ContractResponse;
import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.Signature;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.ContractStatus;
import com.harshalkhade.signvault.enums.NotificationType;
import com.harshalkhade.signvault.enums.SignatureRole;
import com.harshalkhade.signvault.exception.ContractException;
import com.harshalkhade.signvault.exception.ResourceNotFoundException;
import com.harshalkhade.signvault.exception.UnauthorizedException;
import com.harshalkhade.signvault.repository.ContractRepository;
import com.harshalkhade.signvault.repository.SignatureRepository;
import com.harshalkhade.signvault.repository.UserRepository;
import com.harshalkhade.signvault.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignatureService {

    private final SignatureRepository signatureRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public ContractResponse signContract(SignContractRequest request, String email, String ipAddress) {
        Contract contract = contractRepository.findByContractId(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(contract.getSender().getEmail()) && !user.getEmail().equals(contract.getReceiver().getEmail()))
            throw new UnauthorizedException("You are not authorized to sign this contract");

        if (contract.getStatus() == ContractStatus.FULLY_SIGNED || contract.getStatus() == ContractStatus.RENEWED || contract.getStatus() == ContractStatus.EXPIRED)
            throw new ContractException("Contract is no longer available for signing");

        if (signatureRepository.existsByContractAndUser(contract, user))
            throw new ContractException("You have already signed this contract");

        SignatureRole role;
        if (user.getEmail().equals(contract.getSender().getEmail())) {
            role = SignatureRole.SENDER;
        } else {
            role = SignatureRole.RECEIVER;
        }

        if ( role == SignatureRole.SENDER && contract.getStatus() != ContractStatus.PENDING)
            throw new ContractException("Contract is not in signable state for sender");

        if ( role == SignatureRole.RECEIVER && contract.getStatus() != ContractStatus.SENDER_SIGNED)
            throw new ContractException("Sender must sign the contract first");

        String fingerprintHash;
        try {
             fingerprintHash = HashUtil.hashWithSHA256(request.getFingerprintString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("There is a problem hashing the fingerprint, try again");
        }

        Signature signature = Signature.builder()
                .contract(contract)
                .user(user)
                .role(role)
                .fingerprintSha256(fingerprintHash)
                .build();
        signatureRepository.save(signature);

        if (role == SignatureRole.SENDER) {
            contract.setStatus(ContractStatus.SENDER_SIGNED);
        }
        if (role == SignatureRole.RECEIVER) {
            contract.setStatus(ContractStatus.FULLY_SIGNED);
        }
        contractRepository.save(contract);

        auditLogService.log(contract, user, "CONTRACT_SIGNED", ipAddress);

        if (contract.getStatus() == ContractStatus.SENDER_SIGNED ) {
            notificationService.createNotification(
                    contract.getReceiver(),
                    contract,
                    NotificationType.CONTRACT_SIGNED,
                    "📝 "+ contract.getSender().getFullName() + " has signed the contract. It's your turn to sign."
            );
            emailService.sendContractNotification(
                    contract.getReceiver().getEmail(),
                    contract.getSender().getFullName(),
                    contract.getTitle(),
                    contract.getContractId());
        }

        if (contract.getStatus() == ContractStatus.FULLY_SIGNED) {
            notificationService.createNotification(
                    contract.getSender(),
                    contract,
                    NotificationType.CONTRACT_SIGNED,
                    "✅ Contract fully signed by both parties."
            );

            notificationService.createNotification(
                    contract.getReceiver(),
                    contract,
                    NotificationType.CONTRACT_SIGNED,
                    "✅ Contract fully signed by both parties."
            );
            emailService.sendContractNotification(
                    contract.getSender().getEmail(),
                    contract.getSender().getFullName(),
                    contract.getTitle(),
                    contract.getContractId()
            );
            emailService.sendContractNotification(
                    contract.getReceiver().getEmail(),
                    contract.getReceiver().getFullName(),
                    contract.getTitle(),
                    contract.getContractId()
            );
        }

        log.info("Contract signed by {} with role {}", email, role);

        return mapToResponse(contract);
    }

    private ContractResponse mapToResponse(Contract contract) {

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
}
