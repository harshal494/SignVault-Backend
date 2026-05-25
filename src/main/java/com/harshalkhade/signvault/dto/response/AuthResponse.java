package com.harshalkhade.signvault.dto.response;

import com.harshalkhade.signvault.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AuthResponse {

    private String token;
    private String email;
    private String fullName;
    private Role role;
    private boolean profileComplete;


}
