package com.rose.back.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.chat.entity.ChatParticipant;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
  
}
