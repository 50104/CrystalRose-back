package com.rose.back.domain.diary.controller;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.diary.entity.DiaryEntity;
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
    public ResponseEntity<?> addDiary(@RequestBody DiaryRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[POST][/api/diaries/register] - 성장 기록 등록 요청");
        log.info("Request data: note={}, imageUrl={}, recordedAt={}", request.note(), request.imageUrl(), request.recordedAt());
        try {
            diaryService.saveDiaryWithImages(userDetails.getUserNo(), request.roseId(), request.note(), request.imageUrl(), request.recordedAt());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("성장 기록 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "등록 실패: " + e.getMessage()));
        }
    }

    // 이미지 업로드
    @PostMapping("/image/upload")
    public ResponseEntity<Map<String, Object>> uploadDiaryImage(@RequestParam("file") MultipartFile file) {
        log.info("[POST][/api/diaries/image/upload] - 다이어리 이미지 업로드 요청: {}", file.getOriginalFilename());
        try {
            String url = diaryImageService.uploadImage(file);
            return ResponseEntity.ok(Map.of("uploaded", true, "url", url));
        } catch (IOException e) {
            log.error("다이어리 이미지 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("uploaded", false, "error", e.getMessage()));
        }
    }

    public record DiaryRequest(Long roseId, String note, String imageUrl, LocalDateTime recordedAt) {}
}
