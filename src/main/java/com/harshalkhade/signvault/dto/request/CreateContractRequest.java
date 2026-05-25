package com.harshalkhade.signvault.dto.request;

import com.harshalkhade.signvault.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CreateContractRequest {

    @NotBlank(message = "Must have some title")
    private String title;

    @NotBlank(message = "Receivers Email required")
    private String receiverEmail;

    private PeriodType periodType;
    private Integer periodValue;
    private LocalDate periodFrom;
}
