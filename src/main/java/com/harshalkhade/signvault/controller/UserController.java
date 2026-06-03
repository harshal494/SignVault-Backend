package com.harshalkhade.signvault.controller;

import com.harshalkhade.signvault.dto.request.UpdateProfileRequest;
import com.harshalkhade.signvault.dto.response.ApiResponse;
import com.harshalkhade.signvault.dto.response.UserResponse;
import com.harshalkhade.signvault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        log.info("Incoming request: getProfile for {}", email);
        UserResponse result = userService.getProfile(email);
        return ResponseEntity.ok(new ApiResponse(true,"Profile fetched successfully", result));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> updateProfile(@RequestBody @Valid UpdateProfileRequest request, Authentication authentication) {
        String email = authentication.getName();
        log.info("Incoming request: updateProfile for {}", email);
        UserResponse result = userService.updateProfile(email, request);
        return ResponseEntity.ok(new ApiResponse(true, "profile updated successfully", result));
    }

    @DeleteMapping("/deactivate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> deactivateAccount(Authentication authentication) {
        String email = authentication.getName();
        log.info("Incoming request: deactivateAccount for {}", email);
        userService.deactivateAccount(email);
        return ResponseEntity.ok(new ApiResponse(true, "Account deactivated successfully", null));

    }
}
