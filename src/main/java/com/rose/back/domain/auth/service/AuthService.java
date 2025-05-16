package com.rose.back.domain.auth.service;

import org.springframework.http.ResponseEntity;

import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.EmailVerifyRequest;
import com.rose.back.domain.user.dto.request.EmailSendRequest;
import com.rose.back.domain.user.dto.request.IdCheckRequest;
import com.rose.back.domain.user.dto.response.EmailVerifyResponse;
import com.rose.back.domain.user.dto.response.EmailSendResponse;
import com.rose.back.domain.user.dto.response.CommonResponse;

public interface AuthService {
    
    ResponseEntity<? super CommonResponse> userIdCheck(IdCheckRequest dto);

    ResponseEntity<? super EmailSendResponse> checkEmail(EmailSendRequest dto);

    ResponseEntity<? super EmailSendResponse> emailCertification(EmailSendRequest dto);

    ResponseEntity<? super EmailVerifyResponse> checkCertification(EmailVerifyRequest dto);

    ResponseEntity<? super CommonResponse> join(UserInfoDto dto);

    ResponseEntity<? super CommonResponse> findUserId(String userEmail); // 아이디 찾기

    ResponseEntity<? super CommonResponse> resetUserPwd(String userEmail, String userId); // 비밀번호 초기화
}
