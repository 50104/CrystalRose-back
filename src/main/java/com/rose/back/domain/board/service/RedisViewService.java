package com.rose.back.domain.board.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisViewService {

    private final StringRedisTemplate redisTemplate;

    public RedisViewService(@Qualifier("viewRedisTemplate") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isDuplicateView(String userId, Long boardNo) {
        String key = "view:" + userId + ":" + boardNo;
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            return true;
        }

        redisTemplate.opsForValue().set(key, "1", Duration.ofHours(48));
        return false;
    }
}
