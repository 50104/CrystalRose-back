package com.rose.back.domain.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.service.RefreshTokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ReissueController implements ReissueControllerDocs {

    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    private static final long ACCESS_EXP_TIME = 30 * 60 * 1000L; // 30분
    private static final long REFRESH_EXP_TIME = 24 * 60 * 60 * 1000L; // 1일

    public ReissueController(JwtTokenProvider jwtProvider, RefreshTokenService refreshTokenService) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/reissue")
    @Transactional
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        log.info("Reissue started");

        // refresh token 확인
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "no cookies"));
        }
        if (refresh == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "refresh null"));
        }

        // 토큰 유효성 검증
        try {
            jwtProvider.validateExpiration(refresh);
        } catch (ExpiredJwtException e) {
            log.info("Refresh expired");
            return ResponseEntity.badRequest().body(Map.of("error", "refresh expired"));
        }

        // 카테고리 확인
        if (!"refresh".equals(jwtProvider.getCategory(refresh))) {
            log.info("Invalid refresh category");
            return ResponseEntity.badRequest().body(Map.of("error", "invalid refresh category"));
        }

        // 사용자 정보
        String userId = jwtProvider.getUserId(refresh);
        String userNick = jwtProvider.getUserNick(refresh);
        String userRole = jwtProvider.getUserRole(refresh);
        Long userNo = jwtProvider.getUserNo(refresh);

        // Redis에서 유효한지 검증
        if (!refreshTokenService.isValid(userId, refresh)) {
            log.warn("Refresh not in Redis");
            return ResponseEntity.badRequest().body(Map.of("error", "invalid refresh redis"));
        }

        // 새 토큰 생성
        String newAccess = jwtProvider.create("access", userId, userNick, userRole, ACCESS_EXP_TIME, userNo);
        String newRefresh = jwtProvider.create("refresh", userId, userNick, userRole, REFRESH_EXP_TIME, userNo);

        // Redis 갱신
        refreshTokenService.delete(userId);
        refreshTokenService.save(userId, newRefresh, REFRESH_EXP_TIME);

        // 쿠키 생성 및 추가
        Cookie refreshCookie = new Cookie("refresh", newRefresh);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (REFRESH_EXP_TIME / 1000));

        // HTTPS 요청일 경우에만 Secure, SameSite=None 설정
        boolean isHttps = request.getRequestURL().toString().startsWith("https://");
        boolean isLocal = request.getRequestURL().toString().contains("localhost");
        if (isHttps && !isLocal) {
            refreshCookie.setSecure(true);
            refreshCookie.setAttribute("SameSite", "None");
        } else {
            refreshCookie.setSecure(false);
        }
        response.addCookie(refreshCookie);

        log.info("Reissue success for userId={}", userId);
        log.info("access token 재발급 성공: {}", newAccess);
        log.info("refresh token 재발급 성공: {}", newRefresh);

        return ResponseEntity.ok(Map.of("accessToken", newAccess));
    }
}