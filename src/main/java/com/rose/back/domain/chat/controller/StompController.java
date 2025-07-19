package com.rose.back.domain.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.domain.chat.controller.docs.StompControllerDocs;
import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import com.rose.back.domain.chat.service.ChatService;
import com.rose.back.domain.chat.service.RedisPubSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class StompController implements StompControllerDocs {

    private final ChatService chatService;
    private final RedisPubSubService pubSubService;
    private final ObjectMapper objectMapper;

    public StompController(ChatService chatService, RedisPubSubService pubSubService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.pubSubService = pubSubService;
        this.objectMapper = objectMapper;
    }

    // STOMP 메시지 수신 및 Redis Pub/Sub 발행
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable("roomId") Long roomId, 
                          ChatMessageReqDto chatMessageReqDto,
                          SimpMessageHeaderAccessor headerAccessor) {
        log.info("[STOMP][roomId: {}] 채팅 메시지 수신: {}", roomId, chatMessageReqDto.getMessage());
        try {
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                log.error("[STOMP] 세션에 userId가 없습니다 - roomId: {}", roomId);
                return;
            }
            chatService.saveMessage(roomId, chatMessageReqDto, userId);

            chatMessageReqDto.setRoomId(roomId);
            String message = objectMapper.writeValueAsString(chatMessageReqDto);
            pubSubService.publish("chat", message);
            log.info("[REDIS][channel: chat] 메시지 발행 성공");
        } catch (Exception e) {
            log.error("[STOMP] 메시지 처리 중 오류 - roomId={}, message={}", roomId, chatMessageReqDto.getMessage(), e);
        }
    }
}
