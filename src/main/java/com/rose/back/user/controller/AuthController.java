package com.rose.back.user.controller;

import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    // 아이디 중복 체크
    @PostMapping("/id-check")
    public ResponseEntity<? super IdCheckResponseDto> idCheck (
        @RequestBody @Valid IdCheckRequestDto requestBody
    ) {
        ResponseEntity<? super IdCheckResponseDto> response = authService.userIdCheck(requestBody);
        return response;
    }

    // 이메일 인증
    @PostMapping("/email-certification")
    public ResponseEntity<? super EmailCertificationResponseDto> 
    emailCertification(@RequestBody @Valid EmailCertificationRequestDto requestBody) {
        ResponseEntity<? super EmailCertificationResponseDto> response = authService.emailCertification(requestBody);
        return response;
    }

    // 인증번호 확인
    @PostMapping("/check-certification")
    public ResponseEntity<? super CheckCertificationResponseDto> checkCertification(@RequestBody @Valid CheckCertificationRequestDto requestBody) {
        ResponseEntity<? super CheckCertificationResponseDto> response = authService.checkCertification(requestBody);
        return response;
    }

    // 회원 가입
    @PostMapping("/sign-up")
    public ResponseEntity<? super SignUpResponseDto> signUp(@RequestBody @Valid SignUpRequestDto requestBody) {
        ResponseEntity<? super SignUpResponseDto> response = authService.signUp(requestBody);
        return response;
    }

    // 로그인
    @PostMapping("/sign-in")
    public ResponseEntity<? super SignInResponseDto> signIn (@RequestBody @Valid SignInRequestDto requestBody) {
        ResponseEntity<? super SignInResponseDto> response = authService.signIn(requestBody);
        return response;
    }

    // 로그아웃
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // JSESSIONID 쿠키 삭제
        response.setHeader("Set-Cookie", "JSESSIONID=; Max-Age=0; Path=/; HttpOnly; SameSite=None; Secure");
        // 토큰 쿠키 삭제
        response.setHeader("Set-Cookie", "jwtToken=; Max-Age=0; Path=/; HttpOnly; SameSite=None; Secure");
    }
}
