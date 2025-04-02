package com.rose.back.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.chat.entity.ChatMessage;
import com.rose.back.chat.entity.ChatRoom;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  List<ChatMessage> findByChatRoomOrderByCreatedTimeAsc(ChatRoom chatRoom);
}
