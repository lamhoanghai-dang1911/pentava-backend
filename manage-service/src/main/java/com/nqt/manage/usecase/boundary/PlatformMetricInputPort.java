package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.PlatformMetricRequestDTO;
import com.nqt.manage.usecase.dto.PlatformMetricResponseDTO;

import java.util.List;

public interface PlatformMetricInputPort {
    PlatformMetricResponseDTO createMetric(PlatformMetricRequestDTO requestDTO);
    PlatformMetricResponseDTO updateMetric(Long id, PlatformMetricRequestDTO requestDTO);
    PlatformMetricResponseDTO getMetricById(Long id);
    PlatformMetricResponseDTO getMetricByKey(String key);
    List<PlatformMetricResponseDTO> getAllMetrics();
    List<PlatformMetricResponseDTO> getActiveMetrics();
    void deleteMetric(Long id);
}
