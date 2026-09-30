package com.nqt.manage.usecase.dto;

import com.nqt.manage.domain.model.ContactStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInquiryResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private ContactStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
