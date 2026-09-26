package com.nqt.skin.domain.exception;

public class SkinNotOwnedException extends RuntimeException {
    public SkinNotOwnedException(String message) {
        super(message);
    }
}
