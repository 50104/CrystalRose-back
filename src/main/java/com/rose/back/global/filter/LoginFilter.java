package com.rose.back.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.auth.service.RefreshTokenService;

import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtProvider, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    // 로그인 요청
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException { 
        String userId = obtainUsername(request);
        String userPwd = obtainPassword(request);
        log.info("Attempting authentication for user: {}", userId);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userId, userPwd); 
        return authenticationManager.authenticate(token);
    }

    //로그인 성공 시 수행할 작업
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        //사용자의 아이디, 비밀번호, 역할, 닉네임을 갖는 CustomUserDetails 가져옴
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = authentication.getName();

        //역할은 Collection 으로 저장되어있음
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String userRole = authorities.iterator().next().getAuthority();
        String userNick = customUserDetails.getUserNick();

        String access = jwtProvider.create("access", userId, userRole, userNick, 30*60*1000L); // 30분
        String refresh = jwtProvider.create("refresh", userId, userRole, userNick, 24*60*60*1000L); // 1일

        refreshTokenService.delete(userId);
        refreshTokenService.save(userId, refresh, 24 * 60 * 60 * 1000L);

        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
        log.info("Authentication successful for user: {}", userId);
    }

    //로그인 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401);
        log.info("Authentication failed: {}", failed.getMessage());
    }
    
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter("userPwd");
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter("userId");
    }
}
