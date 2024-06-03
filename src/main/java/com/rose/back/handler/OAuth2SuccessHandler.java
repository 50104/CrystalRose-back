package com.rose.back.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.rose.back.entity.CustomOAuth2User;
import com.rose.back.provider.JwtProvider;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtProvider jwtProvider;

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