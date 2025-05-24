package com.rose.back.domain.wiki.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.wiki.service.WikiImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/wiki/image")
@RequiredArgsConstructor
public class WikiImageController {

    private final WikiImageService wikiImageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        log.info("[POST][/api/v1/wiki/image/upload] - 이미지 업로드 요청: {}", file.getOriginalFilename());
        try {
            String url = wikiImageService.uploadImage(file);
            return ResponseEntity.ok(Map.of("uploaded", true, "url", url));
        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("uploaded", false, "error", e.getMessage()));
        }
    }
}
