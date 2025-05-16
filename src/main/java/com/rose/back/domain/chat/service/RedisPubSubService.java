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
  private final ObjectMapper objectMapper;  // ObjectMapper를 멤버 변수로 선언

  public RedisPubSubService(@Qualifier("chatPubSub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messagingTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.messagingTemplate = messagingTemplate;
    this.objectMapper = new ObjectMapper(); // ObjectMapper를 생성자에서 한 번만 생성
  }

  public void publish(String channel, String message) {
    stringRedisTemplate.convertAndSend(channel, message);
  }

  // pattern에는 topic의 이름의 패턴이 담겨있고, 이 패턴을 기반으로 다이나믹한 코딩
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
