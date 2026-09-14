package com.nqt.onboarding.usecase.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTaskStatusResponseDTO {
    private boolean hasConfirmedToday;          // hôm nay đã confirm 5 task chưa
    private List<TaskResponseDTO> todayTasks;   // nếu đã confirm -> 5 task hôm nay

    private boolean hasYesterdayTasks;          // có task hôm qua (dù xong hay chưa) để giữ tiếp không
    private List<TaskResponseDTO> yesterdayTasks; // 5 task hôm qua (kèm trạng thái completed)
}