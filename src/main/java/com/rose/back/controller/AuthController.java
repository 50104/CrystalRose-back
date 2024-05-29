package com.rose.back.controller;

import org.springframework.web.bind.annotation.RestController;

import com.rose.back.dto.request.auth.EmailCertificationRequestDto;
import com.rose.back.dto.request.auth.IdCheckRequestDto;
import com.rose.back.dto.response.auth.EmailCertificationResponseDto;
import com.rose.back.dto.response.auth.IdCheckResponseDto;
import com.rose.back.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/id-check")
    public ResponseEntity<? super IdCheckResponseDto> idCheck (
        @RequestBody @Valid IdCheckRequestDto requestBody
    ) {
        ResponseEntity<? super IdCheckResponseDto> response = authService.userIdCheck(requestBody);
        return response;
    }

    @PostMapping("/email-certification")
    public ResponseEntity<? super EmailCertificationResponseDto> 
    emailCertification(@RequestBody @Valid EmailCertificationRequestDto requestBody) {

        ResponseEntity<? super EmailCertificationResponseDto> response = authService.emailCertification(requestBody);
        return response;
    }
}
