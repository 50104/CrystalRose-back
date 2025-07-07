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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final AccessTokenBlacklistService accessTokenBlacklistService;

    public JWTFilter(JwtTokenProvider jwtProvider, AccessTokenBlacklistService accessTokenBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.matches("^/connect(/.*)?$");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.startsWith("/login") ||
            uri.startsWith("/oauth2/") ||
            uri.startsWith("/reissue") ||
            uri.startsWith("/actuator/health") ||
            uri.startsWith("/connect") ||
            uri.startsWith("/api/v1/auth/") ||
            uri.startsWith("/api/v1/wiki/list") ||
            uri.startsWith("/api/v1/wiki/detail/") ||
            uri.equals("/") ||
            uri.equals("/join") ||
            uri.startsWith("/static/") ||
            uri.startsWith("/images/") ||
            uri.startsWith("/upload/") ||
            uri.startsWith("/swagger-ui") ||
            uri.startsWith("/v3/api-docs") ||
            uri.startsWith("/swagger-resources") ||
            uri.startsWith("/webjars") ||
            uri.equals("/favicon.ico") ||
            uri.equals("/error")
        ) {
            log.info("토큰 검사 제외: {}", uri);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("토큰 검사 대상 URI: {}", uri);

        String bearerToken = request.getHeader("Authorization");

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
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
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
