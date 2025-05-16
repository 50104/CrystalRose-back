package com.rose.back.global.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rose.back.domain.user.dto.response.CommonResponse;

@RestControllerAdvice
public class ValidationExceptionHandler {
    
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<CommonResponse> validationExceptionHandler(Exception exception) {
        return CommonResponse.validationFail();
    }
}
