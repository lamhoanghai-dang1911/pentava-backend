package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.Feature;

import java.util.List;
import java.util.Optional;

public interface FeatureRepositoryGateway {
    Feature save(Feature feature);
    Optional<Feature> findById(Long id);
    List<Feature> findAll();
    List<Feature> findActiveFeatures();
    void deleteById(Long id);
}
