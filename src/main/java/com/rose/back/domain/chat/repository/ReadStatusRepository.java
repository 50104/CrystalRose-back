package com.rose.back.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.chat.entity.ChatRoom;
import com.rose.back.domain.chat.entity.ReadStatus;
import com.rose.back.domain.user.entity.UserEntity;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
  
  List<ReadStatus> findByChatRoomAndUserEntity(ChatRoom chatRoom, UserEntity userEntity); 

  Long countByChatRoomAndUserEntityAndIsReadFalse(ChatRoom chatRoom, UserEntity userEntity); 
}
