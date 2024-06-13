package com.rose.back.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "User", description = "User 관련 API입니다.")
public interface UserControllerDocs {

        @Operation(summary = "마이 페이지", description = "마이 페이지에 입장합니다")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "마이 페이지 입장 성공"),
        @ApiResponse(responseCode = "409", description = "마이 페이지 입장 실패")})
        public String myAPI();
}
