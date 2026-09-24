package com.nqt.cinema.usecase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateClipRequestDTO(
        @NotEmpty(message = "Phải chọn ít nhất 1 ảnh")
        @Size(min = 3, max = 20, message = "Số ảnh phải từ 3 đến 20")
        List<Long> photoIds
) {
}