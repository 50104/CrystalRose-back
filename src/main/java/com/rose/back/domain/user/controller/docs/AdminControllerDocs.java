package com.rose.back.domain.user.controller.docs;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.user.dto.AdminResponse;
import com.rose.back.domain.wiki.dto.WikiDetailResponse;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.handler.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@Tag(name = "Admin", description = "Admin 관련 API입니다.")
public interface AdminControllerDocs {

    @Operation(summary = "승인 대기 도감 목록 조회", description = "승인 대기 중인 도감 목록을 조회합니다. ADMIN 권한이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "승인 대기 도감 목록 조회 성공",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = AdminResponse.class)))
        ),
        @ApiResponse(responseCode = "409", description = "승인 대기 도감 목록 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "승인 대기 도감 목록 조회에 실패했습니다.",
                      "path": "/api/v1/admin/wiki/pending"
                    }
                    """)))
    })
    ResponseEntity<?> getPendingWikiList();

    @Operation(summary = "도감 승인", description = "특정 ID의 도감을 승인합니다. ADMIN 권한이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "도감 승인 성공"),
        @ApiResponse(responseCode = "409", description = "도감 승인 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "도감 승인에 실패했습니다.",
                      "path": "/api/v1/admin/wiki/{id}/approve"
                    }
                    """))),
    })
    ResponseEntity<?> approveWiki(@Parameter(description = "승인할 도감의 ID", required = true) Long id);

    @Operation(summary = "도감 거절", description = "특정 ID의 도감을 거절합니다. ADMIN 권한이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "도감 거절 성공"),
        @ApiResponse(responseCode = "409", description = "도감 거절 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "도감 거절에 실패했습니다.",
                      "path": "/api/v1/admin/wiki/{id}/reject"
                    }
                    """))),
    })
    ResponseEntity<?> rejectWiki(@Parameter(description = "거절할 도감의 ID", required = true) Long id);

    @Operation(summary = "신고 내역 조회", description = "관리자의 신고 내역을 조회합니다. ADMIN 권한이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "신고 내역 조회 성공",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = AdminResponse.class)))
        ),
        @ApiResponse(responseCode = "409", description = "신고 내역 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "신고 내역 조회에 실패했습니다.",
                      "path": "/api/v1/admin/reports"
                    }
                    """)))
    })
    ResponseEntity<?> getReports();
    
    @Operation(summary = "도감 삭제", description = "특정 ID의 도감을 삭제합니다. ADMIN 권한이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "도감 삭제 성공"),
        @ApiResponse(responseCode = "409", description = "도감 삭제 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "도감 삭제에 실패했습니다.",
                      "path": "/api/v1/admin/wiki/{id}/delete"
                    }
                    """))),
    })
    ResponseEntity<MessageResponse> deleteWikiByAdmin(@PathVariable("id") Long id);

    // public ResponseEntity<WikiDetailResponse> getWikiDetailByAdmin(@PathVariable("id") Long id);
    @Operation(summary = "도감 상세 조회", description = "특정 ID의 도감을 상세 조회합니다. ADMIN 권한이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "도감 상세 조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = WikiDetailResponse.class))
        ),
        @ApiResponse(responseCode = "409", description = "도감 상세 조회 실패 - 도감이 존재하지 않음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "도감 상세 조회에 실패했습니다. 도감이 존재하지 않습니다.",
                      "path": "/api/v1/admin/wiki/detail/{id}"
                    }
                    """)))
    })
    ResponseEntity<WikiDetailResponse> getWikiDetailByAdmin(@PathVariable("id") Long id);

    @Operation(summary = "게시글 고정/해제", description = "특정 게시글을 고정하거나 해제합니다. ADMIN 권한이 필요합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "게시글 고정/해제 성공"),
        @ApiResponse(responseCode = "409", description = "게시글 고정/해제 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Conflict", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글 고정/해제에 실패했습니다.",
                      "path": "/api/v1/admin/board/{id}/fix"
                    }
                    """)))
    })
    ResponseEntity<?> toggleFixed(@PathVariable Long id);
}