package com.rose.back.domain.comment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.comment.dto.CommentRequestDto;
import com.rose.back.domain.comment.dto.CommentResponseDto;
import com.rose.back.domain.comment.entity.CommentEntity;
import com.rose.back.domain.comment.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ContentRepository contentRepository;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long boardNo) {
        List<CommentEntity> comments = commentRepository.findByContentEntityBoardNo(boardNo);
        return comments.stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addComment(Long boardNo, CommentRequestDto dto) {
        ContentEntity content = contentRepository.findById(boardNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다"));
        log.info("댓글 저장 시도: boardNo={}, userId={}, content={}", boardNo, dto.getUserId(), dto.getContent());

        CommentEntity comment = CommentEntity.builder()
                .content(dto.getContent())
                .userId(dto.getUserId())
                .contentEntity(content)
                .build();

        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다"));
        commentRepository.delete(comment);
    }
}
