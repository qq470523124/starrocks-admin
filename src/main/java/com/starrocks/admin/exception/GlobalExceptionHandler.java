package com.starrocks.admin.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        int code = ex.getCode();
        HttpStatus status;

        if (code >= 1001 && code <= 1999) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (code >= 2001 && code <= 2999) {
            status = HttpStatus.BAD_REQUEST;
        } else if (code >= 3000 && code <= 3999) {
            status = HttpStatus.NOT_FOUND;
        } else if (code >= 4001 && code <= 4999) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(code, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(5000, "Internal server error: " + ex.getMessage(), null));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiErrorResponse {
        private int code;
        private String message;
        private Object details;
    }
}
