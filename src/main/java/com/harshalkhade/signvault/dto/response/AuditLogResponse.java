package com.harshalkhade.signvault.dto.response;

import com.harshalkhade.signvault.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private String action;
    private String ipAddress;
    private LocalDateTime createdAt;
    private String contractId;
    private String contractTitle;
    private Long userId;
    private String userFullname;
    private String userEmail;
    private Role userRole;
}
