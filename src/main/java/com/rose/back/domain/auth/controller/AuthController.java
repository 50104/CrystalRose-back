package com.rose.back.domain.auth.controller;

import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.controller.docs.AuthControllerDocs;
import com.rose.back.domain.auth.service.AuthService;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.EmailVerifyRequest;
import com.rose.back.domain.user.dto.request.EmailSendRequest;
import com.rose.back.domain.user.dto.request.IdCheckRequest;
import com.rose.back.domain.user.dto.response.EmailVerifyResponse;
import com.rose.back.domain.user.dto.response.EmailSendResponse;
import com.rose.back.domain.user.dto.response.IdCheckResponse;

import com.rose.back.domain.user.dto.response.CommonResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<? super IdCheckResponse> idCheck (@RequestBody @Valid IdCheckRequest requestBody) {

        log.info("아이디 중복 체크 컨트롤러 실행");

        ResponseEntity<? super IdCheckResponse> response = authService.userIdCheck(requestBody);
        return response;
    }

    // 이메일 중복 확인
    @PostMapping("/email-check")
    public ResponseEntity<? super EmailSendResponse> checkEmail(@RequestBody @Valid EmailSendRequest requestBody) {

        log.info("이메일 중복 체크 컨트롤러 실행");
        ResponseEntity<? super EmailSendResponse> response = authService.checkEmail(requestBody);
        return response;

    }

    // 이메일 인증
    @PostMapping("/email-certification")
    public ResponseEntity<? super EmailSendResponse> emailCertification(@RequestBody @Valid EmailSendRequest requestBody) {

        log.info("이메일 인증 컨트롤러 실행");
        ResponseEntity<? super EmailSendResponse> response = authService.emailCertification(requestBody);
        return response;
    }

    // 인증번호 확인
    @PostMapping("/check-certification")
    public ResponseEntity<? super EmailVerifyResponse> checkCertification(@RequestBody @Valid EmailVerifyRequest requestBody) {

        log.info("이메일 인증 확인 컨트롤러 실행");
        ResponseEntity<? super EmailVerifyResponse> response = authService.checkCertification(requestBody);
        return response;
    }
    
    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<? super CommonResponse> join(@RequestBody UserInfoDto userDto, BindingResult bindingResult){

        log.info("회원가입 컨트롤러 실행");
        if (bindingResult.hasErrors()) { // 유효성 검사 오류 처리
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return authService.join(userDto);
    }

    // 아이디 찾기
    @PostMapping("/findUserId")
    public ResponseEntity<?> findUserId(@RequestBody EmailSendRequest request) {

        log.info("아이디찾기 컨트롤러 실행");
        return authService.findUserId(request.getUserEmail());
    }

    // 비밀번호 초기화
    @PostMapping("/findUserPwd")
    public ResponseEntity<?> findUserPwd(@RequestBody EmailSendRequest request) {

        log.info("비밀번호 초기화 컨트롤러 실행");
        return authService.resetUserPwd(request.getUserEmail(), request.getUserId());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> body) {
        return authService.withdraw(request, response, body);
    }
}
