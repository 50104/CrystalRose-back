package com.rose.back.domain.diary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.diary.controller.docs.CareLogControllerDocs;
import com.rose.back.domain.diary.dto.CareLogRequest;
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

    // 관리 날짜만 조회 (타임라인 점용)
    @GetMapping("/caredates/list")
    public ResponseEntity<List<String>> getCareDates(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/diaries/caredates/list] - 케어 날짜 조회 요청 (userNo: {})", userDetails.getUserNo());
        return ResponseEntity.ok(careLogService.getCareDates(userDetails.getUserNo()));
    }

    @PutMapping("/carelogs/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long id, @RequestBody CareLogRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[PUT][/api/diaries/carelogs/{}] - 케어로그 수정 요청 (userNo: {})", id, userDetails.getUserNo());
        careLogService.update(id, request, userDetails.getUserNo());
        return ResponseEntity.ok().build();
    }
}