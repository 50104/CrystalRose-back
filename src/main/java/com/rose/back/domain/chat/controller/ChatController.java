package com.rose.back.domain.chat.controller;

import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import com.rose.back.domain.chat.dto.ChatRoomListResDto;
import com.rose.back.domain.chat.dto.MyChatListResDto;
import com.rose.back.domain.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
        log.info("[POST][room/group/create] 그룹 채팅방 개설 요청 - roomName: {}", roomName);
        try {
            chatService.createGroupRoom(roomName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("그룹 채팅방 개설 실패 - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("채팅방 생성 실패");
        }
    }

    // 그룹 채팅 목록 조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupChatRooms() {
        log.info("[GET][room/group/list] 그룹 채팅 목록 조회 요청");
        try {
            List<ChatRoomListResDto> chatRooms = chatService.getGroupChatRooms();
            return new ResponseEntity<>(chatRooms, HttpStatus.OK);
        } catch (Exception e) {
            log.error("그룹 채팅 목록 조회 실패 - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회 실패");
        }
    }

    // 그룹 채팅방 참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable("roomId") Long roomId) {
        log.info("[POST][room/group/{}/join] 그룹 채팅방 참여 요청", roomId);
        try {
            chatService.addParticipantToGroupChat(roomId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("그룹 채팅방 참여 실패 - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("참여 실패");
        }
    }

    // 이전 메세지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable("roomId") Long roomId, @RequestParam(required = false) LocalDateTime cursor) {
        log.info("[GET][history/{}] 채팅 히스토리 조회 요청", roomId);
        try {
            List<ChatMessageReqDto> chatMessageDtos = chatService.getChatHistory(roomId, cursor);
            return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
        } catch (Exception e) {
            log.error("채팅 히스토리 조회 실패 - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("히스토리 조회 실패");
        }
    }

    // 메세지 읽음 처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable("roomId") Long roomId) {
        log.info("[POST][room/{}/read] 메시지 읽음 처리 요청", roomId);
        try {
            chatService.messageRead(roomId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("메시지 읽음 처리 실패 - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("읽음 처리 실패");
        }
    }

    // 내 채팅방 목록 조회 : roomId, roomName, 그룹 채팅 여부, 메세지 읽음 개수
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms() {
        log.info("[GET][my/rooms] 내 채팅방 목록 조회 요청");
        try {
            List<MyChatListResDto> myChatListResDtos = chatService.getMyChatRooms();
            return new ResponseEntity<>(myChatListResDtos, HttpStatus.OK);
        } catch (Exception e) {
            log.error("내 채팅방 목록 조회 실패 - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회 실패");
        }
    }

    // 그룹 채팅방 나가기
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable("roomId") Long roomId) {
        log.info("[DELETE][room/group/{}/leave] 그룹 채팅방 나가기 요청", roomId);
        try {
            chatService.leaveGroupChatRoom(roomId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("그룹 채팅방 나가기 실패 - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("나가기 실패");
        }
    }

    // 개인 채팅방 생성 or 기존 반환
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam("otherMemberId") Long otherMemberId) {
        log.info("[POST][room/private/create] 개인 채팅방 생성 또는 조회 요청 - 대상 ID: {}", otherMemberId);
        try {
            Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
            return new ResponseEntity<>(roomId, HttpStatus.OK);
        } catch (Exception e) {
            log.error("개인 채팅방 생성/조회 실패 - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("실패");
        }
    }
}
