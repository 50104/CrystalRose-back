package com.rose.back.domain.comment.dto;

import com.rose.back.domain.comment.entity.CommentEntity;
import com.rose.back.domain.user.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponseDto {

    private Long id;
    private String content;
    private String userId;
    private String userStatus; 
    private String createdDate;
    private Long parentId;
    private String parentNickname;
    private boolean deleted;

    public static CommentResponseDto fromEntity(CommentEntity entity) {
        UserEntity writer = entity.getWriter();

        return CommentResponseDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .userId(writer.getUserId())
                .userStatus(writer.getUserStatus().name())
                .createdDate(entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : "")
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentNickname(entity.getParent() != null ? entity.getParent().getWriter().getUserId() : null)
                .deleted(entity.isDeleted())
                .build();
    }
}
