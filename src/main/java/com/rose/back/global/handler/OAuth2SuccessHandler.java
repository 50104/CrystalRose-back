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
import java.net.URL;
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
        
        // 환경에 따른 쿠키 설정
        String requestUrl = request.getRequestURL().toString();
        boolean isHttps = requestUrl.startsWith("https://");
        
        if (isHttps) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setSecure(false);
        }
        
        cookie.setPath("/");
        response.addCookie(cookie);

        // 동적 리다이렉트 URL 설정
        String redirectUrl = determineRedirectUrl(request);
        response.sendRedirect(redirectUrl);
    }
    
    private String determineRedirectUrl(HttpServletRequest request) {
        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null && isValidRedirectUri(redirectUri)) {
            return redirectUri + "/getAccess";
        }

        String origin = request.getHeader("Origin");
        if (origin != null && !origin.contains("localhost")) {
            return origin + "/getAccess";
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                URL url = new URL(referer);
                return url.getProtocol() + "://" + url.getHost() +
                    (url.getPort() != -1 ? ":" + url.getPort() : "") + "/getAccess";
            } catch (Exception e) {
            }
        }

        return frontendUrl + "/getAccess";
    }

    private boolean isValidRedirectUri(String uri) {
        return uri.startsWith("https://dodorose.com") || uri.startsWith("http://localhost:3000");
    }
}
