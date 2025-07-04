package com.rose.back.config.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.rose.back.domain.auth.jwt.JwtTokenProvider;
import com.rose.back.domain.auth.oauth2.CustomOAuth2UserService;
import com.rose.back.domain.auth.service.AccessTokenBlacklistService;
import com.rose.back.domain.auth.service.CustomUserDetailsService;
import com.rose.back.domain.auth.service.RefreshTokenService;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.global.filter.CustomLogoutFilter;
import com.rose.back.global.filter.JWTFilter;
import com.rose.back.global.filter.LoginFilter;
import com.rose.back.global.handler.OAuth2SuccessHandler;
import com.rose.back.global.handler.OAuth2FailureHandler;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 인증 Authentication 방문자가 들어 갈 수있는지 확인
// 인가 Authorization 방문자가 방문했을 때, 허가된 공간에만 접근 가능

@Configurable
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final DefaultOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CustomOAuth2UserService oAuth2UserServiceImplement;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect.frontend-url}")
    private String frontendUrl;

    @Bean
    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .cors(cors -> cors
                .configurationSource(corsConfigurationSource())
            )
            .sessionManagement(sessionManagement -> 
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 생성 정책 설정 (STATELESS: 세션을 사용하지 않음)
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/v1/auth/oauth2")) // OAuth2 인증 엔드포인트 설정
                .redirectionEndpoint(endpoint -> endpoint.baseUri("/oauth2/callback/*")) // OAuth2 리다이렉션 엔드포인트 설정
                .userInfoEndpoint(endpoint -> endpoint.userService(oAuth2UserService) // OAuth2 사용자 정보 엔드포인트 설정
                .userService(oAuth2UserServiceImplement)) // OAuth2UserService 설정
                .successHandler(oAuth2SuccessHandler) // OAuth2 인증 성공 핸들러 설정
                .failureHandler(oAuth2FailureHandler) // OAuth2 인증 실패 핸들러 설정
            )
            .authorizeHttpRequests(request -> request
                // sockJS 사용 위한 /connect/**
                .requestMatchers("/", "/login", "/join", "/connect/**", "/reissue", "/oauth2/**").permitAll() // 특정 URL 패턴에 대한 접근 권한 설정
                .requestMatchers("/api/v1/auth/sign-up", "/api/v1/auth/sign-in", "/api/v1/auth/id-check", "/api/v1/auth/email-certification", "/api/v1/auth/check-certification", "/api/v1/auth/password-reset").permitAll() // 일반 인증 API만 허용
                .requestMatchers("/api/calendar/data").permitAll() // 캘린더 통합 데이터 API 허용
                .requestMatchers("/api/v1/wiki/list").permitAll() // 위키 목록 조회 API 허용
                .requestMatchers("/api/v1/wiki/detail/**").permitAll() // 위키 상세 조회 API 허용 (숫자 ID만)
                .requestMatchers("/static/**", "/images/**", "/upload/**").permitAll() // 정적 리소스 허용
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/favicon.ico").permitAll() // Swagger UI 허용
                .requestMatchers("/api/v1/user/**").hasRole("USER") // 특정 URL 패턴에 대한 접근 권한 설정 (USER 역할 필요)
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // 특정 URL 패턴에 대한 접근 권한 설정 (ADMIN 역할 필요)
                .requestMatchers("/actuator/health").permitAll() // API 헬스 체크
                .requestMatchers("/api/v1/auth/withdraw/**").authenticated()
                .requestMatchers("/error").permitAll() // 에러 페이지 허용
                .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new FailedAuthenticationEntryPoint()) // 인증 실패 핸들러 설정
            )
            .addFilterAt(new LoginFilter(authenticationManager(), jwtProvider, refreshTokenService, userRepository), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JWTFilter(jwtProvider, accessTokenBlacklistService), LoginFilter.class) // JWT 인증 필터 추가
            .addFilterBefore(new CustomLogoutFilter(jwtProvider, refreshTokenService, accessTokenBlacklistService), LogoutFilter.class)
            .csrf((auth) -> auth.disable())
            .formLogin((auth) -> auth.disable())
            .httpBasic((auth) -> auth.disable());
            ;

        return httpSecurity.build();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 개발 환경
        corsConfiguration.addAllowedOrigin("http://localhost:3000"); // 허용된 오리진 설정 (리액트 서버)
        corsConfiguration.addAllowedOrigin("http://localhost:4000"); // 백엔드 서버
        corsConfiguration.addAllowedOrigin(frontendUrl); // 프로덕션 환경에서의 프론트엔드 URL

        corsConfiguration.addAllowedMethod("*"); // 허용된 HTTP 메서드 설정
        corsConfiguration.addAllowedHeader("*"); // 허용된 헤더 설정
        corsConfiguration.setAllowCredentials(true); // 자격 증명 허용 설정
        corsConfiguration.setMaxAge(3600L); // 1시간 동안 캐싱
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "access", "refresh", "withdrawal", "Authorization"
            // , "Access-Control-Allow-Origin","Access-Control-Allow-Headers", "Set-Cookie"
            /* "x-xsrf-token, Origin, Accept, X-Requested-With, Content-Type, 
                Access-Control-Request-Method, Access-Control-Request-Headers" */
        )); // 노출된 헤더 설정
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    } // xx cors 에러2 같이 확인 "/api/v1/**" 변경
}

// 인증 실패 핸들러 클래스
class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json"); // 응답의 Content-Type을 JSON으로 설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 응답의 상태 코드를 403 Forbidden으로 설정
        response.getWriter().write("{\"code\": \"NP\", \"message\": \"No Permission.\"}"); // 응답의 내용을 JSON 형식으로 작성하여 출력
        // {"code" : "NP", "message" : "No Permission"}
    }
}