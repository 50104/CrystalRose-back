package com.rose.back.domain.user.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.common.util.ImageValidator;
import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.domain.user.entity.UserImageEntity;
import com.rose.back.domain.user.repository.UserImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
import com.rose.back.infra.S3.S3PresignedService;
import com.rose.back.infra.S3.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserImageService {

    private final S3Uploader s3Uploader;
    private final S3PresignedService s3PresignedService;
    private final ImageTempRepository tempRepository;
    private final UserImageRepository userImageRepository;

    public String uploadImage(MultipartFile file, UserEntity user) throws IOException {
        ImageValidator.validate(file);
        String url = s3Uploader.uploadFile("profiles", file);

        tempRepository.save(ImageTempEntity.builder()
            .fileUrl(url)
            .domainType(ImageTempEntity.DomainType.USER)
            .uploadedAt(new Date())
            .build());

        return url;
    }

    @Transactional
    public void saveImageEntityAndDeleteTemp(String fileUrl, String originalFileName, UserEntity user) {
        if (!userImageRepository.existsByFileUrl(fileUrl)) {
            String s3Key = s3PresignedService.extractKeyFromUrl(fileUrl);
            
            UserImageEntity saved = userImageRepository.save(UserImageEntity.builder()
                .fileUrl(fileUrl)
                .s3Key(s3Key)
                .storedFileName(fileUrl.replace("https://dodorose.com/", ""))
                .originalFileName(originalFileName)
                .user(user)
                .build());
            log.info("UserImageEntity 저장 완료: id={}, url={}, s3Key={}", saved.getId(), saved.getFileUrl(), saved.getS3Key());
        }
        tempRepository.findByFileUrl(fileUrl).ifPresent(tempRepository::delete);
    }

    @Transactional
    public void deleteImageAndUnbind(String imageUrl, UserEntity user) {
        String s3Key = s3PresignedService.extractKeyFromUrl(imageUrl);
        s3PresignedService.deleteFile(s3Key);
        userImageRepository.deleteByUserUserNo(user.getUserNo());
        tempRepository.findByFileUrl(imageUrl).ifPresent(tempRepository::delete);
        user.setUserProfileImg(null);
    }

    @Transactional
    public void updateImageChanged(String newFileUrl, UserEntity user) {
        if (newFileUrl == null || newFileUrl.isBlank()) return;

        List<UserImageEntity> existingImages = userImageRepository.findByUserUserNo(user.getUserNo());

        if (!existingImages.isEmpty()) {
            UserImageEntity current = existingImages.get(0);
            if (current.getFileUrl().equals(newFileUrl)) {
                log.info("이미지 변경 없음: {}", newFileUrl);
                tempRepository.findByFileUrl(newFileUrl).ifPresent(tempRepository::delete);
                user.setUserProfileImg(newFileUrl);
                return;
            }

            String oldS3Key = s3PresignedService.extractKeyFromUrl(current.getFileUrl());
            s3PresignedService.deleteFile(oldS3Key);

            String newS3Key = s3PresignedService.extractKeyFromUrl(newFileUrl);
            current.setFileUrl(newFileUrl);
            current.setS3Key(newS3Key);
            current.setStoredFileName(newFileUrl.replace("https://dodorose.com/", ""));
            current.setOriginalFileName(null);
            log.info("기존 UserImageEntity 수정 완료 (id 유지): {}", current.getId());
        } else {
            String s3Key = s3PresignedService.extractKeyFromUrl(newFileUrl);
            UserImageEntity saved = userImageRepository.save(UserImageEntity.builder()
                .fileUrl(newFileUrl)
                .s3Key(s3Key)
                .storedFileName(newFileUrl.replace("https://dodorose.com/", ""))
                .originalFileName(null)
                .user(user)
                .build());
            log.info("UserImageEntity 새로 저장: {}", saved.getId());
        }

        tempRepository.findByFileUrl(newFileUrl).ifPresent(tempRepository::delete);
        user.setUserProfileImg(newFileUrl);
    }
}
