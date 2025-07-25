package com.rose.back.domain.diary.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.diary.controller.docs.CareLogControllerDocs;
import com.rose.back.domain.diary.dto.CareLogRequest;
import com.rose.back.domain.diary.dto.CareLogResponse;
import com.rose.back.domain.diary.service.CareLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class CareLogController implements CareLogControllerDocs {

    private final CareLogService careLogService;

    @PostMapping("/carelogs/register")
    public ResponseEntity<Void> create(@RequestBody CareLogRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[POST][/api/diaries/carelogs/register] - 케어로그 등록 요청 (userNo: {})", userDetails.getUserNo());
        careLogService.save(request, userDetails.getUserNo());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/carelogs/list")
    public ResponseEntity<List<CareLogResponse>> getCareLogs(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/diaries/carelogs/list] - 케어로그 전체 조회 요청 (userNo: {})", userDetails.getUserNo());
        return ResponseEntity.ok(careLogService.getAllByUser(userDetails.getUserNo()));
    }

    @PutMapping("/carelogs/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long id, @RequestBody CareLogRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[PUT][/api/diaries/carelogs/{}] - 케어로그 수정 요청 (userNo: {})", id, userDetails.getUserNo());
        careLogService.update(id, request, userDetails.getUserNo());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/carelogs/{roseId}")
    public ResponseEntity<CareLogResponse> getCareLogByDate(
            @PathVariable("roseId") Long roseId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/diaries/carelogs/{}] - 케어로그 단건 조회 요청 (userNo: {}, date: {})", roseId, userDetails.getUserNo(), date);
        return ResponseEntity.ok(careLogService.getByDate(roseId, date, userDetails.getUserNo()));
    }
}