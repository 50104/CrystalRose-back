package com.rose.back.config.websocket;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import java.time.Duration;

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

        // WebSocket 연결 요청 시 토큰 검증 및 세션 정보 설정
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
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

            // Redis에 접속자 등록 (유효시간 10분)
            redisTemplate.opsForSet().add("chat:room:" + roomId + ":members", userId);
            redisTemplate.expire("chat:room:" + roomId + ":members", Duration.ofMinutes(10));

            // 세션에 유저 정보 저장
            accessor.getSessionAttributes().put("userId", userId);
            accessor.getSessionAttributes().put("roomId", roomId);
        }

        // 메시지 구독 요청 시 권한 확인
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String sessionId = accessor.getSessionId();
            String destination = accessor.getDestination();

            log.info("SUBSCRIBE 요청: sessionId={}, destination={}", sessionId, destination);

            String userId = (String) accessor.getSessionAttributes().get("userId");
            if (userId == null) {
                log.warn("세션에 유저 정보 없음 - sessionId={}", sessionId);
                throw new AuthenticationServiceException("세션에 유저 정보가 없습니다.");
            }

            if (destination == null || !destination.matches("^/api/v1/chat/topic/\\d+$")) {
                log.warn("유효하지 않은 destination: {}", destination);
                throw new IllegalArgumentException("destination 형식이 잘못되었습니다.");
            }

            String[] parts = destination.split("/");
            String roomIdStr = parts[parts.length - 1];

            try {
                Long roomId = Long.parseLong(roomIdStr);
                if (!chatService.isRoomPaticipant(userId, roomId)) {
                    log.warn("구독 권한 없음: userId={}, roomId={}", userId, roomId);
                    throw new AuthenticationServiceException("해당 방에 참여 권한이 없습니다.");
                }
            } catch (NumberFormatException e) {
                log.error("roomId 파싱 실패: {}", roomIdStr);
                throw new IllegalArgumentException("roomId가 유효한 숫자가 아닙니다.");
            }
        }
        return message;
    }
}
