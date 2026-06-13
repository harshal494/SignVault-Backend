package com.harshalkhade.signvault.controller;

import com.harshalkhade.signvault.dto.request.CreateContractRequest;
import com.harshalkhade.signvault.dto.response.ApiResponse;
import com.harshalkhade.signvault.dto.response.ContractResponse;
import com.harshalkhade.signvault.dto.response.VerifyContractResponse;
import com.harshalkhade.signvault.service.ContractService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts")
@Slf4j

public class ContractController {

    private final ContractService contractService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> createAndSend(
            @RequestPart("data") @Valid CreateContractRequest request,
            @Parameter(description = "Contract PDF file", required = true)
            @RequestPart("file")
            @Schema(type = "string", format = "binary")
            MultipartFile file,
            Authentication authentication,
            HttpServletRequest httpRequest) throws Exception {

        String email = authentication.getName();
        log.info("Incoming request: createAndSend from {}", email);
        String ip = httpRequest.getRemoteAddr();
        ContractResponse result = contractService.createAndSend(request, file, email, ip);
        return ResponseEntity.ok(new ApiResponse(true, "Contract created and sent successfully", result));

    }

    @GetMapping("/{contractId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> getContract(
            @PathVariable String contractId,
            Authentication authentication) throws Exception {

        String email = authentication.getName();
        log.info("Incoming request: getContract {} by {}", contractId, email);
        ContractResponse result = contractService.getContract(contractId, email);
        return ResponseEntity.ok(new ApiResponse(true, "Contract fetched successfully", result));
    }

    @GetMapping("/verify/{contractId}")
    public ResponseEntity<ApiResponse> verifyContract(
            @PathVariable String contractId) {

        log.info("Incoming request: verifyContract {}", contractId);
        VerifyContractResponse result = contractService.verifyContract(contractId);
        return ResponseEntity.ok(new ApiResponse(true, "Contract verified successfully", result));
    }

    @PutMapping("/{contractId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> cancelContract(@PathVariable String contractId, Authentication authentication, HttpServletRequest httpRequest) {
        String email = authentication.getName();
        log.info("Incoming request: cancelContract {} by {}", contractId, email);
        ContractResponse result = contractService.cancelContract(contractId, email, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(new ApiResponse(true, "Contract cancelled successfully", result));
    }

    @PutMapping("/{contractId}/reject")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> rejectContract(@PathVariable String contractId, Authentication authentication, HttpServletRequest httpRequest) {
        String email = authentication.getName();
        log.info("Incoming request: rejectContract {} by {}", contractId, email);
        ContractResponse result = contractService.rejectContract(contractId, email, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(new ApiResponse(true, "Contract rejected successfully", result));
    }
}
