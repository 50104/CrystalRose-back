package com.rose.back.domain.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminResponse {

    private Long id;
    private String name;
    private String category;
    private String status;
    private LocalDateTime createdDate;
    private String rejectionReason;
}