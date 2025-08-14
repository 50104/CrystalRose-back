package com.rose.back.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.auth.service.RefreshTokenService;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.entity.UserEntity.UserStatus;
import com.rose.back.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    private static final long ACCESS_EXP_TIME = 15 * 60 * 1000L; // 15분
    private static final long REFRESH_EXP_TIME = 14L * 24 * 60 * 60 * 1000L; // 14일

    public LoginFilter(AuthenticationManager authenticationManager,
                        JwtTokenProvider jwtProvider,
                        RefreshTokenService refreshTokenService,
                        UserRepository userRepository,
                        ObjectMapper objectMapper) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
        this.objectMapper = objectMapper;

        this.setFilterProcessesUrl("/api/v1/auth/login");
        this.setUsernameParameter("userId");
        this.setPasswordParameter("userPwd");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String userId = obtainUsername(request);
        String userPwd = obtainPassword(request);

        try {
            if ((userId == null || userPwd == null) &&
                "application/json".equalsIgnoreCase(Objects.toString(request.getContentType(), ""))) {
                String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                LoginRequest loginRequest = objectMapper.readValue(body, LoginRequest.class);
                userId = loginRequest.getUserId();
                userPwd = loginRequest.getUserPwd();
            }
        } catch (Exception e) {
            throw new AuthenticationException("Invalid JSON request") {};
        }

        if (userId == null || userPwd == null) {
            throw new AuthenticationException("Username and password are required") {};
        }

        // 계정 상태 확인
        UserEntity user = userRepository.findByUserId(userId);
        if (user != null) {
            if (user.getUserStatus() == UserStatus.DELETED) {
                throw new DisabledException("삭제된 계정입니다.");
            }
            if (user.getUserStatus() == UserStatus.WITHDRAWAL_PENDING) {
                request.setAttribute("withdrawalPending", true);
                response.setHeader("withdrawal", "true");
            }
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userId, userPwd);
        return authenticationManager.authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication)
            throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = authentication.getName();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();
        String userNick = userDetails.getUserNick();
        Long userNo = userDetails.getUserNo();

        String access = jwtProvider.create("access", userId, userNick, userRole, ACCESS_EXP_TIME, userNo);
        String refresh = jwtProvider.create("refresh", userId, userNick, userRole, REFRESH_EXP_TIME, userNo);

        // Refresh 회전 대비 저장
        refreshTokenService.delete(userId);
        refreshTokenService.save(userId, refresh, REFRESH_EXP_TIME);

        // Refresh 쿠키 추가(경로 최소화)
        boolean secure = isHttps(request);
        addRefreshSetCookieHeader(response, refresh, (int) (REFRESH_EXP_TIME / 1000), secure);

        // Access는 헤더 + JSON 동시 제공
        response.setHeader("Authorization", "Bearer " + access);
        if (Boolean.TRUE.equals(request.getAttribute("withdrawalPending"))) {
            response.setHeader("withdrawal", "true");
        }

        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"accessToken\":\"" + access + "\"}");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {
        String msg = failed.getMessage();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        if ("탈퇴 요청된 계정입니다.".equals(msg)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"withdrawal\"}");
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\":\"invalid_credentials\"}");
    }

    private boolean isHttps(HttpServletRequest request) {
        String xfProto = request.getHeader("X-Forwarded-Proto");
        if (xfProto != null) {
            return "https".equalsIgnoreCase(xfProto);
        }
        return request.isSecure() || request.getRequestURL().toString().startsWith("https://");
    }

    private void addRefreshSetCookieHeader(HttpServletResponse response, String value, int maxAgeSeconds, boolean secure) {
        StringBuilder sb = new StringBuilder();
        sb.append("refresh=").append(value)
          .append("; Path=/")
          .append("; HttpOnly")
          .append("; Max-Age=").append(maxAgeSeconds);
        if (secure) {
            sb.append("; Secure").append("; SameSite=None");
        }
        response.addHeader("Set-Cookie", sb.toString());
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter("userPwd");
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter("userId");
    }

    // 요청 JSON 바인딩용 DTO
    static class LoginRequest {
        private String userId;
        private String userPwd;
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserPwd() { return userPwd; }
        public void setUserPwd(String userPwd) { this.userPwd = userPwd; }
    }
}
