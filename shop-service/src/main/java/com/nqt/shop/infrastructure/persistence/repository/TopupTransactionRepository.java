package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.TopupTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopupTransactionRepository extends JpaRepository<TopupTransactionEntity, Long> {
    Optional<TopupTransactionEntity> findByTransactionCode(String transactionCode);

    List<TopupTransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
