package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.dto.response.ContractResponse;
import com.harshalkhade.signvault.entity.Contract;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.ContractStatus;
import com.harshalkhade.signvault.exception.ContractException;
import com.harshalkhade.signvault.exception.ResourceNotFoundException;
import com.harshalkhade.signvault.exception.UnauthorizedException;
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
public class VaultService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    public List<ContractResponse> getVault(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Contract> contracts = contractRepository.findBySenderOrReceiver(user, user);
        log.info("Fetching vault for {}", email);
        List<ContractResponse> result = contracts.stream()
                .filter(contract -> contract.getStatus() == ContractStatus.FULLY_SIGNED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return result;
    }

    public ContractResponse getVaultContract(String email, String contractId) {
        Contract contract = contractRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(contract.getSender().getEmail()) && !user.getEmail().equals(contract.getReceiver().getEmail())){
            throw new UnauthorizedException("You are not authorized to access this contract");
        }

        if (!ContractStatus.FULLY_SIGNED.equals(contract.getStatus())) {
            throw new ContractException("This contract is not in the vault yet");
        }
        log.info("Fetching vault for {}", email);
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

