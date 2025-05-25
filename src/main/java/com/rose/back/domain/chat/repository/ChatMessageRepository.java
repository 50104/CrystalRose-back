package com.rose.back.domain.chat.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.chat.entity.ChatMessage;
import com.rose.back.domain.chat.entity.ChatRoom;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  List<ChatMessage> findTop30ByChatRoomOrderByCreatedDateDesc(ChatRoom chatRoom);

  List<ChatMessage> findTop30ByChatRoomAndCreatedDateBeforeOrderByCreatedDateDesc(ChatRoom chatRoom, LocalDateTime before);
}
