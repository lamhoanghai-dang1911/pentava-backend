package com.nqt.cinema.usecase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateJourneyClipRequestDTO(
        @NotEmpty(message = "Danh sách ảnh ghép hành trình không được để trống")
        @Size(min = 3, max = 50, message = "Số ảnh ghép hành trình phải từ 3 đến 50 ảnh")
        List<Long> photoIds,

        List<Integer> weekNumbers
) {
}
