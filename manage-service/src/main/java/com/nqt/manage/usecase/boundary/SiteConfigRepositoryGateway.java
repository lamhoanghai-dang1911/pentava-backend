package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.SiteConfig;

import java.util.List;
import java.util.Optional;

public interface SiteConfigRepositoryGateway {
    SiteConfig save(SiteConfig siteConfig);
    Optional<SiteConfig> findById(Long id);
    Optional<SiteConfig> findByKey(String configKey);
    List<SiteConfig> findAll();
    List<SiteConfig> findPublicConfigs();
    void deleteById(Long id);
}
