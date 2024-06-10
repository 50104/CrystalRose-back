package com.rose.back.user.service;

import org.springframework.http.ResponseEntity;

import com.rose.back.user.dto.request.auth.CheckCertificationRequestDto;
import com.rose.back.user.dto.request.auth.EmailCertificationRequestDto;
import com.rose.back.user.dto.request.auth.IdCheckRequestDto;
import com.rose.back.user.dto.request.auth.SignInRequestDto;
import com.rose.back.user.dto.request.auth.SignUpRequestDto;
import com.rose.back.user.dto.response.auth.CheckCertificationResponseDto;
import com.rose.back.user.dto.response.auth.EmailCertificationResponseDto;
import com.rose.back.user.dto.response.auth.IdCheckResponseDto;
import com.rose.back.user.dto.response.auth.SignInResponseDto;
import com.rose.back.user.dto.response.auth.SignUpResponseDto;

public interface AuthService {
    
    ResponseEntity<? super IdCheckResponseDto> userIdCheck(IdCheckRequestDto dto);

    ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto);

    ResponseEntity<? super CheckCertificationResponseDto> checkCertification(CheckCertificationRequestDto dto);

    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);

    ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto);
}
