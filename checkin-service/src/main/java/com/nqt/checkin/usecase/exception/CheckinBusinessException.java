package com.nqt.checkin.usecase.exception;

public class CheckinBusinessException extends RuntimeException {
    public CheckinBusinessException(String message) {
        super(message);
    }
}