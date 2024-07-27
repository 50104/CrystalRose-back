package com.rose.back.user.filter;

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

import com.rose.back.user.dto.oauth2.CustomUserDetails;
import com.rose.back.user.entity.RefreshEntity;
import com.rose.back.user.provider.JwtProvider;
import com.rose.back.user.repository.RefreshRepository;

import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider, RefreshRepository refreshRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.refreshRepository = refreshRepository;
    }

    // 로그인 요청
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException { 
        String userId = obtainUsername(request);
        String userPwd = obtainPassword(request);
        log.debug("Attempting authentication for user: {}", userId);
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
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        
        String userRole = auth.getAuthority();
        String userNick = customUserDetails.getUserNick();
        String access = jwtProvider.create("access", userId, userRole, userNick, 30*60*1000L); // 30분
        String refresh = jwtProvider.create("refresh", userId, userRole, userNick, 24*60*60*1000L); // 1일
        addRefreshEntity(userId, refresh, 86400000L); // 1일

        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
        log.debug("Authentication successful for user: {}", userId);
        System.out.println("ooo");
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

    //로그인 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401);
        log.debug("Authentication failed: {}", failed.getMessage());
        System.out.println("xxx");
    }
    
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setHttpOnly(true);
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
