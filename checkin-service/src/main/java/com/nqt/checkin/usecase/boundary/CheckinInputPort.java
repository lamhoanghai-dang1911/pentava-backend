package com.nqt.checkin.usecase.boundary;

import com.nqt.checkin.usecase.dto.CheckinRequestDTO;

public interface CheckinInputPort {
    void saveCheckinImageUrl(Long taskId, CheckinRequestDTO requestDTO);
    void getCheckinByTask(Long taskId);
}