package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.dto.request.CreateContractRequest;
import com.harshalkhade.signvault.dto.response.ContractResponse;
import com.harshalkhade.signvault.dto.response.VerifyContractResponse;
import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.ContractFile;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.ContractStatus;
import com.harshalkhade.signvault.enums.NotificationType;
import com.harshalkhade.signvault.enums.PeriodType;
import com.harshalkhade.signvault.exception.ContractException;
import com.harshalkhade.signvault.exception.ResourceNotFoundException;
import com.harshalkhade.signvault.exception.UnauthorizedException;
import com.harshalkhade.signvault.repository.ContractFileRepository;
import com.harshalkhade.signvault.repository.ContractRepository;
import com.harshalkhade.signvault.repository.UserRepository;
import com.harshalkhade.signvault.util.ContractIdGenerator;
import com.harshalkhade.signvault.util.DateUtil;
import com.harshalkhade.signvault.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractFileRepository contractFileRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final ContractIdGenerator contractIdGenerator;
    private final DateUtil dateUtil;

    private record UploadResult(String url, String hash) {}

    private UploadResult uploadPdf(MultipartFile file, String email) throws Exception {
        byte[] bytes = file.getBytes();
        String url = cloudinaryService.uploadPdf(bytes);
        String hash = HashUtil.hashWithSHA256(url);
        return new UploadResult(url, hash);
    }

    public ContractResponse createAndSend(CreateContractRequest request, MultipartFile file, String senderEmail, String ipAddress) throws Exception {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (sender.getEmail().equals(receiver.getEmail()))
            throw new ContractException("Sender and receiver cannot be the same");

        UploadResult upload = uploadPdf(file, senderEmail);

        LocalDate periodTo = switch (request.getPeriodType()) {
            case DAYS -> request.getPeriodFrom().plusDays(request.getPeriodValue());
            case MONTHS -> request.getPeriodFrom().plusMonths(request.getPeriodValue());
            case YEARS -> request.getPeriodFrom().plusYears(request.getPeriodValue());
            case PERMANENT -> null;
        };

        LocalDateTime expiresAt = dateUtil.calculateExpiryDate(request.getPeriodType(),request.getPeriodValue());

        Contract contract = Contract.builder()
                .contractId(contractIdGenerator.generateContractId())
                .sender(sender)
                .receiver(receiver)
                .title(request.getTitle())
                .status(ContractStatus.PENDING)
                .fileHash(upload.hash())
                .periodType(request.getPeriodType())
                .periodValue(request.getPeriodValue())
                .periodFrom(request.getPeriodFrom())
                .periodTo(periodTo)
                .permanent(request.getPeriodType() == PeriodType.PERMANENT)
                .expiresAt(expiresAt)
                .renewal(false)
                .renewalDone(false)
                .build();

        contractRepository.save(contract);
        log.info("Contract created: {} by sender: {}", contract.getContractId(), senderEmail);

        ContractFile contractFile = ContractFile.builder()
                .contract(contract)
                .cloudinaryUrl(upload.url())
                .originalHash(upload.hash())
                .build();

        contractFileRepository.save(contractFile);
        log.info("ContractFile saved for contractId: {}", contract.getContractId());

        auditLogService.log(contract, sender, "CONTRACT_CREATED", ipAddress);
        notificationService.createNotification(
                receiver,
                contract,
                NotificationType.CONTRACT_RECEIVED,
                "📄 New contract received from " + sender.getFullName() + ". Please review and sign it."
        );
        emailService.sendContractNotification(request.getReceiverEmail(), receiver.getFullName(), contract.getTitle(), contract.getContractId());
        log.info("Contract notification sent to receiver: {}", receiver.getEmail());

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

    private VerifyContractResponse mapToVerifyResponse(Contract contract) {
        return VerifyContractResponse.builder()
                .contractId(contract.getContractId())
                .title(contract.getTitle())
                .status(contract.getStatus())
                .createdAt(contract.getCreatedAt())
                .verified(true)
                .build();
    }

    public ContractResponse getContract(String contractId, String email) {

        Contract contract = contractRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!user.getEmail().equals(contract.getSender().getEmail()) && !user.getEmail().equals(contract.getReceiver().getEmail()))
                throw new UnauthorizedException("You are not authorized to view this contract");

        return mapToResponse(contract);
    }

    public VerifyContractResponse verifyContract(String contractId ) {
        Contract contract = contractRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));

        return mapToVerifyResponse(contract);
    }

}
