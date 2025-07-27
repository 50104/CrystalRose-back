package com.rose.back.domain.user.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.report.dto.CommentReportResponseDto;
import com.rose.back.domain.report.dto.ReportResponseDto;
import com.rose.back.domain.report.service.ReportService;
import com.rose.back.domain.user.controller.docs.AdminControllerDocs;
import com.rose.back.domain.user.dto.AdminResponse;
import com.rose.back.domain.user.service.AdminService;
import com.rose.back.domain.wiki.dto.WikiModificationRequestDto;
import com.rose.back.domain.wiki.dto.WikiDetailResponse;
import com.rose.back.domain.wiki.dto.WikiModificationComparisonDto;

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

    // 수정 요청 승인
    @GetMapping("/wiki/modifications/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WikiModificationRequestDto>> getPendingModificationRequests() {
        log.info("[GET][/api/v1/admin/wiki/modifications/pending] - 수정 요청 대기 중인 도감 목록 조회");
        return ResponseEntity.ok(adminService.getPendingModificationRequests());
    }

    @PatchMapping("/wiki/modifications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> approveModificationRequest(@PathVariable("id") Long id) {
        log.info("[PATCH][/api/v1/admin/wiki/modifications/{}/approve] - 도감 수정 요청 승인", id);
        adminService.approveModificationRequest(id);
        return ResponseEntity.ok(new MessageResponse("도감 수정 요청이 승인되었습니다."));
    }

    @PatchMapping("/wiki/modifications/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> rejectModificationRequest(@PathVariable("id") Long id) {
        log.info("[PATCH][/api/v1/admin/wiki/modifications/{}/reject] - 도감 수정 요청 거부", id);
        adminService.rejectModificationRequest(id);
        return ResponseEntity.ok(new MessageResponse("도감 수정 요청이 거부되었습니다."));
    }

    @GetMapping("/wiki/{requestId}/original")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WikiModificationComparisonDto> getModificationComparison(@PathVariable("requestId") Long requestId) {
        log.info("도감 수정 요청 변경 사항 비교 조회 - 요청 ID: {}", requestId);
        
        try {
            WikiModificationComparisonDto comparison = adminService.getModificationComparison(requestId);
            log.info("변경 사항 비교 조회 완료 - 변경된 필드 수: {}", comparison.getChangedFields().size());
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            log.error("변경 사항 비교 조회 실패 - 요청 ID: {}, 오류: {}", requestId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/wiki/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteWikiByAdmin(@PathVariable("id") Long id) {
        log.info("[DELETE][/api/v1/admin/wiki/{}] - 도감 삭제 요청 (관리자)", id);
        adminService.deleteWikiByAdmin(id);
        return ResponseEntity.ok(new MessageResponse("도감이 삭제 처리되었습니다."));
    }

    @GetMapping("/wiki/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WikiDetailResponse> getWikiDetailByAdmin(@PathVariable("id") Long id) {
        log.info("[GET][/api/v1/admin/wiki/detail/{}] - 관리자 도감 상세 조회 요청", id);
        WikiDetailResponse response = adminService.getWikiDetailByAdmin(id);
        return ResponseEntity.ok(response);
    }
}
