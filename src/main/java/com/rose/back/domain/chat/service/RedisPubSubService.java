package com.rose.back.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisPubSubService implements MessageListener {
  
    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ObjectMapper objectMapper;

    public RedisPubSubService(@Qualifier("chatPubSub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messagingTemplate, ObjectMapper objectMapper) {
      this.stringRedisTemplate = stringRedisTemplate;
      this.messagingTemplate = messagingTemplate;
      this.objectMapper = objectMapper;
    }

    public void publish(String channel, String message) {
      stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);

            ChatMessageReqDto chatMessage = objectMapper.readValue(payload, ChatMessageReqDto.class);
            log.info("[RedisSub] 메시지 수신: {}", chatMessage.getMessage());

            messagingTemplate.convertAndSend(
                "/api/v1/chat/topic/" + chatMessage.getRoomId(),
                chatMessage
            );
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패", e);
        }
    }
}
