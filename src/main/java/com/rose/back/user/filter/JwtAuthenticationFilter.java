package com.rose.back.user.filter;

import java.io.IOException;
import java.util.*;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rose.back.user.repository.UserRepository;
import com.rose.back.user.entity.UserEntity;
import com.rose.back.user.provider.JwtProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    //OncePerRequestFilter 추상 클래스 확장
    
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            
            String token = parseBearerToken(request); // 요청에서 Bearer 토큰 파싱
            if (token == null) { // 토큰이 없으면 다음 필터로 이동

                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 유효성 검사
            String userId = jwtProvider.validate(token); // token을 jwtProvider로 validate로 검증 -> 검증 통해서 userId 꺼내옴 
            if (userId == null) { // userId 가 null이면 doFilter로 검증 

                filterChain.doFilter(request, response);
                return;
            }

            // 사용자 정보 조회
            UserEntity userEntity = userRepository.findByUserId(userId); // user 정보 꺼내오기 
            String role = userEntity.getUserRole(); // role : ROLE_USER, ROLE_ADMIN // 권한 지정

            // System.out.println(role);

            // 권한 생성
            // ROLE_DEVELOPER, ROLE_BOSS ... etc 권한의 배열형태
            List<GrantedAuthority> authorities = new ArrayList<>(); // 권한 리스트
            authorities.add(new SimpleGrantedAuthority(role));

            // 인증 토큰 생성
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext(); // 빈 context 만들어주기
            // context 안에 담을 token 만들기
            AbstractAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, authorities); // object를 받을수있음 // 유저정보, 비밀번호, 권한 리스트
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // request detail에 넣어줌

            // 인증 정보 설정
            securityContext.setAuthentication(authenticationToken); // context에 token 넣어줌
            SecurityContextHolder.setContext(securityContext); // context를 등록시켜줌

        } catch (Exception e) {
            
            e.printStackTrace();
        }

        filterChain.doFilter(request, response); // 다음 filter로 넘어가도록 해줌
    }

    // request 객체로부터 token 가져오는 작업
    private String parseBearerToken(HttpServletRequest request) {

        // Authorization 헤더에서 토큰 파싱
        String authorization = request.getHeader("Authorization"); // request의 header로 부터 Authorization 가져옴//

        boolean hasAuthorization = StringUtils.hasText(authorization);
        if (!hasAuthorization) return null;

        boolean isBearer = authorization.startsWith("Bearer "); // 'Bearer '로 시작하는지 확인
        if (!isBearer) return null;

        // Bearer 토큰 추출
        String token = authorization.substring(7);
        return token;
    }
}
