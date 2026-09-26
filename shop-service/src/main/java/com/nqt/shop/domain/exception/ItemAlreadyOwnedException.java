package com.nqt.shop.domain.exception;

public class ItemAlreadyOwnedException extends RuntimeException {
    public ItemAlreadyOwnedException(String message) {
        super(message);
    }
}
