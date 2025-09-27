package com.rose.back.domain.auth.service.impl;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.rose.back.common.constants.ResponseCode;
import com.rose.back.common.util.CertificationNumber;
import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.repository.AuthRepository;
import com.rose.back.domain.auth.repository.EmailCertificationRepository;
import com.rose.back.domain.auth.entity.EmailCertificationEntity;
import com.rose.back.domain.auth.service.AccessTokenBlacklistService;
import com.rose.back.domain.auth.service.AuthService;
import com.rose.back.domain.auth.service.RefreshTokenService;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.domain.user.dto.request.EmailVerifyRequest;
import com.rose.back.domain.user.dto.request.EmailSendRequest;
import com.rose.back.domain.user.dto.request.IdCheckRequest;
import com.rose.back.domain.user.dto.response.EmailVerifyResponse;
import com.rose.back.domain.user.dto.response.EmailSendResponse;
import com.rose.back.domain.user.dto.response.IdCheckResponse;
import com.rose.back.domain.user.dto.response.CommonResponse;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.entity.UserEntity.UserStatus;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.global.exception.MissingAccessTokenException;
import com.rose.back.global.service.EmailService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;
    private final EmailCertificationRepository emailCertificationRepository;

    public AuthServiceImpl(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder, AuthRepository authRepository, JwtTokenProvider jwtProvider, RefreshTokenService refreshTokenService, AccessTokenBlacklistService accessTokenBlacklistService, EmailCertificationRepository emailCertificationRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.authRepository = authRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
        this.emailCertificationRepository = emailCertificationRepository;
    }

    // 아이디 중복 확인
    @Override
    public ResponseEntity<? super CommonResponse> userIdCheck(IdCheckRequest dto) {
        try {
            String userId = dto.getUserId();
            boolean isExistId = userRepository.existsByUserId(userId);
            if (isExistId) return IdCheckResponse.duplicateId();
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.databaseError();
        }
        // return IdCheckResponseDto.success();
        return ResponseEntity.ok(new CommonResponse());
    }

    // 이메일 중복 확인
    @Override
    public ResponseEntity<? super EmailSendResponse> checkEmail(EmailSendRequest dto) {
        try {
            String userEmail = dto.getUserEmail();
            boolean isExistEmail = userRepository.existsByUserEmail(userEmail);
            if (isExistEmail) {
                return EmailSendResponse.duplicateEmail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.databaseError();
        }
        return EmailSendResponse.success();
    }

    // 이메일 인증
    @Override
    @Transactional
    public ResponseEntity<? super EmailSendResponse> emailCertification(EmailSendRequest dto) {
        String userId = dto.getUserId();
        String userEmail = dto.getUserEmail();
        String code = CertificationNumber.getCertificationNumber();

        // 추가 및 미사용 시 재발급
        Optional<EmailCertificationEntity> optional = emailCertificationRepository.findByUserIdAndUserEmailAndUsedFalse(userId, userEmail);

        if (optional.isPresent()) {
            EmailCertificationEntity entity = optional.get();
            entity.reissue(code, 5);
            emailCertificationRepository.save(entity);
        } else {
            EmailCertificationEntity newEntity = EmailCertificationEntity.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .certificationNumber(code)
                    .build();
            emailCertificationRepository.save(newEntity);
        }

        emailService.sendCertificationMailAsync(userEmail, code);
        return EmailSendResponse.success();
    }

    // 이메일 인증 확인
    @Override
    @Transactional
    public ResponseEntity<? super EmailVerifyResponse> checkCertification(EmailVerifyRequest dto) {
        try {
            String userId = dto.getUserId();
            String userEmail = dto.getUserEmail();
            String inputCode = dto.getCertificationNumber();

            // 미사용 조회
            Optional<EmailCertificationEntity> optionalEntity =
                    emailCertificationRepository.findByUserIdAndUserEmailAndUsedFalse(userId, userEmail);
            if (optionalEntity.isEmpty()) return EmailVerifyResponse.certificationFail();

            EmailCertificationEntity certificationEntity = optionalEntity.get();

            // 만료 확인
            if (certificationEntity.isExpired()) {
                certificationEntity.markUsed();
                emailCertificationRepository.save(certificationEntity);
                return EmailVerifyResponse.certificationFail();
            }

            // 번호 비교
            String storedCode = certificationEntity.getCertificationNumber();
            boolean isMatched = false;
            if (storedCode != null && inputCode != null) {
                byte[] a = storedCode.getBytes(StandardCharsets.UTF_8);
                byte[] b = inputCode.getBytes(StandardCharsets.UTF_8);
                isMatched = MessageDigest.isEqual(a, b);
            }

            if (!isMatched) {
                emailCertificationRepository.save(certificationEntity);
                return EmailVerifyResponse.certificationFail();
            }

            certificationEntity.markUsed();
            emailCertificationRepository.save(certificationEntity);

            return EmailVerifyResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.databaseError();
        }
    }

    // 회원가입
    @Override
    public ResponseEntity<? super CommonResponse> join(UserInfoDto dto) {
        try {
            String userId = dto.getUserName();
            boolean isExistId = userRepository.existsByUserId(userId);
            if (isExistId) return IdCheckResponse.duplicateId();

            // 닉네임 자동 중복 회피 처리
            String baseNick = dto.getUserNick();
            String finalNick = getAvailableNick(baseNick);

            UserEntity user = UserEntity.builder()
                    .userId(userId)
                    .userPwd(passwordEncoder.encode(dto.getUserPwd()))
                    .userEmail(dto.getUserEmail())
                    .userNick(finalNick)
                    .userRole("ROLE_USER")
                    .userType("web")
                    .userStatus(UserStatus.ACTIVE)
                    .apDate(LocalDate.now())
                    .build();

            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.databaseError();
        }
        return ResponseEntity.ok(new CommonResponse());
    }

    private String getAvailableNick(String baseNick) {
        String nick = baseNick;
        int suffix = 1;
        while (userRepository.existsByUserNick(nick)) {
            nick = baseNick + suffix;
            suffix++;
        }
        return nick;
    }

    // 아이디 찾기
    @Override
    public ResponseEntity<? super CommonResponse> findUserId(String userEmail) {
        try {
            UserEntity user = userRepository.findByUserEmail(userEmail);
            if (user != null) {
                String userIdPart = user.getUserId();

                emailService.userIdFound(userEmail, userIdPart);
                return ResponseEntity.ok(new CommonResponse());
            } else {
                return ResponseEntity.badRequest().body(new CommonResponse(ResponseCode.VALIDATION_FAIL.getCode(), "해당 이메일로 등록된 아이디가 없습니다."));
            }
        } catch (Exception e) {
            return CommonResponse.databaseError();
        }
    }

    // 비밀번호 찾기
    @Override
    public ResponseEntity<? super CommonResponse> resetUserPwd(String userEmail, String userId) {
        try {
            UserEntity user = userRepository.findByUserEmailAndUserId(userEmail, userId);
            if (user != null) {
                String randomUserPwd = generateRandom();
                String hashedUserPwd = passwordEncoder.encode(randomUserPwd);
                user.setUserPwd(hashedUserPwd);
                userRepository.save(user);

                emailService.resetPassword(userEmail, randomUserPwd);
                return ResponseEntity.ok(new CommonResponse());
            } else {
                return ResponseEntity.badRequest().body(new CommonResponse(ResponseCode.VALIDATION_FAIL.getCode(), "해당 이메일과 아이디로 등록된 사용자가 없습니다."));
            }
        } catch (Exception e) {
            return CommonResponse.databaseError();
        }
    }

    private String generateRandom() {
        int length = 8;
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String number = "0123456789";
        String userPwd = lowerCase + number;
        Random random = new Random();
        StringBuilder password = new StringBuilder(length);

        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(number.charAt(random.nextInt(number.length())));

        for (int i = 2; i < length; i++) {
            password.append(userPwd.charAt(random.nextInt(userPwd.length())));
        }
        return password.toString();
    }

    // 회원 탈퇴
    public ResponseEntity<?> withdraw(HttpServletRequest request, HttpServletResponse response, Map<String, String> body) {
        String authorizationHeader = request.getHeader("Authorization");
        String accessToken = resolveToken(authorizationHeader);
        String refresh = null;

        for (Cookie cookie : Optional.ofNullable(request.getCookies()).orElse(new Cookie[0])) {
            if ("refresh".equals(cookie.getName())) {
                refresh = cookie.getValue();
            }
        }

        if (accessToken == null || refresh == null) {
            log.warn("[탈퇴 요청] 토큰 누락 - access: {}, refresh: {}", accessToken, refresh);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Access 또는 Refresh 토큰이 누락되었습니다.");
        }

        try {
            jwtProvider.validateExpiration(accessToken);
            jwtProvider.validateExpiration(refresh);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!"access".equals(jwtProvider.getCategory(accessToken)) ||
                !"refresh".equals(jwtProvider.getCategory(refresh))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwtProvider.getUserId(accessToken);
        String inputPassword = body.get("userPwd");

        try {
            log.info("입력된 비밀번호 = {}", inputPassword);
            deleteUser(userId, inputPassword);
        } catch (IllegalArgumentException e) {
            log.warn("탈퇴 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("탈퇴 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }

        refreshTokenService.delete(userId);

        long exp = jwtProvider.getExpiration(accessToken);
        accessTokenBlacklistService.blacklist(accessToken, exp);

        Cookie deleteCookie = new Cookie("refresh", null);
        deleteCookie.setMaxAge(0);
        deleteCookie.setPath("/");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(true);
        response.addCookie(deleteCookie);

        return ResponseEntity.noContent().build();
    }

    private void deleteUser(String userId, String rawPassword) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            log.warn("탈퇴 요청: 존재하지 않는 유저입니다. userId={}", userId);
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        if (user.getUserPwd() == null) {
            log.error("DB에서 가져온 유저의 비밀번호가 null입니다. userId={}", userId);
            throw new IllegalStateException("DB 저장된 비밀번호가 없습니다.");
        }
        if (!passwordEncoder.matches(rawPassword, user.getUserPwd())) {
            throw new IllegalArgumentException("비밀번호 불일치");
        }

        user.setReservedDeleteAt(LocalDateTime.now().plusDays(7));
        user.setUserStatus(UserStatus.WITHDRAWAL_PENDING);
        userRepository.save(user);

        emailService.sendWithdrawalNotice(user.getUserEmail(), user.getUserNick());
        log.info("탈퇴 요청: 유저 {}({})가 탈퇴 요청을 하였습니다.", user.getUserNick(), userId);
    }

    // 회원 탈퇴 예약 철회
    @Transactional
    public ResponseEntity<?> cancelWithdraw(String accessToken) {
        String userId = jwtProvider.getUserId(accessToken);
        UserEntity user = authRepository.findByUserId(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
        if (user.getReservedDeleteAt() == null || user.getUserStatus() != UserStatus.WITHDRAWAL_PENDING) {
            return ResponseEntity.badRequest().body("탈퇴 요청 상태가 아닙니다.");
        }

        user.setReservedDeleteAt(null);
        user.setUserStatus(UserStatus.ACTIVE);
        authRepository.saveAndFlush(user);
        refreshTokenService.delete(userId); // 기존 토큰 무효화

        return ResponseEntity.ok("탈퇴 철회가 완료되었습니다. 다시 로그인해주세요.");
    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new MissingAccessTokenException();
    }
}
