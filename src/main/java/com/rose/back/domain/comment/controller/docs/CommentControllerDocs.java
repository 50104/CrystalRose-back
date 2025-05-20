package com.rose.back.domain.comment.controller.docs;

import com.rose.back.domain.comment.dto.CommentRequestDto;
import com.rose.back.domain.comment.dto.CommentResponseDto;
import com.rose.back.global.exception.CommonErrorResponses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "댓글 관련 API입니다.")
public interface CommentControllerDocs {

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "댓글 목록 조회 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "댓글 목록을 찾을 수 없습니다.",
                      "path": "/board/{boardNo}/comments"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable("boardNo") Long boardNo);

    @Operation(summary = "댓글 등록", description = "특정 게시글에 댓글을 등록합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "댓글 등록 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "댓글 등록 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "댓글 등록에 실패했습니다.",
                      "path": "/board/{boardNo}/comments"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Void> addComment(@PathVariable("boardNo") Long boardNo, @RequestBody CommentRequestDto dto);

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
    @CommonErrorResponses
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
        @ApiResponse(
            responseCode = "409",
            description = "댓글 삭제 실패",
            content = @Content(
                schema = @Schema(implementation = com.rose.back.global.exception.ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "status": 409,
                      "error": "CONFLICT",
                      "message": "댓글 삭제에 실패했습니다.",
                      "path": "/board/comments/{commentId}"
                    }
                    """
                )
            )
        )
    })
    ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId);
}
