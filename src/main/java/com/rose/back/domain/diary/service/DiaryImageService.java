package com.rose.back.domain.diary.service;

import com.rose.back.common.util.ImageValidator;
import com.rose.back.domain.diary.entity.DiaryEntity;
import com.rose.back.domain.diary.entity.DiaryImageEntity;
import com.rose.back.domain.diary.repository.DiaryImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
import com.rose.back.infra.S3.S3Uploader;
import com.rose.back.infra.S3.ImageTempEntity.DomainType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryImageService {

    private final S3Uploader s3Uploader;
    private final ImageTempRepository tempRepository;
    private final DiaryImageRepository diaryImageRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        ImageValidator.validate(file);
        String url = s3Uploader.uploadFile("diaries", file);
        tempRepository.save(ImageTempEntity.builder()
                .fileUrl(url)
                .domainType(DomainType.DIARY)
                .uploadedAt(new Date())
                .build());
        return url;
    }

    @Transactional
    public void saveAndBindImage(String imageUrl, DiaryEntity diary) {
        diaryImageRepository.save(DiaryImageEntity.builder()
            .fileUrl(imageUrl)
            .originalFileName(null)
            .storedFileName(imageUrl.replace("https://crystalrose-web.s3.ap-northeast-2.amazonaws.com/", ""))
            .diary(diary)
            .build());
        tempRepository.findByFileUrl(imageUrl).ifPresent(tempRepository::delete);
    }
}
