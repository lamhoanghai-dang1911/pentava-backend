package com.nqt.auth.usecase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserQueryResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String avatarUrl;
    private String bio;
}