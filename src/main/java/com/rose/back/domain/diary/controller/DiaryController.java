package com.rose.back.domain.diary.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.diary.service.DiaryImageService;
import com.rose.back.domain.diary.service.DiaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryImageService diaryImageService;

    // 성장 기록 등록
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> addDiary(@RequestBody DiaryRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[POST][/api/diaries/register] - 성장 기록 등록 요청");
        diaryService.registerDiary(userDetails.getUserNo(), request);
        return ResponseEntity.ok(new MessageResponse("성장 기록이 등록되었습니다."));
    }

    // 이미지 업로드
    @PostMapping("/image/upload")
    public ResponseEntity<ImageUploadResponse> uploadDiaryImage(@RequestParam("file") MultipartFile file) {
        log.info("[POST][/api/diaries/image/upload] - 다이어리 이미지 업로드 요청: {}", file.getOriginalFilename());
        try {
            String url = diaryImageService.uploadImage(file);
            return ResponseEntity.ok(new ImageUploadResponse(true, url, null));
        } catch (Exception e) {
            log.error("다이어리 이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(new ImageUploadResponse(false, null, e.getMessage()));
        }
    }

    // 전체 성장기록
    @GetMapping("/list")
    public ResponseEntity<List<DiaryResponse>> getMyTimeline(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/diaries/list] - 내 성장기록 조회 요청 (userId: {})", userDetails.getUserNo());
        List<DiaryResponse> timeline = diaryService.getUserTimeline(userDetails.getUserNo());
        return ResponseEntity.ok(timeline);
    }

    // 장미별 성장기록
    @GetMapping("/{roseId}/timeline")
    public ResponseEntity<List<DiaryResponse>> getRoseTimeline(@PathVariable Long roseId) {
        log.info("[GET][/api/diaries/{}/timeline] - 장미별 성장기록 조회 요청", roseId);
        List<DiaryResponse> diaryList = diaryService.getRoseTimeline(roseId);
        return ResponseEntity.ok(diaryList);
    }

    // 요청 DTO
    public record DiaryRequest(
        Long roseId,
        String note,
        String imageUrl,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime recordedAt
    ) {}

    // 응답 DTO
    public record DiaryResponse(
        Long id,
        String note,
        String imageUrl,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime recordedAt
    ) {}

    public record ImageUploadResponse(boolean uploaded, String url, String error) {}
}
