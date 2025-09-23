package com.rose.back.domain.wiki.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import com.rose.back.domain.wiki.entity.WikiModificationRequest;

public record WikiModificationListDto(
    Long id,
    Long originalWikiId,
    String name,
    String description,
    LocalDateTime createdDate,
    WikiModificationRequest.Status status,
    Long remainingDays
) {
    public static WikiModificationListDto from(WikiModificationRequest entity) {
        Long remainingDays = null;
        
        // 삭제 예정 REJECTED
        if (entity.getStatus() == WikiModificationRequest.Status.REJECTED && entity.getModifiedDate() != null) {
            LocalDate rejectedDate = entity.getModifiedDate().toLocalDate();
            LocalDate deletionDate = rejectedDate.plusDays(4);
            LocalDateTime deletionScheduledDate = deletionDate.atTime(LocalTime.MIDNIGHT);
            
            long hoursBetween = ChronoUnit.HOURS.between(LocalDateTime.now(), deletionScheduledDate);
            remainingDays = Math.max(0, hoursBetween / 24);
        }
        
        return new WikiModificationListDto(
            entity.getId(),
            entity.getOriginalWiki().getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedDate(),
            entity.getStatus(),
            remainingDays
        );
    }
}
