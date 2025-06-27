package com.rose.back.domain.wiki.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.wiki.dto.WikiRequest;
import com.rose.back.domain.wiki.dto.WikiResponse;
import com.rose.back.domain.wiki.service.WikiImageService;
import com.rose.back.domain.wiki.service.WikiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wiki")
public class WikiController {

    private final WikiService wikiService;
    private final WikiImageService wikiImageService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerWiki(@RequestBody @Valid WikiRequest dto) {
        log.info("[POST][/api/v1/wiki/register] - 도감 등록 요청: {}", dto);
        wikiService.registerWiki(dto);
        return ResponseEntity.ok(new MessageResponse("도감이 등록되었습니다."));
    }

    @PostMapping("/image/upload")
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        log.info("[POST][/api/v1/wiki/image/upload] - 이미지 업로드 요청: {}", file.getOriginalFilename());
                try {
            String url = wikiImageService.uploadImage(file);
            return ResponseEntity.ok(new ImageUploadResponse(true, url, null));
        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(new ImageUploadResponse(false, null, e.getMessage()));
        }
    }

    @PutMapping("/modify/{id}")
    public ResponseEntity<MessageResponse> updateWiki(@PathVariable Long id, @RequestBody @Valid WikiRequest dto) {
        log.info("[PUT][/api/v1/wiki/{}] - 도감 수정 요청: {}", id, dto);
        wikiService.updateWiki(id, dto);
        return ResponseEntity.ok(new MessageResponse("도감이 수정되었습니다."));
    }

    @GetMapping("/list")
    public ResponseEntity<List<WikiResponse>> getWikiList() {
        log.info("[GET][/api/v1/wiki/list] - 도감 목록 조회 요청");
        List<WikiResponse> wikiList = wikiService.getApprovedWikiList();
        return ResponseEntity.ok(wikiList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WikiResponse> getWikiDetail(@PathVariable Long id) {
        log.info("[GET][/api/v1/wiki/{}] - 도감 상세 정보 조회 요청", id);
        WikiResponse wikiDetail = wikiService.getApprovedWikiDetail(id);
        return ResponseEntity.ok(wikiDetail);
    }

    public record ImageUploadResponse(boolean uploaded, String url, String error) {}
}
