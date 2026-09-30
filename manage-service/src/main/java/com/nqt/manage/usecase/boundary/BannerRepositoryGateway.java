package com.nqt.manage.usecase.boundary;

import com.nqt.manage.domain.model.Banner;

import java.util.List;
import java.util.Optional;

public interface BannerRepositoryGateway {
    Banner save(Banner banner);
    Optional<Banner> findById(Long id);
    List<Banner> findAll();
    List<Banner> findActiveBanners();
    void deleteById(Long id);
}
