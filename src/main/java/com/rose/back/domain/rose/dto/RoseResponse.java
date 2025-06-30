package com.rose.back.domain.rose.dto;

import java.time.LocalDate;

public record RoseResponse(
    Long id,
    String nickname,
    String varietyName,
    LocalDate acquiredDate,
    String locationNote,
    String imageUrl
) {}
