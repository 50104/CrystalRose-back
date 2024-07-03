package com.rose.back.user.controller;

import org.springframework.web.bind.annotation.RestController;

import com.rose.back.user.dto.UserDTO;
import com.rose.back.user.dto.request.CheckCertificationRequestDto;
import com.rose.back.user.dto.request.EmailCertificationRequestDto;
import com.rose.back.user.dto.request.IdCheckRequestDto;
import com.rose.back.user.dto.response.CheckCertificationResponseDto;
import com.rose.back.user.dto.response.EmailCertificationResponseDto;
import com.rose.back.user.dto.response.IdCheckResponseDto;
import com.rose.back.user.dto.response.ResponseDto;
import com.rose.back.user.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs{
    
    private final AuthService authService;

    // 아이디 중복 체크
    @PostMapping("/id-check")
    public ResponseEntity<? super IdCheckResponseDto> idCheck (@RequestBody @Valid IdCheckRequestDto requestBody) {

        log.info("아이디 중복 체크 컨트롤러 실행");

        ResponseEntity<? super IdCheckResponseDto> response = authService.userIdCheck(requestBody);
        return response;
    }

    // 이메일 중복 확인
    @PostMapping("/email-check")
    public ResponseEntity<? super EmailCertificationResponseDto> checkEmail(@RequestBody @Valid EmailCertificationRequestDto requestBody) {

        log.info("이메일 중복 체크 컨트롤러 실행");
        ResponseEntity<? super EmailCertificationResponseDto> response = authService.checkEmail(requestBody);
        return response;

    }

    // 이메일 인증
    @PostMapping("/email-certification")
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(@RequestBody @Valid EmailCertificationRequestDto requestBody) {

        log.info("이메일 인증 컨트롤러 실행");
        ResponseEntity<? super EmailCertificationResponseDto> response = authService.emailCertification(requestBody);
        return response;
    }

    // 인증번호 확인
    @PostMapping("/check-certification")
    public ResponseEntity<? super CheckCertificationResponseDto> checkCertification(@RequestBody @Valid CheckCertificationRequestDto requestBody) {

        log.info("이메일 인증 확인 컨트롤러 실행");
        ResponseEntity<? super CheckCertificationResponseDto> response = authService.checkCertification(requestBody);
        return response;
    }
    
    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<? super ResponseDto> join(@RequestBody UserDTO userDto){

        log.info("회원가입 컨트롤러 실행");
        return authService.join(userDto);
    }
}
