package com.rose.back.domain.user.controller.docs;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.rose.back.global.exception.CommonErrorResponses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@Tag(name = "Admin", description = "Admin 관련 API입니다.")
public interface AdminControllerDocs {

    @Operation(summary = "어드민 페이지", description = "어드민 페이지로 입장합니다")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "어드민 페이지 입장 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "어드민 페이지 입장 충돌",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "어드민 페이지 입장에 실패했습니다.",
                      "path": "/api/v1/admin"
                    }
                    """
                )
            )
        )
    })
    String adminP();

    @Operation(summary = "메인 페이지", description = "메인 페이지로 입장합니다")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메인 페이지 입장 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "메인 페이지 입장 충돌",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "메인 페이지 입장에 실패했습니다.",
                      "path": "/api/v1/admin/main"
                    }
                    """
                )
            )
        )
    })
    String mainApi();
}