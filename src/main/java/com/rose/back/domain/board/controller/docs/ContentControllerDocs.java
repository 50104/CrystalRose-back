package com.rose.back.domain.board.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.global.exception.CommonErrorResponses;
import com.rose.back.global.exception.ImageUploadErrorResponses;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.util.List;
import java.util.Map;

@Tag(name = "Content", description = "게시글 및 게시판 관련 API입니다.")
public interface ContentControllerDocs {

    @Operation(summary = "게시글 작성 페이지", description = "게시글 작성 페이지를 반환합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 작성 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "게시글 작성 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글 작성에 실패했습니다.",
                      "path": "/api/v1/content/editor",
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<String> editorPage();

    @Operation(summary = "게시글 수정 페이지", description = "특정 게시글의 수정 페이지 데이터를 반환합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 수정 페이지 불러오기 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "게시글 수정 페이지 불러오기 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글 수정 페이지를 불러올 수 없습니다.",
                      "path": "/api/v1/content/editor/{boardNo}",
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, Object>> updatePage(@PathVariable("boardNo") Long boardNo);

    @Operation(summary = "게시글 저장", description = "게시글을 저장합니다.")
    @CommonErrorResponses
    @ImageUploadErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 저장 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "게시글 저장 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글 저장에 실패했습니다.",
                      "path": "/api/v1/content/save",
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, Object>> saveContent(
        @ModelAttribute @Validated ContentRequestDto req,
        @RequestParam(value = "files", required = false) List<MultipartFile> files
    );

    @Operation(summary = "게시글 수정", description = "특정 게시글을 수정합니다.")
    @CommonErrorResponses
    @ImageUploadErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "게시글 수정 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글 수정에 실패했습니다.",
                      "path": "/api/v1/content/update/{boardNo}",
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, Object>> updateLogic(@ModelAttribute ContentRequestDto req, @PathVariable("boardNo") Long boardNo);

    @Operation(summary = "게시글 리스트", description = "게시글 리스트를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 리스트 조회 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "게시글 리스트 조회 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글 리스트를 조회할 수 없습니다.",
                      "path": "/api/v1/content/list",
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, Object>> listPage();

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "게시글 조회 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글을 조회할 수 없습니다.",
                      "path": "/api/v1/content/{boardNo}",
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, Object>> contentPage(@PathVariable("boardNo") Long boardNo);

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "게시글 삭제 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "게시글 삭제에 실패했습니다.",
                      "path": "/api/v1/content/delete/{boardNo}",
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Map<String, String>> deleteC(@PathVariable("boardNo") Long boardNo);
}