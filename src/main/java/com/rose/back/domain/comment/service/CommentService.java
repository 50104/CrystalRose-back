package com.rose.back.domain.comment.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.board.entity.ContentEntity;
import com.rose.back.domain.board.repository.ContentRepository;
import com.rose.back.domain.comment.dto.CommentRequestDto;
import com.rose.back.domain.comment.dto.CommentResponseDto;
import com.rose.back.domain.comment.entity.CommentEntity;
import com.rose.back.domain.comment.repository.CommentRepository;
import com.rose.back.domain.report.repository.UserBlockRepository;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long boardNo, String currentUserId) {
        Set<Long> blockedUserNos = (currentUserId != null)
            ? userBlockRepository.findAllBlockedByUserId(currentUserId).stream()
                .map(UserEntity::getUserNo)
                .collect(Collectors.toSet())
            : Set.of();
            
        return commentRepository.findByContentEntityBoardNo(boardNo).stream()
            .map(comment -> CommentResponseDto.fromEntity(comment, blockedUserNos))
            .toList();
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
