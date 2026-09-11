package com.nqt.checkin.usecase.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CheckinResponseDTO {
    private String message;
    private Long taskId;
    private String imageUrl;
    private LocalDate checkinDate;
}