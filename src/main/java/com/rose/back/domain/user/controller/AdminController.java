package com.rose.back.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class AdminController implements AdminControllerDocs{

    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/wiki/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminResponse> getPendingWikiList() { // 승인 대기 도감 리스트
        return adminService.getPendingList();
    }

    @PatchMapping("/wiki/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveWiki(@PathVariable("id") Long id) { // 도감 승인
        adminService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/wiki/{id}/reject") 
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectWiki(@PathVariable("id") Long id) { // 도감 거절
        adminService.reject(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')") // ✅ 관리자만 접근 가능
    public ResponseEntity<List<ReportResponseDto>> getReports() {
        log.info("[GET][/admin/reports] - 관리자 신고 내역 조회 요청");
        List<ReportResponseDto> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

}
