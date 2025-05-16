package com.rose.back.domain.auth.jwt;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ReissueControllerDocs {

    @Operation(summary = "리프레쉬 토큰 불러오기", description = "리프레쉬 토큰을 불러옵니다")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "불러오기 성공"),
    @ApiResponse(responseCode = "409", description = "불러오기 실패")})
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response);
    
}
