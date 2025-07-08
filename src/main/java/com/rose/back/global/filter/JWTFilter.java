package com.rose.back.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.auth.service.AccessTokenBlacklistService;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.global.handler.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final AccessTokenBlacklistService accessTokenBlacklistService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 중앙 집중식 JWT 검사 제외 경로 리스트
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/login/**",
            "/oauth2/**",
            "/reissue",
            "/actuator/health",
            "/connect/**",
            "/api/v1/auth/**",
            "/api/v1/wiki/list",
            "/api/v1/wiki/list/",
            "/api/v1/wiki/detail/**",
            "/",
            "/join",
            "/static/**",
            "/images/**",
            "/upload/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico",
            "/error"
    );

    public JWTFilter(JwtTokenProvider jwtProvider, AccessTokenBlacklistService accessTokenBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : EXCLUDE_PATHS) {
            if (pathMatcher.match(pattern, uri) || uri.startsWith(pattern.replace("/**", ""))) {
                log.info("토큰 검사 제외: {}", uri);
                return true;
            }
        }
        log.info("토큰 검사 대상: {}", uri);
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        log.info("토큰 검사 대상 URI: {}", uri);

        String bearerToken = request.getHeader("Authorization");

        // 캘린더 API는 토큰이 없어도 허용
        if (uri.startsWith("/api/calendar/data")) {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                log.info("캘린더 API - 비인증 사용자 요청: {}", uri);
                filterChain.doFilter(request, response);
                return;
            }
        } else {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                log.warn("Authorization 헤더가 없거나 Bearer 형식이 아님: {}", bearerToken);
                sendError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "AT", "Access token이 존재하지 않거나 Bearer 형식이 아닙니다.");
                return;
            }
        }

        String accessToken = resolveToken(bearerToken);

        if (accessTokenBlacklistService.isBlacklisted(accessToken)) {
            log.warn("블랙리스트 토큰: {}", accessToken);
            if (uri.startsWith("/api/calendar/data")) {
                filterChain.doFilter(request, response);
                return;
            } else {
                sendError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "BL", "로그아웃된 토큰입니다.");
                return;
            }
        }

        try {
            jwtProvider.validateExpiration(accessToken);
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료: {}", e.getMessage());
            if (uri.startsWith("/api/calendar/data")) {
                filterChain.doFilter(request, response);
                return;
            } else {
                sendError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "EX", "Access token이 만료되었습니다.");
                return;
            }
        }

        if (!"access".equals(jwtProvider.getCategory(accessToken))) {
            log.warn("잘못된 토큰 카테고리");
            if (uri.startsWith("/api/calendar/data")) {
                filterChain.doFilter(request, response);
                return;
            } else {
                sendError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "CT", "유효하지 않은 토큰 카테고리입니다.");
                return;
            }
        }

        String userId = jwtProvider.getUserId(accessToken);
        String userRole = jwtProvider.getUserRole(accessToken);
        String userNick = jwtProvider.getUserNick(accessToken);
        Long userNo = jwtProvider.getUserNo(accessToken);

        UserInfoDto userDto = UserInfoDto.builder()
                .userNo(userNo)
                .userName(userId)
                .userRole(userRole)
                .userNick(userNick)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(userDto);
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("인증 완료: {}", userId);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(String bearerToken) {
        if (!StringUtils.hasText(bearerToken)) return null;
        if (!bearerToken.startsWith("Bearer ")) return null;
        return bearerToken.substring(7);
    }

    private void sendError(HttpServletResponse response, HttpServletRequest request, int status, String code, String message) throws IOException {
        ErrorResponse err = new ErrorResponse(
            status,
            code,
            message,
            request.getRequestURI(),
            LocalDateTime.now()
        );
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(err));
    }
}