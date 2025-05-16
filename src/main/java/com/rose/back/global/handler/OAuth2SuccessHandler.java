package com.rose.back.global.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomOAuth2User;
import com.rose.back.domain.auth.service.RefreshTokenService;

import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler { // OAuth2 인증 성공 시 호출
    
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String userRole = authorities.iterator().next().getAuthority();
        String userNick = oAuth2User.getName();
        String userId = oAuth2User.getUsername();

        String refresh = jwtProvider.create("refresh", userId, userNick, userRole, 24 * 60 * 60 * 1000L);

        refreshTokenService.delete(userId);
        refreshTokenService.save(userId, refresh, 24 * 60 * 60 * 1000L);

        // 쿠키 세팅
        Cookie cookie = new Cookie("refresh", refresh);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
        response.sendRedirect("http://localhost:3000/getAccess");
    }
}
