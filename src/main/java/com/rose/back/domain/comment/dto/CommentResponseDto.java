package com.rose.back.domain.comment.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String userNick;
    private String userStatus; 
    private String createdDate;
    private Long parentId;
    private String parentNickname;
    private boolean deleted;
    private String parentStatus;
    @JsonProperty("isBlocked")
    private boolean isBlocked;

    public static CommentResponseDto fromEntity(CommentEntity entity, Set<Long> blockedUserNos) {
        UserEntity writer = entity.getWriter();
        boolean isBlocked = writer != null && blockedUserNos.contains(writer.getUserNo());

        return CommentResponseDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .userId(writer.getUserId())
                .userNick(writer.getUserNick())
                .userStatus(writer.getUserStatus().name())
                .createdDate(entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : "")
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentNickname(
                    entity.getParent() != null && entity.getParent().getWriter() != null
                        ? entity.getParent().getWriter().getUserNick()
                        : null
                )
                .deleted(entity.isDeleted())
                .parentStatus(
                    entity.getParent() != null && entity.getParent().getWriter() != null
                        ? entity.getParent().getWriter().getUserStatus().name()
                        : null
                )
                .isBlocked(isBlocked)
                .build();
    }
}
