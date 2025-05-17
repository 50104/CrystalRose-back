package com.rose.back.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.auth.service.RefreshTokenService;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.enums.UserStatus;
import com.rose.back.domain.user.repository.UserRepository;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtProvider, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.userRepository = userRepository;
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
        
        UserEntity user = userRepository.findByUserId(userId); // 탈퇴 예약된 계정인지 확인
        if (user != null) {
            if (user.getUserStatus() == UserStatus.DELETED) {
                throw new DisabledException("삭제된 계정입니다.");
            }
            if (user.getUserStatus() == UserStatus.WITHDRAWAL_PENDING) {
                log.info("탈퇴 예약 계정입니다: {}", userId);
                request.setAttribute("withdrawalPending", true);
            }
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userId, userPwd); 
        return authenticationManager.authenticate(token);
    }

    //로그인 성공 시 수행할 작업
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        //사용자의 아이디, 비밀번호, 역할, 닉네임을 갖는 CustomUserDetails 가져옴
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = authentication.getName();
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        String userNick = customUserDetails.getUserNick();

        String access = jwtProvider.create("access", userId, userRole, userNick, 30*60*1000L); // 30분
        String refresh = jwtProvider.create("refresh", userId, userRole, userNick, 24*60*60*1000L); // 1일

        refreshTokenService.delete(userId);
        refreshTokenService.save(userId, refresh, 24 * 60 * 60 * 1000L);

        response.setHeader("access", access);
        if (Boolean.TRUE.equals(request.getAttribute("withdrawalPending"))) {
            response.setHeader("withdrawal", "true");
        }
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
        log.info("Authentication successful for user: {}", userId);
    }

    //로그인 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        String msg = failed.getMessage();
        log.info("Authentication failed: {}", msg);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        if ("탈퇴 요청된 계정입니다.".equals(msg)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.getWriter().write("탈퇴 요청된 계정입니다.");
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 일반 로그인 실패
        response.getWriter().write("아이디 또는 비밀번호 오류입니다.");
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
