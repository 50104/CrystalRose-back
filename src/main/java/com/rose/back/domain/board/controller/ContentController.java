package com.rose.back.domain.board.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.board.controller.docs.ContentControllerDocs;
import com.rose.back.domain.board.dto.ContentListResponse;
import com.rose.back.domain.board.dto.ContentRequestDto;
import com.rose.back.domain.board.dto.ContentSummaryDto;
import com.rose.back.domain.board.dto.ContentWithWriterDto;
import com.rose.back.domain.board.service.ContentImageService;
import com.rose.back.domain.board.service.ContentService;
import com.rose.back.domain.board.service.RecommendationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class ContentController implements ContentControllerDocs {

    private final ContentImageService contentImageService;
    private final ContentService contentService;
    private final RecommendationService recommendationService;

    @GetMapping("/editor")
    public ResponseEntity<MessageResponse> editorPage() {
        log.info("[GET][/api/v1/board/editor] - 게시글 작성 페이지 요청");
        return ResponseEntity.ok(new MessageResponse("게시글 작성 페이지입니다."));
    }

    @GetMapping("/editor/{boardNo}")
    public ResponseEntity<ContentResponse> updatePage(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/api/v1/board/editor/{}] - 게시글 수정 요청", boardNo);
        ContentWithWriterDto dto = contentService.selectOneContentDto(boardNo);
        return ResponseEntity.ok(new ContentResponse(true, dto));
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveContent(@Valid @ModelAttribute ContentRequestDto req, BindingResult bindingResult, @RequestParam(value = "files", required = false) List<MultipartFile> files) throws IOException {
        log.info("[POST][/api/v1/board/save] - 게시글 저장 요청: {}", req);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        Long savedBoardNo = contentService.saveContent(req);
        log.info("게시글 저장 완료, boardNo: {}", savedBoardNo);

        if (files != null && !files.isEmpty()) {
            contentImageService.saveImagesForBoard(savedBoardNo, files);
        }
        return ResponseEntity.ok(new BoardNoResponse(true, new BoardNoResponse.BoardData(savedBoardNo)));
    }

    @PostMapping("/save/{boardNo}")
    public ResponseEntity<BoardNoResponse> updateLogic(@ModelAttribute ContentRequestDto req, @PathVariable("boardNo") Long boardNo) {
        log.info("[POST][/api/v1/board/save/{}] - 게시글 수정 요청: {}", boardNo, req);
        contentService.updateOneContent(req, boardNo);
        return ResponseEntity.ok(new BoardNoResponse(true, new BoardNoResponse.BoardData(boardNo)));
    }

    @GetMapping("/list")
    public ResponseEntity<ContentListResponse> listPage(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "size", defaultValue = "3") int size, Authentication authentication) {
        String currentUserId = null;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            currentUserId = customUserDetails.getUsername();
        }

        ContentListResponse response = contentService.selectContentPageWithFixed(page, size, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/content/{boardNo}")
    public ResponseEntity<ContentResponse> contentPage(@PathVariable("boardNo") Long boardNo, Authentication authentication) {
        String currentUserId = authentication.getName();
        log.info("[GET][/api/v1/board/content/{}] - 게시글 조회 요청, 사용자: {}", boardNo, currentUserId);

        ContentWithWriterDto dto = contentService.selectOneContentDto(boardNo, currentUserId, true);
        return ResponseEntity.ok(new ContentResponse(true, dto));
    }

    @DeleteMapping("/delete/{boardNo}")
    public ResponseEntity<MessageResponse> deleteContent(@PathVariable("boardNo") Long boardNo, Authentication authentication) {
        log.info("[DELETE][/api/v1/board/delete/{}] - 게시글 삭제 요청", boardNo);
        String username = authentication.getName();
        contentService.deleteOneContent(boardNo, username);
        return ResponseEntity.ok(new MessageResponse("게시글이 삭제되었습니다."));
    }

    @PostMapping("/image/upload")
    public ResponseEntity<ImageUploadResponse> imageUpload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("[POST][/api/v1/board/image/upload] - 이미지 업로드 요청: {}", file.getOriginalFilename());
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        String s3Url = contentImageService.saveImageS3(file);
        return ResponseEntity.ok(new ImageUploadResponse(true, true, s3Url));
    }

    @PostMapping("/recommend/{boardNo}")
    public ResponseEntity<?> toggleRecommend(@PathVariable("boardNo") Long boardNo, Authentication authentication) {
        String userId = authentication.getName();
        log.info("[POST][/api/v1/board/recommend/{}] - 추천 토글 요청, 사용자: {}", boardNo, userId);
        
        boolean recommended = recommendationService.toggleRecommendation(boardNo, userId);
        return ResponseEntity.ok(new MessageResponse(recommended ? "추천 완료" : "추천 취소"));
    }

    @GetMapping("/prev/{boardNo}")
    public ResponseEntity<ContentSummaryDto> getPrevPost(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/api/v1/board/prev/{}] - 이전 게시글 조회 요청", boardNo);
        return contentService.getPreviousPost(boardNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/next/{boardNo}")
    public ResponseEntity<ContentSummaryDto> getNextPost(@PathVariable("boardNo") Long boardNo) {
        log.info("[GET][/api/v1/board/next/{}] - 다음 게시글 조회 요청", boardNo);
        return contentService.getNextPost(boardNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    public record BoardNoResponse(boolean success, BoardData data) {
        public record BoardData(Long boardNo) {}
    }
    public record ContentResponse(boolean success, ContentWithWriterDto data) {}
    public record ImageUploadResponse(boolean success, boolean uploaded, String url) {}
}
