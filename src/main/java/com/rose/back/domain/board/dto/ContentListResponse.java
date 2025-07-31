package com.rose.back.domain.board.dto;

import java.util.List;

public record ContentListResponse(
    List<ContentListDto> fixedList,
    List<ContentListDto> content,
    int totalPage
) {}
