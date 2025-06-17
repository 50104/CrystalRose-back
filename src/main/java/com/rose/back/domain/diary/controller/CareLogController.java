package com.rose.back.domain.diary.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    // 관리 기록 전체 조회 (FullCalendar용)
    @GetMapping("/carelogs/list")
    public ResponseEntity<List<RoseCareLogDto>> getCareLogs() {
        return ResponseEntity.ok(careLogService.getAllLogs());
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

    public record RoseCareLogDto(
        Long id,
        String fertilizer,
        String pesticide,
        String adjuvant,
        String compost,
        String fungicide,
        String note,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate careDate
    ) {}
}