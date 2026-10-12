package com.nqt.onboarding.usecase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomWeekDTO {
    @Min(value = 1, message = "Số thứ tự tuần phải từ 1 đến 4")
    @Max(value = 4, message = "Số thứ tự tuần phải từ 1 đến 4")
    private Integer weekNumber;

    @Valid
    private List<CustomTaskItemDTO> tasks;
}
