package com.nqt.social.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequestDTO {
    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
    private Long parentId;
}
