package com.rose.back.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.chat.entity.ChatMessage;
import com.rose.back.chat.entity.ChatRoom;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  List<ChatMessage> findByChatRoomOrderByCreatedDateAsc(ChatRoom chatRoom);
}
