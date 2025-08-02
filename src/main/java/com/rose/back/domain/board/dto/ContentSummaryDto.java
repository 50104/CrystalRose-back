package com.rose.back.domain.board.dto;

import java.time.LocalDateTime;

import com.rose.back.domain.board.entity.ContentEntity;

public record ContentSummaryDto(
    Long boardNo,
    String boardTitle,
    String boardTag,
    LocalDateTime createdDate
) {
    public static ContentSummaryDto from(ContentEntity entity) {
        return new ContentSummaryDto(
            entity.getBoardNo(),
            entity.getBoardTitle(),
            entity.getBoardTag(),
            entity.getCreatedDate()
        );
    }
}
