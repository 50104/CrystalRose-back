package com.rose.back.domain.auth.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.rose.back.domain.auth.service.AccessTokenBlacklistService;

@Service
public class AccessTokenBlacklistServiceImpl implements AccessTokenBlacklistService {

    private final StringRedisTemplate jwtRedisTemplate;
    private static final String BLACKLIST_PREFIX = "BL:";

    public AccessTokenBlacklistServiceImpl(@Qualifier("jwtRedisTemplate") StringRedisTemplate jwtRedisTemplate) {
        this.jwtRedisTemplate = jwtRedisTemplate;
    }

    @Override
    public void blacklist(String token, long expirationMillis) {
        jwtRedisTemplate.opsForValue().set(
            BLACKLIST_PREFIX + token,
            "blacklisted",
            expirationMillis,
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean isBlacklisted(String token) {
        return jwtRedisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }
}
