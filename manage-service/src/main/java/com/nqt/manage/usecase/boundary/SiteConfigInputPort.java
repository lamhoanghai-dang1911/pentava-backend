package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.SiteConfigRequestDTO;
import com.nqt.manage.usecase.dto.SiteConfigResponseDTO;

import java.util.List;
import java.util.Map;

public interface SiteConfigInputPort {
    SiteConfigResponseDTO saveConfig(SiteConfigRequestDTO requestDTO);
    SiteConfigResponseDTO getConfigByKey(String key);
    List<SiteConfigResponseDTO> getAllConfigs();
    Map<String, String> getPublicConfigsMap();
    void deleteConfig(Long id);
}
