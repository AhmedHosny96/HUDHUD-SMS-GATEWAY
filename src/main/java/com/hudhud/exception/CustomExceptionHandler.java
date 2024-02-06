//package com.hudhud.exception;
//
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//
//import java.time.LocalDateTime;
//
//
//@ControllerAdvice
////@ResponseStatus
//public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
//
//    @ExceptionHandler(CustomException.class)
//    public ResponseEntity<ApiError> customException(CustomException e, HttpServletRequest request) {
//
//        var errorResponse = new ApiError(
//                "400",
//                e.getMessage(),
//                request.getRequestURI(),
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(errorResponse);
//    }
//
//
////    @ExceptionHandler(value = JwtExpiredException.class)
////    public ResponseEntity<?> jwtExceptionHandler(JwtExpiredException e) {
////        var errorResponse = new JSONObject();
////        errorResponse.put("response", "999");
////        errorResponse.put("responseDescription", "JWT token is expired");
////        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
////                .body(errorResponse.toString());
////    }
//}