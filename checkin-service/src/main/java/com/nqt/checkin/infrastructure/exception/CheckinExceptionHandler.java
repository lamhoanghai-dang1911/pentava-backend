package com.nqt.checkin.infrastructure.exception;

import com.nqt.checkin.usecase.exception.CheckinBusinessException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Chỉ xử lý CheckinBusinessException -> 400 {"message": "..."}.
 * Đặt precedence cao nhất để không bị @ExceptionHandler(Exception.class) (nếu có) bắt trước.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CheckinExceptionHandler {

    @ExceptionHandler(CheckinBusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(CheckinBusinessException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}