package com.rose.back.domain.rose.controller;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.rose.service.RoseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/roses")
@RequiredArgsConstructor
public class RoseController {

    private final RoseService roseService;

    @PostMapping("/mine")
    public ResponseEntity<?> registerRose(@RequestBody RoseRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[POST][/api/roses/mine] - 내 장미 등록 요청: {}", request);
        log.info("userId: {}, wikiId: {}, acquiredDate: {}", request.userId(), request.wikiId(), request.acquiredDate());
        try {
            Long userId = userDetails.getUserNo();
            roseService.createUserRose(
                userId,
                request.wikiId(),
                request.nickname(),
                request.acquiredDate(),
                request.locationNote()
            );
            log.info("userId: {}, wikiId: {}, acquiredDate: {}, nickname: {}, locationNote: {}",
                userId, request.wikiId(), request.acquiredDate(), request.nickname(), request.locationNote()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("내 장미 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public record RoseRequest(
        Long userId,
        Long wikiId,
        String nickname,
        LocalDate acquiredDate,
        String locationNote
    ) {}
}
