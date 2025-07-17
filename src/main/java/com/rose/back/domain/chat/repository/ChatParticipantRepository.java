package com.rose.back.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rose.back.domain.chat.entity.ChatParticipant;
import com.rose.back.domain.chat.entity.ChatRoom;
import com.rose.back.domain.user.entity.UserEntity;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
  
  List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

  Optional<ChatParticipant> findByChatRoomAndUserEntity(ChatRoom chatRoom, UserEntity userEntity);

  List<ChatParticipant> findAllByUserEntity(UserEntity userEntity);

  @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id WHERE cp1.userEntity.userNo = :myUserNo AND cp2.userEntity.userNo = :otherUserNo AND cp1.chatRoom.isGroupChat = 'N'")
  Optional<ChatRoom> findExistingPrivateRoom(@Param("myUserNo") Long myUserNo, @Param("otherUserNo") Long otherUserNo);
}
