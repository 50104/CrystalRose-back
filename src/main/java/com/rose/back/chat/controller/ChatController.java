package com.rose.back.chat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.chat.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {
  
  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }
}
