package com.rose.back.domain.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rose.back.domain.auth.service.RefreshTokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@ResponseBody
public class ReissueController implements ReissueControllerDocs{

    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public ReissueController(JwtTokenProvider jwtProvider, RefreshTokenService refreshTokenService) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    // 토큰 재발급
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
            log.info("No cookies");
            return new ResponseEntity<>("no cookies", HttpStatus.BAD_REQUEST);
        }
        if (refresh == null) {
            log.info("refresh null");
            return new ResponseEntity<>("refresh null", HttpStatus.BAD_REQUEST);
        }

        // 토큰 만료 확인
        try {
            jwtProvider.validateExpiration(refresh);
        } catch (ExpiredJwtException e) {
            log.info("refresh expired");
            return new ResponseEntity<>("refresh expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰 카테고리 확인
        String category = jwtProvider.getCategory(refresh);

        if (!"refresh".equals(category)) {
            log.info("Invalid refresh category");
            return new ResponseEntity<>("invalid refresh category", HttpStatus.BAD_REQUEST);
        }

        // 사용자 정보 확인
        String userId = jwtProvider.getUserId(refresh);
        String userNick = jwtProvider.getUserNick(refresh);
        String userRole = jwtProvider.getUserRole(refresh);
    
        // 레디스 존재여부 확인
        if (!refreshTokenService.isValid(userId, refresh)) {
            log.info("Invalid refresh redis");
            return new ResponseEntity<>("invalid refresh redis", HttpStatus.BAD_REQUEST);
        }

        String newAccess = jwtProvider.create("access", userId, userNick, userRole, 30 * 60 * 1000L);
        String newRefresh = jwtProvider.create("refresh", userId, userNick, userRole, 24 * 60 * 60 * 1000L);
    
        // redis 갱신
        refreshTokenService.delete(userId); // 기존 토큰 삭제
        refreshTokenService.save(userId, newRefresh, 24 * 60 * 60 * 1000L); // 새 토큰 저장

        Cookie refreshCookie = new Cookie("refresh", newRefresh);
        refreshCookie.setMaxAge(24 * 60 * 60); // 1일
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setAttribute("SameSite", "None");
        response.addCookie(refreshCookie);

        response.setHeader("access", newAccess);
        // response.addCookie(createCookie("refresh", newRefresh));

        log.info("reissue success: {}", userId);
        log.info("access token 재발급 성공: {}", newAccess);
        log.info("refresh token 재발급 성공: {}", newRefresh);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}