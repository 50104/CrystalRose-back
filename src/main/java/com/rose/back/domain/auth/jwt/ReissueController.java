package com.rose.back.domain.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
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

    private static final long ACCESS_EXP_TIME = 15 * 60 * 1000L;  // 15분
    private static final long REFRESH_EXP_TIME = 14L * 24 * 60 * 60 * 1000L; // 14일

    public ReissueController(JwtTokenProvider jwtProvider, RefreshTokenService refreshTokenService) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/reissue")
    @Transactional
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refresh 추출
        String refresh = Arrays.stream(Objects.requireNonNullElse(request.getCookies(), new jakarta.servlet.http.Cookie[0]))
                .filter(c -> "refresh".equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst().orElse(null);

        if (refresh == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "refresh cookie missing"));
        }

        // 만료/위조/카테고리 검증
        try {
            jwtProvider.validateExpiration(refresh);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "refresh expired"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "refresh invalid"));
        }

        if (!"refresh".equals(jwtProvider.getCategory(refresh))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "invalid category"));
        }

        // 사용자 정보 추출
        String userId = jwtProvider.getUserId(refresh);
        String userNick = jwtProvider.getUserNick(refresh);
        String userRole = jwtProvider.getUserRole(refresh);
        Long userNo = jwtProvider.getUserNo(refresh);

        // Redis 허용 검증
        if (!refreshTokenService.isValid(userId, refresh)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "refresh not allowed"));
        }

        // 새 access/refresh 발급
        String newAccess = jwtProvider.create("access", userId, userNick, userRole, ACCESS_EXP_TIME, userNo);
        String newRefresh = jwtProvider.create("refresh", userId, userNick, userRole, REFRESH_EXP_TIME, userNo);

        refreshTokenService.delete(userId);
        refreshTokenService.save(userId, newRefresh, REFRESH_EXP_TIME);

        // Refresh 쿠키 갱신
        boolean secure = isHttps(request);
        addRefreshSetCookieHeader(response, newRefresh, (int) (REFRESH_EXP_TIME / 1000), secure);

        // Access 토큰 헤더 + JSON 동시 제공
        response.setHeader("Authorization", "Bearer " + newAccess);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        log.info("Reissue success for userId={}", userId);
        log.info("access token 재발급 성공: {}", newAccess);
        log.info("refresh token 재발급 성공: {}", newRefresh);
        return ResponseEntity.ok(Map.of("accessToken", newAccess));
    }

    private boolean isHttps(HttpServletRequest request) {
        String xfProto = request.getHeader("X-Forwarded-Proto");
        if (xfProto != null) {
            return "https".equalsIgnoreCase(xfProto);
        }
        return request.isSecure() || request.getRequestURL().toString().startsWith("https://");
    }

    private void addRefreshSetCookieHeader(HttpServletResponse response, String value, int maxAgeSeconds, boolean secure) {
        StringBuilder sb = new StringBuilder();
        sb.append("refresh=").append(value)
          .append("; Path=/auth/refresh")
          .append("; HttpOnly")
          .append("; Max-Age=").append(maxAgeSeconds);
        if (secure) {
            sb.append("; Secure").append("; SameSite=None");
        }
        response.addHeader("Set-Cookie", sb.toString());
    }
}
