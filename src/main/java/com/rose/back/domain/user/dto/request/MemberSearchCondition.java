package com.rose.back.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberSearchCondition {
  
  private Long userNo;
  private String userId;
  private String userEmail;
}
