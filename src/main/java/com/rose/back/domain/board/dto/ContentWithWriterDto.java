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
    long likeCount,
    boolean recommended,
    LocalDateTime createdDate
) {
    public static ContentWithWriterDto from(ContentEntity entity, long commentCount, long likeCount, boolean recommended) {
        return new ContentWithWriterDto(
            entity.getBoardNo(),
            entity.getBoardTitle(),
            entity.getBoardContent(),
            entity.getBoardTag(),
            WriterDto.from(entity.getWriter()),
            commentCount,
            likeCount,
            recommended,
            entity.getCreatedDate()
        );
    }
}