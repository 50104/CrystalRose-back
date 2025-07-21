package com.rose.back.domain.diary.dto;

import java.time.LocalDate;

public record CareLogResponse(
    LocalDate careDate,
    String watering,
    String fertilizer,
    String pesticide,
    String adjuvant,
    String fungicide,
    String compost,
    String note
) {}