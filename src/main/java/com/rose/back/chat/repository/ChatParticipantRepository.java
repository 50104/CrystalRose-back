package com.rose.back.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.chat.entity.ChatParticipant;
import com.rose.back.chat.entity.ChatRoom;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
  
  List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
}
