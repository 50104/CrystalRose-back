package com.rose.back.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResetUserPwdRequest {

    @NotBlank(message = "이메일은 공백일 수 없습니다.")
    private String userEmail;

    @NotBlank(message = "아이디는 공백일 수 없습니다.")
    private String userId;
}
