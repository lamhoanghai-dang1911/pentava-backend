package com.nqt.auth.usecase.dto;

public record GoogleUserInfo(String googleId, String email, boolean emailVerified, String name) {
}