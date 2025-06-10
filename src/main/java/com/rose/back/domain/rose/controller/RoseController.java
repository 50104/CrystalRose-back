package com.rose.back.domain.rose.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.rose.service.RoseImageService;
import com.rose.back.domain.rose.service.RoseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/roses")
@RequiredArgsConstructor
public class RoseController {

    private final RoseService roseService;
    private final RoseImageService roseImageService;

    @PostMapping("/mine")
    public ResponseEntity<?> registerRose(@RequestBody RoseRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[POST][/api/roses/mine] - 내 장미 등록 요청: {}", request);
        try {
            roseService.registerUserRose(userDetails, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("내 장미 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/image/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        log.info("[POST][/api/v1/roses/image/upload] - 장미 이미지 업로드 요청: {}", file.getOriginalFilename());
        try {
            String url = roseImageService.uploadImage(file);
            return ResponseEntity.ok(Map.of("uploaded", true, "url", url));
        } catch (IOException e) {
            log.error("장미 이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("uploaded", false, "error", e.getMessage()));
        }
    }

    public record RoseRequest(
        Long userId,
        Long wikiId,
        String nickname,
        LocalDate acquiredDate,
        String locationNote,
        String imageUrl
    ) {}
}
