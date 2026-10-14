package com.nqt.onboarding.usecase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskResponseDTO {
    private Long id;          // id của task (bảng tasks) - giữ nguyên
    private Long goalId;      // ➕ goalId để lọc chuẩn theo mục tiêu
    private Long progressId;  // ➕ id dòng trong user_task_daily, dùng để checkin/lưu nháp. null nếu task chưa được chọn cho hôm nay
    private String title;
    private String content;
    private Integer weekNumber;
    private Boolean isAiGenerated;
    private Boolean isCompleted;
    private String moodType; // ➕ mới, để FE biết task thuộc mood nào
}