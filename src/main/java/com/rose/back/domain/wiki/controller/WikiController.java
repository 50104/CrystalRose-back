package com.rose.back.domain.wiki.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.wiki.dto.WikiRequest;
import com.rose.back.domain.wiki.dto.WikiResponse;
import com.rose.back.domain.wiki.service.WikiImageService;
import com.rose.back.domain.wiki.service.WikiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wiki")
public class WikiController {

    private final WikiService wikiService;
    private final WikiImageService wikiImageService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerWiki(@RequestBody @Valid WikiRequest dto) {
        log.info("[POST][/api/v1/wiki/register] - 수신된 도감 등록 요청: {}", dto);
        try {
            wikiService.registerWiki(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("도감 등록 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("image/upload")
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

    @GetMapping("/list")
    public ResponseEntity<List<WikiResponse>> getWikiList() {
        try {
            log.info("[GET][/api/v1/wiki/list] - 도감 목록 조회 요청");
            List<WikiResponse> wikiList = wikiService.getApprovedWikiList();
            return ResponseEntity.ok(wikiList);
        } catch (Exception e) {
            log.error("도감 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<WikiResponse> getWikiDetail(@PathVariable Long id) {
        try {
            log.info("[GET][/api/v1/wiki/{}] - 도감 상세 정보 조회 요청", id);
            WikiResponse wikiDetail = wikiService.getApprovedWikiDetail(id);
            return ResponseEntity.ok(wikiDetail);
        } catch (RuntimeException e) { 
            log.warn("도감 상세 정보 조회 실패 (ID: {}): {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
        } catch (Exception e) {
            log.error("도감 상세 정보 조회 중 알 수 없는 오류 발생 (ID: {}):", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
