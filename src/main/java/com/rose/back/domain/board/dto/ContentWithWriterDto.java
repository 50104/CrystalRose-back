package com.rose.back.domain.board.dto;

import com.rose.back.domain.board.entity.ContentEntity;

public record ContentWithWriterDto(
    Long boardNo,
    String boardTitle,
    String boardContent,
    WriterDto writer
) {
    public static ContentWithWriterDto from(ContentEntity entity) {
        return new ContentWithWriterDto(
            entity.getBoardNo(),
            entity.getBoardTitle(),
            entity.getBoardContent(),
            WriterDto.from(entity.getWriter())
        );
    }
}