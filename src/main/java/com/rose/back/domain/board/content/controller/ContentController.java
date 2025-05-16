package com.rose.back.domain.board.content.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.board.content.dto.ContentRequestDto;
import com.rose.back.domain.board.content.entity.Board;
import com.rose.back.domain.board.content.service.BoardService;
import com.rose.back.domain.board.content.service.ContentService;

import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class ContentController {

    private final BoardService boardService;
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
            map.put("data", contentService.selectOneContent(boardNo));
            return ResponseEntity.ok().body(map);
        } catch (Exception e) {
            log.error("게시글 수정 페이지 불러오기 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 수정 페이지 불러오기 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveLogic(@ModelAttribute ContentRequestDto req) {
        log.info("[POST][/board/save] - 게시글 저장 컨트롤러, 데이터: {}", req);
        try {
            Long savedBoardNo = contentService.saveContent(req);
            Map<String, Object> response = new HashMap<>();
            response.put("data", Collections.singletonMap("boardNo", savedBoardNo));
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
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("게시글 수정 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 수정 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listPage() {
        log.info("[GET][/board/list] - 게시글 리스트 컨트롤러");
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("ContentList", contentService.selectContent());
            return ResponseEntity.ok().body(map);
        } catch (Exception e) {
            log.error("게시글 리스트 불러오기 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시글 리스트 불러오기 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/content/{boardNo}")
    public ResponseEntity<Map<String, Object>> contentPage(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/board/content/{}] - 게시글 조회 컨트롤러", boardNo);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("Content", contentService.selectOneContent(boardNo));
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

    @PostMapping("/board")
    public ResponseEntity<Map<String, String>> createBoard(
            @Validated @RequestParam("files") List<MultipartFile> files
    ) throws Exception {
        log.info("[POST][/board] - 게시판 생성 컨트롤러, 파일 개수: {}", files.size());
        try {
            boardService.addBoard(Board.builder().build(), files);
            Map<String, String> response = new HashMap<>();
            response.put("message", "게시판 생성 성공");
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("게시판 생성 실패: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시판 생성 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/board")
    public ResponseEntity<Map<String, Object>> getBoard(@RequestParam long id) {
        log.info("[GET][/board] - 게시판 조회 컨트롤러, ID: {}", id);
        try {
            Board board = boardService.findBoard(id).orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다."));
            String imgPath = board.getStoredFileName();
            log.info("저장된 이미지 경로: {}", imgPath);
            Map<String, Object> response = new HashMap<>();
            response.put("imgPath", imgPath);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("게시판 조회 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시판 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
