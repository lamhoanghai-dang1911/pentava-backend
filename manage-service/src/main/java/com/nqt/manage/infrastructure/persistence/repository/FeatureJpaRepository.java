package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.infrastructure.persistence.entity.FeatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureJpaRepository extends JpaRepository<FeatureEntity, Long> {
    List<FeatureEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<FeatureEntity> findAllByOrderByDisplayOrderAsc();
}
