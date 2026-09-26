package com.nqt.skin.infrastructure.persistence.repository;

import com.nqt.skin.domain.model.Slot;
import com.nqt.skin.infrastructure.persistence.entity.UserEquippedSkinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEquippedSkinRepository extends JpaRepository<UserEquippedSkinEntity, Long> {
    List<UserEquippedSkinEntity> findByUserIdOrderBySkinItemLayerOrderAsc(Long userId);

    Optional<UserEquippedSkinEntity> findByUserIdAndSlot(Long userId, Slot slot);

    void deleteByUserIdAndSlot(Long userId, Slot slot);
}
