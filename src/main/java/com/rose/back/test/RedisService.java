package com.rose.back.test;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
  private RedisRepository redisRepository;

  public RedisService(RedisRepository redisRepository) {
    this.redisRepository = redisRepository;
  }

  @Cacheable(cacheNames = "getRedis", key = "'redis:page:' + #page + ':size:' + #size", cacheManager = "redisCacheManager")
  public List<Redis> getRedis(int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<Redis> pageOfRedis = redisRepository.findAllByOrderByCreatedAtDesc(pageable);
    return pageOfRedis.getContent();
  }
}
