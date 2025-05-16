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
        responseCode = "413",
        description = "파일 용량 초과",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {
                  "status": 413,
                  "error": "Payload Too Large",
                  "message": "업로드 가능한 최대 파일 용량을 초과했습니다.",
                  "path": "/api/image/upload"
                }
                """)
        )
    ),
    @ApiResponse(
        responseCode = "415",
        description = "지원하지 않는 파일 형식",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = """
                {
                  "status": 415,
                  "error": "Unsupported Media Type",
                  "message": "지원하지 않는 파일 확장자입니다. (jpg, png만 가능)",
                  "path": "/api/image/upload"
                }
                """)
        )
    )
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageUploadErrorResponses {
}
