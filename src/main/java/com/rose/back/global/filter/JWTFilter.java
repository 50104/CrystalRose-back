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
        String requestUri = request.getRequestURI(); // 재로그인, 무한로프 방지
        log.info("Request URI: {}", requestUri);

        // 토큰 검사 제외 경로
        if (requestUri.matches("^\\/login(?:\\/.*)?$") ||
            requestUri.matches("^\\/oauth2(?:\\/.*)?$") ||
            requestUri.matches("^\\/reissue$") ||
            requestUri.matches("^\\/actuator\\/health$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");
        String accessToken = resolveToken(bearerToken);

        if (accessToken == null) {
            log.info("Authorization 헤더에 토큰이 없습니다.");
            filterChain.doFilter(request, response);
            return;
        }

        // 블랙리스트 확인
        if (accessTokenBlacklistService.isBlacklisted(accessToken)) {
            log.warn("Access토큰이 블랙리스트에 있습니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    
        // 토큰 만료 확인
        try {
            jwtProvider.validateExpiration(accessToken);
        } catch (ExpiredJwtException e) {
            log.warn("Access토큰이 만료되었습니다: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!"access".equals(jwtProvider.getCategory(accessToken))) {
            log.warn("토큰 카테고리가 'access'가 아닙니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
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

        // 인증 객체 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("인증 정보 설정 완료: userId={}, userRole={}, userNick={}", userId, userRole, userNick);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new MissingAccessTokenException();
    }
}
