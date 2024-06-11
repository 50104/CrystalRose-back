package com.rose.back.user.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.rose.back.user.entity.CustomOAuth2User;
import com.rose.back.user.provider.JwtProvider;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler { // OAuth2 인증 성공 시 호출
    
    private final JwtProvider jwtProvider;

    /**
     * 인증 성공 시 호출되는 메소드
     * 
     * @param request 인증 요청 객체
     * @param response 인증 응답 객체
     * @param authentication 인증 객체
     * @throws IOException 입출력 예외가 발생할 경우
     * @throws ServletException 서블릿 예외가 발생할 경우
     */

    @Override
    public void onAuthenticationSuccess(
    HttpServletRequest request, 
    HttpServletResponse response,
    Authentication authentication
    ) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String userId = oAuth2User.getName();
        String token = jwtProvider.create(userId);

        response.sendRedirect("http://localhost:3000/auth/oauth-response/" + token + "/3600");

    }

}