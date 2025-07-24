package com.rose.back.domain.rose.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.rose.controller.docs.RoseControllerDocs;
import com.rose.back.domain.rose.dto.ImageUploadResponse;
import com.rose.back.domain.rose.dto.RoseRequest;
import com.rose.back.domain.rose.dto.RoseResponse;
import com.rose.back.domain.rose.service.RoseImageService;
import com.rose.back.domain.rose.service.RoseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/roses")
@RequiredArgsConstructor
public class RoseController implements RoseControllerDocs {

    private final RoseService roseService;
    private final RoseImageService roseImageService;

    @PostMapping("/mine")
    public ResponseEntity<MessageResponse> registerRose(@RequestBody RoseRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[POST][/api/roses/mine] - 내 장미 등록 요청");
        roseService.registerUserRose(userDetails, request);
        return ResponseEntity.ok(new MessageResponse("장미가 등록되었습니다."));
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicateRose(@RequestParam("wikiId") Long wikiId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/roses/check-duplicate] - 장미 중복 확인 요청: wikiId={}", wikiId);
        boolean exists = roseService.existsByUserIdAndWikiId(userDetails.getUserNo(), wikiId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/mine/wiki-ids")
    public ResponseEntity<List<Long>> getMyRoseWikiIds(@AuthenticationPrincipal CustomUserDetails user) {
        log.info("[GET][/api/roses/mine/wiki-ids] - 내 장미 위키 ID 목록 조회 요청");
        List<Long> registeredWikiIds = roseService.getUserRoses(user.getUserNo()).stream()
            .map(rose -> rose.getWikiEntity().getId())
            .toList();
        return ResponseEntity.ok(registeredWikiIds);
    }

    @PostMapping("/image/upload")
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        log.info("[POST][/api/roses/image/upload] - 장미 이미지 업로드 요청: {}", file.getOriginalFilename());
        try {
            String url = roseImageService.uploadImage(file);
            return ResponseEntity.ok(new ImageUploadResponse(true, url, null));
        } catch (Exception e) {
            log.error("장미 이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(new ImageUploadResponse(false, null, e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<RoseResponse>> getMyRoses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[GET][/api/roses/list] - 내 장미 목록 조회 요청");
        if (userDetails == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }

        List<RoseResponse> responses = roseService.getUserRoseResponses(userDetails.getUserNo());
        log.info("내 장미 목록 조회 완료: {} 개", responses.size());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/modify/{roseId}")
    public ResponseEntity<MessageResponse> updateRose(
        @PathVariable("roseId") Long roseId,
        @RequestBody RoseRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("[PUT][/api/roses/modify/{}] - 장미 수정 요청", roseId);
        roseService.updateUserRose(userDetails.getUserNo(), roseId, request);
        return ResponseEntity.ok(new MessageResponse("장미 정보가 수정되었습니다."));
    }

    @DeleteMapping("/delete/{roseId}")
    public ResponseEntity<?> deleteRose(@PathVariable Long roseId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[DELETE][/api/roses/delete/{}] - 장미 삭제 요청: roseId={}", roseId, roseId);
        Long userId = userDetails.getUserNo();
        roseService.deleteRoseIfNoDiaries(roseId, userId);
        return ResponseEntity.ok().body(Map.of("message", "장미가 삭제되었습니다."));
    }
}