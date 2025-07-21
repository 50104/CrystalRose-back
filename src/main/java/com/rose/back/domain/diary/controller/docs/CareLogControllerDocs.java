package com.rose.back.domain.diary.controller.docs;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.diary.dto.CareLogRequest;
import com.rose.back.domain.diary.dto.CareLogResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "CareLog", description = "장미 관리 기록 관련 API")
public interface CareLogControllerDocs {

    @Operation(summary = "관리 기록 등록", description = "장미 관리 기록(비료, 농약, 물주기 등)을 등록합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "관리 기록 등록 성공"),
        @ApiResponse(responseCode = "409", description = "관리 기록 등록 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "InternalError", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "관리 기록 등록 실패",
                      "path": "/api/diaries/carelogs/register"
                    }
                """)))
    })
    ResponseEntity<Void> create(@RequestBody CareLogRequest request, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "관리 기록 목록 조회", description = "사용자의 모든 관리 기록 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "관리 기록 조회 성공"),
        @ApiResponse(responseCode = "409", description = "관리 기록 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "InternalError", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "관리 기록 조회 실패",
                      "path": "/api/diaries/carelogs/list"
                    }
                """)))
    })
    ResponseEntity<List<CareLogResponse>> getCareLogs(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "관리 기록 수정", description = "기존 관리 기록을 수정합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "관리 기록 수정 성공"),
        @ApiResponse(responseCode = "404", description = "관리 기록을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "NotFound", value = """
                    {
                      "status": 404,
                      "error": "NOT_FOUND",
                      "message": "관리 기록을 찾을 수 없습니다",
                      "path": "/api/diaries/carelogs/{id}"
                    }
                """))),
        @ApiResponse(responseCode = "409", description = "관리 기록 수정 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "InternalError", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "관리 기록 수정 실패",
                      "path": "/api/diaries/carelogs/{id}"
                    }
                """)))
    })
    ResponseEntity<Void> update(@PathVariable("id") Long id, @RequestBody CareLogRequest request, @AuthenticationPrincipal CustomUserDetails userDetails);
}
