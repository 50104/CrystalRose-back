package com.rose.back.domain.wiki.dto;

import java.time.LocalDateTime;

import com.rose.back.domain.wiki.entity.WikiModificationRequest;

public record WikiModificationListDto(
    Long id,
    Long originalWikiId,
    String name,
    String description,
    LocalDateTime createdDate,
    WikiModificationRequest.Status status
) {
    public static WikiModificationListDto from(WikiModificationRequest entity) {
        return new WikiModificationListDto(
            entity.getId(),
            entity.getOriginalWiki().getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedDate(),
            entity.getStatus()
        );
    }
}
