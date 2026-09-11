package com.nqt.onboarding.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OnboardingRequestDTO {
    // Step 1: Tên
    @NotBlank(message = "Tên không được để trống")
    private String name;

    // Step 2: Giới tính & Tuổi
    @NotBlank(message = "Giới tính không được để trống")
    private String gender;  // "Nam" / "Nữ" / "Khác"

    @NotBlank(message = "Tuổi không được để trống")
    private String age;     // "Dưới 18" / "18-24" / "25-34"

    // Step 3: Primary goal (mục tiêu chính)
    @NotBlank(message = "Mục tiêu không được để trống")
    private String primaryGoal;  // "Bớt stress" / "Ngủ tốt hơn" / ...

    // Step 4: Routine preference
    @NotBlank(message = "Loại routine không được để trống")
    private String routineType;  // "Nhẹ nhàng" / "Cân bằng" / "Thử thách"

    // Step 5: Sleep hours
    @NotBlank(message = "Giờ ngủ không được để trống")
    private String sleepHours;   // "Dưới 5" / "5-6" / "7-8" / "Không đều"

    // Step 6: Exercise frequency
    @NotBlank(message = "Tần suất tập luyện không được để trống")
    private String exerciseFrequency;  // "Gần như không" / "1-2/tuần" / "3-4/tuần" / "Gần như mỗi ngày"

    // Step 7: Stress frequency
    @NotBlank(message = "Tần suất stress không được để trống")
    private String stressFrequency;    // "Gần như mỗi ngày" / "Vài lần/tuần" / "Thỉnh thoảng" / "Hiếm khi"

    // Step 8: Screen time
    @NotBlank(message = "Thời gian màn hình không được để trống")
    private String screenTime;         // "Trên 8" / "5-8" / "3-5" / "Dưới 3"

    // Step 9: Habit retention
    @NotBlank(message = "Thói quen duy trì không được để trống")
    private String habitDuration;      // "Dưới 3 ngày" / "1 tuần" / "2-3 tuần" / "Dài hơn"

    // Step 10: Free time slots
    @NotEmpty(message = "Phải chọn ít nhất một khoảng thời gian rảnh")
    private List<String> freeTimes;    // ["Sáng", "Chiều", "Tối"]
}