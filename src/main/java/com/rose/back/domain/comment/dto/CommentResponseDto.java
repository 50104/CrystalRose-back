package com.rose.back.domain.comment.dto;

import com.rose.back.domain.comment.entity.CommentEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponseDto {

    private Long id;
    private String content;
    private String userId;
    private String createdDate;
    private Long parentId;
    private String parentNickname;
    private boolean deleted;

    public static CommentResponseDto fromEntity(CommentEntity entity) {
        return CommentResponseDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .userId(entity.getWriter().getUserId())
                .createdDate(entity.getCreatedDate() != null
                    ? entity.getCreatedDate().toString()
                    : "") // null 방지
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentNickname(entity.getParent() != null ? entity.getParent().getWriter().getUserId() : null)
                .deleted(entity.isDeleted())
                .build();
    }
}
