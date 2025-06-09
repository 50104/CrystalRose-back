package com.rose.back.domain.board.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.common.util.PageUtil;
import com.rose.back.domain.board.dto.ContentListDto;
import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.domain.board.service.ContentService;
import com.rose.back.domain.board.service.ContentImageService;

import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class ContentController {

    private final ContentImageService contentImageService;
    private final ContentService contentService;

    @GetMapping("/editor")
    public ResponseEntity<String> editorPage() {
        log.info("[GET][/board/editor] - 게시글 작성 컨트롤러");
        try {
            return ResponseEntity.ok().body("게시글 작성 성공");
        } catch (Exception e) {
            log.error("게시글 작성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("게시글 작성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/editor/{boardNo}")
    public ResponseEntity<Map<String, Object>> updatePage(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/board/editor/{}] - 게시글 수정 시도 컨트롤러", boardNo);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("data", contentService.selectOneContentDto(boardNo));
            return ResponseEntity.ok().body(map);
        } catch (Exception e) {
            log.error("게시글 수정 페이지 불러오기 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 수정 페이지 불러오기 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveContent(
        @ModelAttribute ContentRequestDto req,
        @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        log.info("[POST][/board/save] - 게시글 저장 요청: {}", req);
        try {
            Long savedBoardNo = contentService.saveContent(req); // 게시글 저장
            log.info("게시글 저장 완료, boardNo: {}", savedBoardNo);
            if (files != null && !files.isEmpty()) {
                log.info("업로드할 파일 수: {}", files.size());
                for (MultipartFile file : files) {
                    log.info("파일 이름: {}", file.getOriginalFilename());
                }
                contentImageService.saveImagesForBoard(savedBoardNo, files); // 이미지 저장
            }
            Map<String, Object> response = new HashMap<>();
            response.put("data", Collections.singletonMap("boardNo", savedBoardNo));
            log.info("/save 응답 데이터 구조 확인: {}", response);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            log.error("게시글 저장 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 저장 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/save/{boardNo}")
    public ResponseEntity<Map<String, Object>> updateLogic(@ModelAttribute ContentRequestDto req, @PathVariable("boardNo") Long boardNo) {
        log.info("[POST][/board/save/{}] - 게시글 수정 컨트롤러, 데이터: {}", boardNo, req);
        try {
            contentService.updateOneContent(req, boardNo);
            Map<String, Object> response = new HashMap<>();
            response.put("data", Collections.singletonMap("boardNo", boardNo));
            log.info("/save/{boardNo} 응답 데이터 구조 확인: {}", response);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("게시글 수정 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 수정 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listPage(
        @RequestParam(name = "page", defaultValue = "1") int page) {

        log.info("[GET][/board/list] - 게시글 목록 조회 컨트롤러, 페이지: {}", page);
        try {
            Page<ContentListDto> contentPage = contentService.selectContentPage(page, 3);
            return ResponseEntity.ok(PageUtil.toPageResponse(contentPage));
        } catch (Exception e) {
            log.error("게시글 목록 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/content/{boardNo}")
    public ResponseEntity<Map<String, Object>> contentPage(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/board/content/{}] - 게시글 조회 컨트롤러", boardNo);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("Content", contentService.selectOneContentDto(boardNo));
            return ResponseEntity.ok().body(map);
        } catch (Exception e) {
            log.error("게시글 조회 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/delete/{boardNo}")
    public ResponseEntity<Map<String, String>> deleteC(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/board/delete/{}] - 게시글 삭제 컨트롤러", boardNo);
        try {
            contentService.deleteOneContent(boardNo);
            Map<String, String> response = new HashMap<>();
            response.put("message", "게시글 삭제 성공");
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("게시글 삭제 실패: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 삭제 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @ResponseBody
    @PostMapping("/image/upload")
    public ResponseEntity<Map<String, Object>> imageUpload(@RequestParam("file") MultipartFile file) {
        log.info("[POST][/image/upload] - 이미지 업로드 요청: {}", file.getOriginalFilename());
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("uploaded", false, "error", "파일 누락"));
        }
        try {
            String s3Url = contentImageService.saveImageS3(file);
            return ResponseEntity.ok(Map.of("uploaded", true, "url", s3Url));
        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("uploaded", false, "error", e.getMessage()));
        }
    }
}
