package com.nqt.auth.usecase.interactor;

import com.nqt.auth.domain.User;
import com.nqt.auth.usecase.boundary.LoginInputPort;
import com.nqt.auth.usecase.boundary.LoginOutputPort;
import com.nqt.auth.usecase.dto.LoginRequestDTO;
import com.nqt.auth.usecase.dto.LoginResponseDTO;

import java.util.Optional;

public class LoginInteractor implements LoginInputPort {

    private final UserRepositoryGateway userReadRepository; // Interface do tầng Adapter cung cấp
    private final LoginOutputPort outputPort;

    public LoginInteractor(UserRepositoryGateway userReadRepository, LoginOutputPort outputPort) {
        this.userReadRepository = userReadRepository;
        this.outputPort = outputPort;
    }

    @Override
    public void login(LoginRequestDTO requestDTO) {
        Optional<User> userOpt = userReadRepository.findByEmail(requestDTO.getEmail());

        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(requestDTO.getPassword())) {
            outputPort.presentError("Email hoặc mật khẩu không chính xác");
            return;
        }

        User user = userOpt.get();
        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .accessToken("mock-jwt-token-" + user.getEmail())
                .email(user.getEmail())
                .message("Đăng nhập thành công")
                .build();

        outputPort.present(responseDTO);
    }
}