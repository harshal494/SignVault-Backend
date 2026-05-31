package com.harshalkhade.signvault.controller;

import com.harshalkhade.signvault.dto.request.SignContractRequest;
import com.harshalkhade.signvault.dto.response.ApiResponse;
import com.harshalkhade.signvault.dto.response.ContractResponse;
import com.harshalkhade.signvault.service.SignatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signatures")
@RequiredArgsConstructor
@Slf4j
public class SignatureController {

    private final SignatureService signatureService;

    @PostMapping("/sign")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> signContract(
            @RequestBody @Valid SignContractRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String email = authentication.getName();
        String ipAddress = httpRequest.getRemoteAddr();

        log.info("Incoming request: signContract by {}", email);

        ContractResponse result = signatureService.signContract(request, email, ipAddress);
        return  ResponseEntity.ok(new ApiResponse(true, "Contract signed successfully", result));
    }
}
