package com.rose.back.domain.user.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.report.dto.CommentReportResponseDto;
import com.rose.back.domain.report.dto.ReportResponseDto;
import com.rose.back.domain.report.service.ReportService;
import com.rose.back.domain.user.controller.docs.AdminControllerDocs;
import com.rose.back.domain.user.dto.AdminResponse;
import com.rose.back.domain.user.service.AdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController implements AdminControllerDocs {

    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/wiki/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminResponse>> getPendingWikiList() {
        log.info("[GET][/api/v1/admin/wiki/pending] - 승인 대기 도감 리스트 요청");
        return ResponseEntity.ok(adminService.getPendingList());
    }

    @PatchMapping("/wiki/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> approveWiki(@PathVariable("id") Long id) {
        log.info("[PATCH][/api/v1/admin/wiki/{}/approve] - 도감 승인 요청", id);
        adminService.approve(id);
        return ResponseEntity.ok(new MessageResponse("도감이 승인되었습니다."));
    }

    @PatchMapping("/wiki/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> rejectWiki(@PathVariable("id") Long id) {
        log.info("[PATCH][/api/v1/admin/wiki/{}/reject] - 도감 거절 요청", id);
        adminService.reject(id);
        return ResponseEntity.ok(new MessageResponse("도감이 거절되었습니다."));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponseDto>> getReports() {
        log.info("[GET][/api/v1/admin/reports] - 신고 내역 요청");
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/comment-reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentReportResponseDto>> getCommentReports() {
        log.info("[GET][/api/v1/admin/comment-reports] - 댓글 신고 내역 요청");
        return ResponseEntity.ok(adminService.getAllCommentReports());
    }

    // 수정 승인 관련 엔드포인트들
    @GetMapping("/wiki/modifications/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminResponse>> getPendingModificationList() {
        log.info("[GET][/api/v1/admin/wiki/modifications/pending] - 수정 대기 중인 도감 목록 조회");
        return ResponseEntity.ok(adminService.getPendingModificationList());
    }

    @PatchMapping("/wiki/modifications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> approveModification(@PathVariable("id") Long id) {
        log.info("[PATCH][/api/v1/admin/wiki/modifications/{}/approve] - 도감 수정 승인 요청", id);
        adminService.approveModification(id);
        return ResponseEntity.ok(new MessageResponse("도감 수정이 승인되었습니다."));
    }

    @PatchMapping("/wiki/modifications/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> rejectModification(@PathVariable("id") Long id) {
        log.info("[PATCH][/api/v1/admin/wiki/modifications/{}/reject] - 도감 수정 거부 요청", id);
        adminService.rejectModification(id);
        return ResponseEntity.ok(new MessageResponse("도감 수정이 거부되었습니다."));
    }
}
