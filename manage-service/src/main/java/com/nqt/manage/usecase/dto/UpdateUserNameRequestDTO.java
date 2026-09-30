package com.nqt.manage.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserNameRequestDTO {
    @NotBlank(message = "Tên người dùng không được để trống")
    private String name;
}
