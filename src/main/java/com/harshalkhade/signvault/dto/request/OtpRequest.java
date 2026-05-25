package com.harshalkhade.signvault.dto.request;

import com.harshalkhade.signvault.enums.OtpType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class OtpRequest {

    @NotBlank(message = "Enter valid OTP")
    private String otp;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private OtpType type;
}
