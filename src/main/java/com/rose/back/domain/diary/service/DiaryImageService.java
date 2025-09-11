package com.rose.back.domain.diary.service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.common.util.ImageValidator;
import com.rose.back.domain.diary.entity.DiaryEntity;
import com.rose.back.domain.diary.entity.DiaryImageEntity;
import com.rose.back.domain.diary.repository.DiaryImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
import com.rose.back.infra.S3.S3PresignedService;
import com.rose.back.infra.S3.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryImageService {

    private final S3PresignedService s3PresignedService;
    private final S3Uploader s3Uploader;
    private final ImageTempRepository tempRepository;
    private final DiaryImageRepository diaryImageRepository;

    // MultipartFile 직접 업로드 (기존 방식)
    public String uploadImage(MultipartFile file, String uploader) throws IOException {
        ImageValidator.validate(file);
        String url = s3Uploader.uploadFile("diaries", file);
        tempRepository.save(ImageTempEntity.builder()
            .fileUrl(url)
            .domainType(ImageTempEntity.DomainType.DIARY)
            .uploadedAt(new Date())
            .uploadedBy(uploader)
            .build());
        return url;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, null);
    }

    // presigned URL로 업로드된 이미지 처리
    @Transactional
    public void saveAndBindImage(String imageUrl, DiaryEntity diary) {
        String s3Key = s3PresignedService.extractKeyFromUrl(imageUrl);
        
        diaryImageRepository.save(DiaryImageEntity.builder()
            .fileUrl(imageUrl)
            .s3Key(s3Key)
            .storedFileName(imageUrl.replace("https://dodorose.com/", ""))
            .originalFileName(null)
            .diary(diary)
            .build());
            
        tempRepository.findByFileUrl(imageUrl).ifPresent(tempRepository::delete);
        log.info("DiaryImageEntity 저장 완료: url={}, s3Key={}", imageUrl, s3Key);
    }

    @Transactional
    public void deleteImageAndUnbind(String imageUrl, DiaryEntity diary) {
        String s3Key = s3PresignedService.extractKeyFromUrl(imageUrl);
        s3PresignedService.deleteFile(s3Key);
        diaryImageRepository.deleteByDiaryId(diary.getId());
        tempRepository.findByFileUrl(imageUrl).ifPresent(tempRepository::delete);
        log.info("DiaryImageEntity 삭제 완료: url={}, s3Key={}", imageUrl, s3Key);
    }

    @Transactional
    public void updateImageUrl(String newImageUrl, DiaryEntity diary) {
        Optional<DiaryImageEntity> existingOpt = diaryImageRepository.findByDiaryId(diary.getId());

        if (existingOpt.isPresent()) {
            DiaryImageEntity existing = existingOpt.get();
            String oldS3Key = s3PresignedService.extractKeyFromUrl(existing.getFileUrl());
            s3PresignedService.deleteFile(oldS3Key);
            
            String newS3Key = s3PresignedService.extractKeyFromUrl(newImageUrl);
            existing.setFileUrl(newImageUrl);
            existing.setS3Key(newS3Key);
            existing.setStoredFileName(newImageUrl.replace("https://dodorose.com/", ""));
            diaryImageRepository.save(existing);
            log.info("기존 DiaryImageEntity 수정 완료: id={}, newUrl={}, s3Key={}", existing.getId(), newImageUrl, newS3Key);
        } else {
            String s3Key = s3PresignedService.extractKeyFromUrl(newImageUrl);
            diaryImageRepository.save(DiaryImageEntity.builder()
                .fileUrl(newImageUrl)
                .s3Key(s3Key)
                .originalFileName(null)
                .storedFileName(newImageUrl.replace("https://dodorose.com/", ""))
                .diary(diary)
                .build());
            log.info("새 DiaryImageEntity 저장: url={}, s3Key={}", newImageUrl, s3Key);
        }
        tempRepository.findByFileUrl(newImageUrl).ifPresent(tempRepository::delete);
    }

    @Transactional
    public void replaceImage(String newImageUrl, DiaryEntity diary) {
        updateImageUrl(newImageUrl, diary);
    }
}
