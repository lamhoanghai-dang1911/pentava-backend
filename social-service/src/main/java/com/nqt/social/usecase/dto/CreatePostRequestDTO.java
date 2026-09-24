package com.nqt.social.usecase.dto;

import com.nqt.social.domain.PrivacyMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDTO {
    @NotNull(message = "clipId không được để trống")
    private Long clipId;
    private String caption;
    private String tags;
    @Builder.Default
    private PrivacyMode privacyMode = PrivacyMode.PUBLIC;
}
