package com.rose.back.global.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomOAuth2User;
import com.rose.back.domain.auth.service.RefreshTokenService;
import com.rose.back.domain.user.repository.UserRepository;

import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    
    @Value("${app.oauth2.redirect.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String userRole = authorities.iterator().next().getAuthority();
        String userNick = oAuth2User.getName();
        String userId = oAuth2User.getUsername();
        Long userNo = userRepository.findByUserId(userId).getUserNo();

        String refresh = jwtProvider.create("refresh", userId, userNick, userRole, 24 * 60 * 60 * 1000L, userNo);
        refreshTokenService.delete(userId);
        refreshTokenService.save(userId, refresh, 24 * 60 * 60 * 1000L);

        // refresh 토큰만 쿠키에 저장
        Cookie cookie = new Cookie("refresh", refresh);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);

        // 동적 리다이렉트 URL 설정
        String redirectUrl = determineRedirectUrl(request);
        response.sendRedirect(redirectUrl);
    }
    
    private String determineRedirectUrl(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        
        // Origin이나 Referer를 통해 동적으로 결정
        if (origin != null) {
            return origin + "/getAccess";
        } else if (referer != null) {
            // Referer에서 도메인 추출
            try {
                java.net.URL url = new java.net.URL(referer);
                return url.getProtocol() + "://" + url.getHost() + 
                      (url.getPort() != -1 ? ":" + url.getPort() : "") + "/getAccess";
            } catch (Exception e) {
                return frontendUrl + "/getAccess";
            }
        }
        return frontendUrl + "/getAccess"; // 기본값 (설정값 사용)
    }
}
