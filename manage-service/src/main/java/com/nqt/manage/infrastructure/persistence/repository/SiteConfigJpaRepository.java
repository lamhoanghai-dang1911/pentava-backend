package com.nqt.manage.infrastructure.persistence.repository;

import com.nqt.manage.infrastructure.persistence.entity.SiteConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteConfigJpaRepository extends JpaRepository<SiteConfigEntity, Long> {
    Optional<SiteConfigEntity> findByConfigKey(String configKey);
    List<SiteConfigEntity> findByIsPublicTrue();
}
