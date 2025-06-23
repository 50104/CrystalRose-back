package com.rose.back.domain.chat.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import com.rose.back.domain.chat.dto.ChatRoomInfoDto;
import com.rose.back.domain.chat.dto.ChatRoomListResDto;
import com.rose.back.domain.chat.dto.MyChatListResDto;
import com.rose.back.domain.chat.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    // 그룹 채팅방 개설
    @PostMapping("/room/group/create")
    public ResponseEntity<MessageResponse> createGroupRoom(@RequestParam("roomName") String roomName) {
        log.info("[POST][room/group/create] 그룹 채팅방 개설 요청 - roomName: {}", roomName);
        chatService.createGroupRoom(roomName);
        return ResponseEntity.ok(new MessageResponse("채팅방 생성 완료"));
    }

    // 그룹 채팅 목록 조회
    @GetMapping("/room/group/list")
    public ResponseEntity<List<ChatRoomListResDto>> getGroupChatRooms() {
        log.info("[GET][room/group/list] 그룹 채팅 목록 조회 요청");
        List<ChatRoomListResDto> chatRooms = chatService.getGroupChatRooms();
        return ResponseEntity.ok(chatRooms);
    }

    // 그룹 채팅방 참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<MessageResponse> joinGroupChatRoom(@PathVariable("roomId") Long roomId) {
        log.info("[POST][room/group/{}/join] 그룹 채팅방 참여 요청", roomId);
        chatService.addParticipantToGroupChat(roomId);
        return ResponseEntity.ok(new MessageResponse("참여 완료"));
    }

    // 이전 메세지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<ChatMessageReqDto>> getChatHistory(@PathVariable("roomId") Long roomId, @RequestParam(required = false) LocalDateTime cursor) {
        log.info("[GET][history/{}] 채팅 히스토리 조회 요청", roomId);
        return ResponseEntity.ok(chatService.getChatHistory(roomId, cursor));
    }

    // 메세지 읽음 처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<MessageResponse> messageRead(@PathVariable("roomId") Long roomId) {
        log.info("[POST][room/{}/read] 메시지 읽음 처리 요청", roomId);
        chatService.messageRead(roomId);
        return ResponseEntity.ok(new MessageResponse("읽음 처리 완료"));
    }

    // 내 채팅방 목록 조회 : roomId, roomName, 그룹 채팅 여부, 메세지 읽음 개수
    @GetMapping("/my/rooms")
    public ResponseEntity<List<MyChatListResDto>> getMyChatRooms() {
        log.info("[GET][my/rooms] 내 채팅방 목록 조회 요청");
        return ResponseEntity.ok(chatService.getMyChatRooms());
    }

    // 그룹 채팅방 나가기
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<MessageResponse> leaveGroupChatRoom(@PathVariable("roomId") Long roomId) {
        log.info("[DELETE][room/group/{}/leave] 그룹 채팅방 나가기 요청", roomId);
        chatService.leaveGroupChatRoom(roomId);
        return ResponseEntity.ok(new MessageResponse("퇴장 완료"));
    }

    // 개인 채팅방 생성 or 기존 반환
    @PostMapping("/room/private/create")
    public ResponseEntity<RoomIdResponse> getOrCreatePrivateRoom(@RequestParam("otherMemberId") Long otherMemberId) {
        log.info("[POST][room/private/create] 개인 채팅방 생성 또는 조회 요청 - 대상 ID: {}", otherMemberId);
        Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
        return ResponseEntity.ok(new RoomIdResponse(roomId));
    }

    // 채팅방 이름
    @GetMapping("/room/{roomId}/info")
    public ResponseEntity<ChatRoomInfoDto> getChatRoomInfo(@PathVariable Long roomId) {
        log.info("[GET][room/{}/info] 채팅방 정보 조회 요청", roomId);
        return ResponseEntity.ok(chatService.getChatRoomInfo(roomId));
    }

    public record RoomIdResponse(Long roomId) {}
}