package com.nqt.onboarding.usecase.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoalAssessmentRequestDTO {
    @NotBlank(message = "Câu trả lời về giấc ngủ không được để trống")
    private String sleepAnswer;

    @NotBlank(message = "Câu trả lời về vận động không được để trống")
    private String exerciseAnswer;

    @NotBlank(message = "Câu trả lời về căng thẳng không được để trống")
    private String stressAnswer;

    @NotBlank(message = "Câu trả lời về kỷ luật không được để trống")
    private String disciplineAnswer;

    @NotNull(message = "Điểm đánh giá mục tiêu không được để trống")
    @Min(value = 1, message = "Điểm mục tiêu từ 1 đến 4")
    @Max(value = 4, message = "Điểm mục tiêu từ 1 đến 4")
    private Integer goalQuestionAnswer;

    private String goalQuestionText;
    private String goalQuestionAnswerLabel;

    @NotNull(message = "Điểm tự chấm % không được để trống")
    @Min(value = 0, message = "Điểm tự chấm tối thiểu 0%")
    @Max(value = 100, message = "Điểm tự chấm tối đa 100%")
    private Integer selfPerceptionPercent;
}
