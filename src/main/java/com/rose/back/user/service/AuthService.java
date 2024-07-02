package com.rose.back.user.service;

import org.springframework.http.ResponseEntity;

import com.rose.back.user.dto.UserDTO;
import com.rose.back.user.dto.request.CheckCertificationRequestDto;
import com.rose.back.user.dto.request.EmailCertificationRequestDto;
import com.rose.back.user.dto.request.IdCheckRequestDto;
import com.rose.back.user.dto.response.CheckCertificationResponseDto;
import com.rose.back.user.dto.response.EmailCertificationResponseDto;
import com.rose.back.user.dto.response.ResponseDto;

public interface AuthService {
    
    ResponseEntity<? super ResponseDto> userIdCheck(IdCheckRequestDto dto);

    ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto);

    ResponseEntity<? super CheckCertificationResponseDto> checkCertification(CheckCertificationRequestDto dto);

    ResponseEntity<? super ResponseDto> join(UserDTO dto);
}
