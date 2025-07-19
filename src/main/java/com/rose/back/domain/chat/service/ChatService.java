package com.rose.back.domain.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.domain.chat.dto.ChatMessageReqDto;
import com.rose.back.domain.chat.dto.ChatRoomInfoDto;
import com.rose.back.domain.chat.dto.ChatRoomListResDto;
import com.rose.back.domain.chat.dto.MyChatListResDto;
import com.rose.back.domain.chat.dto.RoomInfoResponse;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ChatService {
  
  private final ChatRoomRepository chatRoomRepository;
  private final ChatParticipantRepository chatParticipantRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final StringRedisTemplate stringRedisTemplate;

  public ChatService(
      ChatRoomRepository chatRoomRepository,
      ChatParticipantRepository chatParticipantRepository,
      ChatMessageRepository chatMessageRepository,
      ReadStatusRepository readStatusRepository,
      UserRepository userRepository,
      @Qualifier("stringRedisTemplate") StringRedisTemplate stringRedisTemplate
  ) {
      this.chatRoomRepository = chatRoomRepository;
      this.chatParticipantRepository = chatParticipantRepository;
      this.chatMessageRepository = chatMessageRepository;
      this.readStatusRepository = readStatusRepository;
      this.userRepository = userRepository;
      this.stringRedisTemplate = stringRedisTemplate;
  }

  public void saveMessage(Long roomId, ChatMessageReqDto chatMessageReqDto, String userId) {
    ChatRoom chatRoom = getChatRoomById(roomId);
    
    UserEntity sender = userRepository.findByUserId(userId);
    if (sender == null) {
        throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
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
    UserEntity userEntity = getCurrentUser();
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
    ChatRoom chatRoom = getChatRoomById(roomId);
    UserEntity userEntity = getCurrentUser();

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
    ChatRoom chatRoom = getChatRoomById(roomId);
    UserEntity userEntity = getCurrentUser();

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
  public boolean isRoomParticipant(String userId, Long roomId) {
    ChatRoom chatRoom = getChatRoomById(roomId);
    UserEntity userEntity = getCurrentUser();

    List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom); // 채팅방 참여자 조회
    for(ChatParticipant c : chatParticipants) {
      if(c.getUserEntity().equals(userEntity)) {
        return true;
      }
    }
    return false;
  }

  public void messageRead(Long roomId) {
    ChatRoom chatRoom = getChatRoomById(roomId);
    UserEntity userEntity = getCurrentUser();

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
    UserEntity userEntity = getCurrentUser();
    List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByUserEntity(userEntity);

    return chatParticipants.stream()
      .map(c -> {
        Long count = readStatusRepository.countByChatRoomAndUserEntityAndIsReadFalse(c.getChatRoom(), userEntity);
        return MyChatListResDto.builder()
          .roomId(c.getChatRoom().getId())
          .roomName(c.getDisplayName())
          .isGroupChat(c.getChatRoom().getIsGroupChat())
          .unReadCount(count)
          .build();
      }).collect(Collectors.toList());
  }

  // 채팅방 나가기
  public void leaveGroupChatRoom(Long roomId){
    ChatRoom chatRoom = getChatRoomById(roomId);
    UserEntity userEntity = getCurrentUser();

    // if(chatRoom.getIsGroupChat().equals("N")){
    //   throw new IllegalArgumentException("그룹 채팅방이 아닙니다17.");
    // }
    ChatParticipant c = chatParticipantRepository.findByChatRoomAndUserEntity(chatRoom, userEntity).orElseThrow(() -> new EntityNotFoundException("참여자를 찾을 수 없습니다.18"));
    chatParticipantRepository.delete(c);

    List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
    if(chatParticipants.isEmpty()){
      chatRoomRepository.delete(chatRoom);
    }
  }

  public RoomInfoResponse getOrCreatePrivateRoom(Long otherUserNo) {
    UserEntity currentUser = getCurrentUser();
    UserEntity targetUser = userRepository.findByUserNo(otherUserNo);
    Optional<ChatRoom> existingRoom = chatParticipantRepository.findExistingPrivateRoom(currentUser.getUserNo(), targetUser.getUserNo());

    ChatRoom room = existingRoom.orElseGet(() -> {
      String roomName = currentUser.getUserNo() < targetUser.getUserNo()
        ? currentUser.getUserNo() + "-" + targetUser.getUserNo()
        : targetUser.getUserNo() + "-" + currentUser.getUserNo();

      ChatRoom newRoom = chatRoomRepository.save(ChatRoom.builder()
        .isGroupChat("N")
        .roomName(roomName)
        .build());

      chatParticipantRepository.save(ChatParticipant.of(newRoom, currentUser, targetUser.getUserNick())); // 나에게 보여지는 상대 닉네임
      chatParticipantRepository.save(ChatParticipant.of(newRoom, targetUser, currentUser.getUserNick())); // 상대에게 보여지는 내 닉네임
      return newRoom;
    });
    return new RoomInfoResponse(room.getId(), targetUser.getUserNick()); // 상대방 닉네임 기준 전달
  }

  // 채팅방 이름 변경
  public ChatRoomInfoDto getChatRoomInfo(Long roomId) {
    ChatRoom chatRoom = getChatRoomById(roomId);
    List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);

    List<ChatRoomInfoDto.ParticipantDto> participantDtos = participants.stream()
        .map(p -> {
            UserEntity user = p.getUserEntity();
            return ChatRoomInfoDto.ParticipantDto.builder()
                .userId(user.getUserId())
                .userNick(user.getUserNick())
                .userProfileImg(user.getUserProfileImg())
                .build();
        }).collect(Collectors.toList());

    return ChatRoomInfoDto.builder()
        .roomId(chatRoom.getId())
        .roomName(chatRoom.getRoomName())
        .isGroupChat(chatRoom.getIsGroupChat())
        .participants(participantDtos)
        .build();
  }

  private UserEntity getCurrentUser() {
    UserEntity user = userRepository.findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    if (user == null) {
        throw new EntityNotFoundException("현재 로그인 사용자를 찾을 수 없습니다.");
    }
    return user;
  }

  private ChatRoom getChatRoomById(Long roomId) {
    return chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
  }
}