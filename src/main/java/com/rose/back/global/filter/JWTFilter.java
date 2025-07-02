package com.rose.back.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.auth.service.AccessTokenBlacklistService;
import com.rose.back.domain.user.dto.UserInfoDto;
import com.rose.back.global.exception.MissingAccessTokenException;

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
        String uri = request.getRequestURI();

        if (uri.startsWith("/login") ||
            uri.startsWith("/oauth2") ||
            uri.startsWith("/reissue") ||
            uri.startsWith("/actuator/health") ||
            uri.startsWith("/connect") ||
            uri.startsWith("/api/v1/auth/id-check") ||  
            uri.startsWith("/api/v1/auth/email") ||    
            uri.equals("/api/v1/auth/join") ||        
            uri.equals("/api/v1/auth/find") ||    
            uri.equals("/api/v1/auth/reset")       
        ) {
            log.info("토큰 검사 제외: {}", uri);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("토큰 검사 대상 URI: {}", uri);

        String bearerToken = request.getHeader("Authorization");
        String accessToken = resolveToken(bearerToken);

        // 블랙리스트 확인
        if (accessTokenBlacklistService.isBlacklisted(accessToken)) {
            log.warn("블랙리스트 토큰: {}", accessToken);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰 만료 확인
        try {
            jwtProvider.validateExpiration(accessToken);
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!"access".equals(jwtProvider.getCategory(accessToken))) {
            log.warn("잘못된 토큰 카테고리");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 인증 정보 설정
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
        throw new MissingAccessTokenException();
    }
}
