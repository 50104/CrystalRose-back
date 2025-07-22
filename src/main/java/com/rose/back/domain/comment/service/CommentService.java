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
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long boardNo) {
        List<CommentEntity> comments = commentRepository.findByContentEntityBoardNo(boardNo);
        return comments.stream()
                .map(comment -> {
                    if (comment.isDeleted()) {
                        return CommentResponseDto.builder()
                                .id(comment.getId())
                                .userId(comment.getWriter().getUserId())
                                .userNick(comment.getWriter().getUserNick())
                                .createdDate(comment.getCreatedDate().toString())
                                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                                .parentNickname(comment.getParent() != null ? comment.getParent().getWriter().getUserId() : null)
                                .deleted(true)
                                .content(null)
                                .build();
                    } else {
                        return CommentResponseDto.fromEntity(comment);
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void addComment(Long boardNo, CommentRequestDto dto) {
        ContentEntity content = contentRepository.findById(boardNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다"));
        UserEntity writer = userRepository.findByUserId(dto.getUserId());
        if (writer == null) {
            throw new IllegalArgumentException("작성자 유저가 존재하지 않습니다");
        }
        CommentEntity parent = null;
        if (dto.getParentId() != null) {
            parent = commentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
        }

        log.info("댓글 저장 시도: boardNo={}, userId={}, content={}", boardNo, dto.getUserId(), dto.getContent());

        CommentEntity comment = CommentEntity.builder()
                .content(dto.getContent())
                .writer(writer)
                .contentEntity(content)
                .parent(parent)
                .build();

        commentRepository.save(comment);
    }

    public long countByBoardNo(Long boardNo) {
        return commentRepository.countByContentEntity_BoardNo(boardNo);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다"));
        comment.markAsDeleted();
    }

    @Transactional
    public void updateComment(Long commentId, CommentRequestDto dto) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다"));

        log.info("댓글 수정: id={}, 새로운 내용={}", commentId, dto.getContent());

        comment.updateContent(dto.getContent());
    }
}
