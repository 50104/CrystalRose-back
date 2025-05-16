package com.rose.back.global.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ApiResponses(value = {
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Bad Request",
                value = """
                {
                  "status": 400,
                  "error": "Bad Request",
                  "message": "필수 파라미터 userId가 없습니다.",
                  "path": "/api/{endpoint}"
                }
                """
            )
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Unauthorized",
                value = """
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "인증 토큰이 유효하지 않습니다.",
                  "path": "/api/{endpoint}"
                }
                """
            )
        )
    ),
    @ApiResponse(
        responseCode = "403",
        description = "권한 없음",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Forbidden",
                value = """
                {
                  "status": 403,
                  "error": "Forbidden",
                  "message": "접근 권한이 없습니다.",
                  "path": "/api/{endpoint}"
                }
                """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "리소스 없음",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Not Found",
                value = """
                {
                  "status": 404,
                  "error": "Not Found",
                  "message": "사용자를 찾을 수 없습니다.",
                  "path": "/api/{endpoint}"
                }
                """
            )
        )
    ),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                name = "Internal Server Error",
                value = """
                {
                  "status": 500,
                  "error": "Internal Server Error",
                  "message": "예기치 못한 오류가 발생했습니다.",
                  "path": "/api/{endpoint}"
                }
                """
            )
        )
    )
})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommonErrorResponses {
}
