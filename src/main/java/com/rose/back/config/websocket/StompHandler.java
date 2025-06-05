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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    // CONNECT 시 토큰 검증
    if (StompCommand.CONNECT == accessor.getCommand()) {
      log.info("CONNECT 요청: {}", accessor.getSessionId());

      String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
      if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        throw new AuthenticationServiceException("Authorization 헤더가 누락되었거나 잘못되었습니다.");
      }
      String token = authorizationHeader.substring(7);

      if (!jwtProvider.validateToken(token)) {
        throw new AuthenticationServiceException("유효하지 않은 토큰입니다.");
      }
      log.info("토큰 검증 완료: {}", token);

      String userId = jwtProvider.getUserId(token);
      String userRole = jwtProvider.getUserRole(token);
      String userNick = jwtProvider.getUserNick(token);
      log.info("유저 정보: ID={}, 닉네임={}, 역할={}", userId, userNick, userRole);

      String roomId = accessor.getFirstNativeHeader("roomId");
      if (roomId == null) {
        throw new IllegalArgumentException("roomId 누락");
      }

      // Redis 접속자 등록
      redisTemplate.opsForSet().add("chat:room:" + roomId + ":members", userId);
      redisTemplate.expire("chat:room:" + roomId + ":members", Duration.ofMinutes(10));

      // 세션에 사용자 정보 저장 → SUBSCRIBE에서 재활용 가능
      accessor.getSessionAttributes().put("userId", userId);
      accessor.getSessionAttributes().put("roomId", roomId);
    }

    // SUBSCRIBE 시에는 세션정보 활용
    if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
      log.info("SUBSCRIBE 요청: {}", accessor.getSessionId());

      // CONNECT에서 저장해둔 유저 정보 꺼내기
      String userId = (String) accessor.getSessionAttributes().get("userId");
      if (userId == null) {
        throw new AuthenticationServiceException("세션에 유저 정보가 없습니다.");
      }

      String roomId = accessor.getDestination().split("/")[2];
      if (!chatService.isRoomPaticipant(userId, Long.parseLong(roomId))) {
        throw new AuthenticationServiceException("해당 방에 참여 권한이 없습니다.");
      }
    }
    return message;
  }
}
