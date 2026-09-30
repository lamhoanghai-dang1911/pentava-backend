package com.nqt.manage.usecase.dto;

import com.nqt.manage.domain.model.ContactStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContactStatusRequestDTO {
    @NotNull(message = "Trạng thái liên hệ không được để trống")
    private ContactStatus status;
    private String adminNote;
}
