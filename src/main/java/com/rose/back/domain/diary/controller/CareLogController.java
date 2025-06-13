package com.rose.back.domain.diary.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rose.back.domain.diary.service.CareLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class CareLogController {

    private final CareLogService careLogService;

    @PostMapping("/carelogs/register")
    public ResponseEntity<Void> create(@RequestBody CareLogRequest request) {
        careLogService.save(request);
        return ResponseEntity.ok().build();
    }

    // 관리 날짜만 조회 (타임라인 점용)
    @GetMapping("/caredates/list")
    public ResponseEntity<List<String>> getCareDates() {
        return ResponseEntity.ok(careLogService.getAllCareDates());
    }

    public record CareLogRequest(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate careDate,
        String fertilizer,
        String pesticide,
        String adjuvant,
        String compost,
        String fungicide,
        String note
    ) {}
}