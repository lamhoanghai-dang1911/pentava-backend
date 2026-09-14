package com.nqt.onboarding.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoodTaskResponseDTO {
    private String moodType;
    private List<TaskResponseDTO> availableTasks; // 20 task
    private List<Long> yesterdayTaskIds;           // 5 id đã hoàn thành hôm qua (nếu có)
}