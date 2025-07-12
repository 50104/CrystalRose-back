package com.rose.back.domain.rose.controller.docs;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.rose.dto.ImageUploadResponse;
import com.rose.back.domain.rose.dto.RoseRequest;
import com.rose.back.domain.rose.dto.RoseResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Rose", description = "장미 등록 및 이미지 업로드 API")
public interface RoseControllerDocs {

    @Operation(summary = "내 장미 등록", description = "도감에서 선택한 품종으로 내 장미를 등록합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장미 등록 성공"),
        @ApiResponse(responseCode = "409", description = "장미 등록 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "InternalError", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "내 장미 등록 실패",
                      "path": "/api/roses/mine"
                    }
                """)))
    })
    ResponseEntity<MessageResponse> registerRose(
        @RequestBody RoseRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "내 장미 등록 중복 확인", description = "사용자가 이미 등록한 도감(Wiki ID) 장미인지 확인합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "중복 여부 반환 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "ExistsTrue", value = """
                    {
                      "exists": true
                    }
                """)))
    })
    ResponseEntity<?> checkDuplicateRose(
        @RequestParam("wikiId") Long wikiId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "등록된 장미의 Wiki ID 목록 조회", description = "사용자가 등록한 장미 도감(Wiki ID) 리스트를 반환합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Wiki ID 목록 반환 성공",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(name = "WikiIdList", value = """
                    [1, 3, 5]
                """)))
    })
    ResponseEntity<List<Long>> getMyRoseWikiIds(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "장미 이미지 업로드", description = "S3에 장미 이미지를 업로드하고 URL을 반환합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
        @ApiResponse(responseCode = "409", description = "이미지 업로드 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "UploadFail", value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "이미지 업로드 실패",
                      "path": "/api/roses/image/upload"
                    }
                """)))
    })
    ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file);

    @Operation(summary = "내 장미 목록 조회", description = "사용자가 등록한 장미 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장미 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Unauthorized", value = """
                    {
                      "status": 401,
                      "error": "UNAUTHORIZED",
                      "message": "인증된 사용자 정보가 없습니다.",
                      "path": "/api/roses/list"
                    }
                """)))
    })
    ResponseEntity<List<RoseResponse>> getMyRoses(@AuthenticationPrincipal CustomUserDetails userDetails);
}
