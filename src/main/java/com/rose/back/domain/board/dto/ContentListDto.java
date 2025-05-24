package com.rose.back.domain.board.dto;

import com.rose.back.domain.board.entity.ContentEntity;

public record ContentListDto(
    Long boardNo,
    String boardTitle,
    String writerNick
) {
    public static ContentListDto from(ContentEntity entity) {
        return new ContentListDto(
            entity.getBoardNo(),
            entity.getBoardTitle(),
            entity.getWriter() != null ? entity.getWriter().getUserNick() : "탈퇴한 사용자"
        );
    }
}
