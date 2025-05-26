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
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @PostMapping("/id-check")
    public ResponseEntity<? super IdCheckResponse> idCheck(@RequestBody @Valid IdCheckRequest requestBody) {
        log.info("[POST][/api/v1/auth/id-check] - 아이디 중복 체크 요청");
        try {
            return authService.userIdCheck(requestBody);
        } catch (Exception e) {
            log.error("아이디 중복 체크 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/email-check")
    public ResponseEntity<? super EmailSendResponse> checkEmail(@RequestBody @Valid EmailSendRequest requestBody) {
        log.info("[POST][/api/v1/auth/email-check] - 이메일 중복 체크 요청");
        try {
            return authService.checkEmail(requestBody);
        } catch (Exception e) {
            log.error("이메일 중복 체크 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/email-certification")
    public ResponseEntity<? super EmailSendResponse> emailCertification(@RequestBody @Valid EmailSendRequest requestBody) {
        log.info("[POST][/api/v1/auth/email-certification] - 이메일 인증 요청");
        try {
            return authService.emailCertification(requestBody);
        } catch (Exception e) {
            log.error("이메일 인증 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/check-certification")
    public ResponseEntity<? super EmailVerifyResponse> checkCertification(@RequestBody @Valid EmailVerifyRequest requestBody) {
        log.info("[POST][/api/v1/auth/check-certification] - 인증번호 확인 요청");
        try {
            return authService.checkCertification(requestBody);
        } catch (Exception e) {
            log.error("인증번호 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/join")
    public ResponseEntity<? super CommonResponse> join(@RequestBody UserInfoDto userDto, BindingResult bindingResult) {
        log.info("[POST][/api/v1/auth/join] - 회원가입 요청");
        try {
            if (bindingResult.hasErrors()) {
                log.warn("회원가입 유효성 실패: {}", bindingResult.getAllErrors());
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            return authService.join(userDto);
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/findUserId")
    public ResponseEntity<?> findUserId(@RequestBody EmailSendRequest request) {
        log.info("[POST][/api/v1/auth/findUserId] - 아이디 찾기 요청");
        try {
            return authService.findUserId(request.getUserEmail());
        } catch (Exception e) {
            log.error("아이디 찾기 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/findUserPwd")
    public ResponseEntity<?> findUserPwd(@RequestBody EmailSendRequest request) {
        log.info("[POST][/api/v1/auth/findUserPwd] - 비밀번호 초기화 요청");
        try {
            return authService.resetUserPwd(request.getUserEmail(), request.getUserId());
        } catch (Exception e) {
            log.error("비밀번호 초기화 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> body) {
        log.info("[POST][/api/v1/auth/withdraw] - 회원 탈퇴 요청");
        try {
            return authService.withdraw(request, response, body);
        } catch (Exception e) {
            log.error("회원 탈퇴 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/withdraw/cancel")
    public ResponseEntity<?> cancelWithdraw(@RequestHeader("access") String accessToken, HttpServletResponse response) {
        log.info("[PUT][/api/v1/auth/withdraw/cancel] - 탈퇴 취소 요청");
        try {
            return authService.cancelWithdraw(accessToken, response);
        } catch (Exception e) {
            log.error("탈퇴 취소 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}