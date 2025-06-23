package com.rose.back.domain.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import com.rose.back.domain.chat.dto.ChatRoomListResDto;
import com.rose.back.domain.chat.dto.MyChatListResDto;
import com.rose.back.domain.chat.entity.ChatMessage;
import com.rose.back.domain.chat.entity.ChatParticipant;
import com.rose.back.domain.chat.entity.ChatRoom;
import com.rose.back.domain.chat.entity.ReadStatus;
import com.rose.back.domain.chat.repository.ChatMessageRepository;
import com.rose.back.domain.chat.repository.ChatParticipantRepository;
import com.rose.back.domain.chat.repository.ChatRoomRepository;
import com.rose.back.domain.chat.repository.ReadStatusRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
  
  private final ChatRoomRepository chatRoomRepository;
  private final ChatParticipantRepository chatParticipantRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final StringRedisTemplate stringRedisTemplate;

  public void saveMessage(Long roomId, ChatMessageReqDto chatMessageReqDto) {
    // 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다.1"));

    // 보낸사람 조회
    UserEntity sender = userRepository.findByUserId(chatMessageReqDto.getSenderId());
    if (sender == null) {
      throw new IllegalArgumentException("사용자가 존재하지 않습니다.2");
    }

    // 메세지 저장
    ChatMessage chatMessage = ChatMessage.builder()
      .chatRoom(chatRoom)
      .userEntity(sender)
      .content(chatMessageReqDto.getMessage())
      .build();
    chatMessageRepository.save(chatMessage);

    // 사용자별로 읽음 여부 저장
    List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
    for(ChatParticipant c : chatParticipants) {
      ReadStatus readStatus = ReadStatus.builder()
        .chatRoom(chatRoom)
        .userEntity(c.getUserEntity())
        .chatMessage(chatMessage)
        .isRead(c.getUserEntity().equals(sender))
        .build();
        readStatusRepository.save(readStatus);
    }
  }

  public void createGroupRoom(String chatRoomName) {
    UserEntity userEntity = userRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.3");
    }
    // 채팅방 생성
    ChatRoom chatRoom = ChatRoom.builder()
      .roomName(chatRoomName)
      .isGroupChat("Y")
      .build();
    chatRoomRepository.save(chatRoom);

    // 채팅 잠여자로 개설자 추가
    ChatParticipant chatParticipant = ChatParticipant.builder()
      .chatRoom(chatRoom)
      .userEntity(userEntity)
      .build();
    chatParticipantRepository.save(chatParticipant);  
  }

  public List<ChatRoomListResDto> getGroupChatRooms(){
    List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
    List<ChatRoomListResDto> dtos = new ArrayList<>();
    for(ChatRoom c : chatRooms) {
      ChatRoomListResDto dto = ChatRoomListResDto
        .builder()
        .roomId(c.getId())
        .roomName(c.getRoomName())
        .build();
        dtos.add(dto);
    }
    return dtos;
  }

  public void addParticipantToGroupChat(Long roomId) {
    // 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다.4"));

    // UserEntity 조회
    UserEntity userEntity = userRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.5");
    }
    if(chatRoom.getIsGroupChat().equals("N")) {
      throw new IllegalArgumentException("그룹 채팅방이 아닙니다.6");
    }

    // 기존 참여자 여부 확인
    Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndUserEntity(chatRoom, userEntity);
    if(!participant.isPresent()) {
      addParticipantToRoom(chatRoom, userEntity);
    }
  }

  // ChatParticipant 객체 생성 후 저장
  public void addParticipantToRoom(ChatRoom chatRoom, UserEntity userEntity) {
    ChatParticipant chatParticipant = ChatParticipant.builder()
      .chatRoom(chatRoom)
      .userEntity(userEntity)
      .build();
    chatParticipantRepository.save(chatParticipant);  
  }

  // 참여자가 아닌 경우 조회 불가
  public List<ChatMessageReqDto> getChatHistory(Long roomId, LocalDateTime cursorCreatedAt) {
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다.7"));

    UserEntity userEntity = userRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());

    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다8.");
    }

    boolean isParticipant = chatParticipantRepository.findByChatRoom(chatRoom).stream()
      .anyMatch(p -> p.getUserEntity().equals(userEntity));
    if (!isParticipant) {
      throw new IllegalArgumentException("채팅방에 참여하고 있지 않은 사용자입니다.9");
    }

    List<ChatMessage> chatMessages;
    if (cursorCreatedAt != null) {
      chatMessages = chatMessageRepository.findTop30ByChatRoomAndCreatedDateBeforeOrderByCreatedDateDesc(chatRoom, cursorCreatedAt);
    } else {
      chatMessages = chatMessageRepository.findTop30ByChatRoomOrderByCreatedDateDesc(chatRoom);
    }

    Collections.reverse(chatMessages); // 최신순 → 오래된 순으로 변환

    return chatMessages.stream().map(m ->
      ChatMessageReqDto.builder()
          .id(m.getId())
          .message(m.getContent())
          .senderId(m.getUserEntity().getUserId())
          .senderNick(m.getUserEntity().getUserNick())
          .senderProfileImg(m.getUserEntity().getUserProfileImg())
          .createdDate(m.getCreatedDate())
          .build()
    ).collect(Collectors.toList());
  }

  // 특정 방에 대한 참여 권한 조회
  public boolean isRoomPaticipant(String userId, Long roomId){
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다.10")); // 채팅방 조회

    UserEntity userEntity = userRepository.findByUserId(userId);
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.11");
    }

    List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom); // 채팅방 참여자 조회
    for(ChatParticipant c : chatParticipants) {
      if(c.getUserEntity().equals(userEntity)) {
        return true;
      }
    }
    return false;
  }

  public void messageRead(Long roomId) {
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다.12"));

    UserEntity userEntity = userRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());

    if (userEntity == null) {
      throw new EntityNotFoundException("사용자가 존재하지 않습니다.13");
    }

    List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndUserEntity(chatRoom, userEntity);
    for (ReadStatus r : readStatuses) {
      r.updateIsRead(true);
      readStatusRepository.save(r);
    }

    // 브로드캐스트 추가
    ChatMessageReqDto readNotice = ChatMessageReqDto.builder()
        .type("READ")
        .roomId(roomId)
        .senderId(userEntity.getUserId())
        .message("읽음 처리됨")
        .build();

    try {
      String json = new ObjectMapper().writeValueAsString(readNotice);
      stringRedisTemplate.convertAndSend("chat", json);
    } catch (Exception e) {
      log.error("읽음 메시지 브로드캐스트 실패", e);
    }
  }

  public List<MyChatListResDto> getMyChatRooms() {
    UserEntity userEntity = userRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.14");
    }
    List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByUserEntity(userEntity);
    List<MyChatListResDto> chatListResDtos = new ArrayList<>();
    for(ChatParticipant c : chatParticipants) {
      Long count = readStatusRepository.countByChatRoomAndUserEntityAndIsReadFalse(c.getChatRoom(), userEntity);
      MyChatListResDto dto = MyChatListResDto.builder()
        .roomId(c.getChatRoom().getId())
        .roomName(c.getChatRoom().getRoomName())
        .isGroupChat(c.getChatRoom().getIsGroupChat())
        .unReadCount(count)
        .build();
      chatListResDtos.add(dto);
    }
    return chatListResDtos;
  }

  // 채팅방 나가기
  public void leaveGroupChatRoom(Long roomId){
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다.15")); // 채팅방 조회

    UserEntity userEntity = userRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.16");
    }

    if(chatRoom.getIsGroupChat().equals("N")){
      throw new IllegalArgumentException("그룹 채팅방이 아닙니다17.");
    }
    ChatParticipant c = chatParticipantRepository.findByChatRoomAndUserEntity(chatRoom, userEntity).orElseThrow(() -> new EntityNotFoundException("참여자를 찾을 수 없습니다.18"));
    chatParticipantRepository.delete(c);

    List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
    if(chatParticipants.isEmpty()){
      chatRoomRepository.delete(chatRoom);
    }
  }

  public Long getOrCreatePrivateRoom(Long otherUserNo) {
    UserEntity userEntity = userRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다19.");
    }
    UserEntity otherUser = userRepository.findByUserNo(otherUserNo);
    if (otherUser == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다20.");
    }

    // 나와 상대방이 1:1 채팅에 이미 참석하고 있다면 해당 roomId return
    Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(userEntity.getUserNo(), otherUser.getUserNo());
    if(chatRoom.isPresent()){
      return chatRoom.get().getId();
    }
    // 만약 1:1 채팅방이 없을 경우 기존 채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
          .isGroupChat("N")
          .roomName(userEntity.getUserNo() + "-" + otherUser.getUserNo())
          .build();
        chatRoomRepository.save(newRoom);
    // 두 사람 모두 참여자로 새롭게 추가
      addParticipantToRoom(newRoom, userEntity);
      addParticipantToRoom(newRoom, otherUser);

      return newRoom.getId();
  }
}