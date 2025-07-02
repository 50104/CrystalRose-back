package com.rose.back.global.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        
        log.error("OAuth2 인증 실패: {}", exception.getMessage());
        
        String errorMessage = URLEncoder.encode("소셜 로그인에 실패했습니다.", StandardCharsets.UTF_8);
        String redirectUrl = determineFailureRedirectUrl(request, errorMessage);
        
        response.sendRedirect(redirectUrl);
    }
    
    private String determineFailureRedirectUrl(HttpServletRequest request, String errorMessage) {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        
        // 웹 브라우저 처리
        String baseUrl = "http://localhost:3000";
        if (origin != null) {
            baseUrl = origin;
        } else if (referer != null) {
            try {
                java.net.URL url = new java.net.URL(referer);
                baseUrl = url.getProtocol() + "://" + url.getHost() + 
                        (url.getPort() != -1 ? ":" + url.getPort() : "");
            } catch (Exception e) {
                log.warn("Referer URL 파싱 실패: {}", referer);
            }
        }
        return baseUrl + "/login?error=" + errorMessage;
    }
}
