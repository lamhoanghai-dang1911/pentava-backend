package com.nqt.manage.usecase.interactor;

import com.nqt.manage.usecase.boundary.*;
import com.nqt.manage.usecase.dto.LandingOverviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LandingOverviewInteractor implements LandingOverviewInputPort {

    private final BannerInputPort bannerInputPort;
    private final FeatureInputPort featureInputPort;
    private final TestimonialInputPort testimonialInputPort;
    private final FaqInputPort faqInputPort;
    private final PricingPackageInputPort pricingPackageInputPort;
    private final PlatformMetricInputPort platformMetricInputPort;
    private final SiteConfigInputPort siteConfigInputPort;

    @Override
    public LandingOverviewResponseDTO getLandingOverview() {
        return LandingOverviewResponseDTO.builder()
                .banners(bannerInputPort.getActiveBanners())
                .features(featureInputPort.getActiveFeatures())
                .testimonials(testimonialInputPort.getActiveTestimonials())
                .faqs(faqInputPort.getActiveFaqs())
                .pricingPackages(pricingPackageInputPort.getActivePricingPackages())
                .metrics(platformMetricInputPort.getActiveMetrics())
                .siteConfigs(siteConfigInputPort.getPublicConfigsMap())
                .build();
    }
}
