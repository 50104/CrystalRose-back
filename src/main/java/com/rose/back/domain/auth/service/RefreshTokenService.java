package com.rose.back.domain.auth.service;

public interface RefreshTokenService {
    void save(String userId, String refreshToken, long expirationMillis);
    boolean isValid(String userId, String refreshToken);
    void delete(String userId);
}