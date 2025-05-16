package com.rose.back.domain.auth.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.rose.back.domain.auth.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final StringRedisTemplate jwtRedisTemplate;
    private static final String REFRESH_PREFIX = "RT:";

    @Override
    public void save(String userId, String refreshToken, long expirationMillis) {
        jwtRedisTemplate.opsForValue().set(
            REFRESH_PREFIX + userId,
            refreshToken,
            expirationMillis,
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean isValid(String userId, String refreshToken) {
        String stored = jwtRedisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
        return refreshToken.equals(stored);
    }

    @Override
    public void delete(String userId) {
        jwtRedisTemplate.delete(REFRESH_PREFIX + userId);
    }
}
