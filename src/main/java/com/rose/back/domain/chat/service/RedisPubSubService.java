package com.rose.back.domain.chat.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.domain.chat.dto.ChatMessageReqDto;

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
    String payload = new String(message.getBody());
    try {
      ChatMessageReqDto chatMessageReqDto = objectMapper.readValue(payload, ChatMessageReqDto.class);
      messagingTemplate.convertAndSend("/topic/" + chatMessageReqDto.getRoomId(), chatMessageReqDto); // StompController
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
