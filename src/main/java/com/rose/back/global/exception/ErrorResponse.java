package com.rose.back.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "에러 응답 형식")
public class ErrorResponse {

    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 요약", example = "Bad Request")
    private String error;

    @Schema(description = "에러 메시지 상세", example = "필수 파라미터 userId가 없습니다.")
    private String message;

    @Schema(description = "요청 경로", example = "/api/user/update")
    private String path;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
