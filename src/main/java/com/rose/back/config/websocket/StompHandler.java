package com.rose.back.config.websocket;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.chat.service.ChatService;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
public class StompHandler implements ChannelInterceptor {

  private final JwtTokenProvider jwtProvider;
  private final ChatService chatService;
  private final StringRedisTemplate redisTemplate;

  public StompHandler(JwtTokenProvider jwtProvider,
                      ChatService chatService,
                      @Qualifier("chatPubSub") StringRedisTemplate redisTemplate) {
    this.jwtProvider = jwtProvider;
    this.chatService = chatService;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT == accessor.getCommand()) {
      System.out.println("CONNECT 요청 시 토큰 유효성 검증");
      String token = accessor.getFirstNativeHeader("access"); 

      if (token != null && !token.isEmpty()) {
          if (jwtProvider.validateToken(token)) {
              System.out.println("토큰 검증 완료");

              String userId = jwtProvider.getUserId(token);
              String userRole = jwtProvider.getUserRole(token);
              String userNick = jwtProvider.getUserNick(token);
              System.out.println("유저 정보: ID=" + userId + ", 닉네임=" + userNick + ", 역할=" + userRole);
              String roomId = accessor.getFirstNativeHeader("roomId");
              if (roomId == null) {
                throw new IllegalArgumentException("roomId 누락");
              }

              // 접속자 Redis 등록
              redisTemplate.opsForSet().add("chat:room:" + roomId + ":members", userId);
              redisTemplate.expire("chat:room:" + roomId + ":members", Duration.ofMinutes(10));

              accessor.getSessionAttributes().put("userId", userId);
              accessor.getSessionAttributes().put("roomId", roomId);
          } else {
              System.out.println("토큰 검증 실패");
              throw new AuthenticationServiceException("유효하지 않은 토큰입니다.");
          }
      } else {
          System.out.println("토큰이 존재하지 않거나 유효하지 않습니다.");
          throw new AuthenticationServiceException("토큰 누락");
      }
    }

    if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
      System.out.println("SUBSCRIBE 검증");
      String token = accessor.getFirstNativeHeader("access");
      if (token == null || !jwtProvider.validateToken(token)) {
        throw new AuthenticationServiceException("유효하지 않은 토큰입니다.");
      }

      String userId = jwtProvider.getUserId(token);
      String roomId = accessor.getDestination().split("/")[2];
      if (!chatService.isRoomPaticipant(userId, Long.parseLong(roomId))) {
        throw new AuthenticationServiceException("해당 방에 참여 권한이 없습니다.");
      }
    }

    return message;
  }
}
