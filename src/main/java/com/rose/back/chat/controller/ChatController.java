package com.rose.back.chat.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.chat.dto.ChatMessageReqDto;
import com.rose.back.chat.dto.ChatRoomListResDto;
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

  // 그룹 채팅 목록 조회
  @GetMapping("/room/group/list")
  public ResponseEntity<?> getGroupChatRooms() {
    List<ChatRoomListResDto> chatRooms = chatService.getGroupChatRooms();
    return new ResponseEntity<>(chatRooms, HttpStatus.OK);
  }

  // 그룹 채팅방 참여
  @PostMapping("/room/group/{roomId}/join")
  public ResponseEntity<?> joinGroupChatRoom(@PathVariable("roomId") Long roomId) {
    chatService.addParticipantToGroupChat(roomId);
    return ResponseEntity.ok().build();
  }

  // 이전 메세지 조회
  @GetMapping("/history/{roomId}")
  public ResponseEntity<?> getChatHistory(@PathVariable("roomId") Long roomId) {
    List<ChatMessageReqDto> chatMessageDtos = chatService.getChatHistory(roomId);
    return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
  }
}
