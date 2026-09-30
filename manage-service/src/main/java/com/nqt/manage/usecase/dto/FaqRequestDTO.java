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
public class FaqRequestDTO {
    @NotBlank(message = "Câu hỏi không được để trống")
    private String question;
    @NotBlank(message = "Câu trả lời không được để trống")
    private String answer;
    private String category;
    private Integer displayOrder;
    private Boolean isActive;
}
