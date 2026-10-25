package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.CashflowRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashflowRecordRepository extends JpaRepository<CashflowRecordEntity, Long>, JpaSpecificationExecutor<CashflowRecordEntity> {
    Optional<CashflowRecordEntity> findByTransactionCode(String transactionCode);
    List<CashflowRecordEntity> findAllByOrderByCreatedAtDesc();
    List<CashflowRecordEntity> findByCreatedAtAfter(LocalDateTime dateTime);
    long countByStatus(String status);
}
