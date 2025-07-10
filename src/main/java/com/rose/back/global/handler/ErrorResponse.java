package com.rose.back.global.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "에러 응답 형식")
public class ErrorResponse {

    @Schema(description = "에러 발생 시간", example = "2025-06-15T23:40:12")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 코드", example = "BAD_REQUEST")
    private String code;

    @Schema(description = "에러 메시지 상세", example = "필수 파라미터 userId가 없습니다.")
    private String message;

    @Schema(description = "요청 경로", example = "/api/user/update")
    private String path;

    public ErrorResponse(int status, String code, String message, String path, LocalDateTime timestamp) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }
}
