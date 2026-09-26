package com.nqt.skin.interfaceadapters.grpc;

import com.nqt.common.grpc.skin.*;
import com.nqt.skin.usecase.boundary.SkinInputPort;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class SkinInternalGrpcService extends SkinInternalServiceGrpc.SkinInternalServiceImplBase {

    private final SkinInputPort skinInputPort;

    @Override
    public void getUserOwnedSkinIds(GetUserOwnedSkinIdsRequest request, StreamObserver<GetUserOwnedSkinIdsResponse> responseObserver) {
        try {
            long userId = request.getUserId();
            List<Long> ownedIds = skinInputPort.getOwnedSkinIds(userId);
            GetUserOwnedSkinIdsResponse response = GetUserOwnedSkinIdsResponse.newBuilder()
                    .addAllSkinItemIds(ownedIds)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Lỗi khi xử lý getUserOwnedSkinIds cho userId: {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void grantSkin(GrantSkinRequest request, StreamObserver<GrantSkinResponse> responseObserver) {
        try {
            skinInputPort.grantSkin(request.getUserId(), request.getSkinItemId());
            GrantSkinResponse response = GrantSkinResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Cấp skin thành công")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Lỗi khi grantSkin: userId={}, skinId={}", request.getUserId(), request.getSkinItemId(), e);
            GrantSkinResponse response = GrantSkinResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Lỗi: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void checkSkinOwned(CheckSkinOwnedRequest request, StreamObserver<CheckSkinOwnedResponse> responseObserver) {
        try {
            boolean owned = skinInputPort.isSkinOwned(request.getUserId(), request.getSkinItemId());
            CheckSkinOwnedResponse response = CheckSkinOwnedResponse.newBuilder()
                    .setOwned(owned)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Lỗi khi checkSkinOwned: userId={}, skinId={}", request.getUserId(), request.getSkinItemId(), e);
            responseObserver.onError(e);
        }
    }
}
