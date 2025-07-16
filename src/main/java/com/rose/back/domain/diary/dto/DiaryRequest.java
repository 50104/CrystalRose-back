package com.rose.back.domain.diary.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public record DiaryRequest(
    Long roseId,
    String note,
    String imageUrl,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate recordedAt
) {}
