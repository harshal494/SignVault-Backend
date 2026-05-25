package com.harshalkhade.signvault.dto.request;

import com.harshalkhade.signvault.enums.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RenewContractRequest {

    private String contractId;
    private PeriodType periodType;
    private Integer periodValue;
}
