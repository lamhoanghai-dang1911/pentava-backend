package com.nqt.auth.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GoogleLoginRequestDTO {
    @NotBlank(message = "IdToken không được để trống")
    private String idToken;
}