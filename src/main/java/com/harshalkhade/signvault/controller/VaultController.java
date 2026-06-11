package com.harshalkhade.signvault.controller;

import com.harshalkhade.signvault.dto.response.ApiResponse;
import com.harshalkhade.signvault.dto.response.ContractResponse;
import com.harshalkhade.signvault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
@Slf4j
public class VaultController {

    private final VaultService vaultService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> getVault(Authentication authentication) {
        String email = authentication.getName();
        log.info("Incoming request to get vault with email {}", email);
        List<ContractResponse> result = vaultService.getVault(email);
        return ResponseEntity.ok(new ApiResponse(true, "Vault fetched successfully", result));
    }

    @GetMapping("/{contractId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> getVaultContract(@PathVariable String contractId, Authentication authentication) {
        String email = authentication.getName();
        log.info("Incoming request to get vault contract with email {}", email);
        ContractResponse result = vaultService.getVaultContract(email, contractId);
        return ResponseEntity.ok(new ApiResponse(true, "Vault contract fetched successfully", result));
    }
}
