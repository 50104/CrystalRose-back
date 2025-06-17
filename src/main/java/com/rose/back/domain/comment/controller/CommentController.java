package com.rose.back.domain.comment.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.comment.controller.docs.CommentControllerDocs;
import com.rose.back.domain.comment.dto.CommentRequestDto;
import com.rose.back.domain.comment.dto.CommentResponseDto;
import com.rose.back.domain.comment.service.CommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class CommentController implements CommentControllerDocs {

    private final CommentService commentService;

    @GetMapping("/{boardNo}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/board/{}/comments] - 댓글 목록 조회 요청", boardNo);
        List<CommentResponseDto> comments = commentService.getComments(boardNo);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{boardNo}/comments")
    public ResponseEntity<MessageResponse> addComment(@PathVariable("boardNo") Long boardNo, @RequestBody CommentRequestDto dto) {
        log.info("[POST][/board/{}/comments] - 댓글 등록 요청, payload: {}", boardNo, dto);
        commentService.addComment(boardNo, dto);
        return ResponseEntity.status(201).body(new MessageResponse("댓글 등록 완료"));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable("commentId") Long commentId) {
        log.info("[DELETE][/board/comments/{}] - 댓글 삭제 요청", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new MessageResponse("댓글 삭제 완료"));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<MessageResponse> updateComment(@PathVariable("commentId") Long commentId, @RequestBody CommentRequestDto dto) {
        log.info("[PATCH][/board/comments/{}] - 댓글 수정 요청, payload: {}", commentId, dto);
        commentService.updateComment(commentId, dto);
        return ResponseEntity.ok(new MessageResponse("댓글 수정 완료"));
    }
}
