package com.rose.back.domain.diary.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public record RoseCareLogDto(
    Long id,
    String fertilizer,
    String pesticide,
    String adjuvant,
    String compost,
    String fungicide,
    String watering,
    String note,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate careDate
) {}
