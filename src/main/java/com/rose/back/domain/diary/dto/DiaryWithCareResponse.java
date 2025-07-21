package com.rose.back.domain.diary.dto;

import java.time.LocalDate;
import java.util.List;

public record DiaryWithCareResponse(
    Long id,
    String note,
    String imageUrl,
    LocalDate recordedAt,
    List<String> careTypes
) {}
