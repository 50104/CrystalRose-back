package com.rose.back.infra.scheduler;

import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rose.back.domain.rose.entity.RoseImageTempEntity;
import com.rose.back.domain.rose.repository.RoseImageTempRepository;
import com.rose.back.domain.rose.repository.RoseRepository;
import com.rose.back.infra.S3.S3Uploader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnusedRoseImageCleaner {

    private final RoseImageTempRepository tempRepo;
    private final RoseRepository roseRepo;
    private final S3Uploader s3Uploader;

    private static final long THRESHOLD_MILLIS = 1000 * 60 * 60;  // 1시간 기준

    @Scheduled(cron = "0 10 * * * *") // 매시간 10분
    @Transactional
    public void clean() {
        Date threshold = new Date(System.currentTimeMillis() - THRESHOLD_MILLIS);
        List<RoseImageTempEntity> expired = tempRepo.findByUploadedAtBefore(threshold);

        for (RoseImageTempEntity temp : expired) {
            try {
                if (!roseRepo.existsByImageUrl(temp.getFileUrl())) {
                    s3Uploader.deleteFile(temp.getFileUrl());
                    tempRepo.delete(temp);
                    log.info("미사용 장미 이미지 삭제: {}", temp.getFileUrl());
                }
            } catch (Exception e) {
                log.error("장미 이미지 정리 중 오류: {}", e.getMessage(), e);
            }
        }
    }
}
