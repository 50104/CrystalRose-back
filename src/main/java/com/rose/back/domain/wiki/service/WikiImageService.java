package com.rose.back.domain.wiki.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.entity.WikiImageEntity;
import com.rose.back.domain.wiki.repository.WikiImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
import com.rose.back.infra.S3.S3Uploader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiImageService {

    private final S3Uploader s3Uploader;
    private final ImageTempRepository tempRepository;
    private final WikiImageRepository wikiImageRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        String url = s3Uploader.uploadFile("wikis", file);
        tempRepository.save(ImageTempEntity.builder()
            .fileUrl(url)
            .uploadedAt(new Date())
            .domainType(ImageTempEntity.DomainType.WIKI)
            .build());
        return url;
    }

    @Transactional
    public void saveAndBindImage(String fileUrl, WikiEntity wiki) {
        if (!wikiImageRepository.existsByFileUrl(fileUrl)) {
            wikiImageRepository.save(WikiImageEntity.builder()
                .fileUrl(fileUrl)
                .storedFileName(fileUrl.replace("https://crystalrose-web.s3.ap-northeast-2.amazonaws.com/", ""))
                .originalFileName(null) 
                .wiki(wiki)
                .build());
        }
        tempRepository.findByFileUrl(fileUrl).ifPresent(tempRepository::delete);
    }
}