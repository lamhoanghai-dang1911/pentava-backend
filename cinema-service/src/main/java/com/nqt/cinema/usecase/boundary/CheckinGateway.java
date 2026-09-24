package com.nqt.cinema.usecase.boundary;

import com.nqt.cinema.domain.CheckinPhoto;

import java.time.LocalDate;
import java.util.List;

public interface CheckinGateway {
    /** Ảnh check-in của user trong khoảng ngày (gồm cả 2 đầu), sắp theo ngày rồi giờ tạo. */
    List<CheckinPhoto> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}