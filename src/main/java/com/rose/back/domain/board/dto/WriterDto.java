package com.rose.back.domain.board.dto;

import com.rose.back.domain.user.entity.UserEntity;

public record WriterDto(
    Long userNo,
    String userId,
    String userNick
) {
    public static WriterDto from(UserEntity user) {
        return new WriterDto(
            user.getUserNo(),
            user.getUserId(),
            user.getUserNick()
        );
    }
}