package com.rose.back.domain.auth.jwt;

import org.springframework.http.ResponseEntity;

import com.rose.back.global.exception.JwtReissueErrorResponses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "JWT 재발급", description = "JWT 토큰 재발급 관련 API")
public interface ReissueControllerDocs {

    @Operation(
        summary = "리프레시 토큰으로 액세스/리프레시 토큰 재발급",
        description = "쿠키에 저장된 리프레시 토큰을 이용해 새로운 액세스 토큰과 리프레시 토큰을 발급합니다."
    )
    @JwtReissueErrorResponses
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    ResponseEntity<?> reissue(
        @Parameter(description = "HTTP 요청 객체") HttpServletRequest request,
        @Parameter(description = "HTTP 응답 객체") HttpServletResponse response
    );
}