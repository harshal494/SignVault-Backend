package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.dto.request.*;
import com.harshalkhade.signvault.dto.response.AuthResponse;
import com.harshalkhade.signvault.entity.OtpVerification;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.enums.AuthProvider;
import com.harshalkhade.signvault.enums.OtpType;
import com.harshalkhade.signvault.enums.Role;
import com.harshalkhade.signvault.repository.OtpVerificationRepository;
import com.harshalkhade.signvault.repository.UserRepository;
import com.harshalkhade.signvault.security.JwtUtil;
import com.harshalkhade.signvault.util.HashUtil;
import com.harshalkhade.signvault.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpGenerator otpGenerator;
    private final HashUtil hashUtil;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    public AuthResponse register(RegisterRequest request) {

        log.info("Registering new user with email: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        if(userRepository.existsByPhone(request.getPhone())){
            throw new RuntimeException("Phone Number already exists!");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .phoneVerified(false)
                .profileComplete(false)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        sendEmailOtp(savedUser.getEmail());

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getEmail())
                .password(savedUser.getPasswordHash())
                .authorities(savedUser.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .profileComplete(savedUser.isProfileComplete())
                .build();


    }

    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password!");
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email is not verified!");
        }

//        if (!user.isPhoneVerified()) {
//            throw new RuntimeException("Phone number is not verified!");
//        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);


        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .profileComplete(user.isProfileComplete())
                .build();





    }

    public void sendEmailOtp(String email) {

        log.info("Sending email OTP to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        String otp = otpGenerator.generateOtp();

        otpVerificationRepository.deleteByUser(user);

        OtpVerification otpVerification = OtpVerification.builder()
                .user(user)
                .type(OtpType.EMAIL_VERIFICATION)
                .otpHash(passwordEncoder.encode(otp))
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        otpVerificationRepository.save(otpVerification);

        emailService.sendOtpEmail(user.getEmail(), otp);

        log.info("Email OTP sent successfully to: {}", email);
    }

    public void verifyEmailOtp(OtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        OtpVerification otpVerification = otpVerificationRepository
                .findByUserAndTypeAndVerified(user, OtpType.EMAIL_VERIFICATION, false)
                .orElseThrow(() -> new RuntimeException("OTP not found or already verified!"));

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired!. Please request a new one");
        }

        if (!passwordEncoder.matches(request.getOtp(),  otpVerification.getOtpHash())) {
            throw new RuntimeException("Invalid OTP!");
        }

        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Successfully verified OTP for user: {}", user.getEmail());

    }

    public void sendPhoneOtp(String email) {

        log.info("Sending phone OTP to: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        String otp = otpGenerator.generateOtp();

        otpVerificationRepository.deleteByUser(user);

        OtpVerification otpVerification = OtpVerification.builder()
                .user(user)
                .type(OtpType.PHONE_VERIFICATION)
                .otpHash(passwordEncoder.encode(otp))
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        otpVerificationRepository.save(otpVerification);

        smsService.sendOtpSms(user.getPhone(), otp);

        log.info("Phone OTP sent successfully to: {}", email);

    }

    public void verifyPhoneOtp(OtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        OtpVerification otpVerification = otpVerificationRepository
                .findByUserAndTypeAndVerified(user, OtpType.PHONE_VERIFICATION, false)
                .orElseThrow(() -> new RuntimeException("OTP not found or already verified!"));

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired!. Please request a new one");
        }

        if (!passwordEncoder.matches(request.getOtp(),  otpVerification.getOtpHash())) {
            throw new RuntimeException("Invalid OTP!");
        }

        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        user.setPhoneVerified(true);
        userRepository.save(user);

        log.info("Successfully verified OTP for user: {}", user.getEmail());



    }

    public void registerFingerprint(FingerprintRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        String bcryptHash = passwordEncoder.encode(request.getFingerprintString());

        String sha256Hash;
        try {
            sha256Hash = HashUtil.hashWithSHA256(request.getFingerprintString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Fingerprint hashing failed!");
        }

        user.setFingerprintBcrypt(bcryptHash);
        user.setFingerprintSha256(sha256Hash);

        if (user.isEmailVerified() && user.isPhoneVerified()) {
            user.setProfileComplete(true);
        }

        userRepository.save(user);

        log.info("Successfully registered Fingerprint for user: {}", user.getEmail());

    }

    public AuthResponse completeProfile(CompleteProfileRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }

        User savedUser = userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getEmail())
                .password(savedUser.getPasswordHash() != null
                        ? savedUser.getPasswordHash() : "")
                .authorities(savedUser.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        sendPhoneOtp(email);

        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .profileComplete(savedUser.isProfileComplete())
                .build();


    }


}
