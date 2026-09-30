package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.infrastructure.persistence.entity.BannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerJpaRepository extends JpaRepository<BannerEntity, Long> {
    List<BannerEntity> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<BannerEntity> findAllByOrderByDisplayOrderAsc();
}
