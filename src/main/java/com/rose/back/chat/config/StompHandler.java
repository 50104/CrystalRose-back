package com.rose.back.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {
  
  @Value("${spring.jwt.secret}")
  private String secretKey;

  private final JwtDecoder jwtDecoder;

  public StompHandler(JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT == accessor.getCommand()) {
      System.out.println("CONNECT 요청 시 토큰 유효성 검증");
      
      String token = accessor.getFirstNativeHeader("access");

      if (token != null && !token.isEmpty()) {
        // 토큰 검증
        try {
          jwtDecoder.decode(token);
          System.out.println("토큰 검증 완료");
        } catch (JwtException e) {
          System.out.println("토큰 검증 실패: " + e.getMessage());
        }
      } else {
        System.out.println("토큰이 존재하지 않거나 유효하지 않습니다.");
      }
    }
    return message;
  }
}
