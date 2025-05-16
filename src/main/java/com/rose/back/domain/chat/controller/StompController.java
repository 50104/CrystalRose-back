package com.rose.back.domain.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import com.rose.back.domain.chat.service.ChatService;
import com.rose.back.domain.chat.service.RedisPubSubService;

@Controller
public class StompController {
  
  private final SimpMessageSendingOperations messagingTemplate;
  private final ChatService chatService;
  private final RedisPubSubService pubSubService;

  public StompController(SimpMessageSendingOperations messagingTemplate, ChatService chatService, RedisPubSubService pubSubService) {
    this.messagingTemplate = messagingTemplate;
    this.chatService = chatService;
    this.pubSubService = pubSubService;
  } 

  // // 방법1 : MessageMapping(수신)과 SendTo(topic에 메세지 전달)한꺼번에 처리
  // // @MessageMapping : 클라이언트에서 특정 publish/roomId 형태로 메세지를 발행 시 MessageMapping 수신
  // // @SendTo : 해당 roomId에 메세지를 발행하여 구독중인 클라이언트에게 메세지 전송
  // // @DestinationVariable : @MessageMapping 어노테이션으로 정의된 WebSocket Controller 내에서만 사용
  // @MessageMapping("/{roomId}")
  // @SendTo("/topic/{roomId}")
  // public String sendMessage(@DestinationVariable("roomId") Long roomId, String message) {
  //   System.out.println("roomId : " + roomId + ", message : " + message);
  //   return message;
  // }

  // 방법2 : MessageMapping 어노테이션만 활용  @MessageMapping("/{roomId}")
  @MessageMapping("/{roomId}")
  public void sendMessage(@DestinationVariable("roomId") Long roomId, ChatMessageReqDto chatMessageReqDto) {
    try {
      System.out.println("roomId : " + roomId + ", message : " + chatMessageReqDto.getMessage());
      chatService.saveMessage(roomId, chatMessageReqDto);
      chatMessageReqDto.setRoomId(roomId);
      // messagingTemplate.convertAndSend("/topic/" + roomId, chatMessageDto); // sendTo 대신 convertAndSend로 대체
      ObjectMapper objectMapper = new ObjectMapper();
      String message = objectMapper.writeValueAsString(chatMessageReqDto);
      pubSubService.publish("chat", message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
