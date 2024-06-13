package com.rose.back.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Admin", description = "Admin 관련 API입니다.")
public interface AdminControllerDocs {

        @Operation(summary = "어드민 페이지", description = "어드민 페이지로 입장합니다")
        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "어드민 페이지 입장 성공"),
        @ApiResponse(responseCode = "409", description = "어드민 페이지 입장 실패") })
        public String adminP();
}
