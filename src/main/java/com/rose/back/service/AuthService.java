package com.rose.back.service;

import org.springframework.http.ResponseEntity;

import com.rose.back.dto.request.auth.CheckCertificationRequestDto;
import com.rose.back.dto.response.auth.CheckCertificationResponseDto;
import com.rose.back.dto.request.auth.EmailCertificationRequestDto;
import com.rose.back.dto.request.auth.IdCheckRequestDto;
import com.rose.back.dto.request.auth.SignInRequestDto;
import com.rose.back.dto.request.auth.SignUpRequestDto;
import com.rose.back.dto.response.auth.SignUpResponseDto;
import com.rose.back.dto.response.auth.SignInResponseDto;
import com.rose.back.dto.response.auth.EmailCertificationResponseDto;
import com.rose.back.dto.response.auth.IdCheckResponseDto;

public interface AuthService {
    
    ResponseEntity<? super IdCheckResponseDto> userIdCheck(IdCheckRequestDto dto);

    ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto);

    ResponseEntity<? super CheckCertificationResponseDto> checkCertification(CheckCertificationRequestDto dto);

    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);

    ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto);
}
