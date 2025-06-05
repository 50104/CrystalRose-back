package com.rose.back.global.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.service.AccessTokenBlacklistService;
import com.rose.back.domain.auth.service.RefreshTokenService;

import java.io.IOException;

@Component
public class CustomLogoutFilter extends GenericFilterBean {
    
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService; 

    public CustomLogoutFilter(JwtTokenProvider jwtProvider, RefreshTokenService refreshTokenService, AccessTokenBlacklistService accessTokenBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI(); // 로그아웃 요청이 아닌 경우
        String requestMethod = httpRequest.getMethod(); // 로그아웃 POST 요청(refresh token 전달)

        // 로그아웃 요청이 확정된 이후
        if (!"/logout".equals(requestUri) || !"POST".equals(requestMethod)) {
            chain.doFilter(request, response);
            return;
        }

        // AccessToken 헤더에서 추출
        String accessToken = resolveAccessToken(httpRequest);
        if (accessToken != null) {
            try {
                jwtProvider.validateExpiration(accessToken);
                long exp = jwtProvider.getExpiration(accessToken);
                accessTokenBlacklistService.blacklist(accessToken, exp);
            } catch (ExpiredJwtException ignored) {} // 이미 만료된 경우 블랙리스트 제외
        }

        // refresh token 확인
        String refresh = null; 
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }
        }

        if (refresh == null) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jwtProvider.validateExpiration(refresh); // 만료된 경우 예외 발생
        } catch (ExpiredJwtException e) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!"refresh".equals(jwtProvider.getCategory(refresh))) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String userId = jwtProvider.getUserId(refresh);
        refreshTokenService.delete(userId); // Redis에서 삭제

        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");

        httpResponse.addCookie(cookie);
        httpResponse.setStatus(HttpServletResponse.SC_OK);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}