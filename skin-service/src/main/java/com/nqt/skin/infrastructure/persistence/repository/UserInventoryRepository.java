package com.nqt.skin.infrastructure.persistence.repository;

import com.nqt.skin.infrastructure.persistence.entity.UserInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventoryEntity, Long> {
    List<UserInventoryEntity> findByUserId(Long userId);

    Optional<UserInventoryEntity> findByUserIdAndSkinItemId(Long userId, Long skinItemId);

    boolean existsByUserIdAndSkinItemId(Long userId, Long skinItemId);

    @Query("SELECT ui.skinItem.id FROM UserInventoryEntity ui WHERE ui.userId = :userId")
    List<Long> findSkinItemIdsByUserId(@Param("userId") Long userId);
}
