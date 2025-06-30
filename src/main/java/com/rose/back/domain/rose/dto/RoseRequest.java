package com.rose.back.domain.rose.dto;

import java.time.LocalDate;

public record RoseRequest(
    Long userId,
    Long wikiId,
    String nickname,
    LocalDate acquiredDate,
    String locationNote,
    String imageUrl
) {}
