package com.rose.back.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.chat.entity.ChatParticipant;
import com.rose.back.chat.entity.ChatRoom;
import com.rose.back.user.entity.UserEntity;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
  
  List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

  Optional<ChatParticipant> findByChatRoomAndMember(ChatRoom chatRoom, UserEntity userEntity);
}
