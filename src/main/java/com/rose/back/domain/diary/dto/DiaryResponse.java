package com.rose.back.domain.diary.dto;

import java.time.LocalDate;

import com.rose.back.domain.diary.entity.DiaryEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DiaryResponse {
    private Long id;
    private Long roseId;
    private String note;
    private String imageUrl;
    private LocalDate recordedAt;

    public static DiaryResponse from(DiaryEntity entity) {
        return DiaryResponse.builder()
            .id(entity.getId())
            .roseId(entity.getRoseEntity().getId())
            .note(entity.getNote())
            .imageUrl(entity.getImageUrl())
            .recordedAt(entity.getRecordedAt())
            .build();
    }
}