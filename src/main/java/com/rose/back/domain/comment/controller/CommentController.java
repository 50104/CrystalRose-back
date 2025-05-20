package com.rose.back.domain.comment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.comment.controller.docs.CommentControllerDocs;
import com.rose.back.domain.comment.dto.CommentRequestDto;
import com.rose.back.domain.comment.dto.CommentResponseDto;
import com.rose.back.domain.comment.service.CommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class CommentController implements CommentControllerDocs {

    private final CommentService commentService;

    @GetMapping("/{boardNo}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/board/{}/comments] - 댓글 목록 조회 컨트롤러", boardNo);
        try {
            List<CommentResponseDto> comments = commentService.getComments(boardNo);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("댓글 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{boardNo}/comments")
    public ResponseEntity<Void> addComment(@PathVariable("boardNo") Long boardNo, @RequestBody CommentRequestDto dto) {
        log.info("[POST][/board/{}/comments] - 댓글 등록 요청, payload: {}", boardNo, dto);
        try {
            commentService.addComment(boardNo, dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("댓글 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        log.info("[DELETE][/board/comments/{}] - 댓글 삭제 요청", commentId);
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("댓글 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable("commentId") Long commentId, @RequestBody CommentRequestDto dto) {
        log.info("[PATCH][/board/comments/{}] - 댓글 수정 요청, payload: {}", commentId, dto);
        try {
            commentService.updateComment(commentId, dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("댓글 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}