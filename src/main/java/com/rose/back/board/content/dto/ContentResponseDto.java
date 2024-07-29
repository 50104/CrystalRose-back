package com.rose.back.board.content.dto;

import java.time.LocalDateTime;

public record ContentResponseDto(
    Long boardNo,
    String boardTitle,
    String boardContent,
    String userId,
    int hitCount,
    int commentCount,
    int likeCount,
    String imgUrl,
    LocalDateTime createdDate)

{
    public static ContentResponseDto of(Long boardNo, String boardTitle, String boardContent, String userId, int hitCount, int commentCount, int likeCount, String imgUrl, LocalDateTime createdDate) {
        return new ContentResponseDto(boardNo, boardTitle, boardContent, userId,hitCount, commentCount, likeCount, imgUrl, createdDate);
    }
}
