package com.nqt.shop.infrastructure.grpc;

import com.nqt.common.grpc.skin.*;
import com.nqt.shop.usecase.boundary.SkinServiceGateway;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SkinGrpcClientAdapter implements SkinServiceGateway {

    private static final Logger log = LoggerFactory.getLogger(SkinGrpcClientAdapter.class);
    private static final long TIMEOUT_SECONDS = 5;

    @GrpcClient("skin-service")
    private SkinInternalServiceGrpc.SkinInternalServiceBlockingStub skinServiceStub;

    @Override
    public List<Long> getUserOwnedSkinIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        try {
            GetUserOwnedSkinIdsResponse response = skinServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getUserOwnedSkinIds(GetUserOwnedSkinIdsRequest.newBuilder().setUserId(userId).build());
            return response.getSkinItemIdsList();
        } catch (Exception e) {
            log.error("gRPC call getUserOwnedSkinIds thất bại với userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean checkSkinOwned(Long userId, Long skinItemId) {
        if (userId == null || skinItemId == null) {
            return false;
        }
        try {
            CheckSkinOwnedResponse response = skinServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .checkSkinOwned(CheckSkinOwnedRequest.newBuilder()
                            .setUserId(userId)
                            .setSkinItemId(skinItemId)
                            .build());
            return response.getOwned();
        } catch (Exception e) {
            log.error("gRPC call checkSkinOwned thất bại: userId={}, skinId={}", userId, skinItemId, e);
            return false;
        }
    }

    @Override
    public boolean grantSkin(Long userId, Long skinItemId) {
        try {
            GrantSkinResponse response = skinServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .grantSkin(GrantSkinRequest.newBuilder()
                            .setUserId(userId)
                            .setSkinItemId(skinItemId)
                            .build());
            return response.getSuccess();
        } catch (Exception e) {
            log.error("gRPC call grantSkin thất bại: userId={}, skinId={}", userId, skinItemId, e);
            return false;
        }
    }
}
