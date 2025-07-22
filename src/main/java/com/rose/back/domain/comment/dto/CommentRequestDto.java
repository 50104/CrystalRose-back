package com.rose.back.domain.comment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {

    private String content;
    private String userId;
    private Long parentId;
    private String userNick;
}
