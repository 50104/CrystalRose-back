package com.rose.back.domain.diary.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.domain.diary.dto.DiaryRequest;
import com.rose.back.domain.diary.dto.DiaryResponse;
import com.rose.back.domain.diary.entity.DiaryEntity;
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
    private final DiaryImageService diaryImageService;
    private final RoseService roseService;

    @Transactional
    public void saveDiaryWithImages(Long userId, Long roseId, String note, String imageUrl, LocalDateTime recordedAt) {
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

    public List<DiaryResponse> getRoseTimeline(Long roseId) {
        return diaryRepository.findAllByRoseEntity_IdOrderByRecordedAtAsc(roseId)
            .stream()
            .map(d -> new DiaryResponse(d.getId(), d.getNote(), d.getImageUrl(), d.getRecordedAt()))
            .toList();
    }
}