package com.rose.back.domain.wiki.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.wiki.entity.WikiImageTempEntity;
import com.rose.back.domain.wiki.repository.WikiImageTempRepository;
import com.rose.back.infra.S3.S3Uploader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiImageService {

    private final S3Uploader s3Uploader;
    private final WikiImageTempRepository tempRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        String url = s3Uploader.uploadFile(file, null);
        tempRepository.save(WikiImageTempEntity.builder()
            .fileUrl(url)
            .uploadedAt(new Date())
            .build());
        return url;
    }

    @Transactional
    public void confirmImageUsage(String fileUrl) {
        tempRepository.findByFileUrl(fileUrl)
            .ifPresent(tempRepository::delete);
    }
}
