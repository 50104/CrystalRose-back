package com.rose.back.domain.wiki.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.rose.back.common.util.ImageValidator;
import com.rose.back.domain.wiki.entity.WikiEntity;
import com.rose.back.domain.wiki.entity.WikiImageEntity;
import com.rose.back.domain.wiki.repository.WikiImageRepository;
import com.rose.back.infra.S3.ImageTempEntity;
import com.rose.back.infra.S3.ImageTempRepository;
import com.rose.back.infra.S3.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikiImageService {

    private final S3Uploader s3Uploader;
    private final ImageTempRepository tempRepository;
    private final WikiImageRepository wikiImageRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        ImageValidator.validate(file);
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
                .storedFileName(fileUrl.replace("https://dodorose.com/", ""))
                .originalFileName(null) 
                .wiki(wiki)
                .build());
        }
        tempRepository.findByFileUrl(fileUrl).ifPresent(tempRepository::delete);
    }

    @Transactional
    public void wikiModification(Object imageInput, WikiEntity wiki) {
        List<String> newFileUrls;

        if (imageInput instanceof String singleUrl) {
            newFileUrls = List.of(singleUrl);
        } else if (imageInput instanceof List<?> list) {
            newFileUrls = list.stream()
                .filter(url -> url instanceof String)
                .map(url -> (String) url)
                .toList();
        } else {
            throw new IllegalArgumentException("지원되지 않는 이미지 입력 타입입니다: " + imageInput);
        }
        if (newFileUrls.isEmpty()) return;

        String newFileUrl = newFileUrls.get(0);
        List<WikiImageEntity> existingImages = wikiImageRepository.findByWiki(wiki);

        if (!existingImages.isEmpty()) {
            WikiImageEntity existing = existingImages.get(0);

            if (existing.getFileUrl().equals(newFileUrl)) {
                log.info("이미지 변경 없음 - 기존과 동일: {}", newFileUrl);
                tempRepository.findByFileUrl(newFileUrl).ifPresent(tempRepository::delete);
                wiki.setImageUrl(newFileUrl);
                return;
            }
            s3Uploader.deleteFile(existing.getFileUrl());

            existing.setFileUrl(newFileUrl);
            existing.setStoredFileName(newFileUrl.replace("https://dodorose.com/", ""));
            existing.setOriginalFileName(null);
            log.info("기존 WikiImageEntity 덮어쓰기 완료 (id 유지): {}", existing.getId());

        } else {
            WikiImageEntity saved = wikiImageRepository.save(WikiImageEntity.builder()
                .fileUrl(newFileUrl)
                .storedFileName(newFileUrl.replace("https://dodorose.com/", ""))
                .originalFileName(null)
                .wiki(wiki)
                .build());
            log.info("새 WikiImageEntity 저장: {}", saved.getId());
        }
        tempRepository.findByFileUrl(newFileUrl).ifPresent(tempRepository::delete);
        wiki.setImageUrl(newFileUrl);
    }

    @Transactional
    public void deleteByWiki(WikiEntity wiki) {
        List<WikiImageEntity> images = wikiImageRepository.findByWiki(wiki);

        for (WikiImageEntity image : images) {
            s3Uploader.deleteFile(image.getFileUrl());
            tempRepository.findByFileUrl(image.getFileUrl()).ifPresent(tempRepository::delete);
            log.info("위키 이미지 삭제 완료: id={}, url={}", image.getId(), image.getFileUrl());
        }

        wikiImageRepository.deleteAll(images);
        wiki.setImageUrl(null);
    }
}