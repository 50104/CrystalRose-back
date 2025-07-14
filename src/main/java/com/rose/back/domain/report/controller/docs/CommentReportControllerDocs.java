package com.rose.back.domain.report.controller.docs;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.report.dto.CommentReportRequestDto;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.handler.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "CommentReport", description = "댓글 신고 관련 API")
public interface CommentReportControllerDocs {

    @Operation(summary = "댓글 신고", description = "특정 댓글을 신고합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "댓글 신고 성공"),
        @ApiResponse(responseCode = "409", description = "댓글 신고 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "댓글 신고에 실패했습니다.",
                      "path": "/api/comment-reports"
                    }
                """)))
    })
    ResponseEntity<MessageResponse> reportComment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody CommentReportRequestDto dto
    );

    @Operation(summary = "댓글 신고 여부 확인", description = "특정 댓글의 신고 여부를 확인합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 신고 여부 확인 성공"),
        @ApiResponse(responseCode = "409", description = "댓글 신고 여부 확인 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "댓글 신고 여부 확인에 실패했습니다.",
                      "path": "/api/comment-reports/check"
                    }
                """)))
    })
    ResponseEntity<Map<String, Boolean>> checkReported(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("commentId") Long commentId
    );
}
