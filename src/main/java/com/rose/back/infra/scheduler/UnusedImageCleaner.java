package com.rose.back.infra.scheduler;

import com.rose.back.domain.board.repository.ImageTempRepository;
import com.rose.back.domain.board.repository.ImageRepository;
import com.rose.back.domain.board.entity.ImageTempEntity;
import com.rose.back.infra.S3.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnusedImageCleaner {

    private final ImageTempRepository imageTempRepository;
    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;

    @Scheduled(cron = "0 0 * * * *") // 매시 정각
    @Transactional
    public void cleanUnusedImages() {
        Date oneHourAgo = new Date(System.currentTimeMillis() - 1000 * 60 * 60);
        List<ImageTempEntity> temps = imageTempRepository.findByUploadedAtBefore(oneHourAgo);

        for (ImageTempEntity temp : temps) {
            boolean isUsed = imageRepository.existsByFileUrl(temp.getFileUrl());
            if (!isUsed) {
                s3Uploader.deleteFile(temp.getFileUrl());
                imageTempRepository.delete(temp);
                log.info("삭제된 미사용 이미지: {}", temp.getFileUrl());
            }
        }
    }
}
