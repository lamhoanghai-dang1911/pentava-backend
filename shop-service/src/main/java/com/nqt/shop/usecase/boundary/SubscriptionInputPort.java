package com.nqt.shop.usecase.boundary;

import com.nqt.shop.infrastructure.persistence.entity.SubscriptionOrderEntity;
import com.nqt.shop.usecase.dto.*;

import java.util.List;

public interface SubscriptionInputPort {

    // === Client / User Endpoints ===
    List<SubscriptionPlanResponseDTO> getActivePlans();

    UserSubscriptionDetailResponseDTO getMySubscription(Long userId);

    SubscriptionPurchaseInitResponseDTO initPurchase(Long userId, SubscriptionPurchaseInitRequestDTO request);

    void mockConfirmPurchase(String transactionCode);

    // === Payment Webhook Callback ===
    void processSubscriptionOrderPayment(SubscriptionOrderEntity order, SepayWebhookPayloadDTO payload);

    // === Entitlement Checking (cho internal services) ===
    UserEntitlementCheckResponseDTO checkUserEntitlement(Long userId, String featureCode);

    UserAllEntitlementsResponseDTO getAllUserEntitlements(Long userId);

    // === Admin Management Endpoints ===
    List<SubscriptionPlanResponseDTO> getAllPlansForAdmin();

    SubscriptionPlanResponseDTO getPlanByIdForAdmin(Long planId);

    SubscriptionPlanResponseDTO updatePlanInfo(Long planId, UpdatePlanInfoRequestDTO request);

    SubscriptionPlanResponseDTO updatePlanFeatures(Long planId, UpdatePlanFeatureConfigRequestDTO request);

    List<SubscriptionFeatureResponseDTO> getAllFeaturesForAdmin();

    SubscriptionFeatureResponseDTO createFeature(CreateFeatureRequestDTO request);

    List<UserSubscriptionDetailResponseDTO> getAllSubscribersForAdmin();
}
