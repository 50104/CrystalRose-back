package com.rose.back.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberListReqDto {
  
  private Long userNo;
  private String userId;
  private String userEmail;
}
