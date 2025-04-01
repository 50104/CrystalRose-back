package com.rose.back.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.chat.repository.ChatMessageRepository;
import com.rose.back.chat.repository.ChatParticipantRepository;
import com.rose.back.chat.repository.ChatRoomRepository;
import com.rose.back.chat.repository.ReadStatusRepository;
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
}