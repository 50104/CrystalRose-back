package com.rose.back.domain.diary.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public record DiaryRequest(
    Long roseId,
    String note,
    String imageUrl,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime recordedAt
) {}
