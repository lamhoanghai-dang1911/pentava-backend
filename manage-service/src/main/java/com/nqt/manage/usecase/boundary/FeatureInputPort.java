package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.FeatureRequestDTO;
import com.nqt.manage.usecase.dto.FeatureResponseDTO;

import java.util.List;

public interface FeatureInputPort {
    FeatureResponseDTO createFeature(FeatureRequestDTO requestDTO);
    FeatureResponseDTO updateFeature(Long id, FeatureRequestDTO requestDTO);
    FeatureResponseDTO getFeatureById(Long id);
    List<FeatureResponseDTO> getAllFeatures();
    List<FeatureResponseDTO> getActiveFeatures();
    void deleteFeature(Long id);
}
