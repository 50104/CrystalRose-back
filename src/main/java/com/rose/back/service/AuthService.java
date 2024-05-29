package com.rose.back.service;

import org.springframework.http.ResponseEntity;

import com.rose.back.dto.request.auth.IdCheckRequestDto;
import com.rose.back.dto.response.auth.IdCheckResponseDto;

public interface AuthService {
    
    ResponseEntity<? super IdCheckResponseDto> userIdCheck(IdCheckRequestDto dto);
}
