package com.rose.back.global.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomOAuth2User;
import com.rose.back.domain.user.entity.RefreshEntity;
import com.rose.back.domain.user.repository.RefreshRepository;

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
        String refresh = jwtProvider.create("refresh", userId, userNick, userRole, 24 * 60 * 60 * 1000L);
        addRefreshEntity(userId, refresh, 86400000L);

        String cookieStr = "refresh=" + refresh
                + "; Max-Age=" + (24 * 60 * 60)
                + "; Path=/"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=None";
        response.setHeader("Set-Cookie", cookieStr);
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
}
