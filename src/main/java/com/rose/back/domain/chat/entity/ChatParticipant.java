package com.rose.back.domain.chat.entity;

import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chat_participant")
public class ChatParticipant extends BaseTimeEntity {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_no", nullable = false)
  private ChatRoom chatRoom;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_no", nullable = false)
  private UserEntity userEntity;

  // 상대방 닉네임
  @Column(name = "display_name", length = 50, nullable = false)
  private String displayName;

  public static ChatParticipant of(ChatRoom room, UserEntity user, String displayName) {
    return ChatParticipant.builder()
      .chatRoom(room)
      .userEntity(user)
      .displayName(displayName)
      .build();
  }
}
