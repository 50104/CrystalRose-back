package com.rose.back.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  
}
