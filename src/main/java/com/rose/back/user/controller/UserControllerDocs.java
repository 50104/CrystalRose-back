package com.rose.back.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "User", description = "User 관련 API입니다.")
public interface UserControllerDocs {

        @Operation(summary = "사용자 정보", description = "사용자 정보를 불러옵니다.")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 불러오기 성공"),
        @ApiResponse(responseCode = "409", description = "사용자 정보 불러오기 실패")})
        public ResponseEntity<?> get();
}
