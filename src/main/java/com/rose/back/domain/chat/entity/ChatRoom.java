package com.rose.back.domain.chat.entity;

import java.util.ArrayList;
import java.util.List;

import com.rose.back.global.entity.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "chat_room")
public class ChatRoom extends BaseTimeEntity {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "room_name")
  private String roomName;

  @Builder.Default
  @Column(nullable = false, name = "is_group_chat")
  private String isGroupChat="N";

  @Builder.Default
  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE)
  private List<ChatParticipant> chatParticipants = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<ChatMessage> chatMessages = new ArrayList<>();
}
