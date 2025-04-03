package com.rose.back.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.chat.dto.ChatMessageReqDto;
import com.rose.back.chat.dto.ChatRoomListResDto;
import com.rose.back.chat.dto.MyChatListResDto;
import com.rose.back.chat.entity.ChatMessage;
import com.rose.back.chat.entity.ChatParticipant;
import com.rose.back.chat.entity.ChatRoom;
import com.rose.back.chat.entity.ReadStatus;
import com.rose.back.chat.repository.ChatMessageRepository;
import com.rose.back.chat.repository.ChatParticipantRepository;
import com.rose.back.chat.repository.ChatRoomRepository;
import com.rose.back.chat.repository.ReadStatusRepository;
import com.rose.back.user.entity.UserEntity;
import com.rose.back.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ChatService {
  
  private final ChatRoomRepository chatRoomRepository;
  private final ChatParticipantRepository chatParticipantRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, 
  ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, UserRepository userRepository) {
    this.chatRoomRepository = chatRoomRepository;
    this.chatParticipantRepository = chatParticipantRepository;
    this.chatMessageRepository = chatMessageRepository;
    this.readStatusRepository = readStatusRepository;
    this.userRepository = userRepository;
  }

  public void saveMessage(Long roomId, ChatMessageReqDto chatMessageReqDto) {
    // 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

    // 보낸사람 조회
    UserEntity sender = userRepository.findByUserEmail(chatMessageReqDto.getSenderEmail());
    if (sender == null) {
      throw new IllegalArgumentException("사용자가 존재하지 않습니다.");
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
    UserEntity userEntity = userRepository.findByUserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.");
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
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

    // UserEntity 조회
    UserEntity userEntity = userRepository.findByUserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.");
    }

    // 기존 참여자 여부 확인
    Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, userEntity);
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
  public List<ChatMessageReqDto> getChatHistory(Long roomId) {
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

    UserEntity userEntity = userRepository.findByUserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.");
    }

    List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom); // 채팅방 참여자 조회
    boolean check = false;
    for(ChatParticipant c : chatParticipants) {
      if(c.getUserEntity().equals(userEntity)) {
        check = true;
      }
    }
    if(!check) throw new IllegalArgumentException("채팅방에 참여하고 있지 않은 사용자입니다.");
    
    // 특정 room에 대한 message 조회
    List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
    List<ChatMessageReqDto> chatMessageDtos = new ArrayList<>();
    for(ChatMessage c : chatMessages) {
      ChatMessageReqDto chatMessageDto = ChatMessageReqDto.builder()
        .message(c.getContent())
        .senderEmail(c.getUserEntity().getUserEmail())
        .build();
      chatMessageDtos.add(chatMessageDto);
    }
    return chatMessageDtos;
  }

  // 특정 방에 대한 참여 권한 조회
  public boolean isRoomPaticipant(String email, Long roomId){
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다.")); // 채팅방 조회

    UserEntity userEntity = userRepository.findByUserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.");
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
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다.")); // 채팅방 조회

    UserEntity userEntity = userRepository.findByUserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.");
    }

    List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndMember(chatRoom, userEntity); // 읽음 여부 조회
    for(ReadStatus r : readStatuses) {
      r.updateIsRead(true);
    }
  }

  public List<MyChatListResDto> getMyChatRooms() {
    UserEntity userEntity = userRepository.findByUserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.");
    }
    List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(userEntity);
    List<MyChatListResDto> chatListResDtos = new ArrayList<>();
    for(ChatParticipant c : chatParticipants) {
      Long count = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(), userEntity);
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
    ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다.")); // 채팅방 조회

    UserEntity userEntity = userRepository.findByUserEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    if (userEntity == null) {
        throw new EntityNotFoundException("사용자가 존재하지 않습니다.");
    }

    if(chatRoom.getIsGroupChat().equals("N")){
      throw new IllegalArgumentException("그룹 채팅방이 아닙니다.");
    }
    ChatParticipant c = chatParticipantRepository.findByChatRoomAndMember(chatRoom, userEntity).orElseThrow(() -> new EntityNotFoundException("참여자를 찾을 수 없습니다."));
    chatParticipantRepository.delete(c);

    List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
    if(chatParticipants.isEmpty()){
      chatRoomRepository.delete(chatRoom);
    }
  }
}