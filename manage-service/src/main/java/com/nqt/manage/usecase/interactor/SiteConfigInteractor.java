package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.SiteConfig;
import com.nqt.manage.usecase.boundary.SiteConfigInputPort;
import com.nqt.manage.usecase.boundary.SiteConfigRepositoryGateway;
import com.nqt.manage.usecase.dto.SiteConfigRequestDTO;
import com.nqt.manage.usecase.dto.SiteConfigResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteConfigInteractor implements SiteConfigInputPort {

    private final SiteConfigRepositoryGateway siteConfigRepositoryGateway;

    @Override
    public SiteConfigResponseDTO saveConfig(SiteConfigRequestDTO requestDTO) {
        LocalDateTime now = LocalDateTime.now();
        SiteConfig config = siteConfigRepositoryGateway.findByKey(requestDTO.getConfigKey())
                .orElse(SiteConfig.builder()
                        .configKey(requestDTO.getConfigKey())
                        .build());

        config.setConfigValue(requestDTO.getConfigValue());
        config.setGroupName(requestDTO.getGroupName());
        config.setDescription(requestDTO.getDescription());
        config.setIsPublic(requestDTO.getIsPublic() != null ? requestDTO.getIsPublic() : true);
        config.setUpdatedAt(now);

        return toDTO(siteConfigRepositoryGateway.save(config));
    }

    @Override
    public SiteConfigResponseDTO getConfigByKey(String key) {
        return siteConfigRepositoryGateway.findByKey(key)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình với key: " + key));
    }

    @Override
    public List<SiteConfigResponseDTO> getAllConfigs() {
        return siteConfigRepositoryGateway.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getPublicConfigsMap() {
        return siteConfigRepositoryGateway.findPublicConfigs().stream()
                .collect(Collectors.toMap(SiteConfig::getConfigKey, SiteConfig::getConfigValue, (v1, v2) -> v2));
    }

    @Override
    public void deleteConfig(Long id) {
        if (siteConfigRepositoryGateway.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy cấu hình với ID: " + id);
        }
        siteConfigRepositoryGateway.deleteById(id);
    }

    private SiteConfigResponseDTO toDTO(SiteConfig c) {
        return SiteConfigResponseDTO.builder()
                .id(c.getId())
                .configKey(c.getConfigKey())
                .configValue(c.getConfigValue())
                .groupName(c.getGroupName())
                .description(c.getDescription())
                .isPublic(c.getIsPublic())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
