package com.rose.back.domain.diary.dto;

import java.time.LocalDate;

import com.rose.back.domain.diary.entity.CareLogEntity;

public record CareLogResponse(
    LocalDate careDate,
    String watering,
    String fertilizer,
    String pesticide,
    String adjuvant,
    String fungicide,
    String compost,
    String note
) {
    public static CareLogResponse fromEntity(CareLogEntity entity) {
        return new CareLogResponse(
            entity.getCareDate(),
            entity.getWatering(),
            entity.getFertilizer(),
            entity.getPesticide(),
            entity.getAdjuvant(),
            entity.getFungicide(),
            entity.getCompost(),
            entity.getNote()
        );
    }
}