package com.harshalkhade.signvault.dto.response;

import com.harshalkhade.signvault.enums.AuthProvider;
import com.harshalkhade.signvault.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean profileComplete;
    private boolean active;
    private Integer age;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;
}
