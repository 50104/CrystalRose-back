package com.rose.back.domain.rose.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.rose.entity.RoseImageTempEntity;
import com.rose.back.domain.rose.repository.RoseImageTempRepository;
import com.rose.back.infra.S3.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoseImageService {

    private final S3Uploader s3Uploader;
    private final RoseImageTempRepository tempRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        String url = s3Uploader.uploadFile(file, null);
        tempRepository.save(RoseImageTempEntity.builder()
            .fileUrl(url)
            .uploadedAt(new Date())
            .build());
        return url;
    }

    public void confirmImageUsage(String fileUrl) {
        tempRepository.findByFileUrl(fileUrl)
            .ifPresent(tempRepository::delete);
    }
}
