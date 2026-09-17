package com.nqt.auth.usecase.boundary;

import com.nqt.auth.usecase.dto.GoogleUserInfo;

public interface GoogleTokenGateway {
    GoogleUserInfo verify(String idToken);
}