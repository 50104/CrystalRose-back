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
    private final ObjectMapper objectMapper;

    public JWTFilter(JwtTokenProvider jwtProvider,
                    AccessTokenBlacklistService accessTokenBlacklistService,
                    ObjectMapper objectMapper) {
    this.jwtProvider = jwtProvider;
    this.accessTokenBlacklistService = accessTokenBlacklistService;
    this.objectMapper = objectMapper;
}


    // JWT 검증 제외 경로
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
        "/error",
        "/static/**",
        "/images/**",
        "/upload/**",
        "/webjars/**",
        "/favicon.ico",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/",
        "/reissue",
        "/oauth2/**",
        "/connect/**",
        "/api/v1/auth/**",
        "/actuator/health",
        "/api/v1/wiki/list",
        "/api/v1/wiki/detail/**"
    );

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : EXCLUDE_PATHS) {
            if (pathMatcher.match(pattern, uri)) {
                log.info("토큰 검사 제외: {}", uri);
                return true;
            }
        }
        log.info("토큰 검사 대상: {}", uri);
        return false;
    }

    // 토큰 없이도 접근 가능한 공개 API (토큰 있을 경우 검증)
    private boolean isOptionalAuthPath(String uri) {
        return uri.startsWith("/api/calendar/data");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.info("JWT 필터 실행 URI: {}", uri);

        String bearerToken = request.getHeader("Authorization");
        boolean hasToken = StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ");
        boolean isOptional = isOptionalAuthPath(uri);

        if (!hasToken) {
            if (isOptional) {
                log.info("비로그인 허용 경로 - 토큰 없음: {}", uri);
                filterChain.doFilter(request, response);
                return;
            } else {
                log.warn("Authorization 헤더 없음 또는 Bearer 아님: {}", bearerToken);
                sendError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "AT", "Access token이 존재하지 않거나 Bearer 형식이 아닙니다.");
                return;
            }
        }

        String accessToken = resolveToken(bearerToken);

        // 블랙리스트 검증
        if (accessTokenBlacklistService.isBlacklisted(accessToken)) {
            log.warn("블랙리스트 토큰 요청: {}", accessToken);
            if (isOptional) {
                log.info("Optional 경로지만 블랙리스트 토큰 → 통과");
                filterChain.doFilter(request, response);
                return;
            } else {
                sendError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "BL", "로그아웃된 토큰입니다.");
                return;
            }
        }

        // 만료 검증
        try {
            jwtProvider.validateExpiration(accessToken);
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료: {}", e.getMessage());
            if (isOptional) {
                log.info("Optional 경로지만 만료 토큰 → 통과");
                filterChain.doFilter(request, response);
                return;
            } else {
                sendError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "EX", "Access token이 만료되었습니다.");
                return;
            }
        }

        // 카테고리 검증
        if (!"access".equals(jwtProvider.getCategory(accessToken))) {
            log.warn("유효하지 않은 토큰 카테고리");
            if (isOptional) {
                filterChain.doFilter(request, response);
                return;
            } else {
                sendError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "CT", "유효하지 않은 토큰 카테고리입니다.");
                return;
            }
        }

        // 유저 정보 설정
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
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("SecurityContext 인증 객체 설정 완료: {}", userId);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(String bearerToken) {
        if (!StringUtils.hasText(bearerToken)) return null;
        if (!bearerToken.startsWith("Bearer ")) return null;
        return bearerToken.substring(7);
    }

    private void sendError(HttpServletResponse response, HttpServletRequest request, int status, String code, String message) throws IOException {
        ErrorResponse err = new ErrorResponse(
                status, code, message,
                request.getRequestURI(), LocalDateTime.now()
        );
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(err));
    }
}
