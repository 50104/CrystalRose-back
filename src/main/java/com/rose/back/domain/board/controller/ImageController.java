package com.rose.back.domain.board.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.board.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @ResponseBody
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> imageUpload(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("[POST][/image/upload] - 이미지 업로드 컨트롤러: {}", file);
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "파일 누락"));
        }
        Map<String, Object> responseData = new HashMap<>();
        try {
            String s3Url = imageService.uploadBoardImage(file);
            responseData.put("uploaded", true);
            responseData.put("url", s3Url);
            return ResponseEntity.ok(responseData);
        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage());
            responseData.put("uploaded", false);
            responseData.put("error", "이미지 업로드에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }
}