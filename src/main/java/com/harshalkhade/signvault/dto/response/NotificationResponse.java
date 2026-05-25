package com.harshalkhade.signvault.dto.response;

import com.harshalkhade.signvault.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class NotificationResponse {

    private Long id;
    private String message;
    private NotificationType type;
    private boolean read;
    private String contractId;
    private LocalDateTime createdAt;
}
