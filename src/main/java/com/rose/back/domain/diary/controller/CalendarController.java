package com.rose.back.domain.diary.controller;

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
import com.rose.back.domain.diary.service.CalendarService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController implements CalendarControllerDocs {

    private final CalendarService calendarService;

    @GetMapping("/data")
    public ResponseEntity<CalendarDataResponse> getCalendarData(@RequestParam(name = "startDate") String startDate, @RequestParam(name = "endDate") String endDate) {
        log.info("[GET][/api/calendar/data] - 캘린더 데이터 조회 요청: {} ~ {}", startDate, endDate);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null ||
                !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            CalendarDataResponse response = calendarService.getGuestCalendarData();
            return ResponseEntity.ok(response);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userNo = userDetails.getUserNo();
        
        CalendarDataResponse response = calendarService.getCalendarData(userNo, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
