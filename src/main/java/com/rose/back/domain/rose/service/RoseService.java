package com.rose.back.domain.rose.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.auth.oauth2.CustomUserDetails;
import com.rose.back.domain.diary.entity.DiaryEntity;
import com.rose.back.domain.diary.repository.DiaryRepository;
import com.rose.back.domain.diary.service.DiaryImageService;
import com.rose.back.domain.rose.dto.RoseRequest;
import com.rose.back.domain.rose.dto.RoseResponse;
import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.domain.rose.repository.RoseRepository;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoseService {

    private final RoseRepository userRoseRepository;
    private final WikiRepository roseWikiRepository;
    private final RoseImageService roseImageService;
    private final DiaryRepository diaryRepository;
    private final DiaryImageService diaryImageService;

    @Transactional
    public void registerUserRose(CustomUserDetails userDetails, RoseRequest request) {
        log.info("내 장미 등록 시작: userId={}, wikiId={}", userDetails.getUserNo(), request.wikiId());

        Long userId = userDetails.getUserNo();

        boolean exists = userRoseRepository.existsByUserIdAndWikiEntityId(userId, request.wikiId());
        if (exists) {
            throw new IllegalStateException("이미 등록된 장미 품종입니다.");
        }

        WikiEntity roseWiki = roseWikiRepository.findById(request.wikiId())
            .orElseThrow(() -> new IllegalArgumentException("도감 품종이 존재하지 않습니다"));

        RoseEntity userRose = RoseEntity.builder()
            .wikiEntity(roseWiki)
            .userId(userId)
            .nickname(request.nickname())
            .acquiredDate(request.acquiredDate())
            .locationNote(request.locationNote())
            .imageUrl(request.imageUrl())
            .build();
        userRose = userRoseRepository.save(userRose);

        roseImageService.saveImageEntityAndDeleteTemp(request.imageUrl(), null, userRose);
        createInitialDiary(userRose, request); // 첫 기록 등록

        log.info("내 장미 등록 완료: roseId={}", userRose.getId());
    }
    
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndWikiId(Long userId, Long wikiId) {
        return userRoseRepository.existsByUserIdAndWikiEntityId(userId, wikiId);
    }

    private void createInitialDiary(RoseEntity rose, RoseRequest request) {
        log.info("첫 번째 성장기록 생성 시작: roseId={}", rose.getId());
        
        LocalDate recordedAt = request.acquiredDate() != null  // 등록 시점 시간 기록 (or 입양일)
            ? request.acquiredDate()
            : LocalDate.now();

        DiaryEntity initialDiary = DiaryEntity.builder()
            .roseEntity(rose)
            .note(String.format("%s 첫 기록", rose.getNickname()))
            .imageUrl(request.imageUrl())
            .recordedAt(recordedAt)
            .build();

        initialDiary = diaryRepository.save(initialDiary);

        if (request.imageUrl() != null && !request.imageUrl().isEmpty()) {
            diaryImageService.saveAndBindImage(request.imageUrl(), initialDiary);
        }

        log.info("첫 번째 성장기록 생성 완료: diaryId={}", initialDiary.getId());
    }

    @Transactional(readOnly = true)
    public List<RoseResponse> getUserRoseResponses(Long userId) {
        log.info("사용자 장미 목록 조회 시작: userId={}", userId);
        
        if (userId == null) {
            log.warn("사용자 ID가 null입니다.");
            return List.of();
        }
        try {
            List<RoseEntity> roses = userRoseRepository.findByUserIdOrderByAcquiredDateDesc(userId);
            log.info("조회된 장미 개수: {}", roses != null ? roses.size() : 0);
            
            if (roses == null || roses.isEmpty()) {
                log.info("등록된 장미가 없습니다.");
                return List.of();
            }
            List<RoseResponse> response = roses.stream()
                .map(rose -> {
                    log.debug("장미 처리 중: id={}, nickname={}", rose.getId(), rose.getNickname());
                    return new RoseResponse(
                        rose.getId(),
                        rose.getNickname(),
                        rose.getWikiEntity() != null ? rose.getWikiEntity().getName() : "알 수 없음",
                        rose.getAcquiredDate(),
                        rose.getLocationNote(),
                        rose.getImageUrl()
                    );
                })
                .toList();

            log.info("응답 생성 완료: {} 개 항목", response.size());
            return response;
        } catch (Exception e) {
            log.error("사용자 장미 목록 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    // 내 장미 목록 조회
    @Transactional(readOnly = true)
    public List<RoseEntity> getUserRoses(Long userId) {
        if (userId == null) {
            log.warn("userId가 null입니다.");
            return List.of();
        }
        try {
            List<RoseEntity> roses = userRoseRepository.findByUserIdOrderByAcquiredDateDesc(userId);
            return roses != null ? roses : List.of();
            
        } catch (Exception e) {
            log.error("사용자 장미 목록 조회 실패: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    // 특정 장미 조회 (본인 소유 확인)
    @Transactional(readOnly = true)
    public RoseEntity getUserRose(Long userId, Long roseId) {
        return userRoseRepository.findByIdAndUserId(roseId, userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 장미를 찾을 수 없거나 접근 권한이 없습니다"));
    }
}