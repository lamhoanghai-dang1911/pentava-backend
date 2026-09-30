package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.PlatformMetric;
import com.nqt.manage.usecase.boundary.PlatformMetricInputPort;
import com.nqt.manage.usecase.boundary.PlatformMetricRepositoryGateway;
import com.nqt.manage.usecase.dto.PlatformMetricRequestDTO;
import com.nqt.manage.usecase.dto.PlatformMetricResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformMetricInteractor implements PlatformMetricInputPort {

    private final PlatformMetricRepositoryGateway platformMetricRepositoryGateway;

    @Override
    public PlatformMetricResponseDTO createMetric(PlatformMetricRequestDTO requestDTO) {
        PlatformMetric metric = PlatformMetric.builder()
                .metricKey(requestDTO.getMetricKey())
                .label(requestDTO.getLabel())
                .counterValue(requestDTO.getCounterValue())
                .suffix(requestDTO.getSuffix())
                .icon(requestDTO.getIcon())
                .isAutoCalculated(requestDTO.getIsAutoCalculated() != null ? requestDTO.getIsAutoCalculated() : false)
                .displayOrder(requestDTO.getDisplayOrder() != null ? requestDTO.getDisplayOrder() : 0)
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .updatedAt(LocalDateTime.now())
                .build();
        return toDTO(platformMetricRepositoryGateway.save(metric));
    }

    @Override
    public PlatformMetricResponseDTO updateMetric(Long id, PlatformMetricRequestDTO requestDTO) {
        PlatformMetric metric = platformMetricRepositoryGateway.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỉ số với ID: " + id));

        metric.setMetricKey(requestDTO.getMetricKey());
        metric.setLabel(requestDTO.getLabel());
        metric.setCounterValue(requestDTO.getCounterValue());
        metric.setSuffix(requestDTO.getSuffix());
        metric.setIcon(requestDTO.getIcon());
        if (requestDTO.getIsAutoCalculated() != null) {
            metric.setIsAutoCalculated(requestDTO.getIsAutoCalculated());
        }
        if (requestDTO.getDisplayOrder() != null) {
            metric.setDisplayOrder(requestDTO.getDisplayOrder());
        }
        if (requestDTO.getIsActive() != null) {
            metric.setIsActive(requestDTO.getIsActive());
        }
        metric.setUpdatedAt(LocalDateTime.now());

        return toDTO(platformMetricRepositoryGateway.save(metric));
    }

    @Override
    public PlatformMetricResponseDTO getMetricById(Long id) {
        return platformMetricRepositoryGateway.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỉ số với ID: " + id));
    }

    @Override
    public PlatformMetricResponseDTO getMetricByKey(String key) {
        return platformMetricRepositoryGateway.findByKey(key)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chỉ số với key: " + key));
    }

    @Override
    public List<PlatformMetricResponseDTO> getAllMetrics() {
        return platformMetricRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformMetricResponseDTO> getActiveMetrics() {
        return platformMetricRepositoryGateway.findActiveMetrics().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMetric(Long id) {
        if (platformMetricRepositoryGateway.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy chỉ số với ID: " + id);
        }
        platformMetricRepositoryGateway.deleteById(id);
    }

    private PlatformMetricResponseDTO toDTO(PlatformMetric m) {
        return PlatformMetricResponseDTO.builder()
                .id(m.getId())
                .metricKey(m.getMetricKey())
                .label(m.getLabel())
                .counterValue(m.getCounterValue())
                .suffix(m.getSuffix())
                .icon(m.getIcon())
                .isAutoCalculated(m.getIsAutoCalculated())
                .displayOrder(m.getDisplayOrder())
                .isActive(m.getIsActive())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
