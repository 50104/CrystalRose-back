package com.rose.back.chat.config;

import com.rose.back.user.provider.JwtProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {

  private final JwtProvider jwtProvider;

  public StompHandler(JwtProvider jwtProvider) {
    this.jwtProvider = jwtProvider;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT == accessor.getCommand()) {
      System.out.println("CONNECT 요청 시 토큰 유효성 검증");

      String token = accessor.getFirstNativeHeader("access");

      if (token != null && !token.isEmpty()) {
        // 토큰 검증
        if (jwtProvider.validateToken(token)) {
          System.out.println("토큰 검증 완료");
          String userId = jwtProvider.getUserId(token);
          String userRole = jwtProvider.getUserRole(token);
          String userNick = jwtProvider.getUserNick(token);
          System.out.println("유저 정보: ID=" + userId + ", 닉네임=" + userNick + ", 역할=" + userRole);
        } else {
          System.out.println("토큰 검증 실패");
        }
      } else {
        System.out.println("토큰이 존재하지 않거나 유효하지 않습니다.");
      }
    }
    return message;
  }
}
