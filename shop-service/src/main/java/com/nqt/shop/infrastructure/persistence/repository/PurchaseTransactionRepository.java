package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.PurchaseTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransactionEntity, Long> {
    List<PurchaseTransactionEntity> findByUserIdOrderByPurchasedAtDesc(Long userId);
}
