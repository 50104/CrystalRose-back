package com.rose.back.config.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.rose.back.domain.chat.service.RedisPubSubService;

@Configuration
public class RedisConfig {
  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Bean
  @Primary
  public LettuceConnectionFactory redisConnectionFactory() {
    // Lettuce 라이브러리
    // Redis 연결 관리하는 객체 생성
    // Redis 서버에 대한 정보(host, port) 설정
    return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
  }

  // 연결 기본 객체
  @Bean
  @Qualifier("chatPubSub")
  public RedisConnectionFactory chatPubSubFactory() {
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
    configuration.setHostName(host);
    configuration.setPort(port);
    // redis pub/sub에서는 특정 데이터베이스에 의존적이지 않음
    // configuration.setDatabase(0);
    return new LettuceConnectionFactory(configuration);
  }

  // publish 객체
  // 일반적으로 RedisTemplate<key데이터타입, value데이터타입>을 사용
  @Bean
  @Qualifier("chatPubSub")
  public StringRedisTemplate stringRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

  // subscribe 객체
  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
      MessageListenerAdapter messageListenerAdapter) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));
    return container;
  }

  // redis에서 수신된 메세지를 처리하는 객체 생성
  @Bean
  public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
    // RedisPubSubService의 특정 메서드가 수신된 메세지를 처리할 수 있도록 지정
    return new MessageListenerAdapter(redisPubSubService, "onMessage");
  }
}