package com.rose.back.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FindUserIdRequest {

    @NotBlank(message = "이메일은 공백일 수 없습니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String userEmail;
}
