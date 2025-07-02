package com.rose.back.domain.diary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.diary.controller.docs.CalendarControllerDocs;
import com.rose.back.domain.diary.dto.CalendarDataResponse;
import com.rose.back.domain.diary.dto.DiaryResponse;
import com.rose.back.domain.diary.dto.RoseCareLogDto;
import com.rose.back.domain.diary.service.CareLogService;
import com.rose.back.domain.diary.service.DiaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController implements CalendarControllerDocs {

    private final CareLogService careLogService;
    private final DiaryService diaryService;

    @GetMapping("/data")
    public ResponseEntity<CalendarDataResponse> getCalendarData(
            @RequestParam String startDate, 
            @RequestParam String endDate) {
        
        log.info("[GET][/api/calendar/data] - 캘린더 데이터 조회 요청: {} ~ {}", startDate, endDate);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 비로그인 사용자의 경우 빈 데이터 반환
        if (authentication == null || authentication.getPrincipal() == null || 
            !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            log.info("[GET][/api/calendar/data] - 비로그인 사용자의 캘린더 데이터 조회 요청");
            return ResponseEntity.ok(new CalendarDataResponse(List.of(), List.of()));
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userNo = userDetails.getUserNo();
        
        log.info("[GET][/api/calendar/data] - 로그인 사용자의 캘린더 데이터 조회 요청 (userNo: {})", userNo);
        
        // 병렬로 데이터 조회
        List<RoseCareLogDto> careLogs = careLogService.getAllLogsByDateRange(userNo, startDate, endDate);
        List<DiaryResponse> diaries = diaryService.getUserTimelineByDateRange(userNo, startDate, endDate);
        
        log.info("[GET][/api/calendar/data] - 캘린더 데이터 조회 완료 - 케어로그: {}개, 다이어리: {}개", careLogs.size(), diaries.size());
        
        return ResponseEntity.ok(new CalendarDataResponse(careLogs, diaries));
    }
}
