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
import com.rose.back.global.exception.MissingAccessTokenException;
import com.rose.back.domain.user.dto.response.CommonResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @PostMapping("/id-check")
    public ResponseEntity<? super IdCheckResponse> idCheck(@RequestBody @Valid IdCheckRequest requestBody) {
        log.info("[POST][/api/v1/auth/id-check] - 아이디 중복 체크 요청");
        return authService.userIdCheck(requestBody);
    }

    @PostMapping("/email-check")
    public ResponseEntity<? super EmailSendResponse> checkEmail(@RequestBody @Valid EmailSendRequest requestBody) {
        log.info("[POST][/api/v1/auth/email-check] - 이메일 중복 체크 요청");
        return authService.checkEmail(requestBody);
    }

    @PostMapping("/email-certification")
    public ResponseEntity<? super EmailSendResponse> emailCertification(@RequestBody @Valid EmailSendRequest requestBody) {
        log.info("[POST][/api/v1/auth/email-certification] - 이메일 인증 요청");
        return authService.emailCertification(requestBody);
    }

    @PostMapping("/check-certification")
    public ResponseEntity<? super EmailVerifyResponse> checkCertification(@RequestBody @Valid EmailVerifyRequest requestBody) {
        log.info("[POST][/api/v1/auth/check-certification] - 인증번호 확인 요청");
        return authService.checkCertification(requestBody);
    }

    @PostMapping("/join")
    public ResponseEntity<? super CommonResponse> join(@RequestBody @Valid UserInfoDto userDto, BindingResult bindingResult) {
        log.info("[POST][/api/v1/auth/join] - 회원가입 요청");
        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(errorMessages);
        }
        return authService.join(userDto);
    }

    @PostMapping("/findUserId")
    public ResponseEntity<?> findUserId(@RequestBody @Valid EmailSendRequest request) {
        log.info("[POST][/api/v1/auth/findUserId] - 아이디 찾기 요청");
        return authService.findUserId(request.getUserEmail());
    }

    @PostMapping("/findUserPwd")
    public ResponseEntity<?> findUserPwd(@RequestBody @Valid EmailSendRequest request) {
        log.info("[POST][/api/v1/auth/findUserPwd] - 비밀번호 초기화 요청");
        return authService.resetUserPwd(request.getUserEmail(), request.getUserId());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> body) {
        log.info("[POST][/api/v1/auth/withdraw] - 회원 탈퇴 요청");
        return authService.withdraw(request, response, body);
    }

    @PutMapping("/withdraw/cancel")
    public ResponseEntity<?> cancelWithdraw(HttpServletRequest request, HttpServletResponse response) {
        log.info("[PUT][/api/v1/auth/withdraw/cancel] - 탈퇴 취소 요청");
        String accessToken = resolveAccessToken(request);
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return authService.cancelWithdraw(accessToken, response);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new MissingAccessTokenException();
    }
}
