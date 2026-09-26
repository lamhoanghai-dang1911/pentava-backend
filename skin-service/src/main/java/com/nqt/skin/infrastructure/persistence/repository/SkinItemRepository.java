package com.nqt.skin.infrastructure.persistence.repository;

import com.nqt.skin.infrastructure.persistence.entity.SkinItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkinItemRepository extends JpaRepository<SkinItemEntity, Long> {
    Optional<SkinItemEntity> findByCode(String code);
    List<SkinItemEntity> findByIsActiveTrueOrderByLayerOrderAsc();
}
