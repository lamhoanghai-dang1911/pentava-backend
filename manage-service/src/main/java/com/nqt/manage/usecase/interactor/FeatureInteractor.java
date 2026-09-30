package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.Feature;
import com.nqt.manage.usecase.boundary.FeatureInputPort;
import com.nqt.manage.usecase.boundary.FeatureRepositoryGateway;
import com.nqt.manage.usecase.dto.FeatureRequestDTO;
import com.nqt.manage.usecase.dto.FeatureResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeatureInteractor implements FeatureInputPort {

    private final FeatureRepositoryGateway featureRepositoryGateway;

    @Override
    public FeatureResponseDTO createFeature(FeatureRequestDTO requestDTO) {
        LocalDateTime now = LocalDateTime.now();
        Feature feature = Feature.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .icon(requestDTO.getIcon())
                .tag(requestDTO.getTag())
                .previewImageUrl(requestDTO.getPreviewImageUrl())
                .displayOrder(requestDTO.getDisplayOrder() != null ? requestDTO.getDisplayOrder() : 0)
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toDTO(featureRepositoryGateway.save(feature));
    }

    @Override
    public FeatureResponseDTO updateFeature(Long id, FeatureRequestDTO requestDTO) {
        Feature feature = featureRepositoryGateway.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tính năng với ID: " + id));

        feature.setTitle(requestDTO.getTitle());
        feature.setDescription(requestDTO.getDescription());
        feature.setIcon(requestDTO.getIcon());
        feature.setTag(requestDTO.getTag());
        feature.setPreviewImageUrl(requestDTO.getPreviewImageUrl());
        if (requestDTO.getDisplayOrder() != null) {
            feature.setDisplayOrder(requestDTO.getDisplayOrder());
        }
        if (requestDTO.getIsActive() != null) {
            feature.setIsActive(requestDTO.getIsActive());
        }
        feature.setUpdatedAt(LocalDateTime.now());

        return toDTO(featureRepositoryGateway.save(feature));
    }

    @Override
    public FeatureResponseDTO getFeatureById(Long id) {
        return featureRepositoryGateway.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tính năng với ID: " + id));
    }

    @Override
    public List<FeatureResponseDTO> getAllFeatures() {
        return featureRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeatureResponseDTO> getActiveFeatures() {
        return featureRepositoryGateway.findActiveFeatures().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFeature(Long id) {
        if (featureRepositoryGateway.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy tính năng với ID: " + id);
        }
        featureRepositoryGateway.deleteById(id);
    }

    private FeatureResponseDTO toDTO(Feature f) {
        return FeatureResponseDTO.builder()
                .id(f.getId())
                .title(f.getTitle())
                .description(f.getDescription())
                .icon(f.getIcon())
                .tag(f.getTag())
                .previewImageUrl(f.getPreviewImageUrl())
                .displayOrder(f.getDisplayOrder())
                .isActive(f.getIsActive())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
