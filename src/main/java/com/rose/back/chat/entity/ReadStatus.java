package com.rose.back.chat.entity;

import com.rose.back.board.content.entity.BaseTimeEntity;
import com.rose.back.user.entity.UserEntity;

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
@Entity(name = "chat_read_status")
@Table(name = "chat_read_status")
public class ReadStatus extends BaseTimeEntity {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_no", nullable = false)
  private ChatRoom chatRoom;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_no", nullable = false)
  private UserEntity userEntity;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_no", nullable = false)
  private ChatMessage chatMessage;

  @Column(nullable = false)
  private boolean isRead;

  public void updateIsRead(boolean isRead) {
    this.isRead = isRead;
  }
}
