package com.rose.back.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  
}