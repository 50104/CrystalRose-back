package com.rose.back.infra.scheduler;

import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rose.back.domain.wiki.entity.WikiImageTempEntity;
import com.rose.back.domain.wiki.repository.WikiImageTempRepository;
import com.rose.back.domain.wiki.repository.WikiRepository;
import com.rose.back.infra.S3.S3Uploader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnusedWikiImageCleaner {

    private final WikiImageTempRepository tempRepo;
    private final WikiRepository wikiRepo;
    private final S3Uploader s3Uploader;

    @Scheduled(cron = "0 0 * * * *") // 1시간마다
    @Transactional
    public void clean() {
        Date threshold = new Date(System.currentTimeMillis() - 1000 * 60 * 60);
        List<WikiImageTempEntity> expired = tempRepo.findByUploadedAtBefore(threshold);

        for (WikiImageTempEntity temp : expired) {
            if (!wikiRepo.existsByImageUrl(temp.getFileUrl())) {
                s3Uploader.deleteFile(temp.getFileUrl());
                tempRepo.delete(temp);
                log.info("미사용 위키 이미지 삭제: {}", temp.getFileUrl());
            }
        }
    }
}
