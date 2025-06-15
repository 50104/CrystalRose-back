package com.rose.back.global.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.rose.back.global.handler.ErrorResponse;

@ApiResponses(value = {
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {
                  "status": 400,
                  "error": "Bad Request",
                  "message": "쿠키에 리프레시 토큰이 없습니다.",
                  "path": "/api/auth/reissue"
                }
            """)
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "리프레시 토큰이 만료되었거나 위조되었습니다.",
                  "path": "/api/auth/reissue"
                }
            """)
        )
    ),
    @ApiResponse(
        responseCode = "403",
        description = "권한 없음",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {
                  "status": 403,
                  "error": "Forbidden",
                  "message": "권한이 없습니다.",
                  "path": "/api/auth/reissue"
                }
            """)
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "리소스 없음",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {
                  "status": 404,
                  "error": "Not Found",
                  "message": "리프레시 토큰을 찾을 수 없습니다.",
                  "path": "/api/auth/reissue"
                }
            """)
        )
    ),
    @ApiResponse(
        responseCode = "409",
        description = "재발급 실패",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {
                  "status": 409,
                  "error": "Conflict",
                  "message": "이미 처리된 토큰입니다.",
                  "path": "/api/auth/reissue"
                }
            """)
        )
    ),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {
                  "status": 500,
                  "error": "Internal Server Error",
                  "message": "토큰 재발급 중 서버 오류가 발생했습니다.",
                  "path": "/api/auth/reissue"
                }
            """)
        )
    )
})
@Retention(RetentionPolicy.RUNTIME)
public @interface JwtReissueErrorResponses {
}
