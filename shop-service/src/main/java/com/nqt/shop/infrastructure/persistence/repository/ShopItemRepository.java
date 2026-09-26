package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.ShopItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItemEntity, Long> {
    List<ShopItemEntity> findByIsForSaleTrue();

    Optional<ShopItemEntity> findBySkinItemId(Long skinItemId);

    Optional<ShopItemEntity> findByCode(String code);
}
