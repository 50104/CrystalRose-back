package com.rose.back.domain.wiki.controller;

import com.rose.back.common.dto.MessageResponse;
import com.rose.back.domain.wiki.dto.WikiModificationRequestDto;
import com.rose.back.domain.wiki.dto.WikiModificationResubmitDto;
import com.rose.back.domain.wiki.dto.WikiRequest;
import com.rose.back.domain.wiki.dto.WikiResponse;
import com.rose.back.domain.wiki.service.WikiImageService;
import com.rose.back.domain.wiki.service.WikiService;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.auth.repository.AuthRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
        
        try {
            wikiService.submitModificationRequest(id, dto, requester);
            log.info("수정 요청 처리 완료");
            return ResponseEntity.ok(new MessageResponse("도감 수정 요청이 제출되었습니다. 관리자 승인 후 반영됩니다."));
        } catch (Exception e) {
            log.error("수정 요청 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new MessageResponse("수정 요청 처리 중 오류가 발생했습니다."));
        }
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

    @GetMapping("/modification/rejected")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WikiModificationRequestDto>> getRejectedModifications(@AuthenticationPrincipal CustomUserDetails user) {
        log.info("[GET][/api/v1/wiki/modification/rejected] - 거절된 도감 수정 요청 조회");
        Long userNo = user.getUserNo();
        return ResponseEntity.ok(wikiService.getRejectedModificationRequests(userNo));
    }

    @GetMapping("/user/modification/{id}/resubmit")
    public ResponseEntity<WikiModificationResubmitDto> getResubmitData(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails user) {
        log.info("[GET][/api/v1/wiki/user/modification/{}/resubmit] - 보완 제출 폼 조회 요청", id);
        WikiModificationResubmitDto dto = wikiService.getResubmitFormData(id, user.getUserNo());
        return ResponseEntity.ok(dto);
    }

    public record ImageUploadResponse(boolean uploaded, String url, String error) {}
}
