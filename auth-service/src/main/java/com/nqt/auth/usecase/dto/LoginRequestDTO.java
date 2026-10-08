package com.nqt.auth.usecase.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoginRequestDTO {
    @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}