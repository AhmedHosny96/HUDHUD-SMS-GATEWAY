package com.hudhud.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class CustomHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiError> customException(CustomException e) {

        var errorResponse = new ApiError(
                "400",
                e.getMessage(),
                "",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatusCode.valueOf(400))
                .body(errorResponse);
    }
}
