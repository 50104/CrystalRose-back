package com.rose.back.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rose.back.chat.entity.ChatRoom;
import com.rose.back.chat.entity.ReadStatus;
import com.rose.back.user.entity.UserEntity;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
  
  List<ReadStatus> findByChatRoomAndMember(ChatRoom chatRoom, UserEntity userEntity); 

  Long countByChatRoomAndMemberAndIsReadFalse(ChatRoom chatRoom, UserEntity userEntity); 
}
