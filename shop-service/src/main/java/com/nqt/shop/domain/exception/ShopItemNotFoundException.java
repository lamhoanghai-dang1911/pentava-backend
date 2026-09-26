package com.nqt.shop.domain.exception;

public class ShopItemNotFoundException extends RuntimeException {
    public ShopItemNotFoundException(String message) {
        super(message);
    }
}
