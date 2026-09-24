package com.nqt.cinema.usecase.exception;

public class CinemaBusinessException extends RuntimeException {
    public CinemaBusinessException(String message) {
        super(message);
    }
}