package com.rose.back.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.chat.dto.ChatMessageReqDto;
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
}