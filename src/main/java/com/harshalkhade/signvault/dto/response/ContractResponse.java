package com.harshalkhade.signvault.dto.response;

import com.harshalkhade.signvault.enums.ContractStatus;
import com.harshalkhade.signvault.enums.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ContractResponse {

    private Long id;
    private String contractId;
    private String title;
    private String senderName;
    private String receiverName;
    private ContractStatus status;
    private String fileHash;
    private PeriodType periodType;
    private Integer periodValue;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private boolean permanent;
    private boolean renewal;
    private String parentContractId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

}
