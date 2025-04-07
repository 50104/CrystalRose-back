package com.rose.back.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rose.back.chat.entity.ChatParticipant;
import com.rose.back.chat.entity.ChatRoom;
import com.rose.back.user.entity.UserEntity;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
  
  List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

  Optional<ChatParticipant> findByChatRoomAndUserEntity(ChatRoom chatRoom, UserEntity userEntity);

  List<ChatParticipant> findAllByUserEntity(UserEntity userEntity);

  // @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id WHERE cp1.userEntity.userNo = :myUserNo AND cp2.userEntity.userNo = :otherUserNo AND cp1.chatRoom.isGroupChat = 'N'")
  // Optional<ChatRoom> findExistingPrivateRoom(@Param("myUserNo") Long myUserNo, @Param("otherUserNo") Long otherUserNo);

  @Query(value = "SELECT cr.* FROM chat_room cr " +
        "INNER JOIN chat_participant cp1 ON cr.id = cp1.room_no " +
        "INNER JOIN chat_participant cp2 ON cr.id = cp2.room_no " +
        "WHERE cp1.user_no = :myUserNo AND cp2.user_no = :otherUserNo " +
        "AND cr.is_group_chat = 'N' LIMIT 1", 
        nativeQuery = true)
  Optional<ChatRoom> findExistingPrivateRoom(@Param("myUserNo") Long myUserNo, @Param("otherUserNo") Long otherUserNo);
}
