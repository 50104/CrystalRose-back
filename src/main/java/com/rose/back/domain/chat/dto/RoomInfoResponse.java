package com.rose.back.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class RoomInfoResponse {
  
  private Long roomId;
  private String displayName;
}
