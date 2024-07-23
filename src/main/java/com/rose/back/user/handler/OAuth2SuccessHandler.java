package com.rose.back.user.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.rose.back.user.dto.oauth2.CustomOAuth2User;
import com.rose.back.user.entity.RefreshEntity;
import com.rose.back.user.provider.JwtProvider;
import com.rose.back.user.repository.RefreshRepository;

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
    
    private final JwtProvider jwtProvider;
    private final RefreshRepository refreshRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        
        String userNick = oAuth2User.getName();
        String userId = oAuth2User.getUsername();
        String userRole = auth.getAuthority();
        String refresh = jwtProvider.create("refresh", userId, userRole, userNick, 24 * 60 * 60 * 1000L);
        addRefreshEntity(userId, refresh, 86400000L);

        response.addCookie(createCookie("refresh", refresh));
        response.sendRedirect("http://localhost:3000/getAccess");
    }
    
    // Refresh 토큰 저장
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
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);// 1시간
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
