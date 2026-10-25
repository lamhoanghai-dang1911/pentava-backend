package com.nqt.shop.infrastructure.persistence.repository;

import com.nqt.shop.infrastructure.persistence.entity.UserWalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWalletEntity, Long> {
    Optional<UserWalletEntity> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(w.rubyBalance), 0) FROM UserWalletEntity w")
    long getTotalCirculatingRuby();
}

