package com.harshalkhade.signvault.service;

import com.harshalkhade.signvault.dto.request.UpdateProfileRequest;
import com.harshalkhade.signvault.dto.response.UserResponse;
import com.harshalkhade.signvault.entity.User;
import com.harshalkhade.signvault.exception.ResourceNotFoundException;
import com.harshalkhade.signvault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("Fetching profile for {}", email);

        return mapToResponse(user);
    }

    public UserResponse updateProfile(String email, UpdateProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAge(request.getAge());
        user.setProfileComplete(true);
        userRepository.save(user);
        log.info("Profile updated for {}", email);
        return mapToResponse(user);
    }

    public void deactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(false);
        userRepository.save(user);
        log.info("Account deactivated for {}", email);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .profileComplete(user.isProfileComplete())
                .active(user.isActive())
                .age(user.getAge())
                .authProvider(user.getAuthProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
