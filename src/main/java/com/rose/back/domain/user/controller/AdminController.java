package com.rose.back.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.rose.back.domain.report.dto.ReportResponseDto;
import com.rose.back.domain.report.service.ReportService;
import com.rose.back.domain.user.controller.docs.AdminControllerDocs;
import com.rose.back.domain.user.dto.AdminResponse;
import com.rose.back.domain.user.service.AdminService;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController implements AdminControllerDocs {

    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/wiki/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingWikiList() {
        log.info("[GET][/api/v1/admin/wiki/pending] - 승인 대기 도감 리스트 요청");
        try {
            List<AdminResponse> list = adminService.getPendingList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("도감 승인 대기 리스트 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("도감 승인 대기 리스트 조회 실패");
        }
    }

    @PatchMapping("/wiki/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveWiki(@PathVariable("id") Long id) {
        log.info("[PATCH][/api/v1/admin/wiki/{}/approve] - 도감 승인 요청", id);
        try {
            adminService.approve(id);
            return ResponseEntity.ok().build(); // body 없음
        } catch (Exception e) {
            log.error("도감 승인 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("도감 승인 실패");
        }
    }

    @PatchMapping("/wiki/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectWiki(@PathVariable("id") Long id) {
        log.info("[PATCH][/api/v1/admin/wiki/{}/reject] - 도감 거절 요청", id);
        try {
            adminService.reject(id);
            return ResponseEntity.ok().build(); // body 없음
        } catch (Exception e) {
            log.error("도감 거절 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("도감 거절 실패");
        }
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReports() {
        log.info("[GET][/api/v1/admin/reports] - 관리자 신고 내역 조회 요청");
        try {
            List<ReportResponseDto> reports = reportService.getAllReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("신고 내역 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("신고 내역 조회 실패");
        }
    }
}
