package com.rose.back.domain.chat.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageReqDto {
  
  private Long id;
  private String message;
  private String senderId;
  private String senderNick;
  private String senderProfileImg;
  private Long roomId;
  private String type;
  private LocalDateTime createdDate;
}
