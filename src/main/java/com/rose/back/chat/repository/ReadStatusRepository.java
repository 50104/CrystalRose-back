package com.rose.back.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.chat.entity.ChatRoom;
import com.rose.back.chat.entity.ReadStatus;
import com.rose.back.user.entity.UserEntity;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
  
  List<ReadStatus> findByChatRoomAndUserEntity(ChatRoom chatRoom, UserEntity userEntity); 

  Long countByChatRoomAndUserEntityAndIsReadFalse(ChatRoom chatRoom, UserEntity userEntity); 
}
