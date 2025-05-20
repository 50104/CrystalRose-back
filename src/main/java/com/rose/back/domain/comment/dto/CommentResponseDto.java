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

    public static CommentResponseDto fromEntity(CommentEntity entity) {
        return CommentResponseDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .userId(entity.getUserId())
                .createdDate(entity.getCreatedDate() != null
                    ? entity.getCreatedDate().toString()
                    : "") // null 방지
                .build();
    }
}
