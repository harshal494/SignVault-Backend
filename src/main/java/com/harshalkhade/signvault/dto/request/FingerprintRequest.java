package com.harshalkhade.signvault.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class FingerprintRequest {

    @NotBlank(message = "Fingerprint required")
    private String fingerprintString;
}
