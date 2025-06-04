package com.rose.back.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.auth.service.AccessTokenBlacklistService;
import com.rose.back.domain.user.dto.UserInfoDto;

import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final AccessTokenBlacklistService accessTokenBlacklistService;

    public JWTFilter(JwtTokenProvider jwtProvider, AccessTokenBlacklistService accessTokenBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.matches("^/connect(/.*)?$");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI(); // 재로그인, 무한로프 방지
        log.info("Request URI: {}", requestUri);
        if (requestUri.matches("^\\/login(?:\\/.*)?$") ||
            requestUri.matches("^\\/oauth2(?:\\/.*)?$") ||
            requestUri.matches("^\\/reissue$")) {
            filterChain.doFilter(request, response);
            return;
        }


        String accessToken = request.getHeader("access");
        log.info("Access token: {}", accessToken);

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 블랙리스트 확인
        if (accessTokenBlacklistService.isBlacklisted(accessToken)) {
            log.info("Access token is blacklisted");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    
        // 토큰 만료 확인
        try {
            jwtProvider.validateExpiration(accessToken);
        } catch (ExpiredJwtException e) {
            log.info("access token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인
        String category = jwtProvider.getCategory(accessToken);

        // request header에서 access로 온 토큰이 진짜 access 인지 확인
        if (!category.equals("access")) {
            log.info("invalid access token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // userId, userRole 값 획득
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

        // 인증 객체 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication class: {}", authentication.getClass());
        log.info("Principal class: {}", authentication.getPrincipal().getClass());
        log.info("Principal: {}", authentication.getPrincipal());

        filterChain.doFilter(request, response);
    }
}