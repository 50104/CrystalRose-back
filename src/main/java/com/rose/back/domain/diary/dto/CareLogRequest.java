package com.rose.back.domain.diary.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public record CareLogRequest(
    Long id,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate careDate,
    String fertilizer,
    String pesticide,
    String adjuvant,
    String compost,
    String fungicide,
    String watering,
    String note
) {}
