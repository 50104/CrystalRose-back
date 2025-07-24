package com.rose.back.domain.rose.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.common.util.ImageValidator;
import com.rose.back.domain.rose.entity.RoseEntity;
import com.rose.back.domain.rose.entity.RoseImageEntity;
import com.rose.back.domain.rose.repository.RoseImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
import com.rose.back.infra.S3.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoseImageService {

    private final S3Uploader s3Uploader;
    private final ImageTempRepository tempRepository;
    private final RoseImageRepository roseImageRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        ImageValidator.validate(file);
        String url = s3Uploader.uploadFile("roses", file);

        tempRepository.save(ImageTempEntity.builder()
            .fileUrl(url)
            .domainType(ImageTempEntity.DomainType.ROSE)
            .uploadedAt(new Date())
            .build());
        return url;
    }

    @Transactional
    public void saveImageEntityAndDeleteTemp(String fileUrl, String originalFileName, RoseEntity rose) {
        if (!roseImageRepository.existsByFileUrl(fileUrl)) {
            RoseImageEntity saved = roseImageRepository.save(RoseImageEntity.builder()
                .fileUrl(fileUrl)
                .storedFileName(fileUrl.replace("https://dodorose.com/", ""))
                .originalFileName(originalFileName)
                .rose(rose)
                .build());
            log.info("RoseImageEntity 저장 완료: id={}, url={}", saved.getId(), saved.getFileUrl());
        }
        tempRepository.findByFileUrl(fileUrl).ifPresent(tempRepository::delete);
    }

    @Transactional
    public void deleteImageAndUnbind(String imageUrl, RoseEntity rose) {
        s3Uploader.deleteFile(imageUrl);
        roseImageRepository.deleteByRoseId(rose.getId());
        tempRepository.findByFileUrl(imageUrl).ifPresent(tempRepository::delete);
    }
}
