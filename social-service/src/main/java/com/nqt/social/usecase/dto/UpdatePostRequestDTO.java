package com.nqt.social.usecase.dto;

import com.nqt.social.domain.PrivacyMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequestDTO {

    @Schema(description = "Nội dung caption bài viết", example = "Khoảnh khắc tuyệt vời cùng bạn bè!")
    private String caption;

    @Schema(description = "Tags đính kèm bài viết", example = "#cinema #pentava #review")
    private String tags;

    @Schema(description = "Chế độ riêng tư: PUBLIC, FRIENDS_ONLY, PRIVATE", example = "PUBLIC")
    private PrivacyMode privacyMode;
}
