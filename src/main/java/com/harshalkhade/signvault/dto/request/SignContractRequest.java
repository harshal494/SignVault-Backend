package com.harshalkhade.signvault.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SignContractRequest {

    private String fingerprintString;
    private String contractId;
}
