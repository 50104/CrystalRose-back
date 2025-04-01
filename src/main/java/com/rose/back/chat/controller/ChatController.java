package com.rose.back.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.chat.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {
  
  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  // 그룹 채팅방 개설
  @PostMapping("/room/group/create")
  public ResponseEntity<?> createGroupRoom(@RequestParam("roomName") String roomName) {
    chatService.createGroupRoom(roomName);
    return ResponseEntity.ok().build();
  }
}
