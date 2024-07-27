package com.rose.back.config;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Configurable;
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
import lombok.RequiredArgsConstructor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.rose.back.user.filter.CustomLogoutFilter;
import com.rose.back.user.filter.JWTFilter;
import com.rose.back.user.filter.LoginFilter;
import com.rose.back.user.handler.OAuth2SuccessHandler;
import com.rose.back.user.provider.JwtProvider;
import com.rose.back.user.repository.RefreshRepository;
import com.rose.back.user.service.implement.CustomOAuth2UserService;
import com.rose.back.user.service.implement.CustomUserDetailsService;

// 인증 Authentication 방문자가 들어 갈 수있는지 확인
// 인가 Authorization 방문자가 방문했을 때, 허가된 공간에만 접근 가능

@Configurable
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final DefaultOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService oAuth2UserServiceImplement;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProvider jwtProvider;
    private final RefreshRepository refreshRepository;

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
            .authorizeHttpRequests(request -> request
                .requestMatchers("/", "/login", "/reissue", "/api/v1/auth/**", "/oauth2/**").permitAll() // 특정 URL 패턴에 대한 접근 권한 설정
                .requestMatchers("/api/v1/user/**").hasRole("USER") // 특정 URL 패턴에 대한 접근 권한 설정 (USER 역할 필요)
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // 특정 URL 패턴에 대한 접근 권한 설정 (ADMIN 역할 필요)
                .anyRequest().permitAll() // 모든 요청에 대해 인증이 필요함
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/v1/auth/oauth2")) // OAuth2 인증 엔드포인트 설정
                .redirectionEndpoint(endpoint -> endpoint.baseUri("/oauth2/callback/*")) // OAuth2 리다이렉션 엔드포인트 설정
                .userInfoEndpoint(endpoint -> endpoint.userService(oAuth2UserService) // OAuth2 사용자 정보 엔드포인트 설정
                .userService(oAuth2UserServiceImplement)) // OAuth2UserService 설정
                .successHandler(oAuth2SuccessHandler) // OAuth2 인증 성공 핸들러 설정
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new FailedAuthenticationEntryPoint()) // 인증 실패 핸들러 설정
            )
            .addFilterAt(new LoginFilter(authenticationManager(), jwtProvider, refreshRepository), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JWTFilter(jwtProvider), LoginFilter.class) // JWT 인증 필터 추가
            .addFilterBefore(new CustomLogoutFilter(jwtProvider, refreshRepository), LogoutFilter.class)
            .csrf((auth) -> auth.disable())
            .formLogin((auth) -> auth.disable())
            .httpBasic((auth) -> auth.disable());
            ;

        return httpSecurity.build();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:3000"); // 허용된 오리진 설정 (리액트 서버)
        corsConfiguration.addAllowedMethod("*"); // 허용된 HTTP 메서드 설정
        corsConfiguration.addAllowedHeader("*"); // 허용된 헤더 설정
        corsConfiguration.setAllowCredentials(true); // 자격 증명 허용 설정
        corsConfiguration.setMaxAge(3600L); // 1시간 동안 캐싱
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Headers", "Set-Cookie", "Authorization", "access", "refresh", "Access-Control-Allow-Origin"
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