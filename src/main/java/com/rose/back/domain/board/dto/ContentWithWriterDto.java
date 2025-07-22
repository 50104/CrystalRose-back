package com.rose.back.domain.board.dto;

import java.time.LocalDateTime;

import com.rose.back.domain.board.entity.ContentEntity;

public record ContentWithWriterDto(
    Long boardNo,
    String boardTitle,
    String boardContent,
    String boardTag,
    WriterDto writer,
    long commentCount,
    LocalDateTime createdDate
) {
    public static ContentWithWriterDto from(ContentEntity entity, long commentCount) {
        return new ContentWithWriterDto(
            entity.getBoardNo(),
            entity.getBoardTitle(),
            entity.getBoardContent(),
            entity.getBoardTag(),
            WriterDto.from(entity.getWriter()),
            commentCount,
            entity.getCreatedDate()
        );
    }
}