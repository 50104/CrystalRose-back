package com.rose.back.domain.board.dto;

import java.time.LocalDateTime;

public record ContentResponseDto(
    Long boardNo,
    String boardTitle,
    String boardContent,
    String boardTag,  
    String userId,
    int hitCount,
    int commentCount,
    int likeCount,
    String imgUrl,
    LocalDateTime createdDate)
{
    public static ContentResponseDto of(
        Long boardNo,
        String boardTitle,
        String boardContent,
        String boardTag,
        String userId,
        int hitCount,
        int commentCount,
        int likeCount,
        String imgUrl,
        LocalDateTime createdDate
    ) {
        return new ContentResponseDto(
            boardNo,
            boardTitle,
            boardContent,
            boardTag,
            userId,
            hitCount,
            commentCount,
            likeCount,
            imgUrl,
            createdDate
        );
    }
}