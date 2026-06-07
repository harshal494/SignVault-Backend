package com.harshalkhade.signvault.controller;

import com.harshalkhade.signvault.dto.response.ApiResponse;
import com.harshalkhade.signvault.dto.response.ContractResponse;
import com.harshalkhade.signvault.dto.response.UserResponse;
import com.harshalkhade.signvault.entity.AuditLog;
import com.harshalkhade.signvault.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> getAllUsers(Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin fetching all users by email: {}", email);
        List<UserResponse> result = adminService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse(true, "Users fetched successfully", result));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin fetching user with id: {}", id);
        UserResponse userId = adminService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse(true, "User fetched successfully", userId));
    }

    @PutMapping("/users/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin deactivating user with id: {}", id);
        adminService.deactivateUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "User deactivated successfully", null));
    }

    @PutMapping("/users/{id}/promote")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse> promoteToAdmin(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin promoting user with id: {}", id);
        adminService.promoteToAdmin(id);
        return ResponseEntity.ok(new ApiResponse(true, "User promoted to Admin successfully", null));
    }

    @PutMapping("/users/{id}/demote")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse> demoteToUser(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin demoting user with id: {}", id);
        adminService.demoteToUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "Admin demoted to User successfully", null));
    }

    @GetMapping("/contracts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> getAllContracts(Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin fetching all contracts with email: {}", email);
        List<ContractResponse> result = adminService.getAllContracts();
        return ResponseEntity.ok(new ApiResponse(true, "Contracts fetched successfully", result));
    }

    @GetMapping("/contracts/flagged")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> getFlaggedContracts(Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin fetching flagged contracts with email: {}", email);
        List<ContractResponse> result = adminService.getFlaggedContracts();
        return ResponseEntity.ok(new ApiResponse(true, "Flagged contracts fetched successfully", result));
    }

    @PutMapping("/contracts/{contractId}/flag")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> flagContract(@PathVariable String contractId, Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin flagging contract with id: {}", contractId);
        ContractResponse result = adminService.flagContract(contractId, email);
        return ResponseEntity.ok(new ApiResponse(true, "Contract flagged successfully", result));
    }

    @PutMapping("/contracts/{contractId}/unflag")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public  ResponseEntity<ApiResponse> unflagContract(@PathVariable String contractId, Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin unflagging contract with id: {}", contractId);
        ContractResponse result = adminService.unflagContract(contractId, email);
        return ResponseEntity.ok(new ApiResponse(true, "Contract unflagged successfully", result));
    }

    @PutMapping("/contracts/{contractId}/cancel")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public  ResponseEntity<ApiResponse> cancelContract(@PathVariable String contractId, Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin canceling contract with id: {}", contractId);
        ContractResponse result = adminService.cancelContract(contractId, email);
        return ResponseEntity.ok(new ApiResponse(true, "Contract cancelled successfully", result));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<ApiResponse> getAuditLogs(Authentication authentication) {
        String email = authentication.getName();
        log.info("Admin fetching audit logs with email: {}", email);
        List<AuditLog> logs = adminService.getAuditLogs();
        return ResponseEntity.ok(new ApiResponse(true, "Audit logs fetched successfully", logs));
    }




}
