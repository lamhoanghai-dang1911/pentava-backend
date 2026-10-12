package com.nqt.checkin.usecase.dto;

import lombok.Data;

@Data
public class CheckinRequestDTO {
    private String imageUrl;
    private Long fileSizeBytes;
}