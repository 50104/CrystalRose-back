package com.rose.back.infra.scheduler;

import com.rose.back.domain.diary.repository.DiaryRepository;
import com.rose.back.domain.rose.repository.RoseRepository;
import com.rose.back.domain.user.repository.UserRepository;
import com.rose.back.domain.wiki.repository.WikiRepository;
import com.rose.back.domain.board.repository.ContentImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
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
    private final ContentImageRepository imageRepository;
    private final RoseRepository roseRepository;
    private final WikiRepository wikiRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanAllUnusedImages() {
        Date threshold = new Date(System.currentTimeMillis() - 1000 * 60 * 60);
        List<ImageTempEntity> expired = imageTempRepository.findByUploadedAtBefore(threshold);

        for (ImageTempEntity temp : expired) {
            try {
                boolean inUse = switch (temp.getDomainType()) {
                    case BOARD -> imageRepository.existsByFileUrl(temp.getFileUrl());
                    case ROSE -> roseRepository.existsByImageUrl(temp.getFileUrl());
                    case WIKI -> wikiRepository.existsByImageUrl(temp.getFileUrl());
                    case DIARY -> diaryRepository.existsByImageUrl(temp.getFileUrl());
                    case USER -> userRepository.existsByUserProfileImg(temp.getFileUrl());
                };

                if (!inUse) {
                    s3Uploader.deleteFile(temp.getFileUrl());
                    imageTempRepository.delete(temp);
                    log.info("미사용 {} 이미지 삭제: {}", temp.getDomainType(), temp.getFileUrl());
                }
            } catch (Exception e) {
                log.error("{} 이미지 삭제 실패: {}", temp.getDomainType(), e.getMessage(), e);
            }
        }
    }
}
