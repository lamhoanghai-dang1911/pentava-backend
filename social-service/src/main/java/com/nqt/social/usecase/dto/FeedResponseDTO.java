package com.nqt.social.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponseDTO {
    private List<PostResponseDTO> items;
    private String nextCursor;
    private boolean hasMore;
}
