package com.rose.back.domain.wiki.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.wiki.dto.WikiModificationDetailDto;
import com.rose.back.domain.wiki.dto.WikiModificationListDto;
import com.rose.back.domain.wiki.dto.WikiModificationResubmitDto;
import com.rose.back.domain.wiki.dto.WikiRequest;
import com.rose.back.domain.wiki.dto.WikiResponse;
import com.rose.back.domain.wiki.dto.WikiWishlistAddRequest;
import com.rose.back.domain.wiki.dto.WikiWishlistResponse;
import com.rose.back.domain.wiki.service.WikiImageService;
import com.rose.back.domain.wiki.service.WikiService;
import com.rose.back.domain.wiki.service.WikiWishlistService;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.auth.repository.AuthRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wiki")
public class WikiController {

    private final WikiService wikiService;
    private final WikiImageService wikiImageService;
    private final WikiWishlistService wikiWishlistService;
    private final AuthRepository authRepository;

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
    public ResponseEntity<MessageResponse> submitModificationRequest(@PathVariable("id") Long id, @RequestBody @Valid WikiRequest dto) {
        log.info("[PUT][/api/v1/wiki/modify/{}] - 도감 수정 요청 시작", id);
        log.info("요청 데이터 - name: {}, category: {}, description: {}", dto.getName(), dto.getCategory(), dto.getDescription());
        
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("인증된 사용자 ID: {}", userId);
        
        UserEntity requester = authRepository.findByUserId(userId);
        
        if (requester == null) {
            log.warn("인증되지 않은 사용자의 수정 요청: userId={}", userId);
            return ResponseEntity.badRequest().body(new MessageResponse("인증된 사용자가 아닙니다."));
        }
        log.info("요청자 정보 - userNo: {}, userNick: {}", requester.getUserNo(), requester.getUserNick());
        
        wikiService.submitModificationRequest(id, dto, requester);
        return ResponseEntity.ok(new MessageResponse("도감 수정 요청이 제출되었습니다. 관리자 승인 후 반영됩니다."));
    }

    @GetMapping("/list")
    public ResponseEntity<List<WikiResponse>> getWikiList() {
        log.info("[GET][/api/v1/wiki/list] - 도감 목록 조회 요청");
        List<WikiResponse> wikiList = wikiService.getApprovedWikiList();
        return ResponseEntity.ok(wikiList);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<WikiResponse> getWikiDetail(@PathVariable("id") Long id) {
        log.info("[GET][/api/v1/wiki/detail/{}] - 도감 상세 정보 조회 요청", id);
        WikiResponse wikiDetail = wikiService.getApprovedWikiDetail(id);
        return ResponseEntity.ok(wikiDetail);
    }

    @GetMapping("/user/modification/list")
    public ResponseEntity<List<WikiModificationListDto>> getUserModifications(@AuthenticationPrincipal CustomUserDetails user) {
        log.info("[GET][/api/v1/wiki/user/modification/list] - 사용자 도감 수정 요청 목록 조회");
        List<WikiModificationListDto> dtoList = wikiService.getUserModifications(user.getUserNo());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/user/modification/{id}")
    public ResponseEntity<WikiModificationResubmitDto> getRejectedModification(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[GET][/api/v1/wiki/user/modification/{}] - 거절된 도감 수정 요청 조회", id);
        WikiModificationResubmitDto dto = wikiService.getRejectedModification(id, user.getUserNo());
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/user/modification/{id}/resubmit")
    public ResponseEntity<MessageResponse> resubmit(@PathVariable("id") Long id, @RequestBody WikiModificationResubmitDto dto, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[PATCH][/api/v1/wiki/user/modification/{}/resubmit] - 도감 보완 제출 요청", id);
        wikiService.resubmitModificationRequest(id, user.getUserNo(), dto);
        return ResponseEntity.ok(new MessageResponse("도감 보완 제출 완료"));
    }

    @GetMapping("/user/list")
    public ResponseEntity<Page<WikiResponse>> getMyWikis(@AuthenticationPrincipal CustomUserDetails principal, @RequestParam(value = "status", required = false) List<String> statusStrings, Pageable pageable) {
        log.info("[GET][/api/v1/wiki/user/list] - 신청한 도감 목록 조회 요청");
        Long userId = principal.getUserNo();
        
        Page<WikiResponse> result = wikiService.getMyWikis(userId, statusStrings, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/rejected")
    public ResponseEntity<Page<WikiResponse>> getMyRejectedWikis(@AuthenticationPrincipal CustomUserDetails principal, Pageable pageable) {
        log.info("[GET][/api/v1/wiki/user/rejected] - 거절된 도감 목록 조회 요청");
        
        Page<WikiResponse> result = wikiService.getMyRejectedWikis(principal.getUserNo(), pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/rejected/{id}")
    public ResponseEntity<WikiResponse> getMyRejectedWikiDetail(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("[GET][/api/v1/wiki/user/rejected/{}] - 거절된 도감 단건 상세 조회", id);
        WikiResponse detail = wikiService.getMyRejectedWikiDetail(principal.getUserNo(), id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/user/modify/detail/{id}")
    public ResponseEntity<WikiModificationDetailDto> getUserModificationDetail(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[GET][/api/v1/wiki/user/modify/detail/{}] - 사용자 도감 수정 요청 상세 조회", id);
        
        WikiModificationDetailDto detail = wikiService.getUserModificationDetail(id, user.getUserNo());
        return ResponseEntity.ok(detail);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<MessageResponse> cancelMyWiki(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[DELETE][/api/v1/wiki/user/{}] - 도감 제출 취소 요청", id);

        wikiService.cancelMyWiki(id, user.getUserNo());
        return ResponseEntity.ok(new MessageResponse("도감 제출이 취소되었습니다."));
    }

    @DeleteMapping("/user/modification/{id}/cancel")
    public ResponseEntity<MessageResponse> cancelUserModification(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[DELETE][/api/v1/wiki/user/modification/{}/cancel] - 사용자 도감 수정 요청 취소", id);
        
        wikiService.cancelUserModification(id, user.getUserNo());
        return ResponseEntity.ok(new MessageResponse("도감 수정 요청이 취소되었습니다."));
    }

    @PatchMapping("/user/{id}/resubmit")
    public ResponseEntity<MessageResponse> resubmitRejectedWiki(@PathVariable("id") Long id, @RequestBody WikiRequest dto, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[PATCH][/api/v1/wiki/user/{}/resubmit] - 거절된 도감 보완 재제출", id);
        wikiService.resubmitRejectedWiki(id, user.getUserNo(), dto);
        return ResponseEntity.ok(new MessageResponse("거절된 도감이 보완 제출되었습니다."));
    }

    @PostMapping("/wishlist")
    public ResponseEntity<WikiWishlistResponse> addToWishlist(@RequestBody WikiWishlistAddRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[POST][/api/v1/wiki/wishlist] - 위시리스트 추가 요청: wikiId={}, userNo={}", request.getWikiId(), user.getUserNo());
        
        WikiWishlistResponse response = wikiWishlistService.addToWishlist(user.getUserNo(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/wishlist/{wikiId}")
    public ResponseEntity<MessageResponse> removeFromWishlist(@PathVariable("wikiId") Long wikiId, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[DELETE][/api/v1/wiki/wishlist/{}] - 위시리스트 제거 요청: userNo={}", wikiId, user.getUserNo());
        
        wikiWishlistService.removeFromWishlist(user.getUserNo(), wikiId);
        return ResponseEntity.ok(new MessageResponse("위시리스트에서 제거되었습니다."));
    }

    @GetMapping("/wishlist")
    public ResponseEntity<List<WikiWishlistResponse>> getUserWishlist(
            @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[GET][/api/v1/wiki/wishlist] - 사용자 위시리스트 조회: userNo={}", user.getUserNo());
        
        List<WikiWishlistResponse> wishlist = wikiWishlistService.getUserWishlist(user.getUserNo());
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/wishlist/check/{wikiId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlistStatus(@PathVariable("wikiId") Long wikiId, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[GET][/api/v1/wiki/wishlist/check/{}] - 위시리스트 포함 여부 확인: userNo={}", wikiId, user.getUserNo());
        
        boolean isInWishlist = wikiWishlistService.isInWishlist(user.getUserNo(), wikiId);
        Map<String, Boolean> response = Map.of("isInWishlist", isInWishlist);
        return ResponseEntity.ok(response);
    }

    public record ImageUploadResponse(boolean uploaded, String url, String error) {}
}
