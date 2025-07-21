package com.rose.back.domain.diary.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.diary.dto.DiaryRequest;
import com.rose.back.domain.diary.dto.DiaryResponse;
import com.rose.back.domain.diary.dto.DiaryWithCareResponse;
import com.rose.back.domain.diary.entity.CareLogEntity;
import com.rose.back.domain.diary.entity.DiaryEntity;
import com.rose.back.domain.diary.repository.CareLogRepository;
import com.rose.back.domain.diary.repository.DiaryRepository;
import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.domain.rose.service.RoseService;
import com.rose.back.infra.S3.ImageTempRepository;

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
            .map(d -> new DiaryResponse(d.getId(), d.getNote(), d.getImageUrl(), d.getRecordedAt()))
            .toList();
    }
    
    // 날짜 범위로 다이어리 조회
    public List<DiaryResponse> getUserTimelineByDateRange(Long userId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return diaryRepository.findAllByRoseEntity_UserIdAndRecordedAtBetweenOrderByRecordedAtDesc(userId, start, end)
            .stream()
            .map(d -> new DiaryResponse(d.getId(), d.getNote(), d.getImageUrl(), d.getRecordedAt()))
            .toList();
    }

    public List<DiaryWithCareResponse> getRoseTimeline(Long roseId) {
        List<DiaryEntity> diaries = diaryRepository.findAllByRoseEntity_IdOrderByRecordedAtAsc(roseId);

        return diaries.stream().map(diary -> {
            LocalDate date = diary.getRecordedAt();
            Long userNo = diary.getRoseEntity().getUserId();

            List<CareLogEntity> careLogs = careLogRepository.findByUserNo_UserNoAndCareDate(userNo, date);
            log.info("케어 로그 조회: userNo={}, date={}, careLogs={}", userNo, date, careLogs.size());
            
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
                new ArrayList<>(careTypes)
            );
        }).toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}