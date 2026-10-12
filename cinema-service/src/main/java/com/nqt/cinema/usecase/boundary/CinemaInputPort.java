package com.nqt.cinema.usecase.boundary;

import com.nqt.cinema.usecase.dto.GoalSummaryDTO;
import com.nqt.cinema.usecase.dto.WeekPhotosResponseDTO;
import com.nqt.cinema.usecase.dto.WeeksResponseDTO;

import java.util.List;

public interface CinemaInputPort {
    /** Danh sách tất cả mục tiêu của user, mọi trạng thái. */
    List<GoalSummaryDTO> getGoals();

    /** 4 tuần của một mục tiêu: ngày, số ảnh, có tạo được clip không. */
    WeeksResponseDTO getGoalWeeks(Long goalId);

    /** Ảnh của một tuần thuộc mục tiêu, để user chọn khi tạo clip. */
    WeekPhotosResponseDTO getGoalWeekPhotos(Long goalId, int weekNumber);

    /** Thông tin dung lượng lưu trữ (video + hình ảnh) và trạng thái gói cước của user */
    com.nqt.cinema.usecase.dto.CinemaStorageQuotaDTO getStorageQuota();
}