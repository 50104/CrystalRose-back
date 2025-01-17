package com.rose.back.test;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("Redis")
public class RedisController {

  private RedisService redisService;

  public RedisController(RedisService redisService) {
    this.redisService = redisService;
  }

  @GetMapping("/redisTest")
  public List<Redis> getRedis(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size
  ) {
    return redisService.getRedis(page, size);
  }
}