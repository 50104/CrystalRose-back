package com.rose.back.domain.auth.service;

public interface AccessTokenBlacklistService {
    void blacklist(String token, long expirationMillis);
    boolean isBlacklisted(String token);
}
