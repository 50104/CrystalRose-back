package com.rose.back.domain.diary.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.kms.model.NotFoundException;
import com.rose.back.domain.diary.dto.DiaryRequest;
import com.rose.back.domain.diary.dto.DiaryResponse;
import com.rose.back.domain.diary.dto.DiaryWithCareResponse;
import com.rose.back.domain.diary.entity.CareLogEntity;
import com.rose.back.domain.diary.entity.DiaryEntity;
import com.rose.back.domain.diary.repository.CareLogRepository;
import com.rose.back.domain.diary.repository.DiaryImageRepository;
import com.rose.back.domain.diary.repository.DiaryRepository;
import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.domain.rose.service.RoseService;
import com.rose.back.infra.S3.ImageTempRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final ImageTempRepository tempRepository;
    private final CareLogRepository careLogRepository;
    private final DiaryImageService diaryImageService;
    private final RoseService roseService;
    private final DiaryImageRepository diaryImageRepository;

    @Transactional
    public void saveDiaryWithImages(Long userId, Long roseId, String note, String imageUrl, LocalDate recordedAt) {
        log.info("Saving diary: roseId={}, note={}, imageUrl={}, recordedAt={}", roseId, note, imageUrl, recordedAt);
        RoseEntity rose = roseService.getUserRose(userId, roseId);

        DiaryEntity diary = DiaryEntity.builder()
            .roseEntity(rose)
            .note(note)
            .imageUrl(imageUrl)
            .recordedAt(recordedAt)
            .build();

        diary = diaryRepository.save(diary);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            diaryImageService.saveAndBindImage(imageUrl, diary);
        }
        tempRepository.findByFileUrl(imageUrl).ifPresent(tempRepository::delete);
    }

    @Transactional
    public void registerDiary(Long userId, DiaryRequest request) {
        RoseEntity rose = roseService.getUserRose(userId, request.roseId());

        DiaryEntity diary = diaryRepository.save(
            DiaryEntity.builder()
                .roseEntity(rose)
                .note(request.note())
                .imageUrl(request.imageUrl())
                .recordedAt(request.recordedAt())
                .build()
        );
        
        if (request.imageUrl() != null && !request.imageUrl().isEmpty()) {
            diaryImageService.saveAndBindImage(request.imageUrl(), diary);
            tempRepository.findByFileUrl(request.imageUrl()).ifPresent(tempRepository::delete);
        }
    }

    public List<DiaryResponse> getUserTimeline(Long userId) {
        return diaryRepository.findAllByRoseEntity_UserIdOrderByRecordedAtDesc(userId)
            .stream()
            .map(DiaryResponse::from)
            .toList();
    }
    
    // 날짜 범위로 다이어리 조회
    public List<DiaryResponse> getUserTimelineByDateRange(Long userId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return diaryRepository.findAllByRoseEntity_UserIdAndRecordedAtBetweenOrderByRecordedAtDesc(userId, start, end)
            .stream()
            .map(DiaryResponse::from)
            .toList();
    }

    public List<DiaryWithCareResponse> getRoseTimeline(Long roseId) {
        List<DiaryEntity> diaries = diaryRepository.findAllByRoseEntity_IdOrderByRecordedAtAsc(roseId);

        return diaries.stream().map(diary -> {
            LocalDate date = diary.getRecordedAt();
            Long userNo = diary.getRoseEntity().getUserId();

            Optional<CareLogEntity> careLogs = careLogRepository.findByUserNo_UserNoAndCareDate(userNo, date);
            log.info("케어 로그 조회: userNo={}, date={}, careLogs={}", userNo, date, careLogs.isPresent());
            
            Set<String> careTypes = careLogs.stream()
                .flatMap(log -> Stream.of(
                    hasText(log.getWatering()) ? "watering" : null,
                    hasText(log.getFertilizer()) ? "fertilizer" : null,
                    hasText(log.getPesticide()) ? "pesticide" : null,
                    hasText(log.getAdjuvant()) ? "adjuvant" : null,
                    hasText(log.getFungicide()) ? "fungicide" : null,
                    hasText(log.getCompost()) ? "compost" : null,
                    hasText(log.getNote()) ? "note" : null
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            return new DiaryWithCareResponse(
                diary.getId(),
                diary.getNote(),
                diary.getImageUrl(),
                diary.getRecordedAt(),
                new ArrayList<>(careTypes),
                diary.getRoseEntity().getUserId().equals(userNo), // isMine
                careLogs.isPresent() // hasCareLog
            );
        }).toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Transactional
    public void deleteDiary(Long userId, Long diaryId) {
        Optional<DiaryEntity> optional = diaryRepository.findWithoutJoinById(diaryId);

        if (optional.isEmpty()) {
            forceDeleteDiary(diaryId);
            return;
        }
        DiaryEntity diary = optional.get();

        RoseEntity rose = diary.getRoseEntity();
        if (rose == null) {
            forceDeleteDiary(diaryId);
            return;
        }
        if (!rose.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 다이어리만 삭제할 수 없습니다.");
        }

        String imageUrl = diary.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                diaryImageService.deleteImageAndUnbind(imageUrl, diary);
            } catch (Exception e) {
                log.error("S3 이미지 삭제 실패: {}", imageUrl, e);
            }
        }

        diaryRepository.delete(diary);
        log.info("다이어리 삭제 완료: diaryId = {}", diaryId);
    }

    @Transactional
    public void forceDeleteDiary(Long diaryId) {
        Map<String, Object> diary = diaryRepository.findRawDiary(diaryId);
        if (diary == null || diary.isEmpty()) {
            throw new EntityNotFoundException("해당 다이어리가 존재하지 않습니다.");
        }
        diaryImageRepository.deleteByDiaryId(diaryId);
        diaryRepository.deleteById(diaryId);
    }

    @Transactional
    public void updateDiary(Long userId, Long diaryId, DiaryRequest request) {
        DiaryEntity diary = diaryRepository.findWithRoseById(diaryId)
            .orElseThrow(() -> new NotFoundException("다이어리를 찾을 수 없습니다."));

        if (!diary.getRoseEntity().getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 다이어리만 수정할 수 있습니다.");
        }

        if (!Objects.equals(request.imageUrl(), diary.getImageUrl())) {
            diaryImageService.replaceImage(request.imageUrl(), diary);
            diary.setImageUrl(request.imageUrl());
        }
        diary.setNote(request.note());
        diary.setRecordedAt(request.recordedAt());

        log.info("다이어리 수정 완료: id={}", diaryId);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getDiary(Long diaryId, Long userId) {
        DiaryEntity diary = diaryRepository.findWithRoseById(diaryId)
            .orElseThrow(() -> new NotFoundException("다이어리를 찾을 수 없습니다."));

        if (!diary.getRoseEntity().getUserId().equals(userId)) {
            throw new AccessDeniedException("조회 권한이 없습니다.");
        }
        return DiaryResponse.from(diary);
    }
}