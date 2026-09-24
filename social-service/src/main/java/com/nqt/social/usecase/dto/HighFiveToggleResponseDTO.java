package com.nqt.social.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighFiveToggleResponseDTO {
    private Long postId;
    private boolean highFived;
    private long totalHighFives;
}
