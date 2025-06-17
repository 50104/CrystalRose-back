package com.rose.back.global.exception;

public class MissingAccessTokenException extends RuntimeException {

    public MissingAccessTokenException() {
        super("Access token이 존재하지 않거나 Bearer 형식이 아닙니다.");
    }

    public MissingAccessTokenException(String message) {
        super(message);
    }
}