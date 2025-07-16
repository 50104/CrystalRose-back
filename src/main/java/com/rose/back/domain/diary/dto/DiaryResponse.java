package com.rose.back.domain.diary.dto;

import java.time.LocalDate;

public record DiaryResponse(
    Long id,
    String note,
    String imageUrl,
    LocalDate recordedAt
) {}
