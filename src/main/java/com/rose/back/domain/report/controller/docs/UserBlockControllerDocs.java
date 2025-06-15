package com.rose.back.domain.report.controller.docs;

import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.handler.ErrorResponse;
import com.rose.back.domain.report.dto.UserSummaryDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Tag(name = "Block", description = "사용자 차단 관련 API입니다.")
public interface UserBlockControllerDocs {

    @Schema(name = "BlockRequestDto", description = "차단 요청 DTO")
    record BlockRequestDtoSchema(
        @Schema(description = "차단할 사용자 ID", example = "42") Long blockedUserId
    ) {}

    @Operation(summary = "사용자 차단", description = "특정 사용자를 차단합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "차단 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "이미 차단한 사용자입니다.",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미 차단한 사용자입니다.",
                      "path": "/api/v1/blocks"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Void> block(
        @Parameter(hidden = true) @AuthenticationPrincipal Object userDetails,
        @RequestBody(
            description = "차단 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = BlockRequestDtoSchema.class))
        ) BlockRequestDtoSchema dto
    );

    @Operation(summary = "차단 목록 조회", description = "내가 차단한 사용자 목록을 반환합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "차단 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserSummaryDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "사용자 정보가 유효하지 않음",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "사용자 정보를 확인할 수 없습니다.",
                      "path": "/api/v1/blocks/me"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<List<UserSummaryDto>> getBlockedUsers(
        @Parameter(hidden = true) @AuthenticationPrincipal Object userDetails
    );

    @Operation(summary = "사용자 차단 해제", description = "차단한 사용자를 해제합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "차단 해제 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "해당 사용자를 차단하지 않았거나 존재하지 않는 사용자입니다.",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "차단한 사용자가 아닙니다.",
                      "path": "/api/v1/blocks/{blockedUserId}"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Void> unblock(
        @Parameter(
            name = "blockedUserId",
            description = "차단 해제할 사용자 ID",
            required = true,
            in = ParameterIn.PATH,
            example = "42"
        ) Long blockedUserId,
        @Parameter(hidden = true) @AuthenticationPrincipal Object userDetails
    );
}
