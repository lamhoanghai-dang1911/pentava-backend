package com.nqt.manage.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingOverviewResponseDTO {
    private List<BannerResponseDTO> banners;
    private List<FeatureResponseDTO> features;
    private List<TestimonialResponseDTO> testimonials;
    private List<FaqResponseDTO> faqs;
    private List<PricingPackageResponseDTO> pricingPackages;
    private List<PlatformMetricResponseDTO> metrics;
    private Map<String, String> siteConfigs;
}
