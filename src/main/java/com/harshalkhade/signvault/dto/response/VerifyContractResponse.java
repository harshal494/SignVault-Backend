package com.harshalkhade.signvault.dto.response;

import com.harshalkhade.signvault.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyContractResponse {

    private String contractId;
    private String title;
    private ContractStatus status;
    private LocalDateTime createdAt;
    private boolean verified;
}
