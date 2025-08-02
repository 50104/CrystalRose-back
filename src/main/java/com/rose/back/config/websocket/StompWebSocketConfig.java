package com.rose.back.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
  
  private final StompHandler stompHandler;

  public StompWebSocketConfig(StompHandler stompHandler) {
    this.stompHandler = stompHandler;
  }

  @Override
  public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
    registry.addEndpoint("/api/v1/connect")
      .setAllowedOrigins("https://dodorose.com", "https://api.dodorose.com", "http://localhost:3000", "http://localhost:4000")
      // ws:// 가 아닌 http:// 엔드포인트를 사용할 수 있게 해주는 sockjs 라이브러리를 통안 요청을 허용하는 설정
      .withSockJS();
	}

  @Override
  public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
    // /publish/1 형태로 메세지 발행해야 함을 설정
    // /publish로 시작하는 url패턴으로 메세지가 발행되면 @Controller 객체의 @MessageMapping 메소드로 라우팅
    registry.setApplicationDestinationPrefixes("/api/v1/chat/publish");
    // /topic/1 형태로 메세지를 수신(subscribe)해야 함을 설정
    registry.enableSimpleBroker("/api/v1/chat/topic"); // 내장브로커
    // registry.enableStompBrokerRelay("/topic"); // 외장브로커(RabbitMQ, ActiveMQ, Kafka)에 메세지 전달
  }

  // 웹소켓 요청(connect, subscribe, disconnect)등의 요청시에는 http header 등 http메세지를 넣어올 수 있고, 
  // 이를 interceptor를 통해 가로채 토큰 등 검증 가능
  @Override
  public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
    registration
      .interceptors(stompHandler) // StompHandler를 통해 인증 및 권한 검증
      .taskExecutor()
      .corePoolSize(10) // 기본 스레드 풀 크기
      .maxPoolSize(20) // 최대 스레드 풀 크기
      .keepAliveSeconds(60); // 스레드 유지 시간
  }

  @Override
  public void configureClientOutboundChannel(@NonNull ChannelRegistration registration) {
    registration
      .taskExecutor()
      .corePoolSize(10)
      .maxPoolSize(20)
      .keepAliveSeconds(60);
  }
}
