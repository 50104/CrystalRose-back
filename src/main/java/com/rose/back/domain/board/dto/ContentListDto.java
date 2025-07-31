package com.rose.back.domain.board.dto;

import java.time.LocalDateTime;
import java.util.Optional;

import com.rose.back.domain.board.entity.ContentEntity;

public record ContentListDto(
    Long boardNo,
    String boardTitle,
    String boardTag,
    String writerNick,
    String writerStatus,
    LocalDateTime createdDate,
    Long viewCount,
    Long commentCount,
    Integer recommendCount
) {
    public static ContentListDto from(ContentEntity entity, Long commentCount) {
        String writerNick = Optional.ofNullable(entity.getWriter())
                .map(writer -> writer.getUserNick())
                .orElse("탈퇴한 사용자");

        String writerStatus = Optional.ofNullable(entity.getWriter())
                .map(writer -> writer.getUserStatus())
                .map(status -> status.name())
                .orElse("DELETED");

        return new ContentListDto(
            entity.getBoardNo(),
            entity.getBoardTitle(),
            entity.getBoardTag(),
            writerNick,
            writerStatus,
            entity.getCreatedDate(),
            entity.getViewCount(),
            commentCount,
            entity.getRecommendCount()
        );
    }
}
