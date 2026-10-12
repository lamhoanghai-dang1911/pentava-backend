package com.nqt.cinema.interfaceadapters;

import com.nqt.cinema.usecase.boundary.CinemaInputPort;
import com.nqt.cinema.usecase.boundary.ClipInputPort;
import com.nqt.cinema.usecase.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "PENTA-CINEMA", description = "Tổng hợp ảnh check-in theo tuần của mục tiêu thành clip")
@RestController
@RequestMapping("/api/cinema")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaInputPort inputPort;
    private final ClipInputPort clipInputPort;


    @Operation(summary = "Xem dung lượng lưu trữ hiện tại (video + hình ảnh) và trạng thái gói cước")
    @GetMapping("/storage/quota")
    public ResponseEntity<CinemaStorageQuotaDTO> getStorageQuota() {
        return ResponseEntity.ok(inputPort.getStorageQuota());
    }

    @Operation(summary = "Danh sách mục tiêu của user (mọi trạng thái)")
    @GetMapping("/goals")
    public ResponseEntity<List<GoalSummaryDTO>> getGoals() {
        return ResponseEntity.ok(inputPort.getGoals());
    }

    @Operation(
            summary = "4 tuần của một mục tiêu",
            description = "Tuần 1 bắt đầu từ start_date của mục tiêu, mỗi tuần đúng 7 ngày.")
    @GetMapping("/goals/{goalId}/weeks")
    public ResponseEntity<WeeksResponseDTO> getGoalWeeks(@PathVariable("goalId") Long goalId) {
        return ResponseEntity.ok(inputPort.getGoalWeeks(goalId));
    }

    @Operation(
            summary = "Ảnh của một tuần để chọn khi tạo clip",
            description = "weekNumber từ 1 đến 4. Tuần chưa bắt đầu sẽ báo lỗi.")
    @GetMapping("/goals/{goalId}/weeks/{weekNumber}/photos")
    public ResponseEntity<WeekPhotosResponseDTO> getGoalWeekPhotos(
            @PathVariable("goalId") Long goalId,
            @Parameter(description = "Số thứ tự tuần, từ 1 đến 4")
            @PathVariable("weekNumber") int weekNumber) {
        return ResponseEntity.ok(inputPort.getGoalWeekPhotos(goalId, weekNumber));
    }

    @Operation(
            summary = "Tạo clip từ ảnh đã chọn trong tuần",
            description = "3-20 photoId. Render chạy nền — dùng GET .../clips/{id} để theo dõi trạng thái.")
    @PostMapping("/goals/{goalId}/weeks/{weekNumber}/clips")
    public ResponseEntity<ClipDTO> createClip(
            @PathVariable("goalId") Long goalId,
            @PathVariable("weekNumber") int weekNumber,
            @Valid @RequestBody CreateClipRequestDTO request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(clipInputPort.createClip(goalId, weekNumber, request));
    }

    @Operation(
            summary = "Tạo clip tổng hợp đa tuần / toàn bộ hành trình (Multi-Angle Journey Clip)",
            description = "Ghép ảnh xuyên suốt nhiều tuần (tuần 1 đến 4). Yêu cầu gói đăng ký có tính năng MULTI_ANGLE_TEMPLATES.")
    @PostMapping("/goals/{goalId}/journey-clips")
    public ResponseEntity<ClipDTO> createJourneyClip(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody CreateJourneyClipRequestDTO request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(clipInputPort.createJourneyClip(goalId, request));
    }


    @Operation(summary = "Danh sách clip đã tạo của một tuần")
    @GetMapping("/goals/{goalId}/weeks/{weekNumber}/clips")
    public ResponseEntity<List<ClipDTO>> getClips(
            @PathVariable("goalId") Long goalId,
            @PathVariable("weekNumber") int weekNumber) {
        return ResponseEntity.ok(clipInputPort.getClips(goalId, weekNumber));
    }

    @Operation(summary = "Xem chi tiết 1 clip (trạng thái, URL video nếu đã xong)")
    @GetMapping("/clips/{id}")
    public ResponseEntity<ClipDTO> getClip(@PathVariable("id") Long id) {
        return ResponseEntity.ok(clipInputPort.getClip(id));
    }

    @Operation(summary = "Xoá clip (kể cả file video trên Supabase)")
    @DeleteMapping("/clips/{id}")
    public ResponseEntity<Void> deleteClip(@PathVariable("id") Long id) {
        clipInputPort.deleteClip(id);
        return ResponseEntity.noContent().build();
    }
}