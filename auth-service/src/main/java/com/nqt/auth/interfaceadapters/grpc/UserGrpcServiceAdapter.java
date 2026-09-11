package com.nqt.auth.interfaceadapters.grpc;

import com.nqt.auth.usecase.boundary.GetUserQueryInputPort;
import com.nqt.auth.usecase.dto.UserQueryResponseDTO;
import com.nqt.common.grpc.user.GetUserByEmailRequest;
import com.nqt.common.grpc.user.GetUserByIdRequest;
import com.nqt.common.grpc.user.UserResponse;
import com.nqt.common.grpc.user.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

@GrpcService
public class UserGrpcServiceAdapter extends UserServiceGrpc.UserServiceImplBase {

    private final GetUserQueryInputPort getUserQueryInputPort;

    public UserGrpcServiceAdapter(GetUserQueryInputPort getUserQueryInputPort) {
        this.getUserQueryInputPort = getUserQueryInputPort;
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<UserQueryResponseDTO> userOpt = getUserQueryInputPort.getUserByEmail(request.getEmail());
        responseObserver.onNext(toResponse(userOpt));
        responseObserver.onCompleted();
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<UserQueryResponseDTO> userOpt = getUserQueryInputPort.getUserById(request.getUserId());
        responseObserver.onNext(toResponse(userOpt));
        responseObserver.onCompleted();
    }

    private UserResponse toResponse(Optional<UserQueryResponseDTO> userOpt) {
        return userOpt
                .map(u -> UserResponse.newBuilder()
                        .setId(u.getId())
                        .setEmail(u.getEmail())
                        .setName(u.getName() == null ? "" : u.getName())
                        .setFound(true)
                        .build())
                .orElse(UserResponse.newBuilder().setFound(false).build());
    }
}