package com.rose.back.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PwdValidationRequest {
    private String userId;
    private String userPwd;
}
