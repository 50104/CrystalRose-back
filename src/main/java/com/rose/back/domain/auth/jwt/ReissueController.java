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

import com.rose.back.domain.user.entity.RefreshEntity;
import com.rose.back.domain.user.repository.RefreshRepository;

import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@ResponseBody
public class ReissueController implements ReissueControllerDocs{

    private final JwtTokenProvider jwtProvider;
    private final RefreshRepository refreshRepository;

    public ReissueController(JwtTokenProvider jwtProvider, RefreshRepository refreshRepository) {
        this.jwtProvider = jwtProvider;
        this.refreshRepository = refreshRepository;
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
            jwtProvider.isExpired(refresh);
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
    
        // DB 존재여부 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            log.info("Invalid refresh database");
            return new ResponseEntity<>("invalid refresh database", HttpStatus.BAD_REQUEST);
        }

        String userId = jwtProvider.getUserId(refresh);
        String userNick = jwtProvider.getUserNick(refresh);
        String userRole = jwtProvider.getUserRole(refresh);

        String newAccess = jwtProvider.create("access", userId, userNick, userRole, 30 * 60 * 1000L);
        String newRefresh = jwtProvider.create("refresh", userId, userNick, userRole, 24 * 60 * 60 * 1000L);
    
        // DB에 기존 토큰 삭제 후 새 토큰 저장
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(userId, newRefresh, 24 * 60 * 60 * 1000L);

        String cookieStr = "refresh=" + newRefresh
                + "; Max-Age=" + (24 * 60 * 60)
                + "; Path=/"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=None";
        response.setHeader("Set-Cookie", cookieStr);

        response.setHeader("access", newAccess);
        // response.addCookie(createCookie("refresh", newRefresh));

        log.info("reissue success: {}", userId);
        log.info("access token 재발급 성공: {}", newAccess);
        log.info("refresh token 재발급 성공: {}", newRefresh);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void addRefreshEntity(String userId, String refresh, Long expiredMs) {
        List<RefreshEntity> existingTokens = refreshRepository.findByUserId(userId);
        if (!existingTokens.isEmpty()) {
            refreshRepository.deleteAll(existingTokens);
        }
        Date date = new Date(System.currentTimeMillis() + expiredMs);
        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUserId(userId);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date);
        refreshRepository.save(refreshEntity);
        log.info("new refresh: {}", userId);
    }
}