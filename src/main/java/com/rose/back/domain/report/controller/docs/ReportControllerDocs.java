package com.rose.back.domain.report.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.dto.ReportRequestDto;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.handler.ErrorResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.util.Map;

@Tag(name = "Report", description = "신고 관련 API입니다.")
public interface ReportControllerDocs {

    @Schema(name = "ReportRequestDto", description = "신고 요청 DTO")
    record ReportRequestDtoSchema(
            @Schema(description = "신고할 게시글 ID", example = "1") Long postId,
            @Schema(description = "신고 사유", example = "부적절한 내용") String reason
    ) {}

    @Operation(summary = "신고 요청", description = "게시글을 신고합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "신고 요청 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "신고 요청 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미 신고한 게시글입니다.",
                      "path": "/api/reports"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, String>> report(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReportRequestDto dto
    );

    @Operation(summary = "신고 여부 확인", description = "사용자가 게시글을 이미 신고했는지 확인합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "신고 여부 확인 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportRequestDtoSchema.class))),
        @ApiResponse(
            responseCode = "409",
            description = "신고 여부 확인 실패",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "CONFLICT",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글 ID가 유효하지 않거나, 해당 게시글을 찾을 수 없습니다.",
                      "path": "/api/reports/check"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, Boolean>> checkReport(
            @RequestParam("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}