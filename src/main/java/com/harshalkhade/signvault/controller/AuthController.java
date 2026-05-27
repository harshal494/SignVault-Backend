package com.harshalkhade.signvault.controller;

import com.harshalkhade.signvault.dto.request.*;
import com.harshalkhade.signvault.dto.response.ApiResponse;
import com.harshalkhade.signvault.dto.response.AuthResponse;
import com.harshalkhade.signvault.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Register successfully!", authResponse ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successfuly!", authResponse));
    }

    @PostMapping("/send-email-otp")
    public ResponseEntity<ApiResponse> sendEmailOtp(@RequestParam String email) {
        authService.sendEmailOtp(email);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @PostMapping("/verify-email-otp")
    public ResponseEntity<ApiResponse> verifyEmailOtp(@Valid @RequestBody OtpRequest request) {
        authService.verifyEmailOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", null));
    }

    @PostMapping("/send-phone-otp")
    public ResponseEntity<ApiResponse> sendPhoneOtp(@AuthenticationPrincipal UserDetails userDetails) {
        authService.sendPhoneOtp(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Phone OTP sent successfully", null));
    }

    @PostMapping("/verify-phone-otp")
    public ResponseEntity<ApiResponse> verifyPhoneOtp(@Valid @RequestBody  OtpRequest request) {
        authService.verifyPhoneOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Phone OTP verified successfully", null));
    }

    @PostMapping("/register-fingerprint")
    public ResponseEntity<ApiResponse> registerFingerprint( @AuthenticationPrincipal UserDetails userDetails,@Valid @RequestBody FingerprintRequest request) {
        authService.registerFingerprint(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Fingerprint registered successfully", null));
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<ApiResponse> completeProfile( @AuthenticationPrincipal UserDetails userDetails,@Valid @RequestBody CompleteProfileRequest request) {
        authService.completeProfile(request,  userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complete profile successfully", null));
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<ApiResponse> handleOAuth2Success(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success("Google login successful", token));
    }

}
